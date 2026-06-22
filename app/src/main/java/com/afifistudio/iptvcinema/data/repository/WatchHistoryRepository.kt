package com.afifistudio.iptvcinema.data.repository

import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.LastWatchedDao
import com.afifistudio.iptvcinema.data.local.metadataFrom
import com.afifistudio.iptvcinema.data.local.toContinueWatchingItem
import com.afifistudio.iptvcinema.data.local.toDomain
import com.afifistudio.iptvcinema.data.local.toLastWatchedEntity
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContinueWatchingItem
import com.afifistudio.iptvcinema.domain.model.ContentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepository @Inject constructor(
    private val lastWatchedDao: LastWatchedDao,
    private val channelDao: ChannelDao,
) {
    suspend fun rememberLastWatched(channel: Channel) {
        val existing = lastWatchedDao.get(channel.sourceId, channel.id, channel.contentType)
        lastWatchedDao.upsert(
            (existing?.metadataFrom(channel) ?: channel.toLastWatchedEntity())
                .copy(watchedAt = System.currentTimeMillis()),
        )
    }

    suspend fun savePosition(channel: Channel, positionMs: Long, durationMs: Long) {
        if (channel.contentType == ContentType.LIVE) return
        val existing = lastWatchedDao.get(channel.sourceId, channel.id, channel.contentType)
        val base = existing?.metadataFrom(channel) ?: channel.toLastWatchedEntity()
        lastWatchedDao.upsert(
            base.copy(
                watchedAt = System.currentTimeMillis(),
                playbackPositionMs = positionMs.coerceAtLeast(0L),
                durationMs = durationMs.coerceAtLeast(0L),
            ),
        )
    }

    suspend fun loadSavedPosition(channel: Channel): Long {
        if (channel.contentType == ContentType.LIVE) return 0L
        return lastWatchedDao.get(channel.sourceId, channel.id, channel.contentType)
            ?.playbackPositionMs
            ?: 0L
    }

    suspend fun clearForSource(sourceId: Long) {
        lastWatchedDao.deleteBySource(sourceId)
    }

    suspend fun clearAll() {
        lastWatchedDao.deleteAll()
    }

    suspend fun getRecentContinueWatching(
        sourceId: Long,
        contentType: ContentType? = null,
        limit: Int = 15,
        categoryNames: Map<String?, String?> = emptyMap(),
        excludeNearlyComplete: Boolean = true,
    ): List<ContinueWatchingItem> {
        val filtered = lastWatchedDao
            .getRecentWatchRows(sourceId, contentType, limit)
            .map { row ->
                row.toContinueWatchingItem(categoryNames[row.channelCategoryId ?: row.categoryId])
            }
            .filter { item ->
                !excludeNearlyComplete || !item.isNearlyComplete
            }
        val aggregated = aggregateSeriesEntries(filtered)
        return buildList {
            for (item in aggregated) {
                add(enrichSeriesPresentation(item))
            }
        }
    }

    suspend fun getLatestEpisodeForSeries(
        sourceId: Long,
        seriesId: String,
        categoryNames: Map<String?, String?> = emptyMap(),
    ): ContinueWatchingItem? {
        val row = lastWatchedDao.getLatestEpisodeRowForSeries(sourceId, seriesId) ?: return null
        return row.toContinueWatchingItem(categoryNames[row.categoryId ?: row.channelCategoryId])
    }

    private fun aggregateSeriesEntries(items: List<ContinueWatchingItem>): List<ContinueWatchingItem> {
        val seenSeriesIds = mutableSetOf<String>()
        val result = mutableListOf<ContinueWatchingItem>()
        for (item in items) {
            when (item.channel.contentType) {
                ContentType.EPISODE -> {
                    val seriesId = item.seriesId ?: item.channel.seriesId
                    if (seriesId.isNullOrBlank()) {
                        result.add(item)
                    } else if (seenSeriesIds.add(seriesId)) {
                        result.add(item)
                    }
                }
                ContentType.SERIES -> {
                    val seriesId = item.channel.externalId ?: item.channel.id
                    if (seenSeriesIds.add(seriesId)) {
                        result.add(item)
                    }
                }
                else -> result.add(item)
            }
        }
        return result
    }

    private suspend fun enrichSeriesPresentation(item: ContinueWatchingItem): ContinueWatchingItem {
        val seriesId = item.seriesId ?: item.channel.seriesId
            ?: if (item.channel.contentType == ContentType.SERIES) {
                item.channel.externalId ?: item.channel.id
            } else {
                null
            }
        if (seriesId.isNullOrBlank()) return item
        val series = channelDao.getByExternalId(item.channel.sourceId, seriesId, ContentType.SERIES)
            ?.toDomain(item.seriesName ?: item.channel.seriesName)
            ?: return item
        return item.copy(
            seriesId = seriesId,
            seriesName = series.name,
            seriesPosterUrl = series.logoUrl,
        )
    }
}
