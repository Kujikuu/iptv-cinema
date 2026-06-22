package com.tviptv.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EpisodeNavigatorTest {

    @Test
    fun findNextEpisode_returnsNextInSameSeason() {
        val episodes = listOf(
            episode("e1", season = 1, number = 1),
            episode("e2", season = 1, number = 2),
            episode("e3", season = 1, number = 3),
        )

        assertEquals("e2", findNextEpisode(episodes, "e1")?.id)
        assertEquals("e3", findNextEpisode(episodes, "e2")?.id)
    }

    @Test
    fun findNextEpisode_crossesSeasonBoundary() {
        val episodes = listOf(
            episode("s1e10", season = 1, number = 10),
            episode("s2e1", season = 2, number = 1),
        )

        assertEquals("s2e1", findNextEpisode(episodes, "s1e10")?.id)
    }

    @Test
    fun findNextEpisode_returnsNullForLastEpisode() {
        val episodes = listOf(
            episode("e1", season = 1, number = 1),
            episode("e2", season = 1, number = 2),
        )

        assertNull(findNextEpisode(episodes, "e2"))
    }

    @Test
    fun findNextEpisode_returnsNullForSingleEpisode() {
        val episodes = listOf(episode("e1", season = 1, number = 1))

        assertNull(findNextEpisode(episodes, "e1"))
    }

    @Test
    fun findNextEpisode_returnsNullForUnknownEpisode() {
        val episodes = listOf(episode("e1", season = 1, number = 1))

        assertNull(findNextEpisode(episodes, "missing"))
    }

    @Test
    fun findNextEpisode_sortsAscendingRegardlessOfInputOrder() {
        val episodes = listOf(
            episode("e3", season = 1, number = 3),
            episode("e1", season = 1, number = 1),
            episode("e2", season = 1, number = 2),
        )

        assertEquals("e2", findNextEpisode(episodes, "e1")?.id)
    }

    @Test
    fun firstEpisode_returnsEarliestSeasonAndEpisode() {
        val episodes = listOf(
            episode("s2e1", season = 2, number = 1),
            episode("s1e2", season = 1, number = 2),
            episode("s1e1", season = 1, number = 1),
        )

        assertEquals("s1e1", firstEpisode(episodes)?.id)
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
