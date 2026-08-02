package com.example.rokidphone.ai.catalog

import com.example.rokidphone.data.AiProvider

/**
 * Resolves the effective capabilities of a model.
 *
 * Priority:
 *  1. live Models API metadata (passed in by the catalog parser),
 *  2. manual overrides (user-declared; Custom provider only — the user knows
 *     their local model better than any static list),
 *  3. verified fallback capability map ([FallbackModelCatalog]),
 *  4. conservative provider default — text in/out, streaming by protocol,
 *     image input only where the provider ships vision-capable chat models.
 */
object ModelCapabilityResolver {

    /** Manual override key: model accepts image input. */
    const val OVERRIDE_VISION = "vision"

    /** Manual override key: model accepts audio input. */
    const val OVERRIDE_AUDIO_INPUT = "audio_input"

    /** Providers whose mainstream chat models accept image input. */
    private val providerDefaultVision: Set<AiProvider> = setOf(
        AiProvider.GEMINI,
        AiProvider.OPENAI,
        AiProvider.ANTHROPIC,
        AiProvider.XAI,
        AiProvider.MOONSHOT
    )

    fun resolve(
        provider: AiProvider,
        modelId: String,
        liveCapabilities: ModelCapabilities? = null,
        manualOverrides: ModelCapabilities? = null
    ): ModelCapabilities {
        liveCapabilities?.let { return it }
        manualOverrides?.let { return it }
        FallbackModelCatalog.capabilityFor(provider, modelId)?.let { return it }
        return providerDefault(provider)
    }

    /**
     * Build manual overrides from the user's override keys (persisted in
     * settings). Unknown keys are ignored. Returns null when there is nothing
     * to override so callers can keep the normal resolution path.
     */
    fun overridesFromKeys(provider: AiProvider, keys: Set<String>): ModelCapabilities? {
        if (keys.isEmpty()) return null
        val base = providerDefault(provider)
        return base.copy(
            imageInput = base.imageInput || OVERRIDE_VISION in keys,
            audioInput = base.audioInput || OVERRIDE_AUDIO_INPUT in keys
        )
    }

    /** Conservative default when nothing is known about the model. */
    fun providerDefault(provider: AiProvider): ModelCapabilities {
        val streaming = when (ProviderRegistry.descriptorFor(provider).protocol) {
            ApiProtocol.GEMINI_GENERATE_CONTENT,
            ApiProtocol.OPENAI_RESPONSES,
            ApiProtocol.OPENAI_CHAT_COMPLETIONS,
            ApiProtocol.ANTHROPIC_MESSAGES,
            ApiProtocol.BAIDU_QIANFAN_V2,
            ApiProtocol.CUSTOM_OPENAI_COMPATIBLE,
            ApiProtocol.LOCAL_INFERENCE -> true
            else -> false
        }
        return ModelCapabilities(
            imageInput = provider in providerDefaultVision,
            streaming = streaming,
            realtime = provider == AiProvider.GEMINI_LIVE
        )
    }

    /** Capability lookup used by the image-analysis gate before sending a request. */
    fun supportsImageInput(provider: AiProvider, modelId: String): Boolean =
        resolve(provider, modelId).imageInput

    fun supportsReasoning(provider: AiProvider, modelId: String): Boolean =
        resolve(provider, modelId).reasoning
}
