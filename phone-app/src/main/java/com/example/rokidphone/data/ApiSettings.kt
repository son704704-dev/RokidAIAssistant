package com.example.rokidphone.data

import androidx.annotation.StringRes
import com.example.rokidphone.R
import com.example.rokidphone.service.stt.SttProvider

/**
 * AI Service Providers
 */
enum class AiProvider(
    @StringRes val displayNameResId: Int,
    val description: String,
    val website: String,
    val defaultBaseUrl: String,
    val isOpenAiCompatible: Boolean = true,
    val supportsSpeech: Boolean = false,
    val supportsVision: Boolean = false
) {
    GEMINI(
        displayNameResId = R.string.provider_gemini,
        description = "Google's latest AI model, supports audio and vision",
        website = "https://ai.google.dev",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta/",
        isOpenAiCompatible = false,
        supportsSpeech = true,
        supportsVision = true
    ),
    OPENAI(
        displayNameResId = R.string.provider_openai,
        description = "GPT series models, industry standard",
        website = "https://openai.com",
        defaultBaseUrl = "https://api.openai.com/v1/",
        isOpenAiCompatible = true,
        supportsSpeech = true,  // Whisper
        supportsVision = true   // GPT-5.x Vision
    ),
    ANTHROPIC(
        displayNameResId = R.string.provider_anthropic,
        description = "Claude series, powerful reasoning",
        website = "https://anthropic.com",
        defaultBaseUrl = "https://api.anthropic.com/v1/",
        isOpenAiCompatible = false,
        supportsSpeech = false,
        supportsVision = true
    ),
    DEEPSEEK(
        displayNameResId = R.string.provider_deepseek,
        description = "Cost-effective Chinese model",
        website = "https://deepseek.com",
        defaultBaseUrl = "https://api.deepseek.com/",
        isOpenAiCompatible = true,
        supportsSpeech = false,
        supportsVision = false
    ),
    GROQ(
        displayNameResId = R.string.provider_groq,
        description = "Ultra-fast inference, hardware accelerated",
        website = "https://groq.com",
        defaultBaseUrl = "https://api.groq.com/openai/v1/",
        isOpenAiCompatible = true,
        supportsSpeech = true,  // Whisper
        supportsVision = true   // Llama Vision
    ),
    XAI(
        displayNameResId = R.string.provider_xai,
        description = "Elon Musk's xAI, Grok models",
        website = "https://x.ai",
        defaultBaseUrl = "https://api.x.ai/v1/",
        isOpenAiCompatible = true,
        supportsSpeech = false,
        supportsVision = true   // Grok 4.5+ image input (model-level)
    ),
    ALIBABA(
        displayNameResId = R.string.provider_alibaba,
        description = "Tongyi Qianwen, powerful Chinese model",
        website = "https://dashscope.aliyun.com",
        defaultBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/",
        isOpenAiCompatible = true,
        supportsSpeech = false,
        supportsVision = true
    ),
    ZHIPU(
        displayNameResId = R.string.provider_zhipu,
        description = "GLM series, strong Chinese capabilities",
        website = "https://z.ai/model-api",
        defaultBaseUrl = "https://api.z.ai/api/paas/v4/",
        isOpenAiCompatible = true,
        supportsSpeech = false,
        supportsVision = true   // GLM-V models only (model-level)
    ),
    BAIDU(
        displayNameResId = R.string.provider_baidu,
        description = "Ernie Bot / Wenxin via Qianfan v2 (legacy OAuth kept)",
        website = "https://cloud.baidu.com",
        defaultBaseUrl = "https://qianfan.baidubce.com/v2/",
        isOpenAiCompatible = false,
        supportsSpeech = false,
        supportsVision = true   // ERNIE VL models only (model-level)
    ),
    PERPLEXITY(
        displayNameResId = R.string.provider_perplexity,
        description = "Real-time web search and reasoning (Sonar series)",
        website = "https://www.perplexity.ai",
        defaultBaseUrl = "https://api.perplexity.ai/",
        isOpenAiCompatible = true,
        supportsSpeech = false,
        supportsVision = true   // Sonar image/attachment input (model-level)
    ),
    MOONSHOT(
        displayNameResId = R.string.provider_moonshot,
        description = "Kimi series, multimodal with video support",
        website = "https://moonshot.cn",
        defaultBaseUrl = "https://api.moonshot.ai/v1/",
        isOpenAiCompatible = true,
        supportsSpeech = false,
        supportsVision = true
    ),
    MISTRAL(
        displayNameResId = R.string.provider_mistral,
        description = "European frontier models from Mistral AI (OpenAI-compatible)",
        website = "https://mistral.ai",
        defaultBaseUrl = "https://api.mistral.ai/v1/",
        isOpenAiCompatible = true,
        supportsSpeech = false,
        supportsVision = true
    ),
    GEMINI_LIVE(
        displayNameResId = R.string.provider_gemini_live,
        description = "Gemini Live API - real-time bidirectional voice conversation",
        website = "https://ai.google.dev",
        defaultBaseUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent",
        isOpenAiCompatible = false,
        supportsSpeech = true,
        supportsVision = true
    ),
    ANYTHINGLLM(
        displayNameResId = R.string.provider_anythingllm,
        description = "Document-grounded retrieval via AnythingLLM workspace",
        website = "https://anythingllm.com",
        defaultBaseUrl = "http://localhost:3001",
        isOpenAiCompatible = false,
        supportsSpeech = false,
        supportsVision = false
    ),
    CUSTOM(
        displayNameResId = R.string.provider_custom,
        description = "OpenAI-compatible API (Ollama, LM Studio, etc.)",
        website = "",
        defaultBaseUrl = "http://localhost:11434/v1/",
        isOpenAiCompatible = true,
        supportsSpeech = false,
        supportsVision = false
    );
    
    companion object {
        fun fromName(name: String): AiProvider {
            return entries.find { it.name == name } ?: GEMINI
        }
    }
    
    /**
     * Check if this provider allows custom base URL
     */
    fun allowsCustomBaseUrl(): Boolean = this == CUSTOM
    
    /**
     * Check if this provider requires API key
     */
    fun requiresApiKey(): Boolean = this != CUSTOM
    
    /**
     * Check if this provider requires a secret key (Baidu OAuth)
     */
    fun requiresSecretKey(): Boolean = this == BAIDU
}

