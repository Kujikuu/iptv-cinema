package com.tviptv.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.tviptv.app.data.local.entity.CategoryCount
import com.tviptv.app.data.local.entity.CategoryEntity
import com.tviptv.app.data.local.entity.ChannelEntity
import com.tviptv.app.data.local.entity.FavoriteEntity
import com.tviptv.app.data.local.entity.LastWatchedEntity
import com.tviptv.app.data.local.entity.LastWatchedRow
import com.tviptv.app.data.local.entity.EpgProgramEntity
import com.tviptv.app.data.local.entity.SourceEntity
import com.tviptv.app.domain.model.ContentType
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY name ASC")
    fun observeAll(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM sources ORDER BY name ASC")
    suspend fun getAll(): List<SourceEntity>

    @Query("SELECT * FROM sources WHERE id = :id")
    suspend fun getById(id: Long): SourceEntity?

    @Query("SELECT COUNT(*) FROM sources")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(source: SourceEntity): Long

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE sources SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun touch(id: Long, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface CategoryDao {
    @Query(
        "SELECT * FROM categories WHERE sourceId = :sourceId " +
            "AND (:contentType IS NULL OR contentType = :contentType) ORDER BY name ASC",
    )
    suspend fun getBySource(sourceId: Long, contentType: ContentType? = null): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE sourceId = :sourceId")
    suspend fun deleteBySource(sourceId: Long)
}

@Dao
interface ChannelDao {
    @Query(
        "SELECT COUNT(*) FROM channels WHERE sourceId = :sourceId AND contentType = :contentType",
    )
    suspend fun countBySource(sourceId: Long, contentType: ContentType): Int

    @Query("SELECT COUNT(*) FROM channels WHERE sourceId = :sourceId")
    suspend fun countAllBySource(sourceId: Long): Int

    @Query(
        "SELECT * FROM channels WHERE sourceId = :sourceId " +
            "AND (:categoryId IS NULL OR categoryId = :categoryId) " +
            "AND (:contentType IS NULL OR contentType = :contentType) " +
            "ORDER BY addedAt DESC, sortOrder ASC, name ASC",
    )
    suspend fun getBySource(
        sourceId: Long,
        categoryId: String? = null,
        contentType: ContentType? = null,
    ): List<ChannelEntity>

    @Query(
        "SELECT * FROM channels WHERE sourceId = :sourceId AND categoryId = :categoryId " +
            "AND contentType = :contentType ORDER BY addedAt DESC, sortOrder ASC, name ASC LIMIT :limit",
    )
    suspend fun getByCategory(
        sourceId: Long,
        categoryId: String,
        contentType: ContentType,
        limit: Int = 20,
    ): List<ChannelEntity>

    @Query(
        "SELECT * FROM channels WHERE sourceId = :sourceId AND categoryId = :categoryId " +
            "AND contentType = :contentType ORDER BY addedAt DESC, sortOrder ASC, name ASC " +
            "LIMIT :limit OFFSET :offset",
    )
    suspend fun getByCategoryPage(
        sourceId: Long,
        categoryId: String,
        contentType: ContentType,
        limit: Int,
        offset: Int,
    ): List<ChannelEntity>

    @Query(
        "SELECT COUNT(*) FROM channels WHERE sourceId = :sourceId AND categoryId = :categoryId " +
            "AND contentType = :contentType",
    )
    suspend fun countByCategory(
        sourceId: Long,
        categoryId: String,
        contentType: ContentType,
    ): Int

    @Query(
        "SELECT * FROM channels WHERE sourceId = :sourceId AND categoryId = :categoryId " +
            "AND contentType = :contentType ORDER BY addedAt DESC, sortOrder ASC, name ASC",
    )
    suspend fun getAllByCategory(
        sourceId: Long,
        categoryId: String,
        contentType: ContentType,
    ): List<ChannelEntity>

    @Query(
        "SELECT categoryId, COUNT(*) as count, MAX(addedAt) as latestAddedAt " +
            "FROM channels WHERE sourceId = :sourceId " +
            "AND contentType = :contentType AND categoryId IS NOT NULL GROUP BY categoryId",
    )
    suspend fun getCategoryCounts(
        sourceId: Long,
        contentType: ContentType,
    ): List<CategoryCount>

    @Query(
        """
        SELECT * FROM channels
        WHERE sourceId = :sourceId
            AND contentType = :contentType
            AND categoryId IS NOT NULL
            AND logoUrl IS NOT NULL
            AND logoUrl != ''
        ORDER BY categoryId ASC, addedAt DESC, sortOrder ASC, name ASC
        """,
    )
    suspend fun getChannelsWithLogosForPreviews(
        sourceId: Long,
        contentType: ContentType,
    ): List<ChannelEntity>

    @Query(
        "SELECT * FROM channels WHERE sourceId = :sourceId AND name LIKE '%' || :query || '%' " +
            "AND (:contentType IS NULL OR contentType = :contentType) " +
            "ORDER BY addedAt DESC, name ASC LIMIT :limit",
    )
    suspend fun searchByName(
        sourceId: Long,
        query: String,
        contentType: ContentType? = null,
        limit: Int = 50,
    ): List<ChannelEntity>

    @RawQuery(observedEntities = [ChannelEntity::class])
    suspend fun searchByNameFtsRaw(query: SupportSQLiteQuery): List<ChannelEntity>

    @Query(
        "SELECT * FROM channels WHERE sourceId = :sourceId AND contentType = :contentType " +
            "AND channelNumber = :channelNumber LIMIT 1",
    )
    suspend fun getByChannelNumber(
        sourceId: Long,
        contentType: ContentType,
        channelNumber: Int,
    ): ChannelEntity?

    @Query(
        """
        SELECT * FROM channels
        WHERE sourceId = :sourceId AND contentType = 'LIVE'
        ORDER BY sortOrder ASC, name ASC
        LIMIT 1 OFFSET :index
        """,
    )
    suspend fun getLiveChannelBySortIndex(
        sourceId: Long,
        index: Int,
    ): ChannelEntity?

    @Query(
        "SELECT * FROM channels WHERE sourceId IN (:sourceIds) AND name LIKE '%' || :query || '%' " +
            "AND (:contentType IS NULL OR contentType = :contentType) " +
            "ORDER BY name ASC LIMIT :limit",
    )
    suspend fun searchByNameAllSources(
        sourceIds: List<Long>,
        query: String,
        contentType: ContentType? = null,
        limit: Int = 50,
    ): List<ChannelEntity>

    @RawQuery(observedEntities = [ChannelEntity::class])
    suspend fun searchByNameFtsAllSourcesRaw(query: SupportSQLiteQuery): List<ChannelEntity>

    @Query(
        "SELECT * FROM channels WHERE sourceId = :sourceId AND externalId = :externalId " +
            "AND contentType = :contentType LIMIT 1",
    )
    suspend fun getByExternalId(
        sourceId: Long,
        externalId: String,
        contentType: ContentType,
    ): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE sourceId = :sourceId")
    suspend fun deleteBySource(sourceId: Long)
}

@Dao
interface FavoriteDao {
    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN favorites f ON c.sourceId = f.sourceId
            AND c.externalId = f.channelId
            AND c.contentType = f.contentType
        WHERE f.sourceId = :sourceId
        AND (:contentType IS NULL OR c.contentType = :contentType)
        ORDER BY f.addedAt DESC
        """,
    )
    suspend fun getFavoriteChannels(
        sourceId: Long,
        contentType: ContentType? = null,
    ): List<ChannelEntity>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favorites WHERE sourceId = :sourceId " +
            "AND channelId = :channelId AND contentType = :contentType)",
    )
    suspend fun isFavorite(sourceId: Long, channelId: String, contentType: ContentType): Boolean

    @Query(
        "SELECT channelId FROM favorites WHERE sourceId = :sourceId " +
            "AND (:contentType IS NULL OR contentType = :contentType)",
    )
    suspend fun getFavoriteChannelIds(
        sourceId: Long,
        contentType: ContentType? = null,
    ): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query(
        "DELETE FROM favorites WHERE sourceId = :sourceId AND channelId = :channelId " +
            "AND contentType = :contentType",
    )
    suspend fun delete(sourceId: Long, channelId: String, contentType: ContentType)

    @Query("DELETE FROM favorites WHERE sourceId = :sourceId")
    suspend fun deleteBySource(sourceId: Long)
}

@Dao
interface LastWatchedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lastWatched: LastWatchedEntity)

    @Query(
        """
        SELECT
            lw.sourceId AS sourceId,
            lw.channelId AS channelId,
            lw.contentType AS contentType,
            lw.watchedAt AS watchedAt,
            lw.playbackPositionMs AS playbackPositionMs,
            lw.durationMs AS durationMs,
            lw.title AS title,
            lw.imageUrl AS imageUrl,
            lw.categoryId AS categoryId,
            lw.seriesId AS seriesId,
            lw.seriesName AS seriesName,
            c.name AS channelName,
            c.logoUrl AS channelLogoUrl,
            c.categoryId AS channelCategoryId,
            c.streamUrl AS channelStreamUrl,
            c.sortOrder AS channelSortOrder,
            c.containerExtension AS channelContainerExtension,
            c.plot AS channelPlot,
            c.addedAt AS channelAddedAt
        FROM last_watched lw
        LEFT JOIN channels c ON c.sourceId = lw.sourceId
            AND c.externalId = lw.channelId
            AND c.contentType = lw.contentType
        WHERE lw.sourceId = :sourceId
        AND (:contentType IS NULL OR lw.contentType = :contentType)
        ORDER BY lw.watchedAt DESC
        LIMIT :limit
        """,
    )
    suspend fun getRecentWatchRows(
        sourceId: Long,
        contentType: ContentType? = null,
        limit: Int = 15,
    ): List<LastWatchedRow>

    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN last_watched lw ON c.sourceId = lw.sourceId
            AND c.externalId = lw.channelId
            AND c.contentType = lw.contentType
        WHERE lw.sourceId = :sourceId
        AND (:contentType IS NULL OR c.contentType = :contentType)
        ORDER BY lw.watchedAt DESC LIMIT :limit
        """,
    )
    suspend fun getRecentChannels(
        sourceId: Long,
        contentType: ContentType? = null,
        limit: Int = 15,
    ): List<ChannelEntity>

    @Query(
        "SELECT channelId FROM last_watched WHERE sourceId = :sourceId " +
            "ORDER BY watchedAt DESC LIMIT 1",
    )
    suspend fun getMostRecentChannelId(sourceId: Long): String?

    @Query(
        "SELECT * FROM last_watched WHERE sourceId = :sourceId AND channelId = :channelId " +
            "AND contentType = :contentType LIMIT 1",
    )
    suspend fun get(
        sourceId: Long,
        channelId: String,
        contentType: ContentType,
    ): LastWatchedEntity?

    @Query(
        """
        SELECT
            lw.sourceId AS sourceId,
            lw.channelId AS channelId,
            lw.contentType AS contentType,
            lw.watchedAt AS watchedAt,
            lw.playbackPositionMs AS playbackPositionMs,
            lw.durationMs AS durationMs,
            lw.title AS title,
            lw.imageUrl AS imageUrl,
            lw.categoryId AS categoryId,
            lw.seriesId AS seriesId,
            lw.seriesName AS seriesName,
            c.name AS channelName,
            c.logoUrl AS channelLogoUrl,
            c.categoryId AS channelCategoryId,
            c.streamUrl AS channelStreamUrl,
            c.sortOrder AS channelSortOrder,
            c.containerExtension AS channelContainerExtension,
            c.plot AS channelPlot,
            c.addedAt AS channelAddedAt
        FROM last_watched lw
        LEFT JOIN channels c ON c.sourceId = lw.sourceId
            AND c.externalId = lw.channelId
            AND c.contentType = lw.contentType
        WHERE lw.sourceId = :sourceId
            AND lw.contentType = 'EPISODE'
            AND lw.seriesId = :seriesId
        ORDER BY lw.watchedAt DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestEpisodeRowForSeries(
        sourceId: Long,
        seriesId: String,
    ): LastWatchedRow?

    @Query(
        "DELETE FROM last_watched WHERE sourceId = :sourceId AND channelId = :channelId " +
            "AND contentType = :contentType",
    )
    suspend fun delete(sourceId: Long, channelId: String, contentType: ContentType)

    @Query("DELETE FROM last_watched WHERE sourceId = :sourceId")
    suspend fun deleteBySource(sourceId: Long)

    @Query("DELETE FROM last_watched")
    suspend fun deleteAll()
}

@Dao
interface EpgDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(programs: List<EpgProgramEntity>)

    @Query(
        "SELECT * FROM epg_programs WHERE sourceId = :sourceId AND streamId = :streamId " +
            "AND startMs <= :nowMs AND endMs > :nowMs LIMIT 1",
    )
    suspend fun getCurrentProgram(
        sourceId: Long,
        streamId: String,
        nowMs: Long,
    ): EpgProgramEntity?

    @Query(
        "SELECT * FROM epg_programs WHERE sourceId = :sourceId AND streamId = :streamId " +
            "AND startMs >= :nowMs ORDER BY startMs ASC LIMIT 1",
    )
    suspend fun getNextProgram(
        sourceId: Long,
        streamId: String,
        nowMs: Long,
    ): EpgProgramEntity?

    @Query(
        "SELECT * FROM epg_programs WHERE sourceId = :sourceId AND streamId = :streamId " +
            "ORDER BY startMs ASC",
    )
    suspend fun getProgramsForStream(sourceId: Long, streamId: String): List<EpgProgramEntity>

    @Query("DELETE FROM epg_programs WHERE sourceId = :sourceId AND streamId = :streamId")
    suspend fun deleteByStream(sourceId: Long, streamId: String)

    @Query(
        "SELECT MAX(fetchedAt) FROM epg_programs WHERE sourceId = :sourceId AND streamId = :streamId",
    )
    suspend fun getLastFetchedAt(sourceId: Long, streamId: String): Long?

    @Query("DELETE FROM epg_programs WHERE sourceId = :sourceId")
    suspend fun deleteBySource(sourceId: Long)
}
