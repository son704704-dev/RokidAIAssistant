package com.example.rokidphone.ai.catalog

import com.example.rokidphone.data.AiProvider

/** Result of resolving a provider's model catalog. */
data class ModelCatalogSnapshot(
    val provider: AiProvider,
    val models: List<ModelInfo>,
    val source: CatalogSource,
    val fetchedAtEpochMs: Long?,
    /** Non-null when the remote fetch failed and an older source is being served. */
    val remoteError: String? = null
) {
    val isLive: Boolean get() = source == CatalogSource.LIVE
}

/**
 * Four-tier model catalog:
 *   remote (live) → on-device cache (24h TTL) → verified static fallback → manual ID.
 *
 * The user's currently selected model is never deleted even if it disappears
 * from the remote list; [selectionWarning] reports the mismatch instead.
 */
class ModelCatalogRepository(
    private val remote: RemoteModelCatalogSource,
    private val cache: ModelCatalogCache,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {

    companion object {
        const val DEFAULT_TTL_MS = 24L * 60 * 60 * 1000
    }

    var ttlMs: Long = DEFAULT_TTL_MS

    /**
     * Get the catalog for [provider].
     *
     * @param apiKey credential for the remote fetch; blank skips the network call
     * @param baseUrl optional endpoint override (Custom provider / regional endpoints)
     * @param forceRefresh bypass the TTL and re-fetch from the network
     */
    suspend fun getCatalog(
        provider: AiProvider,
        apiKey: String = "",
        baseUrl: String? = null,
        forceRefresh: Boolean = false
    ): ModelCatalogSnapshot {
        val descriptor = ProviderRegistry.descriptorFor(provider)
        val cached = cache.read(provider)
        val cacheFresh = cached != null && (clock() - cached.fetchedAtEpochMs) < ttlMs

        if (descriptor.hasRemoteCatalog && apiKey.isNotBlank() && (forceRefresh || !cacheFresh)) {
            try {
                val live = remote.fetchModels(provider, apiKey, baseUrl)
                if (live.isNotEmpty()) {
                    cache.write(provider, CachedCatalog(live, clock()))
                    return ModelCatalogSnapshot(provider, live, CatalogSource.LIVE, clock())
                }
            } catch (e: ProviderApiException) {
                return serveStaleOrFallback(provider, cached, e.message)
            }
        }

        if (cached != null) {
            val models = cached.models.map { it.withSource(CatalogSource.CACHED) }
            return ModelCatalogSnapshot(provider, models, CatalogSource.CACHED, cached.fetchedAtEpochMs)
        }

        return fallbackSnapshot(provider)
    }

    /** Synchronous catalog access for UI first-paint (cache/fallback only, no network). */
    fun getCachedOrFallback(provider: AiProvider): ModelCatalogSnapshot {
        val cached = cache.read(provider)
        if (cached != null) {
            val models = cached.models.map { it.withSource(CatalogSource.CACHED) }
            return ModelCatalogSnapshot(provider, models, CatalogSource.CACHED, cached.fetchedAtEpochMs)
        }
        return fallbackSnapshot(provider)
    }

    fun invalidate(provider: AiProvider) = cache.clear(provider)

    /**
     * Build a manual-entry model (user typed the ID by hand). Manual entries
     * are appended to the list and remain selectable forever.
     */
    fun manualModel(provider: AiProvider, modelId: String): ModelInfo = ModelInfo(
        id = modelId,
        displayName = modelId,
        provider = provider,
        capabilities = ModelCapabilityResolver.providerDefault(provider),
        status = ModelStatus.STABLE,
        source = CatalogSource.MANUAL,
        description = "Manually entered model ID"
    )

    /**
     * True when the persisted selection is not present in the catalog
     * currently being served. The selection itself is always kept; the UI
     * shows a localized warning for this case (R.string.model_not_in_catalog).
     */
    fun isSelectionAbsent(snapshot: ModelCatalogSnapshot, selectedModelId: String): Boolean {
        if (selectedModelId.isBlank()) return false
        return snapshot.models.none { it.id == selectedModelId }
    }

    fun selectionWarning(snapshot: ModelCatalogSnapshot, selectedModelId: String): String? {
        return if (isSelectionAbsent(snapshot, selectedModelId)) {
            "Current selection is not in the latest catalog."
        } else {
            null
        }
    }

    /** Resolve effective capabilities for a (possibly manual) selection. */
    fun capabilitiesFor(snapshot: ModelCatalogSnapshot, modelId: String): ModelCapabilities {
        snapshot.models.find { it.id == modelId }?.let { return it.capabilities }
        return ModelCapabilityResolver.resolve(snapshot.provider, modelId)
    }

    private fun serveStaleOrFallback(
        provider: AiProvider,
        cached: CachedCatalog?,
        error: String?
    ): ModelCatalogSnapshot {
        if (cached != null) {
            val models = cached.models.map { it.withSource(CatalogSource.CACHED) }
            return ModelCatalogSnapshot(
                provider, models, CatalogSource.CACHED, cached.fetchedAtEpochMs,
                remoteError = error
            )
        }
        return fallbackSnapshot(provider).copy(remoteError = error)
    }

    private fun fallbackSnapshot(provider: AiProvider) = ModelCatalogSnapshot(
        provider = provider,
        models = FallbackModelCatalog.modelsFor(provider),
        source = CatalogSource.FALLBACK,
        fetchedAtEpochMs = null
    )
}
