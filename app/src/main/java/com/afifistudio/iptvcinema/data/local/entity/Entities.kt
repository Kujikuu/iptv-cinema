package com.afifistudio.iptvcinema.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.SectionImportStatus
import com.afifistudio.iptvcinema.domain.model.SourceType

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: SourceType,
    val name: String,
    val url: String?,
    val username: String?,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "categories",
    primaryKeys = ["sourceId", "externalId", "contentType"],
)
data class CategoryEntity(
    val sourceId: Long,
    val externalId: String,
    val name: String,
    val contentType: ContentType,
)

@Entity(
    tableName = "channels",
    primaryKeys = ["sourceId", "externalId", "contentType"],
    indices = [
        Index(value = ["sourceId", "contentType", "categoryId"]),
        Index(
            value = ["sourceId", "contentType", "categoryId", "addedAt", "sortOrder", "name"],
        ),
    ],
)
data class ChannelEntity(
    val sourceId: Long,
    val externalId: String,
    val name: String,
    val logoUrl: String?,
    val categoryId: String?,
    val streamUrl: String?,
    val sortOrder: Int,
    val contentType: ContentType,
    val containerExtension: String? = null,
    val plot: String? = null,
    val addedAt: Long? = null,
    val channelNumber: Int? = null,
)

@Entity(
    tableName = "section_import_states",
    primaryKeys = ["sourceId", "contentType"],
)
data class SectionImportStateEntity(
    val sourceId: Long,
    val contentType: ContentType,
    val status: SectionImportStatus,
    val updatedAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val errorMessage: String? = null,
)

@Entity(
    tableName = "favorites",
    primaryKeys = ["sourceId", "channelId", "contentType"],
)
data class FavoriteEntity(
    val sourceId: Long,
    val channelId: String,
    val contentType: ContentType,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "last_watched",
    primaryKeys = ["sourceId", "channelId", "contentType"],
)
data class LastWatchedEntity(
    val sourceId: Long,
    val channelId: String,
    val contentType: ContentType,
    val watchedAt: Long = System.currentTimeMillis(),
    val playbackPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val title: String? = null,
    val imageUrl: String? = null,
    val categoryId: String? = null,
    val seriesId: String? = null,
    val seriesName: String? = null,
)

data class LastWatchedRow(
    val sourceId: Long,
    val channelId: String,
    val contentType: ContentType,
    val watchedAt: Long,
    val playbackPositionMs: Long,
    val durationMs: Long,
    val title: String?,
    val imageUrl: String?,
    val categoryId: String?,
    val seriesId: String?,
    val seriesName: String?,
    val channelName: String?,
    val channelLogoUrl: String?,
    val channelCategoryId: String?,
    val channelStreamUrl: String?,
    val channelSortOrder: Int?,
    val channelContainerExtension: String?,
    val channelPlot: String?,
    val channelAddedAt: Long?,
)

@Entity(
    tableName = "epg_programs",
    primaryKeys = ["sourceId", "streamId", "startMs"],
)
data class EpgProgramEntity(
    val sourceId: Long,
    val streamId: String,
    val title: String,
    val description: String?,
    val startMs: Long,
    val endMs: Long,
    val fetchedAt: Long = System.currentTimeMillis(),
)