/**
 * Model Options
 * @param supportsAudio true if this model supports audio/speech input via the provider's STT API
 * @param supportsVision true if this model supports image/vision input
 */
data class ModelOption(
    val id: String,
    val displayName: String,
    val provider: AiProvider,
    val supportsAudio: Boolean = false,
    val supportsVision: Boolean = false,
    val isPreview: Boolean = false,
    val description: String = ""
)

/**
 * Legacy static model list, now backed by
 * [com.example.rokidphone.ai.catalog.FallbackModelCatalog] (verified
 * 2026-08-02). Kept as a compatibility facade for callers that still consume
 * [ModelOption]; new code should use the ModelCatalogRepository four-tier
 * catalog (live → cache → fallback → manual).
 */
object AvailableModels {
    private fun toOption(model: com.example.rokidphone.ai.catalog.ModelInfo) = ModelOption(
        id = model.id,
        displayName = model.displayName,
        provider = model.provider,
        supportsAudio = model.capabilities.audioInput,
        supportsVision = model.capabilities.imageInput,
        isPreview = model.status == com.example.rokidphone.ai.catalog.ModelStatus.PREVIEW,
        description = model.description
    )

    private val catalog get() = com.example.rokidphone.ai.catalog.FallbackModelCatalog

    val geminiModels get() = catalog.geminiModels.map(::toOption)
    val openaiModels get() = catalog.openaiModels.map(::toOption)
    val anthropicModels get() = catalog.anthropicModels.map(::toOption)
    val deepseekModels get() = catalog.deepseekModels.map(::toOption)
    val groqModels get() = catalog.groqModels.map(::toOption)
    val anythingllmModels get() = catalog.anythingllmModels.map(::toOption)
    val customModels get() = catalog.customModels.map(::toOption)
    val xaiModels get() = catalog.xaiModels.map(::toOption)
    val alibabaModels get() = catalog.alibabaModels.map(::toOption)
    val zhipuModels get() = catalog.zhipuModels.map(::toOption)
    val baiduModels get() = catalog.baiduModels.map(::toOption)
    val perplexityModels get() = catalog.perplexityModels.map(::toOption)
    val moonshotModels get() = catalog.moonshotModels.map(::toOption)
    val geminiLiveModels get() = catalog.geminiLiveModels.map(::toOption)
    val mistralModels get() = catalog.mistralModels.map(::toOption)

