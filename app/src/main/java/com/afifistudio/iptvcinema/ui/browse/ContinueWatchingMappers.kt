package com.afifistudio.iptvcinema.ui.browse

import android.content.Context
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.ContinueWatchingItem
import java.util.concurrent.TimeUnit

fun ContinueWatchingItem.toBrowseItem(
    context: Context,
    favoriteIds: Set<String> = emptySet(),
): BrowseItem {
    val channel = channel
    val badge = when (channel.contentType) {
        ContentType.LIVE -> context.getString(R.string.live_badge)
        ContentType.MOVIE -> context.getString(R.string.movie_badge)
        ContentType.SERIES -> context.getString(R.string.series_badge)
        ContentType.EPISODE -> context.getString(R.string.episode_badge)
    }
    val isSeriesPresentation = channel.contentType == ContentType.EPISODE &&
        !seriesName.isNullOrBlank()
    val subtitle = when {
        channel.contentType == ContentType.LIVE -> null
        isSeriesPresentation -> progressLabel(context)
        channel.contentType == ContentType.EPISODE && !seriesName.isNullOrBlank() ->
            seriesName
        !channel.categoryName.isNullOrBlank() -> channel.categoryName
        else -> null
    }
    val title = if (isSeriesPresentation) {
        seriesName ?: channel.name
    } else {
        channel.name
    }
    val imageUrl = when {
        isSeriesPresentation && !seriesPosterUrl.isNullOrBlank() -> seriesPosterUrl
        else -> channel.logoUrl
    }
    return BrowseItem(
        id = "cw_${channel.id}_${channel.contentType.name}",
        title = title,
        subtitle = subtitle,
        imageUrl = imageUrl,
        type = BrowseItemType.CHANNEL,
        sourceId = channel.sourceId,
        channel = channel,
        badge = badge,
        usePosterLayout = channel.contentType != ContentType.LIVE,
        isFavorite = favoriteIds.contains(channel.id),
        progressFraction = progressFraction?.takeIf { it >= ContinueWatchingItem.MIN_VISIBLE_PROGRESS },
        progressLabel = if (isSeriesPresentation) null else progressLabel(context),
        seriesId = seriesId ?: channel.seriesId,
    )
}

private fun ContinueWatchingItem.progressLabel(context: Context): String? {
    if (channel.contentType == ContentType.LIVE) return null
    val remainingMs = durationMs - playbackPositionMs
    if (durationMs <= 0L || remainingMs <= 0L) return null
    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(remainingMs).coerceAtLeast(1L)
    return context.getString(R.string.continue_watching_time_left, minutesLeft)
}

fun Channel.toBrowseItem(
    favoriteIds: Set<String> = emptySet(),
    context: Context? = null,
): BrowseItem {
    val badge = context?.let { ctx ->
        when (contentType) {
            ContentType.LIVE -> ctx.getString(R.string.live_badge)
            ContentType.MOVIE -> ctx.getString(R.string.movie_badge)
            ContentType.SERIES -> ctx.getString(R.string.series_badge)
            ContentType.EPISODE -> ctx.getString(R.string.episode_badge)
        }
    }
    return BrowseItem(
        id = "channel_$id",
        title = name,
        subtitle = categoryName,
        imageUrl = logoUrl,
        type = BrowseItemType.CHANNEL,
        sourceId = sourceId,
        channel = this,
        badge = badge,
        usePosterLayout = contentType != ContentType.LIVE,
        isFavorite = favoriteIds.contains(id),
        seriesId = seriesId,
    )
}
