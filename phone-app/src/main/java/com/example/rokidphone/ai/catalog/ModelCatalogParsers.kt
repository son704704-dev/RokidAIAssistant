package com.example.rokidphone.ai.catalog

import com.example.rokidphone.data.AiProvider
import org.json.JSONObject

/**
 * Parsers for each provider's Models-list endpoint.
 *
 * Inclusion in the chat catalog is metadata-driven (e.g. Gemini
 * `supportedGenerationMethods`, Mistral `capabilities.completion_chat`).
 * A small documented exclusion list removes known non-chat model families
 * (TTS, image generation, embeddings, transcription, safety guard models)
 * whose endpoints only expose IDs; this is a filter, not a capability claim.
 */
object ModelCatalogParsers {

    /** ID fragments of known non-chat model families on OpenAI-style model lists. */
    private val openAiStyleExclusions = listOf(
        "whisper", "tts", "dall-e", "text-embedding", "embedding", "moderation",
        "davinci", "babbage", "realtime", "transcribe", "audio", "image",
        "playai", "guard", "safety", "rerank", "veo", "imagen", "imagine", "voice"
    )

    /** ID fragments of known non-chat model families on the Gemini model list. */
    private val geminiExclusions = listOf(
        "embedding", "aqa", "tts", "imagen", "veo", "native-audio", "image-generation"
    )

    fun parse(
        provider: AiProvider,
        format: CatalogFormat,
        body: String
    ): List<ModelInfo> = when (format) {
        CatalogFormat.OPENAI_STYLE, CatalogFormat.BAIDU_QIANFAN_V2 ->
            parseOpenAiStyle(provider, body)
        CatalogFormat.GEMINI -> parseGemini(provider, body)
        CatalogFormat.ANTHROPIC -> parseAnthropic(provider, body)
        CatalogFormat.MISTRAL -> parseMistral(provider, body)
        CatalogFormat.NONE -> emptyList()
    }

    // ==================== OpenAI-style ====================

    private fun parseOpenAiStyle(provider: AiProvider, body: String): List<ModelInfo> {
        val data = JSONObject(body).optJSONArray("data") ?: return emptyList()
        val result = mutableListOf<ModelInfo>()
        for (i in 0 until data.length()) {
            val obj = data.optJSONObject(i) ?: continue
            val id = obj.optString("id").takeIf { it.isNotBlank() } ?: continue
            if (isExcludedNonChat(id, openAiStyleExclusions)) continue
            result += ModelInfo(
                id = id,
                displayName = id,
                provider = provider,
                capabilities = ModelCapabilityResolver.resolve(provider, id),
                status = ModelStatus.STABLE,
                source = CatalogSource.LIVE
            )
        }
        return result
    }

    // ==================== Gemini ====================

    private fun parseGemini(provider: AiProvider, body: String): List<ModelInfo> {
        val models = JSONObject(body).optJSONArray("models") ?: return emptyList()
        val wantLive = provider == AiProvider.GEMINI_LIVE
        val result = mutableListOf<ModelInfo>()
        for (i in 0 until models.length()) {
            val obj = models.optJSONObject(i) ?: continue
            // "name" is "models/<id>"
            val rawName = obj.optString("name")
            val id = rawName.removePrefix("models/").takeIf { it.isNotBlank() } ?: continue
            if (isExcludedNonChat(id, geminiExclusions)) continue

            val methods = mutableListOf<String>()
            obj.optJSONArray("supportedGenerationMethods")?.let { arr ->
                for (j in 0 until arr.length()) methods += arr.optString(j)
            }
            val isChatModel = "generateContent" in methods
            val isLiveModel = "bidiGenerateContent" in methods
            // Chat catalog excludes Live-only models and vice versa.
            if (wantLive && !isLiveModel) continue
            if (!wantLive && !isChatModel) continue

            val inputLimit = obj.optLong("inputTokenLimit").takeIf { it > 0 }
            val outputLimit = obj.optLong("outputTokenLimit").takeIf { it > 0 }
            val fallbackCaps = ModelCapabilityResolver.resolve(provider, id)
            val caps = fallbackCaps.copy(
                realtime = wantLive,
                maxContextTokens = inputLimit ?: fallbackCaps.maxContextTokens,
                maxOutputTokens = outputLimit ?: fallbackCaps.maxOutputTokens
            )
            result += ModelInfo(
                id = id,
                displayName = obj.optString("displayName").ifBlank { id },
                provider = provider,
                capabilities = caps,
                status = if (id.contains("preview") || id.contains("-exp")) ModelStatus.PREVIEW else ModelStatus.STABLE,
                source = CatalogSource.LIVE,
                description = obj.optString("description").take(160)
            )
        }
        return result
    }

