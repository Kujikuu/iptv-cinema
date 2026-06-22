package com.afifistudio.iptvcinema.ui.launcher

import com.afifistudio.iptvcinema.ui.browse.BrowseSection

data class SectionLauncherItem(
    val section: BrowseSection,
    val title: String,
    val countLabel: String,
    val lastUpdateLabel: String?,
    val iconRes: Int,
    val previewImageUrl: String? = null,
    val previewFallbackRes: Int,
)
