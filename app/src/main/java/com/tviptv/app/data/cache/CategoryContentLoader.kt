package com.tviptv.app.data.cache

import com.tviptv.app.data.local.dao.ChannelDao
import com.tviptv.app.data.local.dao.FavoriteDao
import com.tviptv.app.data.local.toDomain
import com.tviptv.app.domain.model.Category
import com.tviptv.app.domain.model.Channel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

data class CategoryFirstPage(
    val channels: List<Channel>,
    val favoriteIds: Set<String>,
    val totalCount: Int,
)

@Singleton
class CategoryContentLoader @Inject constructor(
    private val channelDao: ChannelDao,
    private val favoriteDao: FavoriteDao,
    private val categoryChannelsCache: CategoryChannelsCache,
) {

    suspend fun loadFirstPage(category: Category): CategoryFirstPage = coroutineScope {
        val totalCountDeferred = async {
            channelDao.countByCategory(
                category.sourceId,
                category.id,
                category.contentType,
            )
        }
        val pageDeferred = async {
            channelDao.getByCategoryPage(
                sourceId = category.sourceId,
                categoryId = category.id,
                contentType = category.contentType,
                limit = PAGE_SIZE,
                offset = 0,
            )
        }
        val favoriteIdsDeferred = async {
            favoriteDao.getFavoriteChannelIds(category.sourceId, category.contentType).toSet()
        }

        val totalCount = totalCountDeferred.await()
        val page = pageDeferred.await()
        val favoriteIds = favoriteIdsDeferred.await()
        CategoryFirstPage(
            channels = page.map { entity -> entity.toDomain(category.name) },
            favoriteIds = favoriteIds,
            totalCount = totalCount,
        )
    }

    suspend fun prefetchFirstPageIfAbsent(category: Category, sourceUpdatedAt: Long) {
        if (categoryChannelsCache.get(category, sourceUpdatedAt) != null) return
        val firstPage = loadFirstPage(category)
        val loadedCount = firstPage.channels.size
        categoryChannelsCache.put(
            category = category,
            sourceUpdatedAt = sourceUpdatedAt,
            data = CachedCategoryChannels(
                channels = firstPage.channels,
                favoriteIds = firstPage.favoriteIds,
                totalCount = firstPage.totalCount,
                loadedCount = loadedCount,
                hasMore = loadedCount < firstPage.totalCount,
                contentType = category.contentType,
                categoryName = category.name,
            ),
        )
    }

    companion object {
        const val PAGE_SIZE = 60
        const val PRELOAD_IMAGE_COUNT = 12
    }
}
