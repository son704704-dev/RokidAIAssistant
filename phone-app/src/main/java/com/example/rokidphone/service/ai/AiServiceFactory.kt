package com.example.rokidphone.service.ai

import com.example.rokidphone.ai.catalog.ApiProtocol
import com.example.rokidphone.ai.catalog.ProviderRegistry
import com.example.rokidphone.ai.provider.AnythingLLMProvider
import com.example.rokidphone.ai.provider.ChatMessage
import com.example.rokidphone.ai.provider.GenerationResult
import com.example.rokidphone.ai.provider.MessageRole
import com.example.rokidphone.ai.provider.ProviderSetting
import com.example.rokidphone.data.AiProvider
import com.example.rokidphone.data.ApiSettings
import com.example.rokidphone.service.SpeechResult

/**
 * AI Service Factory.
 *
 * Services are constructed from [ProviderRegistry] descriptors instead of a
 * growing `when` block with repeated constructor arguments. The registry
 * decides the wire protocol; the factory only supplies credentials, the
 * per-provider model selection and user tuning parameters.
 */
object AiServiceFactory {

    /**
     * Create AI service based on settings
     */
    fun createService(settings: ApiSettings): AiServiceProvider {
        val apiKey = settings.getCurrentApiKey()
        val systemPrompt = settings.systemPrompt
        val modelId = settings.getCurrentModelId()
        val descriptor = ProviderRegistry.descriptorFor(settings.aiProvider)

        return when (descriptor.protocol) {
            ApiProtocol.GEMINI_GENERATE_CONTENT -> GeminiService(
                apiKey = apiKey,
                modelId = modelId,
                systemPrompt = systemPrompt,
                temperature = settings.temperature,
                maxTokens = settings.maxTokens,
                topP = settings.topP
            )

            ApiProtocol.GEMINI_LIVE -> {
                // Gemini Live uses WebSocket streaming, not REST API.
                // Return a standard GeminiService as fallback for non-live operations
                // (e.g., analyzeImage). The actual live session is managed by
                // GeminiLiveSession in PhoneAIService.
                GeminiService(
                    apiKey = apiKey,
                    modelId = modelId,
                    systemPrompt = systemPrompt,
                    temperature = settings.temperature,
                    maxTokens = settings.maxTokens,
                    topP = settings.topP
                )
            }

            ApiProtocol.ANTHROPIC_MESSAGES -> AnthropicService(
                apiKey = apiKey,
                modelId = modelId,
                systemPrompt = systemPrompt,
                temperature = settings.temperature,
                maxTokens = settings.maxTokens,
                topP = settings.topP
            )

            ApiProtocol.OPENAI_RESPONSES -> OpenAiCompatibleService(
                apiKey = apiKey,
                baseUrl = descriptor.defaultBaseUrl,
                modelId = modelId,
                systemPrompt = systemPrompt,
                providerType = AiProvider.OPENAI,
                temperature = settings.temperature,
                maxTokens = settings.maxTokens,
                topP = settings.topP,
                frequencyPenalty = settings.frequencyPenalty,
                presencePenalty = settings.presencePenalty,
                useResponsesApi = ProviderRequestPolicies.openAiPrefersResponses(modelId)
            )

            ApiProtocol.BAIDU_QIANFAN_V2 -> {
                if (settings.isBaiduLegacyMode()) {
                    BaiduService(
                        apiKey = settings.baiduApiKey,
                        secretKey = settings.baiduSecretKey,
                        modelId = modelId,
                        systemPrompt = systemPrompt,
                        temperature = settings.temperature,
                        topP = settings.topP
                    )
                } else {
                    QianfanV2Service(
                        apiKey = settings.baiduQianfanApiKey,
                        modelId = modelId,
                        systemPrompt = systemPrompt,
                        temperature = settings.temperature,
                        maxTokens = settings.maxTokens,
                        topP = settings.topP,
                        frequencyPenalty = settings.frequencyPenalty,
                        presencePenalty = settings.presencePenalty
                    )
                }
            }

            ApiProtocol.ANYTHING_LLM -> createAnythingLlmAdapter(settings)

            ApiProtocol.OPENAI_CHAT_COMPLETIONS, ApiProtocol.CUSTOM_OPENAI_COMPATIBLE ->
                createChatCompletionsService(settings, modelId, apiKey, systemPrompt)

            ApiProtocol.BAIDU_LEGACY_RPC -> BaiduService(
                apiKey = settings.baiduApiKey,
                secretKey = settings.baiduSecretKey,
                modelId = modelId,
                systemPrompt = systemPrompt,
                temperature = settings.temperature,
                topP = settings.topP
            )
        }
    }

