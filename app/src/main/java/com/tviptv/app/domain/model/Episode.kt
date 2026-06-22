package com.tviptv.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Episode(
    val id: String,
    val title: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val containerExtension: String,
    val sourceId: Long,
    val seriesId: String,
    val seriesName: String,
    val imageUrl: String?,
) : Parcelable
