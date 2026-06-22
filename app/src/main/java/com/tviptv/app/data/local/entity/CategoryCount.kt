package com.tviptv.app.data.local.entity

import androidx.room.ColumnInfo

data class CategoryCount(
    @ColumnInfo(name = "categoryId") val categoryId: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "latestAddedAt") val latestAddedAt: Long? = null,
)
