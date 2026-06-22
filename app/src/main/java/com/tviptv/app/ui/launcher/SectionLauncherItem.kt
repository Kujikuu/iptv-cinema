package com.tviptv.app.ui.launcher

import com.tviptv.app.ui.browse.BrowseSection

data class SectionLauncherItem(
    val section: BrowseSection,
    val title: String,
    val countLabel: String,
    val lastUpdateLabel: String?,
    val iconRes: Int,
    val previewImageUrl: String? = null,
    val previewFallbackRes: Int,
)