    // ==================== Anthropic ====================

    private fun parseAnthropic(provider: AiProvider, body: String): List<ModelInfo> {
        val data = JSONObject(body).optJSONArray("data") ?: return emptyList()
        val result = mutableListOf<ModelInfo>()
        for (i in 0 until data.length()) {
            val obj = data.optJSONObject(i) ?: continue
            val id = obj.optString("id").takeIf { it.isNotBlank() } ?: continue
            // Prefer live capability metadata when Anthropic returns it.
            val liveCaps = obj.optJSONObject("capabilities")?.let { caps ->
                ModelCapabilities(
                    imageInput = caps.optJSONObject("image_input")?.optBoolean("supported") ?: caps.optBoolean("image_input"),
                    streaming = true,
                    toolCalling = caps.optJSONObject("tool_use")?.optBoolean("supported") ?: true,
                    reasoning = caps.optJSONObject("extended_thinking")?.optBoolean("supported")
                        ?: caps.optBoolean("extended_thinking"),
                    maxContextTokens = obj.optLong("max_input_tokens").takeIf { it > 0 }
                )
            }
            result += ModelInfo(
                id = id,
                displayName = obj.optString("display_name").ifBlank { id },
                provider = provider,
                capabilities = ModelCapabilityResolver.resolve(provider, id, liveCaps),
                status = ModelStatus.STABLE,
                source = CatalogSource.LIVE
            )
        }
        return result
    }

    // ==================== Mistral ====================

    private fun parseMistral(provider: AiProvider, body: String): List<ModelInfo> {
        val data = JSONObject(body).optJSONArray("data") ?: return emptyList()
        val result = mutableListOf<ModelInfo>()
        for (i in 0 until data.length()) {
            val obj = data.optJSONObject(i) ?: continue
            val id = obj.optString("id").takeIf { it.isNotBlank() } ?: continue
            val capsObj = obj.optJSONObject("capabilities")
            // Only chat-completion models belong in the chat catalog. STT/TTS/OCR
            // capabilities are independent and never imply chat support.
            if (capsObj != null && !capsObj.optBoolean("completion_chat", true)) continue

            val liveCaps = capsObj?.let { caps ->
                ModelCapabilities(
                    imageInput = caps.optBoolean("vision"),
                    audioInput = caps.optJSONObject("audio")?.optBoolean("input") ?: caps.optBoolean("audio_input"),
                    streaming = true,
                    toolCalling = caps.optBoolean("function_calling"),
                    structuredOutput = caps.optBoolean("structured_output"),
                    reasoning = caps.optBoolean("reasoning"),
                    maxContextTokens = obj.optLong("max_context_length").takeIf { it > 0 }
                )
            }
            result += ModelInfo(
                id = id,
                displayName = obj.optString("name").ifBlank { id },
                provider = provider,
                capabilities = ModelCapabilityResolver.resolve(provider, id, liveCaps),
                status = ModelStatus.STABLE,
                source = CatalogSource.LIVE,
                description = obj.optString("description").take(160)
            )
        }
        return result
    }

    private fun isExcludedNonChat(id: String, exclusions: List<String>): Boolean {
        val lower = id.lowercase()
        return exclusions.any { lower.contains(it) }
    }
}
