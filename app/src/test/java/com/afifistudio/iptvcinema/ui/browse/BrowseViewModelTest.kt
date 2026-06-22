package com.afifistudio.iptvcinema.ui.browse

import com.afifistudio.iptvcinema.data.cache.CategoryChannelsCache
import com.afifistudio.iptvcinema.data.cache.HomeFeedCache
import com.afifistudio.iptvcinema.data.cache.SectionFeedCache
import com.afifistudio.iptvcinema.data.cache.SeriesEpisodesCache
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.FavoriteDao
import com.afifistudio.iptvcinema.data.local.entity.CategoryCount
import com.afifistudio.iptvcinema.data.local.entity.ChannelEntity
import com.afifistudio.iptvcinema.data.platform.WatchNextPublisher
import com.afifistudio.iptvcinema.data.prefs.AppPreferences
import com.afifistudio.iptvcinema.data.repository.SourceRefreshPolicy
import com.afifistudio.iptvcinema.data.repository.SourceRepository
import com.afifistudio.iptvcinema.data.repository.WatchHistoryRepository
import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.ContinueWatchingItem
import com.afifistudio.iptvcinema.domain.model.IptvSourceConfig
import com.afifistudio.iptvcinema.domain.model.SourceType
import com.afifistudio.iptvcinema.domain.repository.IptvRepository
import com.afifistudio.iptvcinema.domain.repository.IptvRepositoryFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
class BrowseViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sourceRepository = mockk<SourceRepository>()
    private val repositoryFactory = mockk<IptvRepositoryFactory>()
    private val sourceRefreshPolicy = mockk<SourceRefreshPolicy>()
    private val sectionFeedCache = SectionFeedCache()
    private val categoryChannelsCache = CategoryChannelsCache()
    private val homeFeedCache = HomeFeedCache()
    private val seriesEpisodesCache = SeriesEpisodesCache()
    private val channelDao = mockk<ChannelDao>()
    private val favoriteDao = mockk<FavoriteDao>(relaxed = true)
    private val watchHistoryRepository = mockk<WatchHistoryRepository>(relaxed = true)
    private val appPreferences = mockk<AppPreferences>(relaxed = true)
    private val watchNextPublisher = mockk<WatchNextPublisher>(relaxed = true)
    private val repository = mockk<IptvRepository>()

    private val source = IptvSourceConfig(
        id = 1L,
        type = SourceType.M3U_URL,
        name = "Test Source",
        url = "http://example.com/list.m3u",
        username = null,
        password = null,
        updatedAt = 1_700_000_000_000L,
    )

    private val newsCategory = Category(id = "news", name = "News", sourceId = 1L, contentType = ContentType.LIVE)
    private val sportsCategory = Category(id = "sports", name = "Sports", sourceId = 1L, contentType = ContentType.LIVE)

    private val recentChannel = channelEntity("recent-1", "Recent One", "news")
    private val newsChannel = channelEntity("news-1", "News One", "news")
    private val sportsChannel = channelEntity("sports-1", "Sports One", "sports")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns dispatcher
        coEvery { sourceRepository.getSources() } returns listOf(source)
        coEvery { sourceRepository.getSource(1L) } returns source
        every { appPreferences.getSelectedSourceId() } returns null
        coEvery { watchNextPublisher.sync() } returns Unit
        coEvery { sourceRefreshPolicy.shouldRefreshFromNetwork(any(), any()) } returns false
        coEvery { repositoryFactory.forSource(1L) } returns repository
        coEvery { repository.getCategories(1L, any()) } returns Result.success(listOf(newsCategory, sportsCategory))
        coEvery { channelDao.getByCategory(1L, "news", ContentType.LIVE, any()) } returns listOf(newsChannel)
        coEvery { channelDao.getPreviewLogosForCategory(1L, "news", ContentType.LIVE, any()) } returns listOf(newsChannel)
        coEvery { channelDao.getPreviewLogosForCategory(1L, "sports", ContentType.LIVE, any()) } returns listOf(sportsChannel)
        coEvery { channelDao.getPreviewLogosForCategory(1L, any(), ContentType.MOVIE, any()) } returns emptyList()
        coEvery { channelDao.getPreviewLogosForCategory(1L, any(), ContentType.SERIES, any()) } returns emptyList()
        coEvery { channelDao.getCategoryCounts(1L, ContentType.LIVE) } returns listOf(
            CategoryCount(categoryId = "news", count = 1),
            CategoryCount(categoryId = "sports", count = 1),
        )
        coEvery { channelDao.getCategoryCounts(1L, ContentType.MOVIE) } returns emptyList()
        coEvery { channelDao.getCategoryCounts(1L, ContentType.SERIES) } returns emptyList()
        coEvery { repository.getCategories(1L, ContentType.MOVIE) } returns Result.success(emptyList())
        coEvery { repository.getCategories(1L, ContentType.SERIES) } returns Result.success(emptyList())
        coEvery { channelDao.countAllBySource(1L) } returns 3
        coEvery { channelDao.countBySource(1L, ContentType.LIVE) } returns 3
        coEvery { channelDao.countBySource(1L, ContentType.MOVIE) } returns 0
        coEvery { channelDao.countBySource(1L, ContentType.SERIES) } returns 0
        coEvery { repository.refreshSource(1L) } returns Result.success(Unit)
        coEvery { repository.refreshSection(1L, any()) } returns Result.success(Unit)
        coEvery { favoriteDao.getFavoriteChannels(1L, any()) } returns emptyList()
        coEvery { favoriteDao.isFavorite(any(), any(), any()) } returns false
        coEvery {
            watchHistoryRepository.getRecentContinueWatching(
                sourceId = 1L,
                contentType = null,
                limit = any(),
                categoryNames = any(),
                excludeNearlyComplete = any(),
            )
        } returns listOf(continueWatchingItem(recentChannel))
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
        Dispatchers.resetMain()
    }

    @Test
    fun loadInitial_buildsCategoryRowsAndFeaturedFromContinueWatching() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isBootstrapLoading)
        assertFalse(state.isHomeRowsLoading)
        assertEquals("Recent One", state.featuredChannel?.name)
        assertEquals(1, state.continueWatching.size)
        assertTrue(state.categoryRows.isEmpty())
        assertTrue(state.homeCategoryHighlights.isNotEmpty())
    }

    @Test
    fun loadInitial_publishesHomeShellBeforeHomeRowsFinish() = runTest {
        val continueWatchingGate = CompletableDeferred<Unit>()
        coEvery {
            watchHistoryRepository.getRecentContinueWatching(
                sourceId = 1L,
                contentType = null,
                limit = any(),
                categoryNames = any(),
                excludeNearlyComplete = any(),
            )
        } coAnswers {
            continueWatchingGate.await()
            listOf(continueWatchingItem(recentChannel))
        }

        val viewModel = createViewModel()
        runCurrent()

        val shellState = viewModel.uiState.value
        assertFalse(shellState.isBootstrapLoading)
        assertTrue(shellState.isHomeRowsLoading)
        assertFalse(shellState.isLoading)
        assertEquals(3, shellState.liveCount)
        assertEquals("Test Source", shellState.activeSourceName)
        assertTrue(shellState.continueWatching.isEmpty())

        continueWatchingGate.complete(Unit)
        advanceUntilIdle()

        val hydratedState = viewModel.uiState.value
        assertFalse(hydratedState.isHomeRowsLoading)
        assertEquals(1, hydratedState.continueWatching.size)
    }

    @Test
    fun loadInitial_withNoSources_leavesBrowseEmpty() = runTest {
        coEvery { sourceRepository.getSources() } returns emptyList()

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isBootstrapLoading)
        assertTrue(state.sources.isEmpty())
        assertNull(state.featuredChannel)
    }

    @Test
    fun featuredChannel_fallsBackToFavoriteWhenNoContinueWatching() = runTest {
        coEvery {
            watchHistoryRepository.getRecentContinueWatching(
                sourceId = 1L,
                contentType = null,
                limit = any(),
                categoryNames = any(),
                excludeNearlyComplete = any(),
            )
        } returns emptyList()
        coEvery { favoriteDao.getFavoriteChannels(1L, any()) } returns listOf(newsChannel)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("News One", viewModel.uiState.value.featuredChannel?.name)
    }

    @Test
    fun featuredChannel_fallsBackToHomeCategoryHighlight() = runTest {
        coEvery {
            watchHistoryRepository.getRecentContinueWatching(
                sourceId = 1L,
                contentType = null,
                limit = any(),
                categoryNames = any(),
                excludeNearlyComplete = any(),
            )
        } returns emptyList()
        coEvery { favoriteDao.getFavoriteChannels(1L, any()) } returns emptyList()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("News One", viewModel.uiState.value.featuredChannel?.name)
    }

    @Test
    fun toggleFavorite_insertsAndDeletesFavoriteWithoutFullReload() = runTest {
        coEvery { favoriteDao.isFavorite(1L, "news-1", ContentType.LIVE) } returnsMany listOf(false, true)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val channel = channel(
            id = "news-1",
            name = "News One",
            categoryId = "news",
            categoryName = "News",
        )

        viewModel.toggleFavorite(channel)
        advanceUntilIdle()
        coVerify {
            favoriteDao.insert(match { it.sourceId == 1L && it.channelId == "news-1" })
        }
        assertEquals(1, viewModel.uiState.value.favorites.size)

        viewModel.toggleFavorite(channel)
        advanceUntilIdle()
        coVerify { favoriteDao.delete(1L, "news-1", ContentType.LIVE) }
        assertTrue(viewModel.uiState.value.favorites.isEmpty())
    }

    @Test
    fun rememberLastWatched_upsertsChannel() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val channel = channel(
            id = "news-1",
            name = "News One",
            categoryId = "news",
            categoryName = "News",
        )

        viewModel.rememberLastWatched(channel)
        advanceUntilIdle()

        coVerify { watchHistoryRepository.rememberLastWatched(channel) }
    }

    @Test
    fun continueWatchingForSection_filtersByContentType() = runTest {
        val movieEntity = channelEntity("movie-1", "Movie One", "movies", ContentType.MOVIE)
        val episodeEntity = channelEntity("ep-1", "Episode One", "series", ContentType.EPISODE)
        coEvery {
            watchHistoryRepository.getRecentContinueWatching(
                sourceId = 1L,
                contentType = null,
                limit = any(),
                categoryNames = any(),
                excludeNearlyComplete = any(),
            )
        } returns listOf(
            continueWatchingItem(recentChannel),
            continueWatchingItem(movieEntity),
            continueWatchingItem(episodeEntity),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.continueWatchingForSection(BrowseSection.LIVE).size)
        assertEquals(1, viewModel.continueWatchingForSection(BrowseSection.MOVIES).size)
        assertEquals(1, viewModel.continueWatchingForSection(BrowseSection.SERIES).size)
    }

    @Test
    fun refreshContinueWatching_updatesStateWithoutFullReload() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val updatedMovie = continueWatchingItem(
            channelEntity("movie-2", "Updated Movie", "movies", ContentType.MOVIE),
            playbackPositionMs = 90_000L,
        )
        coEvery {
            watchHistoryRepository.getRecentContinueWatching(
                sourceId = 1L,
                contentType = null,
                limit = 15,
                categoryNames = any(),
                excludeNearlyComplete = any(),
            )
        } returns listOf(updatedMovie)

        viewModel.refreshContinueWatching()
        advanceUntilIdle()

        assertEquals("Updated Movie", viewModel.uiState.value.continueWatching.first().channel.name)
        assertEquals(90_000L, viewModel.uiState.value.continueWatching.first().playbackPositionMs)
    }

    @Test
    fun refreshCurrentSource_reloadsFeed() = runTest {
        coEvery { repository.refreshSource(1L) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.refreshCurrentSource()
        advanceUntilIdle()

        coVerify { repository.refreshSource(1L) }
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isBootstrapLoading)
    }

    @Test
    fun refreshSection_routesThroughRepositoryAndReloadsHome() = runTest {
        coEvery { repository.refreshSection(1L, ContentType.MOVIE) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        var callbackSuccess = false
        viewModel.refreshSection(BrowseSection.MOVIES) { result ->
            callbackSuccess = result.isSuccess
        }
        advanceUntilIdle()

        coVerify { repository.refreshSection(1L, ContentType.MOVIE) }
        assertTrue(callbackSuccess)
        assertEquals(BrowseSection.HOME, viewModel.uiState.value.selectedSection)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isBootstrapLoading)
    }

    @Test
    fun selectSection_buildsCategorySummaries() = runTest {
        coEvery { channelDao.getCategoryCounts(1L, ContentType.LIVE) } returns listOf(
            CategoryCount(categoryId = "news", count = 2, latestAddedAt = 2_000L),
            CategoryCount(categoryId = "sports", count = 1, latestAddedAt = 1_000L),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectSection(BrowseSection.LIVE)
        advanceUntilIdle()

        val summaries = viewModel.uiState.value.categorySummaries
        assertEquals(2, summaries.size)
        assertEquals("news", summaries[0].category.id)
        assertEquals("News", summaries.first { it.category.id == "news" }.category.name)
        assertEquals(2, summaries.first { it.category.id == "news" }.channelCount)
        coVerify { channelDao.getPreviewLogosForCategory(1L, "news", ContentType.LIVE, any()) }
    }

    @Test
    fun selectSection_sortsCategoriesByLatestAddedAt() = runTest {
        coEvery { channelDao.getCategoryCounts(1L, ContentType.LIVE) } returns listOf(
            CategoryCount(categoryId = "news", count = 50, latestAddedAt = 100L),
            CategoryCount(categoryId = "sports", count = 200, latestAddedAt = 500L),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectSection(BrowseSection.LIVE)
        advanceUntilIdle()

        val summaries = viewModel.uiState.value.categorySummaries
        assertEquals("sports", summaries[0].category.id)
        assertEquals("news", summaries[1].category.id)
    }

    @Test
    fun markHomeSection_updatesSelectedSectionWithoutReload() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectSection(BrowseSection.LIVE)
        advanceUntilIdle()
        assertEquals(BrowseSection.LIVE, viewModel.uiState.value.selectedSection)

        viewModel.markHomeSection()
        assertEquals(BrowseSection.HOME, viewModel.uiState.value.selectedSection)
    }

    private fun createViewModel(): BrowseViewModel =
        BrowseViewModel(
            sourceRepository,
            repositoryFactory,
            sourceRefreshPolicy,
            sectionFeedCache,
            categoryChannelsCache,
            homeFeedCache,
            seriesEpisodesCache,
            channelDao,
            favoriteDao,
            watchHistoryRepository,
            appPreferences,
            watchNextPublisher,
        )

    private fun continueWatchingItem(
        entity: ChannelEntity,
        playbackPositionMs: Long = 0L,
        durationMs: Long = 0L,
    ): ContinueWatchingItem = ContinueWatchingItem(
        channel = Channel(
            id = entity.externalId,
            name = entity.name,
            logoUrl = entity.logoUrl,
            categoryId = entity.categoryId,
            categoryName = entity.categoryId,
            streamUrl = entity.streamUrl,
            sourceId = entity.sourceId,
            externalId = entity.externalId,
            contentType = entity.contentType,
        ),
        watchedAt = 1L,
        playbackPositionMs = playbackPositionMs,
        durationMs = durationMs,
    )

    private fun channel(
        id: String,
        name: String,
        categoryId: String,
        categoryName: String,
    ) = Channel(
        id = id,
        name = name,
        logoUrl = null,
        categoryId = categoryId,
        categoryName = categoryName,
        streamUrl = "http://example.com/$id.m3u8",
        sourceId = 1L,
        externalId = id,
        contentType = ContentType.LIVE,
    )

    private fun channelEntity(
        id: String,
        name: String,
        categoryId: String,
        contentType: ContentType = ContentType.LIVE,
    ) = ChannelEntity(
        sourceId = 1L,
        externalId = id,
        name = name,
        logoUrl = null,
        categoryId = categoryId,
        streamUrl = "http://example.com/$id.m3u8",
        sortOrder = 0,
        contentType = contentType,
    )
}
