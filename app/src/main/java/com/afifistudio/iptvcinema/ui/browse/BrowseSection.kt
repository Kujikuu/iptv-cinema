package com.afifistudio.iptvcinema.ui.browse

import com.afifistudio.iptvcinema.domain.model.ContentType

enum class BrowseSection(val contentType: ContentType?) {
    HOME(null),
    LIVE(ContentType.LIVE),
    MOVIES(ContentType.MOVIE),
    SERIES(ContentType.SERIES),
}
