package com.tviptv.app.data.cache

import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.ContinueWatchingItem
import com.tviptv.app.ui.browse.CategorySummary
import javax.inject.Inject
import javax.inject.Singleton

data class CachedHomeFeed(
    val favorites: List<Channel>,
    val continueWatching: List<ContinueWatchingItem>,
    val homeCategoryHighlights: List<CategorySummary>,
    val featuredChannel: Channel?,
)

@Singleton
class HomeFeedCache @Inject constructor() {

    private data class Entry(
        val sourceUpdatedAt: Long,
        val feed: CachedHomeFeed,
    )

    private val cache = mutableMapOf<Long, Entry>()

    fun get(sourceId: Long, sourceUpdatedAt: Long): CachedHomeFeed? {
        val entry = cache[sourceId] ?: return null
        if (entry.sourceUpdatedAt != sourceUpdatedAt) {
            cache.remove(sourceId)
            return null
        }
        return entry.feed
    }

    fun put(sourceId: Long, sourceUpdatedAt: Long, feed: CachedHomeFeed) {
        cache[sourceId] = Entry(sourceUpdatedAt, feed)
    }

    fun clearSource(sourceId: Long) {
        cache.remove(sourceId)
    }

    fun clearAll() {
        cache.clear()
    }
}
