package com.example.rokidphone.ai.catalog

import com.example.rokidphone.data.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Fetches the live model list from a provider's Models endpoint.
 *
 * A 404 from the models endpoint is NOT treated as an authentication failure;
 * it simply means the provider has no usable catalog API, in which case the
 * repository falls back to cache/fallback.
 */
class RemoteModelCatalogSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build(),
    /** Test hook: overrides the descriptor's base URL. */
    private val baseUrlOverride: String? = null
) {

    /**
     * @return parsed live models (may be empty if the provider lists none)
     * @throws ProviderApiException on HTTP or network failure
     */
    suspend fun fetchModels(
        provider: AiProvider,
        apiKey: String,
        baseUrl: String? = null
    ): List<ModelInfo> = withContext(Dispatchers.IO) {
        val descriptor = ProviderRegistry.descriptorFor(provider)
        require(descriptor.hasRemoteCatalog) { "$provider has no remote catalog" }

        val root = (baseUrlOverride ?: baseUrl ?: descriptor.defaultBaseUrl).trimEnd('/')
        val url = "$root/${descriptor.modelsEndpointPath}"

        val requestBuilder = Request.Builder().url(url).get()
        applyAuth(requestBuilder, descriptor.protocol, apiKey)

        try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful) {
                    throw ProviderApiException.fromHttpStatus(
                        status = response.code,
                        responseBody = body,
                        retryAfterHeader = response.header("Retry-After")
                    )
                }
                ModelCatalogParsers.parse(provider, descriptor.catalogFormat, body ?: "{}")
            }
        } catch (e: ProviderApiException) {
            throw e
        } catch (e: java.net.SocketTimeoutException) {
            throw ProviderApiException(ProviderErrorKind.TIMEOUT, safeMessage = "Model list request timed out")
        } catch (e: java.io.IOException) {
            throw ProviderApiException(
                ProviderErrorKind.NETWORK_UNAVAILABLE,
                safeMessage = ProviderApiException.sanitize(e.message)
            )
        }
    }

    private fun applyAuth(requestBuilder: Request.Builder, protocol: ApiProtocol, apiKey: String) {
        if (apiKey.isBlank()) return
        when (protocol) {
            ApiProtocol.GEMINI_GENERATE_CONTENT, ApiProtocol.GEMINI_LIVE ->
                requestBuilder.addHeader("x-goog-api-key", apiKey)
            ApiProtocol.ANTHROPIC_MESSAGES -> {
                requestBuilder.addHeader("x-api-key", apiKey)
                requestBuilder.addHeader("anthropic-version", "2023-06-01")
            }
            else -> requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }
    }
}
