package com.afifistudio.iptvcinema.ui.common

import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.domain.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentImageBindingsTest {

    @Test
    fun placeholderFor_live_returnsLiveDrawable() {
        assertEquals(R.drawable.placeholder_img_live, ContentImageBindings.placeholderFor(ContentType.LIVE))
    }

    @Test
    fun placeholderFor_movie_returnsMovieDrawable() {
        assertEquals(R.drawable.placeholder_img_movie, ContentImageBindings.placeholderFor(ContentType.MOVIE))
    }

    @Test
    fun placeholderFor_series_returnsSeriesDrawable() {
        assertEquals(R.drawable.placeholder_img_series, ContentImageBindings.placeholderFor(ContentType.SERIES))
    }

    @Test
    fun placeholderFor_episode_returnsEpisodeDrawable() {
        assertEquals(R.drawable.placeholder_img_episode, ContentImageBindings.placeholderFor(ContentType.EPISODE))
    }
}
