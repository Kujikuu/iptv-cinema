package com.afifistudio.iptvcinema.ui.browse

import com.afifistudio.iptvcinema.domain.model.Category

data class CategorySummary(
    val category: Category,
    val channelCount: Int,
    val previewImageUrls: List<String> = emptyList(),
    val latestAddedAt: Long = 0L,
) {
    val previewImageUrl: String? get() = previewImageUrls.firstOrNull()
}

fun CategorySummary.toBrowseItem(): BrowseItem =
    BrowseItem(
        id = "category_${category.id}",
        title = category.name,
        imageUrl = previewImageUrl,
        previewImageUrls = previewImageUrls,
        type = BrowseItemType.CATEGORY,
        category = category,
        channelCount = channelCount,
    )
