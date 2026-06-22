package com.afifistudio.iptvcinema.data.cache

import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.FavoriteDao
import com.afifistudio.iptvcinema.data.local.entity.ChannelEntity
import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.ContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class CategoryContentLoaderTest {

    private val channelDao = mockk<ChannelDao>()
    private val favoriteDao = mockk<FavoriteDao>()
    private val categoryChannelsCache = CategoryChannelsCache()
    private lateinit var loader: CategoryContentLoader

    private val category = Category(
        id = "movies",
        name = "Movies",
        sourceId = 1L,
        contentType = ContentType.MOVIE,
    )
    private val sourceUpdatedAt = 1_700_000_000_000L

    @Before
    fun setUp() {
        loader = CategoryContentLoader(channelDao, favoriteDao, categoryChannelsCache)
        coEvery {
            channelDao.countByCategory(1L, "movies", ContentType.MOVIE)
        } returns 2
        coEvery {
            channelDao.getByCategoryPage(
                sourceId = 1L,
                categoryId = "movies",
                contentType = ContentType.MOVIE,
                limit = CategoryContentLoader.PAGE_SIZE,
                offset = 0,
            )
        } returns listOf(
            channelEntity("movie-1", "Movie One"),
            channelEntity("movie-2", "Movie Two"),
        )
        coEvery {
            favoriteDao.getFavoriteChannelIds(1L, ContentType.MOVIE)
        } returns listOf("movie-1")
    }

    @Test
    fun loadFirstPage_fetchesCountPageAndFavoritesInParallel() = runTest {
        val result = loader.loadFirstPage(category)

        assertEquals(2, result.totalCount)
        assertEquals(2, result.channels.size)
        assertEquals(setOf("movie-1"), result.favoriteIds)
        coVerify { channelDao.countByCategory(1L, "movies", ContentType.MOVIE) }
        coVerify {
            channelDao.getByCategoryPage(
                sourceId = 1L,
                categoryId = "movies",
                contentType = ContentType.MOVIE,
                limit = CategoryContentLoader.PAGE_SIZE,
                offset = 0,
            )
        }
        coVerify { favoriteDao.getFavoriteChannelIds(1L, ContentType.MOVIE) }
    }

    @Test
    fun prefetchFirstPageIfAbsent_populatesCache() = runTest {
        loader.prefetchFirstPageIfAbsent(category, sourceUpdatedAt)

        val cached = categoryChannelsCache.get(category, sourceUpdatedAt)
        assertNotNull(cached)
        assertEquals(2, cached!!.channels.size)
        assertEquals(2, cached.totalCount)
        assertEquals("Movies", cached.categoryName)
    }

    @Test
    fun prefetchFirstPageIfAbsent_skipsWhenAlreadyCached() = runTest {
        categoryChannelsCache.put(
            category = category,
            sourceUpdatedAt = sourceUpdatedAt,
            data = CachedCategoryChannels(
                channels = emptyList(),
                favoriteIds = emptySet(),
                totalCount = 0,
                loadedCount = 0,
                hasMore = false,
                contentType = ContentType.MOVIE,
                categoryName = "Movies",
            ),
        )

        loader.prefetchFirstPageIfAbsent(category, sourceUpdatedAt)

        coVerify(exactly = 0) { channelDao.countByCategory(any(), any(), any()) }
    }

    private fun channelEntity(id: String, name: String) = ChannelEntity(
        sourceId = 1L,
        externalId = id,
        name = name,
        logoUrl = null,
        categoryId = "movies",
        streamUrl = "http://example.com/$id.m3u8",
        sortOrder = 0,
        contentType = ContentType.MOVIE,
    )
}
