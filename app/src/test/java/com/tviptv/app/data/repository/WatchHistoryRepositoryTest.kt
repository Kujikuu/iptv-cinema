package com.tviptv.app.data.repository

import com.tviptv.app.data.local.dao.ChannelDao
import com.tviptv.app.data.local.dao.LastWatchedDao
import com.tviptv.app.data.local.entity.LastWatchedRow
import com.tviptv.app.domain.model.ContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WatchHistoryRepositoryTest {

    private val lastWatchedDao = mockk<LastWatchedDao>(relaxed = true)
    private val channelDao = mockk<ChannelDao>(relaxed = true)
    private lateinit var repository: WatchHistoryRepository

    @Before
    fun setUp() {
        repository = WatchHistoryRepository(lastWatchedDao, channelDao)
    }

    @Test
    fun getRecentContinueWatching_excludesNearlyCompleteItems() = runTest {
        coEvery {
            lastWatchedDao.getRecentWatchRows(1L, null, 15)
        } returns listOf(
            watchRow(
                channelId = "movie-1",
                contentType = ContentType.MOVIE,
                playbackPositionMs = 3_580_000L,
                durationMs = 3_600_000L,
            ),
            watchRow(
                channelId = "movie-2",
                contentType = ContentType.MOVIE,
                playbackPositionMs = 600_000L,
                durationMs = 3_600_000L,
            ),
        )

        val items = repository.getRecentContinueWatching(
            sourceId = 1L,
            excludeNearlyComplete = true,
        )

        assertEquals(1, items.size)
        assertEquals("movie-2", items.first().channel.id)
    }

    @Test
    fun getRecentContinueWatching_includesEpisodeRowsWithoutChannelJoin() = runTest {
        coEvery {
            lastWatchedDao.getRecentWatchRows(1L, null, 15)
        } returns listOf(
            watchRow(
                channelId = "ep-1",
                contentType = ContentType.EPISODE,
                title = "Pilot",
                seriesId = "series-1",
                seriesName = "Test Series",
            ),
        )

        val items = repository.getRecentContinueWatching(sourceId = 1L)

        assertEquals(1, items.size)
        assertEquals(ContentType.EPISODE, items.first().channel.contentType)
        assertEquals("Pilot", items.first().channel.name)
        assertEquals("series-1", items.first().seriesId)
    }

    @Test
    fun getRecentContinueWatching_aggregatesMultipleEpisodesFromSameSeries() = runTest {
        coEvery {
            lastWatchedDao.getRecentWatchRows(1L, null, 15)
        } returns listOf(
            watchRow(
                channelId = "ep-2",
                contentType = ContentType.EPISODE,
                title = "Episode 2",
                seriesId = "series-1",
                seriesName = "Test Series",
                watchedAt = 20L,
            ),
            watchRow(
                channelId = "ep-1",
                contentType = ContentType.EPISODE,
                title = "Episode 1",
                seriesId = "series-1",
                seriesName = "Test Series",
                watchedAt = 10L,
            ),
        )
        coEvery {
            channelDao.getByExternalId(1L, "series-1", ContentType.SERIES)
        } returns null

        val items = repository.getRecentContinueWatching(sourceId = 1L)

        assertEquals(1, items.size)
        assertEquals("ep-2", items.first().channel.id)
    }

    @Test
    fun rememberLastWatched_upsertsWithMetadata() = runTest {
        val channel = com.tviptv.app.domain.model.Channel(
            id = "ep-1",
            name = "Episode 1",
            logoUrl = "http://img/ep.jpg",
            categoryId = "cat",
            categoryName = "Drama",
            streamUrl = null,
            sourceId = 1L,
            externalId = "ep-1",
            contentType = ContentType.EPISODE,
            seriesId = "series-1",
            seriesName = "My Show",
        )
        coEvery {
            lastWatchedDao.get(1L, "ep-1", ContentType.EPISODE)
        } returns null

        repository.rememberLastWatched(channel)

        coVerify {
            lastWatchedDao.upsert(
                match {
                    it.channelId == "ep-1" &&
                        it.title == "Episode 1" &&
                        it.seriesId == "series-1"
                },
            )
        }
    }

    private fun watchRow(
        channelId: String,
        contentType: ContentType,
        playbackPositionMs: Long = 0L,
        durationMs: Long = 0L,
        title: String? = null,
        seriesId: String? = null,
        seriesName: String? = null,
        watchedAt: Long = 1L,
    ) = LastWatchedRow(
        sourceId = 1L,
        channelId = channelId,
        contentType = contentType,
        watchedAt = watchedAt,
        playbackPositionMs = playbackPositionMs,
        durationMs = durationMs,
        title = title,
        imageUrl = null,
        categoryId = "cat",
        seriesId = seriesId,
        seriesName = seriesName,
        channelName = if (contentType != ContentType.EPISODE) "Channel $channelId" else null,
        channelLogoUrl = null,
        channelCategoryId = "cat",
        channelStreamUrl = null,
        channelSortOrder = 0,
        channelContainerExtension = null,
        channelPlot = null,
        channelAddedAt = null,
    )
}
