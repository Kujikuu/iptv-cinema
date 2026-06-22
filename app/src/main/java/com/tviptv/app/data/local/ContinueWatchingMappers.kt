package com.tviptv.app.data.local

import com.tviptv.app.data.local.entity.LastWatchedEntity
import com.tviptv.app.data.local.entity.LastWatchedRow
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.ContinueWatchingItem
import com.tviptv.app.domain.model.ContentType

fun LastWatchedRow.toContinueWatchingItem(categoryName: String? = null): ContinueWatchingItem {
    val resolvedCategoryId = channelCategoryId ?: categoryId
    val channel = if (channelName != null) {
        Channel(
            id = channelId,
            name = channelName,
            logoUrl = channelLogoUrl,
            categoryId = resolvedCategoryId,
            categoryName = categoryName,
            streamUrl = channelStreamUrl,
            sourceId = sourceId,
            externalId = channelId,
            contentType = contentType,
            containerExtension = channelContainerExtension,
            plot = channelPlot,
            addedAt = channelAddedAt,
            seriesId = seriesId,
            seriesName = seriesName,
        )
    } else {
        Channel(
            id = channelId,
            name = title ?: channelId,
            logoUrl = imageUrl,
            categoryId = resolvedCategoryId,
            categoryName = categoryName,
            streamUrl = null,
            sourceId = sourceId,
            externalId = channelId,
            contentType = contentType,
            seriesId = seriesId,
            seriesName = seriesName,
        )
    }
    return ContinueWatchingItem(
        channel = channel,
        watchedAt = watchedAt,
        playbackPositionMs = playbackPositionMs,
        durationMs = durationMs,
        seriesId = seriesId ?: channel.seriesId,
        seriesName = seriesName ?: channel.seriesName,
    )
}

fun LastWatchedEntity.metadataFrom(channel: Channel): LastWatchedEntity = copy(
    title = channel.name,
    imageUrl = channel.logoUrl,
    categoryId = channel.categoryId,
    seriesId = channel.seriesId,
    seriesName = channel.seriesName,
)

fun Channel.toLastWatchedEntity(
    watchedAt: Long = System.currentTimeMillis(),
    playbackPositionMs: Long = 0L,
    durationMs: Long = 0L,
): LastWatchedEntity = LastWatchedEntity(
    sourceId = sourceId,
    channelId = id,
    contentType = contentType,
    watchedAt = watchedAt,
    playbackPositionMs = playbackPositionMs,
    durationMs = durationMs,
    title = name,
    imageUrl = logoUrl,
    categoryId = categoryId,
    seriesId = seriesId,
    seriesName = seriesName,
)
