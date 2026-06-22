package com.afifistudio.iptvcinema.ui.browse

import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.Channel

data class BrowseItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val type: BrowseItemType,
    val sourceId: Long? = null,
    val channel: Channel? = null,
    val category: Category? = null,
    val badge: String? = null,
    val section: BrowseSection? = null,
    val selected: Boolean = false,
    val usePosterLayout: Boolean = false,
    val isFavorite: Boolean = false,
    val channelCount: Int? = null,
    val previewImageUrls: List<String> = emptyList(),
    val progressFraction: Float? = null,
    val progressLabel: String? = null,
    val seriesId: String? = null,
    @androidx.annotation.DimenRes val cardWidthRes: Int? = null,
    @androidx.annotation.DimenRes val cardHeightRes: Int? = null,
)

enum class BrowseItemType {
    SOURCE,
    CHANNEL,
    CATEGORY,
    ACTION,
    HERO,
    SECTION,
}
