package com.afifistudio.iptvcinema.ui.details

import com.afifistudio.iptvcinema.data.cache.SeriesEpisodesCache
import com.afifistudio.iptvcinema.data.cache.SeriesEpisodesLoader
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.Episode
import com.afifistudio.iptvcinema.domain.repository.IptvRepository
import com.afifistudio.iptvcinema.domain.repository.IptvRepositoryFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesDetailsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repositoryFactory = mockk<IptvRepositoryFactory>()
    private val repository = mockk<IptvRepository>()
    private val seriesEpisodesCache = SeriesEpisodesCache()
    private lateinit var loader: SeriesEpisodesLoader

    private val series = Channel(
        id = "series-1",
        name = "Test Series",
        logoUrl = null,
        categoryId = "series-cat",
        categoryName = "Series",
        streamUrl = null,
        sourceId = 1L,
        externalId = "series-1",
        contentType = ContentType.SERIES,
    )
    private val sourceUpdatedAt = 1_700_000_000_000L
    private val episodes = listOf(
        Episode(
            id = "ep-1",
            title = "Episode 1",
            seasonNumber = 1,
            episodeNumber = 1,
            containerExtension = "mp4",
            sourceId = 1L,
            seriesId = "series-1",
            seriesName = "Test Series",
            imageUrl = null,
        ),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns dispatcher
        loader = SeriesEpisodesLoader(repositoryFactory, seriesEpisodesCache)
        coEvery { repositoryFactory.forSource(1L) } returns repository
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
        Dispatchers.resetMain()
    }

    @Test
    fun load_cacheHit_skipsNetwork() = runTest {
        seriesEpisodesCache.put(1L, "series-1", sourceUpdatedAt, episodes)

        val viewModel = SeriesDetailsViewModel(loader)
        viewModel.load(series, sourceUpdatedAt)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.episodes.size)
    }

    @Test
    fun load_cacheMiss_fetchesAndCachesEpisodes() = runTest {
        coEvery { repository.getSeriesEpisodes(1L, "series-1") } returns Result.success(episodes)

        val viewModel = SeriesDetailsViewModel(loader)
        viewModel.load(series, sourceUpdatedAt)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Episode 1", viewModel.uiState.value.episodes.single().title)
    }
}
