package com.tviptv.app.data.cache

import com.tviptv.app.domain.model.Category
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.ContentType
import javax.inject.Inject
import javax.inject.Singleton

data class CachedCategoryChannels(
    val channels: List<Channel>,
    val favoriteIds: Set<String>,
    val totalCount: Int,
    val loadedCount: Int,
    val hasMore: Boolean,
    val contentType: ContentType,
    val categoryName: String,
)

@Singleton
class CategoryChannelsCache @Inject constructor() {

    private data class Entry(
        val sourceUpdatedAt: Long,
        val data: CachedCategoryChannels,
    )

    private val cache = mutableMapOf<String, Entry>()

    fun get(category: Category, sourceUpdatedAt: Long): CachedCategoryChannels? {
        val entry = cache[cacheKey(category)] ?: return null
        if (entry.sourceUpdatedAt != sourceUpdatedAt) {
            cache.remove(cacheKey(category))
            return null
        }
        return entry.data
    }

    fun put(category: Category, sourceUpdatedAt: Long, data: CachedCategoryChannels) {
        cache[cacheKey(category)] = Entry(sourceUpdatedAt, data)
    }

    fun clearSource(sourceId: Long) {
        cache.keys.removeAll { key -> key.startsWith("$sourceId:") }
    }

    fun clearAll() {
        cache.clear()
    }

    fun patchFavorite(sourceId: Long, channelId: String, contentType: ContentType, isFavorite: Boolean) {
        val prefix = "$sourceId:${contentType.name}:"
        cache.entries.toList().forEach { (key, entry) ->
            if (!key.startsWith(prefix)) return@forEach
            val data = entry.data
            val updatedFavoriteIds = if (isFavorite) {
                data.favoriteIds + channelId
            } else {
                data.favoriteIds - channelId
            }
            if (updatedFavoriteIds == data.favoriteIds) return@forEach
            cache[key] = entry.copy(data = data.copy(favoriteIds = updatedFavoriteIds))
        }
    }

    private fun cacheKey(category: Category): String =
        "${category.sourceId}:${category.contentType.name}:${category.id}"
}
