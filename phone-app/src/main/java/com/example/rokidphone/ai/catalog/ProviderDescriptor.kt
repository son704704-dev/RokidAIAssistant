package com.example.rokidphone.ai.catalog

import com.example.rokidphone.data.AiProvider

/**
 * Static, data-driven description of a provider. [ProviderRegistry] holds the
 * single source of truth; service construction and catalog fetching read from
 * here instead of growing `when` blocks.
 *
 * @param modelsEndpointPath path appended to the base URL for the models list
 *   (null when the provider has no usable models endpoint)
 * @param fallbackDocSource name of the official documentation used to verify
 *   the fallback model list (see [FallbackModelCatalog.LAST_VERIFIED_DATE])
 */
data class ProviderDescriptor(
    val id: AiProvider,
    val displayName: String,
    val defaultBaseUrl: String,
    val protocol: ApiProtocol,
    val catalogFormat: CatalogFormat,
    val modelsEndpointPath: String? = null,
    val requiresApiKey: Boolean,
    val allowsCustomBaseUrl: Boolean,
    val supportsRegionalEndpoint: Boolean = false,
    val fallbackDocSource: String = ""
) {
    val hasRemoteCatalog: Boolean
        get() = catalogFormat != CatalogFormat.NONE && modelsEndpointPath != null
}
