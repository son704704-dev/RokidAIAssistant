package com.example.rokidphone.ai.catalog

/**
 * Wire protocol spoken by a provider. Each protocol has exactly one
 * request adapter in the service layer; request parameter policy is
 * driven by [com.example.rokidphone.service.ai.ProviderRequestPolicy],
 * not by model-ID string matching.
 */
enum class ApiProtocol {
    /** Gemini native `:generateContent` / `:streamGenerateContent`. */
    GEMINI_GENERATE_CONTENT,

    /** Gemini Live bidirectional WebSocket (BidiGenerateContent). */
    GEMINI_LIVE,

    /** OpenAI Responses API (`POST /v1/responses`). */
    OPENAI_RESPONSES,

    /** OpenAI Chat Completions API (`POST /v1/chat/completions`) and compatibles. */
    OPENAI_CHAT_COMPLETIONS,

    /** Anthropic Messages API (`POST /v1/messages`). */
    ANTHROPIC_MESSAGES,

    /** Baidu Qianfan v2 (OpenAI-compatible, bearer API key). */
    BAIDU_QIANFAN_V2,

    /** Baidu legacy RPC (API Key + Secret Key OAuth). Kept for migration only. */
    BAIDU_LEGACY_RPC,

    /** AnythingLLM workspace chat API. */
    ANYTHING_LLM,

    /** User-supplied OpenAI-compatible endpoint (Ollama, LM Studio, vLLM...). */
    CUSTOM_OPENAI_COMPATIBLE
}

/** Format of the provider's Models-list endpoint response. */
enum class CatalogFormat {
    /** `{"data":[{"id":...}]}` — OpenAI, DeepSeek, Groq, xAI, Moonshot, Zhipu, Alibaba, Custom. */
    OPENAI_STYLE,

    /** `{"models":[{"name":"models/x","supportedGenerationMethods":[...]}]}` — Gemini. */
    GEMINI,

    /** `{"data":[{"id","display_name","capabilities"?}]}` — Anthropic. */
    ANTHROPIC,

    /** `{"data":[{"id","capabilities":{...},"max_context_length"?}]}` — Mistral. */
    MISTRAL,

    /** `{"data":[{"id":...}]}` on Qianfan v2 — Baidu. */
    BAIDU_QIANFAN_V2,

    /** No reliable models endpoint; use fallback list + manual entry. */
    NONE
}
