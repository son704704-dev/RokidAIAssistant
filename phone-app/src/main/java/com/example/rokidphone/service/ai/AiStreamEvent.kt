package com.example.rokidphone.service.ai

import com.example.rokidphone.ai.catalog.ProviderErrorKind

/**
 * Unified streaming event emitted by every provider that supports streaming.
 */
sealed interface AiStreamEvent {
    /** Incremental assistant text. */
    data class TextDelta(val text: String) : AiStreamEvent

    /** Incremental tool-call fragment (index-stable id, arguments streamed as JSON text). */
    data class ToolCallDelta(
        val id: String?,
        val name: String?,
        val argumentsDelta: String?
    ) : AiStreamEvent

    /** A citation/source reference attached to the answer (e.g. Perplexity Sonar). */
    data class Citation(val url: String, val title: String? = null) : AiStreamEvent

    /** Token usage report. */
    data class Usage(val inputTokens: Long?, val outputTokens: Long?) : AiStreamEvent

    /** The model is reasoning; the reasoning text itself is never surfaced. */
    data object Thinking : AiStreamEvent

    /** Stream finished normally. [fullText] is the concatenation of all TextDelta text. */
    data class Completed(val fullText: String, val usage: Usage? = null) : AiStreamEvent

    /** Stream failed. */
    data class Error(
        val kind: ProviderErrorKind,
        val message: String,
        val httpStatus: Int? = null
    ) : AiStreamEvent
}