    fun getModelsForProvider(provider: AiProvider): List<ModelOption> =
        catalog.modelsFor(provider).map(::toOption)

    fun findModel(modelId: String): ModelOption? = allModels.find { it.id == modelId }

    val allModels: List<ModelOption>
        get() = AiProvider.entries.flatMap { getModelsForProvider(it) }
}

/**
 * Alibaba Cloud Model Studio regional endpoints.
 *
 * NOTE: regional hostnames follow the documented DashScope naming scheme;
 * verify the current hostname for each region in the Alibaba Cloud Model
 * Studio docs before relying on it (last checked 2026-08-02).
 */
object AlibabaRegions {
    const val CHINA = "china"
    const val SINGAPORE = "singapore"
    const val US = "us"
    const val GERMANY = "germany"
    const val JAPAN = "japan"
    const val CUSTOM = "custom"

    val all = listOf(CHINA, SINGAPORE, US, GERMANY, JAPAN, CUSTOM)

    fun baseUrlFor(region: String, customBaseUrl: String = ""): String = when (region) {
        CHINA -> "https://dashscope.aliyuncs.com/compatible-mode/v1/"
        SINGAPORE -> "https://dashscope-intl.aliyuncs.com/compatible-mode/v1/"
        US -> "https://dashscope-us.aliyuncs.com/compatible-mode/v1/"
        GERMANY -> "https://dashscope-de.aliyuncs.com/compatible-mode/v1/"
        JAPAN -> "https://dashscope-jp.aliyuncs.com/compatible-mode/v1/"
        CUSTOM -> customBaseUrl.ifBlank { baseUrlFor(CHINA) }
        else -> baseUrlFor(CHINA)
    }
}


/**
 * Provider-specific configuration
 */
data class ProviderConfig(
    val apiKey: String = "",
    val baseUrl: String = "",
    val customModelName: String = ""
)

/**
 * API Settings
 */
