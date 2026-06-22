package com.tviptv.app.data.cache

import com.tviptv.app.domain.model.Episode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeriesEpisodesCacheTest {

    private val cache = SeriesEpisodesCache()
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

    @Test
    fun get_returnsCachedEpisodesWhenSourceUpdatedAtMatches() {
        cache.put(1L, "series-1", 100L, episodes)

        assertEquals(episodes, cache.get(1L, "series-1", 100L))
    }

    @Test
    fun get_returnsNullWhenSourceUpdatedAtChanges() {
        cache.put(1L, "series-1", 100L, episodes)

        assertNull(cache.get(1L, "series-1", 200L))
    }

    @Test
    fun clearSource_removesEntriesForSource() {
        cache.put(1L, "series-1", 100L, episodes)
        cache.put(2L, "series-2", 100L, episodes)

        cache.clearSource(1L)

        assertNull(cache.get(1L, "series-1", 100L))
        assertEquals(episodes, cache.get(2L, "series-2", 100L))
    }
}
