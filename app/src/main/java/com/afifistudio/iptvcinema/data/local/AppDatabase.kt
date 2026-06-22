package com.afifistudio.iptvcinema.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.afifistudio.iptvcinema.data.local.dao.CategoryDao
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.FavoriteDao
import com.afifistudio.iptvcinema.data.local.dao.LastWatchedDao
import com.afifistudio.iptvcinema.data.local.dao.EpgDao
import com.afifistudio.iptvcinema.data.local.dao.SourceDao
import com.afifistudio.iptvcinema.data.local.entity.CategoryEntity
import com.afifistudio.iptvcinema.data.local.entity.ChannelEntity
import com.afifistudio.iptvcinema.data.local.entity.FavoriteEntity
import com.afifistudio.iptvcinema.data.local.entity.LastWatchedEntity
import com.afifistudio.iptvcinema.data.local.entity.EpgProgramEntity
import com.afifistudio.iptvcinema.data.local.entity.SourceEntity
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.SourceType

class SourceTypeConverters {
    @TypeConverter
    fun fromSourceType(value: SourceType): String = value.name

    @TypeConverter
    fun toSourceType(value: String): SourceType = SourceType.valueOf(value)
}

class ContentTypeConverters {
    @TypeConverter
    fun fromContentType(value: ContentType): String = value.name

    @TypeConverter
    fun toContentType(value: String): ContentType = ContentType.valueOf(value)
}

@Database(
    entities = [
        SourceEntity::class,
        CategoryEntity::class,
        ChannelEntity::class,
        FavoriteEntity::class,
        LastWatchedEntity::class,
        EpgProgramEntity::class,
    ],
    version = 8,
    exportSchema = true,
)
@TypeConverters(SourceTypeConverters::class, ContentTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao
    abstract fun categoryDao(): CategoryDao
    abstract fun channelDao(): ChannelDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun lastWatchedDao(): LastWatchedDao
    abstract fun epgDao(): EpgDao
}