data class ApiSettings(
    // AI Chat settings
    val aiProvider: AiProvider = AiProvider.GEMINI,
    val aiModelId: String = "gemini-2.5-flash",

    // Per-provider model memory: provider name -> last selected model ID.
    // Switching providers restores the model the user picked for that provider.
    val providerModelIds: Map<String, String> = emptyMap(),

    // API Keys for each provider
    val geminiApiKey: String = "",
    val openaiApiKey: String = "",
    val anthropicApiKey: String = "",
    val deepseekApiKey: String = "",
    val groqApiKey: String = "",
    val xaiApiKey: String = "",
    val alibabaApiKey: String = "",
    val zhipuApiKey: String = "",
    val baiduApiKey: String = "",
    val baiduSecretKey: String = "",  // Baidu legacy: API Key + Secret Key OAuth
    // Baidu Qianfan v2: single bearer API key (preferred). When blank and the
    // legacy key pair exists, legacy mode is used (see baiduUseLegacyAuth).
    val baiduQianfanApiKey: String = "",
    val baiduUseLegacyAuth: Boolean = false,
    val perplexityApiKey: String = "",
    val moonshotApiKey: String = "",
    val mistralApiKey: String = "",
    val customApiKey: String = "",

    // AnythingLLM settings
    val anythingllmServerUrl: String = "",
    val anythingllmApiKey: String = "",
    val anythingllmWorkspaceSlug: String = "",

    // Custom base URLs (for providers that support it)
    val customBaseUrl: String = "http://localhost:11434/v1/",
    val customModelName: String = "llama4",
    // Custom endpoint protocol: "chat_completions" (default) or "responses"
    val customProtocol: String = "chat_completions",
    // Custom endpoint models-list path (default "models")
    val customModelsPath: String = "models",
    // Manual capability overrides for the Custom model: e.g. "vision", "audio_input"
    val customCapabilityOverrides: Set<String> = emptySet(),

    // Alibaba Cloud Model Studio region (see AlibabaRegions)
    val alibabaRegion: String = AlibabaRegions.CHINA,
    val alibabaCustomBaseUrl: String = "",

    // Speech recognition settings
    val sttProvider: SttProvider = SttProvider.GEMINI,
    // Empty string = SettingsRepository will resolve to device locale on first run.
    // TODO: UI should display the resolved locale tag so users know what is active.
    val speechLanguage: String = "",
    
    // === STT Provider Credentials ===
    
    // Deepgram
    val deepgramApiKey: String = "",
    
    // AssemblyAI
    val assemblyaiApiKey: String = "",
    
    // Google Cloud Speech-to-Text
    val gcpProjectId: String = "",
    val gcpApiKey: String = "",
    val gcpServiceAccountJson: String = "",
    val gcpUseServiceAccount: Boolean = false,
    
    // Microsoft Azure AI Speech
    val azureSpeechKey: String = "",
    val azureSpeechRegion: String = "",
    
    // Amazon Transcribe
    val awsAccessKeyId: String = "",
    val awsSecretAccessKey: String = "",
    val awsRegion: String = "us-east-1",
    
    // IBM Watson Speech to Text
    val ibmApiKey: String = "",
    val ibmServiceUrl: String = "",
    
    // iFLYTEK (Xunfei)
    val iflytekAppId: String = "",
    val iflytekApiKey: String = "",
    val iflytekApiSecret: String = "",
    
    // Huawei Cloud SIS
    val huaweiAk: String = "",
    val huaweiSk: String = "",
    val huaweiRegion: String = "cn-north-4",
    val huaweiProjectId: String = "",
    
    // Volcengine (ByteDance)
    val volcengineAk: String = "",
    val volcangineSk: String = "",
    val volcengineAppId: String = "",
    
    // Alibaba Cloud ASR
    val aliyunAccessKeyId: String = "",
    val aliyunAccessKeySecret: String = "",
    val aliyunAppKey: String = "",
    
    // Tencent Cloud ASR
    val tencentSecretId: String = "",
    val tencentSecretKey: String = "",
    val tencentAppId: String = "",
    val tencentEngineModelType: String = "16k_zh",
    
    // Baidu Cloud ASR
    val baiduAsrApiKey: String = "",
    val baiduAsrSecretKey: String = "",
    
    // Rev.ai
    val revaiAccessToken: String = "",
    
    // Speechmatics
    val speechmaticsApiKey: String = "",
    
    // Otter.ai
    val otteraiApiKey: String = "",
    
    // LLM Generation Parameters
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val topP: Float = 1.0f,
    val frequencyPenalty: Float = 0.0f,
    val presencePenalty: Float = 0.0f,
    
    // AI response settings
    // Empty string = SettingsRepository will resolve to device locale on first run.
    // TODO: UI should display the resolved locale tag so users know what is active.
    val responseLanguage: String = "",
    // Note: The default value is set to empty string here. 
    // The actual default (localized) is provided by SettingsRepository.getDefaultSystemPrompt()
    val systemPrompt: String = "",
    
    // TTS settings
    val ttsProvider: TtsProvider = TtsProvider.EDGE_TTS,
    val ttsVoiceOverride: String = "",
    val ttsSpeechRate: Float = 1.0f,
    val ttsPitch: Float = 0.0f,
    val systemTtsSpeechRate: Float = 1.0f,
    val systemTtsPitch: Float = 1.0f,

    // Recording settings
    // Auto-analyze recordings with AI after stopping (default: true)
    val autoAnalyzeRecordings: Boolean = true,
    
    // Glasses push settings
    // Send text chat AI responses to glasses display (default: true)
    val pushChatToGlasses: Boolean = true,
    // Send phone recording results (transcript + AI response) to glasses display (default: true)
    val pushRecordingToGlasses: Boolean = true
) {
    /** Baidu: Qianfan v2 key preferred; legacy key pair when in legacy mode. */
    fun getBaiduEffectiveApiKey(): String =
        if (baiduUseLegacyAuth) baiduApiKey else baiduQianfanApiKey.ifBlank { baiduApiKey }

    /** True when Baidu should use the legacy API Key + Secret Key OAuth flow. */
    fun isBaiduLegacyMode(): Boolean =
        baiduUseLegacyAuth || (baiduQianfanApiKey.isBlank() && baiduApiKey.isNotBlank() && baiduSecretKey.isNotBlank())

    /**
     * Get current AI provider's API Key
     */
    fun getCurrentApiKey(): String {
        return when (aiProvider) {
            AiProvider.GEMINI -> geminiApiKey
            AiProvider.OPENAI -> openaiApiKey
            AiProvider.ANTHROPIC -> anthropicApiKey
            AiProvider.DEEPSEEK -> deepseekApiKey
            AiProvider.GROQ -> groqApiKey
            AiProvider.XAI -> xaiApiKey
            AiProvider.ALIBABA -> alibabaApiKey
            AiProvider.ZHIPU -> zhipuApiKey
            AiProvider.BAIDU -> getBaiduEffectiveApiKey()
            AiProvider.PERPLEXITY -> perplexityApiKey
            AiProvider.MOONSHOT -> moonshotApiKey
            AiProvider.MISTRAL -> mistralApiKey
            AiProvider.GEMINI_LIVE -> geminiApiKey  // Shares Gemini API key
            AiProvider.ANYTHINGLLM -> anythingllmApiKey
            AiProvider.CUSTOM -> customApiKey
        }
    }

    /**
     * Get API Key for specified provider
     */
    fun getApiKeyForProvider(provider: AiProvider): String {
        return when (provider) {
            AiProvider.GEMINI -> geminiApiKey
            AiProvider.OPENAI -> openaiApiKey
            AiProvider.ANTHROPIC -> anthropicApiKey
            AiProvider.DEEPSEEK -> deepseekApiKey
            AiProvider.GROQ -> groqApiKey
            AiProvider.XAI -> xaiApiKey
            AiProvider.ALIBABA -> alibabaApiKey
            AiProvider.ZHIPU -> zhipuApiKey
            AiProvider.BAIDU -> getBaiduEffectiveApiKey()
            AiProvider.PERPLEXITY -> perplexityApiKey
            AiProvider.MOONSHOT -> moonshotApiKey
            AiProvider.MISTRAL -> mistralApiKey
            AiProvider.GEMINI_LIVE -> geminiApiKey  // Shares Gemini API key
            AiProvider.ANYTHINGLLM -> anythingllmApiKey
            AiProvider.CUSTOM -> customApiKey
        }
    }

    /**
     * Get base URL for current provider
     */
    fun getCurrentBaseUrl(): String {
        return when (aiProvider) {
            AiProvider.CUSTOM -> customBaseUrl.ifBlank { AiProvider.CUSTOM.defaultBaseUrl }
            AiProvider.ANYTHINGLLM -> anythingllmServerUrl.ifBlank { AiProvider.ANYTHINGLLM.defaultBaseUrl }
            AiProvider.ALIBABA -> AlibabaRegions.baseUrlFor(alibabaRegion, alibabaCustomBaseUrl)
            else -> aiProvider.defaultBaseUrl
        }
    }

    /**
     * Get model ID for current provider
     */
    fun getCurrentModelId(): String = getModelIdForProvider(aiProvider)

    /**
     * Get the model the user last selected for [provider]; falls back to the
     * legacy single [aiModelId] for the active provider, then to the verified
     * fallback default.
     */
    fun getModelIdForProvider(provider: AiProvider): String {
        if (provider == AiProvider.CUSTOM) return customModelName.ifBlank { aiModelId }
        providerModelIds[provider.name]?.takeIf { it.isNotBlank() }?.let { return it }
        if (provider == aiProvider && aiModelId.isNotBlank()) return aiModelId
        return com.example.rokidphone.ai.catalog.FallbackModelCatalog.defaultModelFor(provider)
    }

    /** Return a copy with [modelId] stored as the selection for [provider]. */
    fun withModelForProvider(provider: AiProvider, modelId: String): ApiSettings {
        val newMap = providerModelIds.toMutableMap().apply { put(provider.name, modelId) }
        return copy(
            providerModelIds = newMap,
            aiModelId = if (provider == aiProvider) modelId else aiModelId
        )
    }

    /** Apply legacy model-ID migrations (e.g. DeepSeek V3 aliases → V4). */
    fun migrateLegacyModelIds(): ApiSettings {
        val migrations = com.example.rokidphone.ai.catalog.FallbackModelCatalog.legacyModelMigration
        val newMap = providerModelIds.mapValues { (providerName, id) ->
            if (providerName == AiProvider.DEEPSEEK.name) migrations[id] ?: id else id
        }
        val newActive = if (aiProvider == AiProvider.DEEPSEEK) migrations[aiModelId] ?: aiModelId else aiModelId
        return if (newMap != providerModelIds || newActive != aiModelId) {
            copy(providerModelIds = newMap, aiModelId = newActive)
        } else this
    }

    /**
     * Check if settings are valid
     */
    fun isValid(): Boolean {
        return when (aiProvider) {
            AiProvider.CUSTOM -> customBaseUrl.isNotBlank() && isValidUrl(customBaseUrl)
            AiProvider.BAIDU ->
                if (isBaiduLegacyMode()) baiduApiKey.isNotBlank() && baiduSecretKey.isNotBlank()
                else baiduQianfanApiKey.isNotBlank()
            AiProvider.ANYTHINGLLM ->
                anythingllmServerUrl.isNotBlank() && anythingllmApiKey.isNotBlank() && anythingllmWorkspaceSlug.isNotBlank()
            else -> getCurrentApiKey().isNotBlank()
        }
    }

    /**
     * Check if specified provider has API Key configured
     */
    fun isProviderConfigured(provider: AiProvider): Boolean {
        return when (provider) {
            AiProvider.CUSTOM -> customBaseUrl.isNotBlank() && isValidUrl(customBaseUrl)
            AiProvider.BAIDU ->
                baiduQianfanApiKey.isNotBlank() || (baiduApiKey.isNotBlank() && baiduSecretKey.isNotBlank())
            AiProvider.GEMINI_LIVE -> geminiApiKey.isNotBlank()  // Shares Gemini API key
            AiProvider.ANYTHINGLLM ->
                anythingllmServerUrl.isNotBlank() && anythingllmApiKey.isNotBlank() && anythingllmWorkspaceSlug.isNotBlank()
            else -> getApiKeyForProvider(provider).isNotBlank()
        }
    }

    /**
     * Check if any speech recognition service is available
     */
    fun hasSpeechServiceConfigured(): Boolean {
        val sttProviders = listOf(AiProvider.GEMINI, AiProvider.OPENAI, AiProvider.GROQ)
        return sttProviders.any { isProviderConfigured(it) }
    }

    /**
     * Get list of configured STT providers
     */
    fun getConfiguredSttProviders(): List<AiProvider> {
        val sttProviders = listOf(AiProvider.GEMINI, AiProvider.OPENAI, AiProvider.GROQ)
        return sttProviders.filter { isProviderConfigured(it) }
    }
    
    /**
     * Get list of missing API keys for core functionality
     */
    fun getMissingApiKeys(): List<AiProvider> {
        val missing = mutableListOf<AiProvider>()
        if (!isProviderConfigured(aiProvider)) {
            missing.add(aiProvider)
        }
        return missing
    }
    
    /**
     * Check if any API key is configured at all
     * Returns true if at least one provider has an API key set
     */
    fun hasAnyApiKeyConfigured(): Boolean {
        return geminiApiKey.isNotBlank() ||
               openaiApiKey.isNotBlank() ||
               anthropicApiKey.isNotBlank() ||
               deepseekApiKey.isNotBlank() ||
               groqApiKey.isNotBlank() ||
               xaiApiKey.isNotBlank() ||
               alibabaApiKey.isNotBlank() ||
               zhipuApiKey.isNotBlank() ||
               (baiduApiKey.isNotBlank() && baiduSecretKey.isNotBlank()) ||
               perplexityApiKey.isNotBlank() ||
               moonshotApiKey.isNotBlank() ||
               mistralApiKey.isNotBlank() ||
               (anythingllmApiKey.isNotBlank() && anythingllmServerUrl.isNotBlank()) ||
               (customApiKey.isNotBlank() || customBaseUrl.isNotBlank())
    }
    
    /**
     * Get list of all configured providers
     */
    fun getConfiguredProviders(): List<AiProvider> {
        return AiProvider.entries.filter { isProviderConfigured(it) }
    }
    
    /**
     * Validate URL format
     */
    private fun isValidUrl(url: String): Boolean {
        return try {
            val trimmed = url.trim()
            trimmed.startsWith("http://") || trimmed.startsWith("https://")
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Settings validation result
 */
sealed class SettingsValidationResult {
    object Valid : SettingsValidationResult()
    data class MissingApiKey(val provider: AiProvider) : SettingsValidationResult()
    data class MissingSpeechService(val requiredProviders: List<AiProvider>) : SettingsValidationResult()
    data class InvalidConfiguration(val message: String) : SettingsValidationResult()
}

/**
 * Validate settings for specific use case
 */
fun ApiSettings.validateForChat(): SettingsValidationResult {
    return when {
        aiProvider == AiProvider.CUSTOM && !isValidUrl(customBaseUrl) ->
            SettingsValidationResult.InvalidConfiguration("Invalid custom provider URL")
        aiProvider == AiProvider.BAIDU && !isProviderConfigured(AiProvider.BAIDU) ->
            SettingsValidationResult.MissingApiKey(AiProvider.BAIDU)
        aiProvider == AiProvider.ANYTHINGLLM && !isValid() ->
            SettingsValidationResult.InvalidConfiguration(
                "Please configure server URL, API key, and workspace slug for AnythingLLM"
            )
        getCurrentApiKey().isBlank() ->
            SettingsValidationResult.MissingApiKey(aiProvider)
        else -> SettingsValidationResult.Valid
    }
}

/**
 * Validate settings for speech recognition
 */
fun ApiSettings.validateForSpeech(): SettingsValidationResult {
    val sttProviders = listOf(AiProvider.GEMINI, AiProvider.OPENAI, AiProvider.GROQ)
    val configuredStt = sttProviders.filter { isProviderConfigured(it) }
    
    return if (configuredStt.isEmpty()) {
        SettingsValidationResult.MissingSpeechService(sttProviders)
    } else {
        SettingsValidationResult.Valid
    }
}

/**
 * Convert ApiSettings to SttCredentials for use with SttServiceFactory
 */
fun ApiSettings.toSttCredentials(): com.example.rokidphone.service.stt.SttCredentials {
    return com.example.rokidphone.service.stt.SttCredentials(
        selectedProvider = sttProvider.name,
        deepgramApiKey = deepgramApiKey,
        assemblyaiApiKey = assemblyaiApiKey,
        gcpProjectId = gcpProjectId,
        gcpApiKey = gcpApiKey,
        gcpServiceAccountJson = gcpServiceAccountJson,
        gcpUseServiceAccount = gcpUseServiceAccount,
        azureSpeechKey = azureSpeechKey,
        azureSpeechRegion = azureSpeechRegion,
        awsAccessKeyId = awsAccessKeyId,
        awsSecretAccessKey = awsSecretAccessKey,
        awsRegion = awsRegion,
        ibmApiKey = ibmApiKey,
        ibmServiceUrl = ibmServiceUrl,
        iflytekAppId = iflytekAppId,
        iflytekApiKey = iflytekApiKey,
        iflytekApiSecret = iflytekApiSecret,
        huaweiAk = huaweiAk,
        huaweiSk = huaweiSk,
        huaweiRegion = huaweiRegion,
        huaweiProjectId = huaweiProjectId,
        volcengineAk = volcengineAk,
        volcangineSk = volcangineSk,
        volcengineAppId = volcengineAppId,
        aliyunAccessKeyId = aliyunAccessKeyId,
        aliyunAccessKeySecret = aliyunAccessKeySecret,
        aliyunAppKey = aliyunAppKey,
        tencentSecretId = tencentSecretId,
        tencentSecretKey = tencentSecretKey,
        tencentAppId = tencentAppId,
        tencentEngineModelType = tencentEngineModelType,
        baiduAsrApiKey = baiduAsrApiKey,
        baiduAsrSecretKey = baiduAsrSecretKey,
        revaiAccessToken = revaiAccessToken,
        speechmaticsApiKey = speechmaticsApiKey,
        otteraiApiKey = otteraiApiKey
    )
}

/**
 * Extension function to check if URL is valid
 */
private fun ApiSettings.isValidUrl(url: String): Boolean {
    return try {
        val trimmed = url.trim()
        trimmed.startsWith("http://") || trimmed.startsWith("https://")
    } catch (e: Exception) {
        false
    }
}
