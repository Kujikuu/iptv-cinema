package com.afifistudio.iptvcinema.data.local

import androidx.sqlite.db.SimpleSQLiteQuery
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.entity.ChannelEntity
import com.afifistudio.iptvcinema.domain.model.ContentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelSearchHelper @Inject constructor(
    private val channelDao: ChannelDao,
) {
    suspend fun search(
        sourceIds: List<Long>,
        query: String,
        contentType: ContentType? = null,
        limit: Int = 50,
    ): List<ChannelEntity> {
        if (sourceIds.isEmpty() || query.isBlank()) return emptyList()
        val ftsQuery = buildFtsQuery(query) ?: return searchLike(sourceIds, query, contentType, limit)
        val sourcePlaceholders = sourceIds.joinToString(",") { "?" }
        val args = mutableListOf<Any>(ftsQuery)
        args.addAll(sourceIds)
        val contentFilter = if (contentType != null) {
            args.add(contentType.name)
            "AND c.contentType = ?"
        } else {
            ""
        }
        args.add(limit)
        val sql = """
            SELECT c.* FROM channels c
            INNER JOIN channels_fts fts ON c.rowid = fts.rowid
            WHERE channels_fts MATCH ?
                AND c.sourceId IN ($sourcePlaceholders)
                $contentFilter
            ORDER BY rank
            LIMIT ?
        """.trimIndent()
        return runCatching {
            channelDao.searchByNameFtsAllSourcesRaw(SimpleSQLiteQuery(sql, args.toTypedArray()))
        }.getOrElse {
            searchLike(sourceIds, query, contentType, limit)
        }
    }

    private suspend fun searchLike(
        sourceIds: List<Long>,
        query: String,
        contentType: ContentType?,
        limit: Int,
    ): List<ChannelEntity> = channelDao.searchByNameAllSources(
        sourceIds = sourceIds,
        query = query,
        contentType = contentType,
        limit = limit,
    )

    private fun buildFtsQuery(query: String): String? {
        val tokens = query.split(Regex("\\s+"))
            .map { it.trim().replace("\"", "").replace("'", "").replace("*", "") }
            .filter { it.isNotBlank() }
        if (tokens.isEmpty()) return null
        return tokens.joinToString(" ") { "$it*" }
    }
}
