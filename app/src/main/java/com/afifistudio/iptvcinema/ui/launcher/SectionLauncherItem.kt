package com.afifistudio.iptvcinema.ui.launcher

import com.afifistudio.iptvcinema.ui.browse.BrowseSection

data class SectionLauncherItem(
    val section: BrowseSection,
    val title: String,
    val countLabel: String,
    val lastUpdateLabel: String?,
    val refreshStatus: SectionRefreshStatus = SectionRefreshStatus.IDLE,
    val iconRes: Int,
    val previewImageUrl: String? = null,
    val previewFallbackRes: Int,
    val isBrowseEnabled: Boolean = true,
    val disabledMessage: String? = null,
)

enum class SectionRefreshStatus {
    IDLE,
    REFRESHING,
    SUCCESS,
    ERROR,
}
