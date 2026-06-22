package com.afifistudio.iptvcinema.ui.common

import androidx.annotation.DrawableRes
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.ui.browse.BrowseSection

object SectionIcons {

    @DrawableRes
    fun forSection(section: BrowseSection): Int = when (section) {
        BrowseSection.LIVE -> R.drawable.ic_material_live_tv
        BrowseSection.MOVIES -> R.drawable.ic_material_movie
        BrowseSection.SERIES -> R.drawable.ic_material_series
        BrowseSection.HOME -> R.drawable.ic_material_live_tv
    }
}
