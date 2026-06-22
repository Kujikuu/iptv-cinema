package com.afifistudio.iptvcinema.data.player

import com.afifistudio.iptvcinema.data.local.dao.EpgDao
import com.afifistudio.iptvcinema.data.local.dao.SourceDao
import com.afifistudio.iptvcinema.data.local.entity.EpgProgramEntity
import com.afifistudio.iptvcinema.data.prefs.CredentialStore
import com.afifistudio.iptvcinema.data.xtream.XtreamApi
import com.afifistudio.iptvcinema.data.xtream.XtreamEpgEntryDto
import com.afifistudio.iptvcinema.data.xtream.XtreamShortEpgResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlayerEpgRepositoryTest {

    private val epgDao = mockk<EpgDao>(relaxed = true)
    private val sourceDao = mockk<SourceDao>(relaxed = true)
    private val credentialStore = mockk<CredentialStore>(relaxed = true)
    private val xtreamApi = mockk<XtreamApi>(relaxed = true)
    private lateinit var repository: PlayerEpgRepository

    @Before
    fun setUp() {
        repository = PlayerEpgRepository(epgDao, sourceDao, credentialStore, xtreamApi)
    }

    @Test
    fun getNowNext_returnsCurrentAndNextPrograms() = runTest {
        val nowMs = 1_700_000_000_000L
        val current = EpgProgramEntity(
            sourceId = 1L,
            streamId = "100",
            title = "News Hour",
            description = "Latest news",
            startMs = nowMs - 1_800_000L,
            endMs = nowMs + 1_800_000L,
        )
        val next = EpgProgramEntity(
            sourceId = 1L,
            streamId = "100",
            title = "Sports",
            description = null,
            startMs = nowMs + 1_800_000L,
            endMs = nowMs + 5_400_000L,
        )
        coEvery { epgDao.getCurrentProgram(1L, "100", any()) } returns current
        coEvery { epgDao.getNextProgram(1L, "100", any()) } returns next

        val result = repository.getNowNext(1L, "100")

        assertEquals("News Hour", result.now?.title)
        assertEquals("Sports", result.next?.title)
    }

    @Test
    fun isStale_returnsTrueWhenNeverFetched() = runTest {
        coEvery { epgDao.getLastFetchedAt(1L, "100") } returns null
        assertTrue(repository.isStale(1L, "100"))
    }

    @Test
    fun fetchAndCache_skipsWhenSourceNotFound() = runTest {
        coEvery { sourceDao.getById(1L) } returns null
        repository.fetchAndCache(1L, "100")
        coVerify(exactly = 0) { xtreamApi.getShortEpg(any(), any(), any(), streamId = any()) }
    }
}
