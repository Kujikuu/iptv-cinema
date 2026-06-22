package com.tviptv.app.ui.player

import com.tviptv.app.data.cache.SeriesEpisodesLoader
import com.tviptv.app.data.local.dao.ChannelDao
import com.tviptv.app.data.local.dao.FavoriteDao
import com.tviptv.app.data.local.dao.SourceDao
import com.tviptv.app.data.local.entity.ChannelEntity
import com.tviptv.app.data.local.entity.SourceEntity
import com.tviptv.app.data.repository.WatchHistoryRepository
import com.tviptv.app.data.player.PlayerEpgRepository
import com.tviptv.app.data.prefs.AppPreferences
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.domain.model.Episode
import com.tviptv.app.domain.model.SourceType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val channelDao = mockk<ChannelDao>(relaxed = true)
    private val sourceDao = mockk<SourceDao>(relaxed = true)
    private val favoriteDao = mockk<FavoriteDao>(relaxed = true)
    private val watchHistoryRepository = mockk<WatchHistoryRepository>(relaxed = true)
    private val epgRepository = mockk<PlayerEpgRepository>(relaxed = true)
    private val seriesEpisodesLoader = mockk<SeriesEpisodesLoader>(relaxed = true)
    private val appPreferences = mockk<AppPreferences>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: PlayerViewModel

    private val liveChannel = Channel(
        id = "1",
        name = "News",
        logoUrl = null,
        categoryId = "cat1",
        categoryName = "News",
        streamUrl = null,
        sourceId = 1L,
        externalId = "100",
        contentType = ContentType.LIVE,
    )

    private val seriesChannel = Channel(
        id = "series-1",
        name = "Test Series",
        logoUrl = null,
        categoryId = "cat1",
        categoryName = "Series",
        streamUrl = null,
        sourceId = 1L,
        externalId = "series-1",
        contentType = ContentType.SERIES,
    )

    private val episodeChannel = Channel(
        id = "e1",
        name = "Episode 1",
        logoUrl = null,
        categoryId = "cat1",
        categoryName = "Series",
        streamUrl = null,
        sourceId = 1L,
        externalId = "e1",
        contentType = ContentType.EPISODE,
        containerExtension = "mp4",
        seriesId = "series-1",
        seriesName = "Test Series",
    )

    private val episodes = listOf(
        episode("e1", 1, 1),
        episode("e2", 1, 2),
        episode("e3", 1, 3),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery {
            channelDao.getByCategoryPage(any(), any(), any(), any(), any())
        } returns emptyList()
        coEvery { favoriteDao.isFavorite(any(), any(), any()) } returns false
        coEvery { watchHistoryRepository.loadSavedPosition(any()) } returns 0L
        every { appPreferences.isAutoplayNextEpisodeEnabled() } returns true
        coEvery { sourceDao.getById(1L) } returns SourceEntity(
            id = 1L,
            type = SourceType.XTREAM,
            name = "Test",
            url = "http://example.com",
            username = "user",
            updatedAt = 1000L,
        )
        coEvery {
            channelDao.getByExternalId(1L, "series-1", ContentType.SERIES)
        } returns ChannelEntity(
            sourceId = 1L,
            externalId = "series-1",
            name = "Test Series",
            logoUrl = null,
            categoryId = "cat1",
            streamUrl = null,
            sortOrder = 0,
            contentType = ContentType.SERIES,
        )
        coEvery {
            seriesEpisodesLoader.loadEpisodes(seriesChannel, 1000L)
        } returns episodes
        viewModel = PlayerViewModel(
            channelDao = channelDao,
            sourceDao = sourceDao,
            favoriteDao = favoriteDao,
            watchHistoryRepository = watchHistoryRepository,
            epgRepository = epgRepository,
            seriesEpisodesLoader = seriesEpisodesLoader,
            appPreferences = appPreferences,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setOverlayMode_updatesState() = runTest {
        viewModel.setOverlayMode(PlayerOverlayMode.Controls)
        assertEquals(PlayerOverlayMode.Controls, viewModel.overlayMode.value)
    }

    @Test
    fun savePosition_persistsToRepository() = runTest {
        val movie = liveChannel.copy(id = "m1", contentType = ContentType.MOVIE)
        viewModel.savePosition(movie, 60_000L, 3_600_000L)
        advanceUntilIdle()
        coVerify { watchHistoryRepository.savePosition(movie, 60_000L, 3_600_000L) }
    }

    @Test
    fun hasNextAndPrevious_defaultFalseForEmptyPlaylist() {
        assertFalse(viewModel.hasNext())
        assertFalse(viewModel.hasPrevious())
    }

    @Test
    fun initialize_loadsSavedPositionForVod() = runTest {
        val movie = liveChannel.copy(id = "m1", contentType = ContentType.MOVIE)
        coEvery {
            channelDao.getByCategoryPage(1L, "cat1", ContentType.MOVIE, 500, 0)
        } returns emptyList()
        coEvery { watchHistoryRepository.loadSavedPosition(movie) } returns 120_000L
        viewModel.initialize(movie, "cat1")
        advanceUntilIdle()
        assertEquals(120_000L, viewModel.savedPositionMs.value)
    }

    @Test
    fun refreshEpg_clearsStateForNonLive() = runTest {
        val movie = liveChannel.copy(contentType = ContentType.MOVIE)
        viewModel.refreshEpg(movie)
        advanceUntilIdle()
        assertEquals(EpgUiState(), viewModel.epgState.value)
    }

    @Test
    fun initializeEpisodeAutoplay_loadsNextEpisode() = runTest {
        viewModel.initialize(episodeChannel, "cat1")
        advanceUntilIdle()
        assertEquals("e2", viewModel.nextEpisodeUiState.value.nextEpisode?.id)
    }

    @Test
    fun onPlaybackProgress_startsCountdownInFinalWindow() = runTest {
        viewModel.prepareAutoplayForChannel(episodeChannel)
        advanceUntilIdle()

        viewModel.onPlaybackProgress(positionMs = 590_000L, durationMs = 600_000L)

        assertEquals(5, viewModel.nextEpisodeUiState.value.countdownSeconds)
    }

    @Test
    fun onPlaybackProgress_clearsCountdownWhenSeekingBack() = runTest {
        viewModel.prepareAutoplayForChannel(episodeChannel)
        advanceUntilIdle()
        viewModel.onPlaybackProgress(positionMs = 590_000L, durationMs = 600_000L)
        assertEquals(5, viewModel.nextEpisodeUiState.value.countdownSeconds)

        viewModel.onPlaybackProgress(positionMs = 100_000L, durationMs = 600_000L)

        assertNull(viewModel.nextEpisodeUiState.value.countdownSeconds)
    }

    @Test
    fun dismissAutoplay_hidesCountdown() = runTest {
        viewModel.prepareAutoplayForChannel(episodeChannel)
        advanceUntilIdle()
        viewModel.onPlaybackProgress(positionMs = 590_000L, durationMs = 600_000L)

        viewModel.dismissAutoplay()

        assertTrue(viewModel.nextEpisodeUiState.value.isAutoplayDismissed)
        assertNull(viewModel.nextEpisodeUiState.value.countdownSeconds)
        assertFalse(viewModel.isAutoplayOverlayVisible())
    }

    @Test
    fun playNextEpisodeNow_returnsNextEpisodeChannel() = runTest {
        viewModel.prepareAutoplayForChannel(episodeChannel)
        advanceUntilIdle()

        val nextChannel = viewModel.playNextEpisodeNow()

        assertEquals("e2", nextChannel?.id)
        assertEquals(ContentType.EPISODE, nextChannel?.contentType)
    }

    @Test
    fun onPlaybackProgress_disabledWhenPreferenceOff() = runTest {
        every { appPreferences.isAutoplayNextEpisodeEnabled() } returns false
        viewModel.prepareAutoplayForChannel(episodeChannel)
        advanceUntilIdle()

        viewModel.onPlaybackProgress(positionMs = 590_000L, durationMs = 600_000L)

        assertNull(viewModel.nextEpisodeUiState.value.countdownSeconds)
    }

    @Test
    fun initializeEpisodeAutoplay_lastEpisodeHasNoNext() = runTest {
        val lastEpisode = episodeChannel.copy(id = "e3", name = "Episode 3", externalId = "e3")
        viewModel.initialize(lastEpisode, "cat1")
        advanceUntilIdle()
        assertNull(viewModel.nextEpisodeUiState.value.nextEpisode)
    }

    private fun episode(id: String, season: Int, number: Int): Episode = Episode(
        id = id,
        title = "Episode $number",
        seasonNumber = season,
        episodeNumber = number,
        containerExtension = "mp4",
        sourceId = 1L,
        seriesId = "series-1",
        seriesName = "Test Series",
        imageUrl = null,
    )
}
