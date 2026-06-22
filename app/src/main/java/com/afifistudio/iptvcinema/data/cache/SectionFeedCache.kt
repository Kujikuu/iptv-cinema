package com.afifistudio.iptvcinema.data.cache

import com.afifistudio.iptvcinema.ui.browse.BrowseSection
import com.afifistudio.iptvcinema.ui.browse.CategorySummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SectionFeedCache @Inject constructor() {

    private data class Key(val sourceId: Long, val section: BrowseSection)

    private data class Entry(
        val sourceUpdatedAt: Long,
        val summaries: List<CategorySummary>,
    )

    private val cache = mutableMapOf<Key, Entry>()

    fun get(sourceId: Long, section: BrowseSection, sourceUpdatedAt: Long): List<CategorySummary>? {
        val entry = cache[Key(sourceId, section)] ?: return null
        if (entry.sourceUpdatedAt != sourceUpdatedAt) {
            cache.remove(Key(sourceId, section))
            return null
        }
        return entry.summaries
    }

    fun put(
        sourceId: Long,
        section: BrowseSection,
        sourceUpdatedAt: Long,
        summaries: List<CategorySummary>,
    ) {
        cache[Key(sourceId, section)] = Entry(sourceUpdatedAt, summaries)
    }

    fun clearSource(sourceId: Long) {
        cache.keys.removeAll { it.sourceId == sourceId }
    }

    fun clearAll() {
        cache.clear()
    }
}
