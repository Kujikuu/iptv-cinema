package com.tviptv.app.data.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.tvprovider.media.tv.TvContractCompat
import androidx.tvprovider.media.tv.WatchNextProgram
import com.tviptv.app.data.repository.SourceRepository
import com.tviptv.app.data.repository.WatchHistoryRepository
import com.tviptv.app.domain.model.ContinueWatchingItem
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.ui.player.PlayerActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchNextPublisher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sourceRepository: SourceRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
) {
    suspend fun sync() {
        runCatching {
            clearExistingPrograms()
            val sources = sourceRepository.getSources()
            if (sources.isEmpty()) return
            val items = sources.flatMap { source ->
                watchHistoryRepository.getRecentContinueWatching(
                    sourceId = source.id,
                    contentType = null,
                    limit = WATCH_NEXT_LIMIT,
                )
            }.take(WATCH_NEXT_LIMIT)
            items.forEach { item ->
                publishItem(item)
            }
        }
    }

    private fun clearExistingPrograms() {
        context.contentResolver.delete(
            TvContractCompat.WatchNextPrograms.CONTENT_URI,
            null,
            null,
        )
    }

    private fun publishItem(item: ContinueWatchingItem) {
        val channel = item.channel
        val intent = PlayerActivity.createIntent(context, channel, channel.categoryId)
        val isSeriesPresentation = channel.contentType == ContentType.EPISODE &&
            !item.seriesName.isNullOrBlank()
        val title = if (isSeriesPresentation) {
            item.seriesName ?: channel.name
        } else {
            channel.name
        }
        val posterUri = when {
            isSeriesPresentation && !item.seriesPosterUrl.isNullOrBlank() -> item.seriesPosterUrl
            else -> channel.logoUrl
        }
        val builder = WatchNextProgram.Builder()
            .setWatchNextType(TvContractCompat.WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE)
            .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE)
            .setTitle(title)
            .setDescription(channel.categoryName)
            .setIntentUri(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)))
            .setLastEngagementTimeUtcMillis(item.watchedAt)
        if (!posterUri.isNullOrBlank()) {
            builder.setPosterArtUri(Uri.parse(posterUri))
        }
        if (item.durationMs > 0L) {
            builder.setDurationMillis(item.durationMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
            builder.setLastPlaybackPositionMillis(
                item.playbackPositionMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            )
        }
        val values = builder.build().toContentValues()
        context.contentResolver.insert(TvContractCompat.WatchNextPrograms.CONTENT_URI, values)
    }

    companion object {
        private const val WATCH_NEXT_LIMIT = 10
    }
}