    /** Chat Completions family: provider-specific subclasses where required. */
    private fun createChatCompletionsService(
        settings: ApiSettings,
        modelId: String,
        apiKey: String,
        systemPrompt: String
    ): OpenAiCompatibleService {
        return when (settings.aiProvider) {
            AiProvider.DEEPSEEK -> DeepSeekService(
                apiKey = apiKey,
                baseUrl = settings.aiProvider.defaultBaseUrl,
                modelId = modelId,
                systemPrompt = systemPrompt,
                temperature = settings.temperature,
                maxTokens = settings.maxTokens,
                topP = settings.topP,
                frequencyPenalty = settings.frequencyPenalty,
                presencePenalty = settings.presencePenalty
            )

            AiProvider.PERPLEXITY -> PerplexityService(
                apiKey = apiKey,
                modelId = modelId,
                systemPrompt = systemPrompt,
                temperature = settings.temperature,
                maxTokens = settings.maxTokens,
                topP = settings.topP
            )

            AiProvider.CUSTOM -> OpenAiCompatibleService(
                apiKey = settings.customApiKey,
                baseUrl = settings.getCurrentBaseUrl(),
                modelId = settings.customModelName.ifBlank { modelId },
                systemPrompt = systemPrompt,
                providerType = AiProvider.CUSTOM,
                temperature = settings.temperature,
                maxTokens = settings.maxTokens,
                topP = settings.topP,
                frequencyPenalty = settings.frequencyPenalty,
                presencePenalty = settings.presencePenalty,
                useResponsesApi = settings.customProtocol == "responses",
                capabilityOverrides = com.example.rokidphone.ai.catalog.ModelCapabilityResolver
                    .overridesFromKeys(AiProvider.CUSTOM, settings.customCapabilityOverrides)
            )

            else -> OpenAiCompatibleService(
                apiKey = apiKey,
                baseUrl = if (settings.aiProvider == AiProvider.ALIBABA) {
                    settings.getCurrentBaseUrl()
                } else {
                    settings.aiProvider.defaultBaseUrl
                },
                modelId = modelId,
                systemPrompt = systemPrompt,
                providerType = settings.aiProvider,
                temperature = settings.temperature,
                maxTokens = settings.maxTokens,
                topP = settings.topP,
                frequencyPenalty = settings.frequencyPenalty,
                presencePenalty = settings.presencePenalty
            )
        }
    }

    private fun createAnythingLlmAdapter(settings: ApiSettings): AiServiceProvider {
        val providerSetting = ProviderSetting.AnythingLLM(
            serverUrl = settings.anythingllmServerUrl,
            apiKey = settings.anythingllmApiKey,
            workspaceSlug = settings.anythingllmWorkspaceSlug
        )
        val anythingLlmProvider = AnythingLLMProvider()
        val history = mutableListOf<ChatMessage>()
        return object : AiServiceProvider {
            override val provider: AiProvider = AiProvider.ANYTHINGLLM
            override suspend fun transcribe(pcmAudioData: ByteArray, languageCode: String): SpeechResult =
                SpeechResult.Error("AnythingLLM does not support speech recognition")
            override suspend fun chat(userMessage: String): String {
                history.add(ChatMessage(MessageRole.USER, userMessage))
                return when (val result = anythingLlmProvider.generateText(providerSetting, history)) {
                    is GenerationResult.Success -> {
                        history.add(ChatMessage(MessageRole.ASSISTANT, result.text))
                        result.text
                    }
                    is GenerationResult.Error -> "Error: ${result.message}"
                }
            }
            override suspend fun analyzeImage(imageData: ByteArray, prompt: String): String =
                "AnythingLLM does not support image analysis"
            override fun clearHistory() { history.clear() }
        }
    }

