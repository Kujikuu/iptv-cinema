package com.tviptv.app.ui.categories

import com.tviptv.app.data.cache.CachedCategoryChannels
import com.tviptv.app.data.cache.CategoryChannelsCache
import com.tviptv.app.data.cache.CategoryContentLoader
import com.tviptv.app.data.cache.CategoryFirstPage
import com.tviptv.app.data.local.dao.ChannelDao
import com.tviptv.app.data.local.entity.ChannelEntity
import com.tviptv.app.domain.model.Category
import com.tviptv.app.domain.model.ContentType
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val channelDao = mockk<ChannelDao>()
    private val categoryChannelsCache = CategoryChannelsCache()
    private val contentLoader = mockk<CategoryContentLoader>()

    private val category = Category(
        id = "movies",
        name = "Movies",
        sourceId = 1L,
        contentType = ContentType.MOVIE,
    )
    private val sourceUpdatedAt = 1_700_000_000_000L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns dispatcher
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
        Dispatchers.resetMain()
    }

    @Test
    fun load_cacheHit_skipsDatabaseAndRestoresState() = runTest {
        categoryChannelsCache.put(
            category = category,
            sourceUpdatedAt = sourceUpdatedAt,
            data = CachedCategoryChannels(
                channels = listOf(
                    com.tviptv.app.domain.model.Channel(
                        id = "movie-1",
                        name = "Movie One",
                        logoUrl = null,
                        categoryId = "movies",
                        categoryName = "Movies",
                        streamUrl = "http://example.com/movie.m3u8",
                        sourceId = 1L,
                        externalId = "movie-1",
                        contentType = ContentType.MOVIE,
                    ),
                ),
                favoriteIds = emptySet(),
                totalCount = 1,
                loadedCount = 1,
                hasMore = false,
                contentType = ContentType.MOVIE,
                categoryName = "Movies",
            ),
        )

        val viewModel = createViewModel()
        viewModel.load(category, sourceUpdatedAt)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Movie One", state.channels.single().name)
    }

    @Test
    fun load_cacheMiss_usesParallelFirstPageLoader() = runTest {
        coEvery { contentLoader.loadFirstPage(category) } returns CategoryFirstPage(
            channels = listOf(
                com.tviptv.app.domain.model.Channel(
                    id = "movie-1",
                    name = "Movie One",
                    logoUrl = null,
                    categoryId = "movies",
                    categoryName = "Movies",
                    streamUrl = "http://example.com/movie.m3u8",
                    sourceId = 1L,
                    externalId = "movie-1",
                    contentType = ContentType.MOVIE,
                ),
            ),
            favoriteIds = setOf("movie-1"),
            totalCount = 1,
        )

        val viewModel = createViewModel()
        viewModel.load(category, sourceUpdatedAt)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.channels.size)
        assertTrue(state.favoriteIds.contains("movie-1"))
    }

    @Test
    fun loadNextPage_afterFirstPage_fetchesNextOffsetFromDao() = runTest {
        coEvery { contentLoader.loadFirstPage(category) } returns CategoryFirstPage(
            channels = (1..CategoryContentLoader.PAGE_SIZE).map { index ->
                com.tviptv.app.domain.model.Channel(
                    id = "movie-$index",
                    name = "Movie $index",
                    logoUrl = null,
                    categoryId = "movies",
                    categoryName = "Movies",
                    streamUrl = "http://example.com/$index.m3u8",
                    sourceId = 1L,
                    externalId = "movie-$index",
                    contentType = ContentType.MOVIE,
                )
            },
            favoriteIds = emptySet(),
            totalCount = CategoryContentLoader.PAGE_SIZE + 1,
        )
        coEvery {
            channelDao.getByCategoryPage(
                sourceId = 1L,
                categoryId = "movies",
                contentType = ContentType.MOVIE,
                limit = CategoryContentLoader.PAGE_SIZE,
                offset = CategoryContentLoader.PAGE_SIZE,
            )
        } returns listOf(
            ChannelEntity(
                sourceId = 1L,
                externalId = "movie-extra",
                name = "Movie Extra",
                logoUrl = null,
                categoryId = "movies",
                streamUrl = "http://example.com/extra.m3u8",
                sortOrder = 0,
                contentType = ContentType.MOVIE,
            ),
        )

        val viewModel = createViewModel()
        viewModel.load(category, sourceUpdatedAt)
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(CategoryContentLoader.PAGE_SIZE + 1, viewModel.uiState.value.channels.size)
        assertFalse(viewModel.uiState.value.hasMore)
    }

    private fun createViewModel(): CategoryDetailViewModel =
        CategoryDetailViewModel(
            channelDao = channelDao,
            categoryChannelsCache = categoryChannelsCache,
            contentLoader = contentLoader,
        )
}
