package com.afifistudio.iptvcinema.data.cache

import com.afifistudio.iptvcinema.domain.model.Episode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeriesEpisodesCache @Inject constructor() {

    private data class Key(val sourceId: Long, val seriesId: String)

    private data class Entry(
        val sourceUpdatedAt: Long,
        val episodes: List<Episode>,
    )

    private val cache = mutableMapOf<Key, Entry>()

    fun get(sourceId: Long, seriesId: String, sourceUpdatedAt: Long): List<Episode>? {
        val entry = cache[Key(sourceId, seriesId)] ?: return null
        if (entry.sourceUpdatedAt != sourceUpdatedAt) {
            cache.remove(Key(sourceId, seriesId))
            return null
        }
        return entry.episodes
    }

    fun put(sourceId: Long, seriesId: String, sourceUpdatedAt: Long, episodes: List<Episode>) {
        cache[Key(sourceId, seriesId)] = Entry(sourceUpdatedAt, episodes)
    }

    fun clearSource(sourceId: Long) {
        cache.keys.removeAll { it.sourceId == sourceId }
    }

    fun clearAll() {
        cache.clear()
    }
}
