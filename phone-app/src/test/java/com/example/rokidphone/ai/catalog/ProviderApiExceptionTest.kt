package com.example.rokidphone.ai.catalog

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Error classification and secret-redaction tests.
 */
@RunWith(RobolectricTestRunner::class)
class ProviderApiExceptionTest {

    @Test
    fun `http status codes map to expected error kinds`() {
        fun kind(status: Int, body: String? = null) =
            ProviderApiException.fromHttpStatus(status, body).kind

        assertThat(kind(401)).isEqualTo(ProviderErrorKind.INVALID_API_KEY)
        assertThat(kind(403)).isEqualTo(ProviderErrorKind.PERMISSION_DENIED)
        assertThat(kind(403, """{"error": {"message": "unsupported country region"}}"""))
            .isEqualTo(ProviderErrorKind.REGION_MISMATCH)
        assertThat(kind(404)).isEqualTo(ProviderErrorKind.MODEL_UNAVAILABLE)
        assertThat(kind(400, """{"error": {"message": "model is deprecated"}}"""))
            .isEqualTo(ProviderErrorKind.MODEL_DEPRECATED)
        assertThat(kind(400, """{"error": {"message": "maximum context length exceeded"}}"""))
            .isEqualTo(ProviderErrorKind.CONTEXT_TOO_LONG)
        assertThat(kind(400, """{"error": {"message": "invalid image format"}}"""))
            .isEqualTo(ProviderErrorKind.UNSUPPORTED_IMAGE)
        assertThat(kind(400, """{"error": {"message": "bad param"}}"""))
            .isEqualTo(ProviderErrorKind.INVALID_REQUEST)
        assertThat(kind(413)).isEqualTo(ProviderErrorKind.UNSUPPORTED_IMAGE)
        assertThat(kind(429)).isEqualTo(ProviderErrorKind.RATE_LIMIT)
        assertThat(kind(429, """{"error": {"message": "quota exhausted"}}"""))
            .isEqualTo(ProviderErrorKind.QUOTA_EXCEEDED)
        assertThat(kind(500)).isEqualTo(ProviderErrorKind.SERVICE_UNAVAILABLE)
        assertThat(kind(503)).isEqualTo(ProviderErrorKind.SERVICE_UNAVAILABLE)
    }

    @Test
    fun `retry-after header is captured`() {
        val e = ProviderApiException.fromHttpStatus(429, null, retryAfterHeader = "7")
        assertThat(e.retryAfterMs).isEqualTo(7000)
        assertThat(e.isRetryable).isTrue()

        val notRetryable = ProviderApiException.fromHttpStatus(401, null)
        assertThat(notRetryable.isRetryable).isFalse()
    }

    @Test
    fun `error message keeps http status and provider code`() {
        val e = ProviderApiException.fromHttpStatus(
            404,
            """{"error": {"message": "model not found", "code": "model_not_found"}}"""
        )
        assertThat(e.message).contains("404")
        assertThat(e.message).contains("model_not_found")
        assertThat(e.providerErrorCode).isEqualTo("model_not_found")
    }

    @Test
    fun `sanitize strips bearer tokens and api keys from messages`() {
        val leaked = "Request failed: Authorization: Bearer sk-abc123secretkey456 key=AIzaSySecret"
        val sanitized = ProviderApiException.sanitize(leaked)

        assertThat(sanitized).doesNotContain("sk-abc123secretkey456")
        assertThat(sanitized).doesNotContain("AIzaSySecret")
        assertThat(sanitized).doesNotContain("Bearer sk-")
    }

    @Test
    fun `classified message never embeds credentials from the response body`() {
        val e = ProviderApiException.fromHttpStatus(
            401,
            """{"error": {"message": "invalid key sk-livekey123456 provided"}}"""
        )
        assertThat(e.message).doesNotContain("sk-livekey123456")
    }

    @Test
    fun `sanitize handles null and blank`() {
        assertThat(ProviderApiException.sanitize(null)).isEqualTo("Unknown provider error")
        assertThat(ProviderApiException.sanitize("   ")).isEqualTo("Unknown provider error")
    }
}
