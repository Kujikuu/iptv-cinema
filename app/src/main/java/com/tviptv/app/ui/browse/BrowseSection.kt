package com.tviptv.app.ui.browse

import com.tviptv.app.domain.model.ContentType

enum class BrowseSection(val contentType: ContentType?) {
    HOME(null),
    LIVE(ContentType.LIVE),
    MOVIES(ContentType.MOVIE),
    SERIES(ContentType.SERIES),
}
