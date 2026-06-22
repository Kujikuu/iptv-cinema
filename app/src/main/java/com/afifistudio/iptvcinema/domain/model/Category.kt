package com.afifistudio.iptvcinema.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val id: String,
    val name: String,
    val sourceId: Long,
    val contentType: ContentType = ContentType.LIVE,
) : Parcelable