    /**
     * Create service for testing connection
     */
    fun createTestService(settings: ApiSettings): OpenAiCompatibleService? {
        return when (settings.aiProvider) {
            AiProvider.GEMINI, AiProvider.ANTHROPIC,
            AiProvider.GEMINI_LIVE, AiProvider.ANYTHINGLLM -> null // Not OpenAI-compatible

            AiProvider.BAIDU -> if (settings.isBaiduLegacyMode()) {
                null // Legacy OAuth flow: use createBaiduTestService
            } else {
                QianfanV2Service(
                    apiKey = settings.baiduQianfanApiKey,
                    modelId = settings.getCurrentModelId(),
                    systemPrompt = ""
                )
            }

            AiProvider.DEEPSEEK -> DeepSeekService(
                apiKey = settings.getCurrentApiKey(),
                baseUrl = settings.aiProvider.defaultBaseUrl,
                modelId = settings.getCurrentModelId(),
                systemPrompt = ""
            )

            AiProvider.PERPLEXITY -> PerplexityService(
                apiKey = settings.getCurrentApiKey(),
                modelId = settings.getCurrentModelId(),
                systemPrompt = ""
            )

            AiProvider.OPENAI -> OpenAiCompatibleService(
                apiKey = settings.getCurrentApiKey(),
                baseUrl = settings.aiProvider.defaultBaseUrl,
                modelId = settings.getCurrentModelId(),
                systemPrompt = "",
                providerType = settings.aiProvider,
                useResponsesApi = ProviderRequestPolicies.openAiPrefersResponses(settings.getCurrentModelId())
            )

            AiProvider.GROQ,
            AiProvider.XAI, AiProvider.ALIBABA, AiProvider.ZHIPU,
            AiProvider.MOONSHOT, AiProvider.MISTRAL -> OpenAiCompatibleService(
                apiKey = settings.getCurrentApiKey(),
                baseUrl = if (settings.aiProvider == AiProvider.ALIBABA) {
                    settings.getCurrentBaseUrl()
                } else {
                    settings.aiProvider.defaultBaseUrl
                },
                modelId = settings.getCurrentModelId(),
                systemPrompt = "",
                providerType = settings.aiProvider
            )

            AiProvider.CUSTOM -> OpenAiCompatibleService(
                apiKey = settings.customApiKey,
                baseUrl = settings.getCurrentBaseUrl(),
                modelId = settings.customModelName.ifBlank { settings.aiModelId },
                systemPrompt = "",
                providerType = AiProvider.CUSTOM,
                useResponsesApi = settings.customProtocol == "responses",
                capabilityOverrides = com.example.rokidphone.ai.catalog.ModelCapabilityResolver
                    .overridesFromKeys(AiProvider.CUSTOM, settings.customCapabilityOverrides)
            )
        }
    }

    /**
     * Create Baidu legacy test service (API Key + Secret Key OAuth)
     */
    fun createBaiduTestService(settings: ApiSettings): BaiduService? {
        return if (settings.aiProvider == AiProvider.BAIDU && settings.isBaiduLegacyMode()) {
            BaiduService(
                apiKey = settings.baiduApiKey,
                secretKey = settings.baiduSecretKey,
                modelId = settings.getCurrentModelId(),
                systemPrompt = ""
            )
        } else null
    }

    /**
     * Create service by provider (for speech recognition service selection).
     * STT is decoupled from chat: only providers with a real transcription
     * endpoint get a service here.
     */
    fun createSpeechService(provider: AiProvider, apiKey: String): AiServiceProvider? {
        return when (provider) {
            AiProvider.GEMINI -> GeminiService(apiKey = apiKey)
            AiProvider.OPENAI -> OpenAiCompatibleService(
                apiKey = apiKey,
                baseUrl = AiProvider.OPENAI.defaultBaseUrl,
                modelId = "gpt-5.1",
                providerType = AiProvider.OPENAI
            )
            AiProvider.GROQ -> OpenAiCompatibleService(
                apiKey = apiKey,
                baseUrl = AiProvider.GROQ.defaultBaseUrl,
                modelId = "llama-3.3-70b-versatile",
                providerType = AiProvider.GROQ
            )
            // All other chat providers do not expose a transcription endpoint.
            else -> null
        }
    }
}
