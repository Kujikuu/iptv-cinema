package com.afifistudio.iptvcinema.data.m3u

import com.afifistudio.iptvcinema.data.local.dao.CategoryDao
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.SourceDao
import com.afifistudio.iptvcinema.data.local.entity.SourceEntity
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.SourceType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class M3uRepositoryTest {

    private val sourceDao = mockk<SourceDao>()
    private val categoryDao = mockk<CategoryDao>(relaxed = true)
    private val channelDao = mockk<ChannelDao>(relaxed = true)
    private val parser = M3uParser()
    private lateinit var repository: M3uRepository

    @Before
    fun setUp() {
        repository = M3uRepository(
            sourceDao = sourceDao,
            categoryDao = categoryDao,
            channelDao = channelDao,
            parser = parser,
            okHttpClient = OkHttpClient(),
        )
    }

    @Test
    fun validateAndParse_readsLocalM3uFile() = runBlocking {
        val fixture = File(requireNotNull(javaClass.classLoader?.getResource("sample.m3u")).toURI())
        val parsed = repository.validateAndParse(fixture.absolutePath, SourceType.M3U_FILE)

        assertEquals(3, parsed.entries.size)
        assertTrue(parsed.categories.containsKey("News"))
    }

    @Test
    fun resolveStreamUrl_returnsChannelStreamUrl() = runBlocking {
        val channel = Channel(
            id = "bbc1.uk",
            name = "BBC One",
            logoUrl = null,
            categoryId = "News",
            categoryName = "News",
            streamUrl = "http://example.com/stream/bbc1.m3u8",
            sourceId = 1L,
            externalId = "bbc1.uk",
        )

        val url = repository.resolveStreamUrl(channel).getOrThrow()

        assertEquals("http://example.com/stream/bbc1.m3u8", url)
    }

    @Test
    fun resolveStreamUrl_failsWhenStreamUrlMissing() = runBlocking {
        val channel = Channel(
            id = "missing",
            name = "Missing",
            logoUrl = null,
            categoryId = null,
            categoryName = null,
            streamUrl = null,
            sourceId = 1L,
            externalId = "missing",
        )

        val result = repository.resolveStreamUrl(channel)

        assertTrue(result.isFailure)
    }

    @Test
    fun refreshSource_cachesParsedPlaylistFromFile() = runBlocking {
        val fixture = File(requireNotNull(javaClass.classLoader?.getResource("sample.m3u")).toURI())
        coEvery { sourceDao.getById(1L) } returns SourceEntity(
            id = 1L,
            type = SourceType.M3U_FILE,
            name = "Sample",
            url = fixture.absolutePath,
            username = null,
        )
        coEvery { sourceDao.touch(1L, any()) } returns Unit

        repository.refreshSource(1L).getOrThrow()

        coVerify { categoryDao.deleteBySource(1L) }
        coVerify { channelDao.deleteBySource(1L) }
        coVerify { categoryDao.insertAll(match { it.isNotEmpty() }) }
        coVerify { channelDao.insertAll(match { it.size == 3 }) }
        coVerify { sourceDao.touch(1L, any()) }
    }

    @Test
    fun getChannels_filtersByCategoryWhenRequested() = runBlocking {
        coEvery { categoryDao.getBySource(1L) } returns listOf(
            com.afifistudio.iptvcinema.data.local.entity.CategoryEntity(
                sourceId = 1L,
                externalId = "News",
                name = "News",
                contentType = ContentType.LIVE,
            ),
        )
        coEvery { channelDao.getBySource(1L, "News") } returns listOf(
            com.afifistudio.iptvcinema.data.local.entity.ChannelEntity(
                sourceId = 1L,
                externalId = "bbc1.uk",
                name = "BBC One",
                logoUrl = null,
                categoryId = "News",
                streamUrl = "http://example.com/stream/bbc1.m3u8",
                sortOrder = 0,
                contentType = ContentType.LIVE,
            ),
        )

        val channels = repository.getChannels(1L, "News").getOrThrow()

        assertEquals(1, channels.size)
        assertEquals("BBC One", channels.first().name)
        assertEquals("News", channels.first().categoryName)
    }
}
