package com.afifistudio.iptvcinema.ui.common

import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.load
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.ui.browse.BrowseItem
import com.afifistudio.iptvcinema.ui.browse.BrowseSection

object ContentImageBindings {

    data class ImageRequestSize(
        val widthPx: Int,
        val heightPx: Int,
    )

    /**
     * Drop-in image placeholders (PNG/WebP in `res/drawable-nodpi/`):
     * - placeholder_img_live
     * - placeholder_img_movie
     * - placeholder_img_series
     * - placeholder_img_episode
     *
     * Add the image file, then remove the matching `.xml` fallback in `res/drawable/`.
     */
    @DrawableRes
    fun placeholderFor(contentType: ContentType): Int = when (contentType) {
        ContentType.LIVE -> R.drawable.placeholder_img_live
        ContentType.MOVIE -> R.drawable.placeholder_img_movie
        ContentType.SERIES -> R.drawable.placeholder_img_series
        ContentType.EPISODE -> R.drawable.placeholder_img_episode
    }

    fun ImageView.bindContentImage(
        url: String?,
        contentType: ContentType,
        crossfade: Boolean = true,
        requestSize: ImageRequestSize? = null,
    ) {
        val placeholder = placeholderFor(contentType)
        if (url.isNullOrBlank()) {
            setImageResource(placeholder)
        } else {
            load(url) {
                crossfade(crossfade)
                requestSize?.let { size(it.widthPx, it.heightPx) }
                placeholder(placeholder)
                error(placeholder)
            }
        }
    }

    fun BrowseItem.contentTypeForImage(): ContentType =
        channel?.contentType ?: category?.contentType ?: ContentType.LIVE

    fun BrowseSection.toContentTypeForImage(): ContentType = when (this) {
        BrowseSection.LIVE -> ContentType.LIVE
        BrowseSection.MOVIES -> ContentType.MOVIE
        BrowseSection.SERIES -> ContentType.SERIES
        BrowseSection.HOME -> ContentType.LIVE
    }
}
