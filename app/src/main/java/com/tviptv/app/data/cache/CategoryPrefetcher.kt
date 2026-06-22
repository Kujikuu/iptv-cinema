package com.tviptv.app.data.cache

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.tviptv.app.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryPrefetcher @Inject constructor(
    private val contentLoader: CategoryContentLoader,
    private val categoryChannelsCache: CategoryChannelsCache,
    @ApplicationContext private val context: Context,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastPrefetchedKey: String? = null

    fun prefetch(category: Category, sourceUpdatedAt: Long) {
        val key = prefetchKey(category)
        if (key == lastPrefetchedKey) return
        lastPrefetchedKey = key
        scope.launch {
            contentLoader.prefetchFirstPageIfAbsent(category, sourceUpdatedAt)
            preloadPosterImages(category, sourceUpdatedAt)
        }
    }

    private fun preloadPosterImages(category: Category, sourceUpdatedAt: Long) {
        val channels = categoryChannelsCache.get(category, sourceUpdatedAt)?.channels.orEmpty()
        channels
            .asSequence()
            .mapNotNull { channel -> channel.logoUrl?.takeIf { url -> url.isNotBlank() } }
            .take(CategoryContentLoader.PRELOAD_IMAGE_COUNT)
            .forEach { url ->
                context.imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(url)
                        .build(),
                )
            }
    }

    private fun prefetchKey(category: Category): String =
        "${category.sourceId}:${category.contentType.name}:${category.id}"
}
