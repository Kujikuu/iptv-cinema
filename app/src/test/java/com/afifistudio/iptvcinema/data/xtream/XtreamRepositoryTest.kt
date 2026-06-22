package com.afifistudio.iptvcinema.data.xtream

import com.afifistudio.iptvcinema.data.local.dao.CategoryDao
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.SourceDao
import com.afifistudio.iptvcinema.data.local.entity.SourceEntity
import com.afifistudio.iptvcinema.data.prefs.CredentialStore
import com.afifistudio.iptvcinema.data.player.PlayerEpgRepository
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.SourceType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class XtreamRepositoryTest {

    private val sourceDao = mockk<SourceDao>()
    private val categoryDao = mockk<CategoryDao>(relaxed = true)
    private val channelDao = mockk<ChannelDao>(relaxed = true)
    private val credentialStore = mockk<CredentialStore>()
    private val xtreamApi = mockk<XtreamApi>()
    private val epgRepository = mockk<PlayerEpgRepository>(relaxed = true)
    private lateinit var repository: XtreamRepository

    @Before
    fun setUp() {
        repository = XtreamRepository(
            sourceDao = sourceDao,
            categoryDao = categoryDao,
            channelDao = channelDao,
            credentialStore = credentialStore,
            xtreamApi = xtreamApi,
            epgRepository = epgRepository,
        )
    }

    @Test
    fun validateCredentials_acceptsActiveUser() = runBlocking {
        coEvery {
            xtreamApi.authenticate(
                url = "http://servx.pro:80/player_api.php",
                username = "user",
                password = "pass",
            )
        } returns XtreamAuthResponse(
            userInfo = XtreamUserInfo(auth = 1, status = "Active"),
        )

        repository.validateCredentials("http://servx.pro:80", "user", "pass")
    }

    @Test(expected = IllegalStateException::class)
    fun validateCredentials_rejectsUnauthorizedUser() = runBlocking {
        coEvery {
            xtreamApi.authenticate(any(), any(), any())
        } returns XtreamAuthResponse(
            userInfo = XtreamUserInfo(auth = 0, status = "Expired"),
        )

        repository.validateCredentials("http://servx.pro:80", "user", "pass")
    }

    @Test
    fun resolveStreamUrl_buildsXtreamLiveUrl() = runBlocking {
        coEvery { sourceDao.getById(1L) } returns SourceEntity(
            id = 1L,
            type = SourceType.XTREAM,
            name = "Test",
            url = "http://servx.pro:80",
            username = "ahmed-afifi",
        )
        coEvery { credentialStore.getPassword(1L) } returns "01091072705"

        val channel = Channel(
            id = "490579",
            name = "Match",
            logoUrl = null,
            categoryId = "681",
            categoryName = "Sports",
            streamUrl = null,
            sourceId = 1L,
            externalId = "490579",
        )

        val result = repository.resolveStreamUrl(channel).getOrThrow()

        assertEquals(
            "http://servx.pro:80/live/ahmed-afifi/01091072705/490579.m3u8",
            result,
        )
    }

    @Test
    fun refreshSource_cachesCategoriesAndChannels() = runBlocking {
        coEvery { sourceDao.getById(1L) } returns SourceEntity(
            id = 1L,
            type = SourceType.XTREAM,
            name = "Test",
            url = "http://servx.pro:80",
            username = "user",
        )
        coEvery { credentialStore.getPassword(1L) } returns "pass"
        coEvery {
            xtreamApi.getLiveCategories(
                url = "http://servx.pro:80/player_api.php",
                username = "user",
                password = "pass",
            )
        } returns listOf(XtreamCategoryDto(categoryId = "681", categoryName = "Sports"))
        coEvery {
            xtreamApi.getLiveStreams(
                url = "http://servx.pro:80/player_api.php",
                username = "user",
                password = "pass",
            )
        } returns listOf(
            XtreamStreamDto(
                streamId = 490579,
                name = "Match",
                streamIcon = "",
                categoryId = "681",
            ),
        )
        coEvery {
            xtreamApi.getVodCategories(any(), any(), any())
        } returns emptyList()
        coEvery {
            xtreamApi.getVodStreams(any(), any(), any())
        } returns emptyList()
        coEvery {
            xtreamApi.getSeriesCategories(any(), any(), any())
        } returns emptyList()
        coEvery {
            xtreamApi.getSeries(any(), any(), any())
        } returns emptyList()
        coEvery { sourceDao.touch(1L, any()) } returns Unit

        repository.refreshSource(1L).getOrThrow()

        coVerify { categoryDao.deleteBySource(1L) }
        coVerify { channelDao.deleteBySource(1L) }
        coVerify { categoryDao.insertAll(match { it.size == 1 && it.first().externalId == "681" }) }
        coVerify { channelDao.insertAll(match { it.size == 1 && it.first().externalId == "490579" }) }
        coVerify { sourceDao.touch(1L, any()) }
    }

    @Test
    fun getCategories_returnsMappedCategories() = runBlocking {
        coEvery { categoryDao.getBySource(1L) } returns listOf(
            com.afifistudio.iptvcinema.data.local.entity.CategoryEntity(
                sourceId = 1L,
                externalId = "681",
                name = "Sports",
                contentType = ContentType.LIVE,
            ),
        )

        val categories = repository.getCategories(1L).getOrThrow()

        assertEquals(1, categories.size)
        assertEquals("Sports", categories.first().name)
        assertTrue(categories.first().id == "681")
    }
}
