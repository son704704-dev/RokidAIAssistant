package com.example.rokidphone.ai.catalog

import com.example.rokidphone.data.AiProvider

/**
 * A single model entry in a provider's catalog.
 *
 * @param id exact model ID sent to the API
 * @param displayName human readable name
 * @param capabilities model-level capability flags (never name-inferred)
 * @param status stable / preview / deprecated / legacy
 * @param source where this entry came from (live API, cache, fallback, manual)
 * @param description short user-facing description
 */
data class ModelInfo(
    val id: String,
    val displayName: String,
    val provider: AiProvider,
    val capabilities: ModelCapabilities = ModelCapabilities.TEXT_ONLY,
    val status: ModelStatus = ModelStatus.STABLE,
    val source: CatalogSource = CatalogSource.FALLBACK,
    val description: String = ""
) {
    val isSelectableDefault: Boolean
        get() = status == ModelStatus.STABLE

    fun withSource(newSource: CatalogSource): ModelInfo = copy(source = newSource)
}
