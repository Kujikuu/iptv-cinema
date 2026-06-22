package com.afifistudio.iptvcinema.ui.search

import com.afifistudio.iptvcinema.data.local.ChannelSearchHelper
import com.afifistudio.iptvcinema.data.local.entity.ChannelEntity
import com.afifistudio.iptvcinema.data.repository.SourceRepository
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.IptvSourceConfig
import com.afifistudio.iptvcinema.domain.model.SourceType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val channelSearchHelper = mockk<ChannelSearchHelper>()
    private val sourceRepository = mockk<SourceRepository>()
    private val source = IptvSourceConfig(
        id = 1L,
        type = SourceType.M3U_URL,
        name = "Test",
        url = "http://example.com/list.m3u",
        username = null,
        password = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { sourceRepository.getSources() } returns listOf(source)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun search_debouncesQueriesBeforeSearching() = runTest {
        coEvery {
            channelSearchHelper.search(listOf(1L), "bbc", any(), any())
        } returns listOf(
            ChannelEntity(
                sourceId = 1L,
                externalId = "bbc1",
                name = "BBC One",
                logoUrl = null,
                categoryId = "news",
                streamUrl = null,
                sortOrder = 0,
                contentType = ContentType.LIVE,
            ),
        )

        val viewModel = SearchViewModel(channelSearchHelper, sourceRepository)

        viewModel.search("bbc")
        advanceTimeBy(301)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.results.size)
        assertEquals("BBC One", viewModel.uiState.value.results.first().name)
    }

    @Test
    fun search_blankQueryClearsResults() = runTest {
        val viewModel = SearchViewModel(channelSearchHelper, sourceRepository)

        viewModel.search("")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertEquals("", viewModel.uiState.value.query)
    }

    @Test
    fun search_trimsQueryBeforeSearching() = runTest {
        coEvery {
            channelSearchHelper.search(listOf(1L), "bbc", any(), any())
        } returns emptyList()

        val viewModel = SearchViewModel(channelSearchHelper, sourceRepository)

        viewModel.search("  bbc  ")
        advanceTimeBy(301)
        advanceUntilIdle()

        coVerify { channelSearchHelper.search(listOf(1L), "bbc", any(), any()) }
    }

    @Test
    fun search_withoutSources_returnsEmptyResults() = runTest {
        coEvery { sourceRepository.getSources() } returns emptyList()

        val viewModel = SearchViewModel(channelSearchHelper, sourceRepository)

        viewModel.search("news")
        advanceTimeBy(301)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.results.isEmpty())
    }
}
