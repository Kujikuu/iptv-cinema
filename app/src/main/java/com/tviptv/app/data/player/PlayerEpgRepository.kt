package com.tviptv.app.data.player

import com.tviptv.app.data.local.dao.EpgDao
import com.tviptv.app.data.local.dao.SourceDao
import com.tviptv.app.data.local.entity.EpgProgramEntity
import com.tviptv.app.data.prefs.CredentialStore
import com.tviptv.app.data.xtream.XtreamApi
import com.tviptv.app.data.xtream.XtreamEpgEntryDto
import com.tviptv.app.domain.model.EpgProgram
import com.tviptv.app.domain.model.NowNextEpg
import com.tviptv.app.domain.model.SourceType
import com.tviptv.app.util.normalizeBaseUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerEpgRepository @Inject constructor(
    private val epgDao: EpgDao,
    private val sourceDao: SourceDao,
    private val credentialStore: CredentialStore,
    private val xtreamApi: XtreamApi,
) {

    suspend fun getNowNext(sourceId: Long, streamId: String): NowNextEpg {
        val nowMs = System.currentTimeMillis()
        val current = epgDao.getCurrentProgram(sourceId, streamId, nowMs)?.toDomain()
        val next = epgDao.getNextProgram(sourceId, streamId, nowMs)?.toDomain()
        return NowNextEpg(now = current, next = next)
    }

    suspend fun getCurrentProgramTitle(sourceId: Long, streamId: String): String? {
        val nowMs = System.currentTimeMillis()
        return epgDao.getCurrentProgram(sourceId, streamId, nowMs)?.title
    }

    suspend fun isStale(sourceId: Long, streamId: String): Boolean {
        val lastFetched = epgDao.getLastFetchedAt(sourceId, streamId) ?: return true
        return System.currentTimeMillis() - lastFetched > STALE_THRESHOLD_MS
    }

    suspend fun refreshIfNeeded(sourceId: Long, streamId: String) {
        if (!isStale(sourceId, streamId)) return
        fetchAndCache(sourceId, streamId)
    }

    suspend fun fetchAndCache(sourceId: Long, streamId: String) {
        val source = sourceDao.getById(sourceId) ?: return
        if (source.type != SourceType.XTREAM) return
        val password = credentialStore.getPassword(sourceId) ?: return
        val username = source.username ?: return
        val baseUrl = source.url ?: return

        runCatching {
            val response = xtreamApi.getShortEpg(
                url = playerApiUrl(baseUrl),
                username = username,
                password = password,
                streamId = streamId,
            )
            val listings = response.epgListings.orEmpty()
            if (listings.isEmpty()) return
            epgDao.deleteByStream(sourceId, streamId)
            val fetchedAt = System.currentTimeMillis()
            val entities = listings.mapNotNull { it.toEntity(sourceId, streamId, fetchedAt) }
            if (entities.isNotEmpty()) {
                epgDao.upsertAll(entities)
            }
        }
    }

    suspend fun prefetchBatch(sourceId: Long, streamIds: List<String>) {
        streamIds.forEach { streamId ->
            if (isStale(sourceId, streamId)) {
                fetchAndCache(sourceId, streamId)
            }
        }
    }

    private fun EpgProgramEntity.toDomain() = EpgProgram(
        title = title,
        description = description,
        startMs = startMs,
        endMs = endMs,
    )

    private fun XtreamEpgEntryDto.toEntity(
        sourceId: Long,
        streamId: String,
        fetchedAt: Long,
    ): EpgProgramEntity? {
        val startMs = startTimestamp?.toLongOrNull()?.times(1000)
            ?: parseDateTimeMs(start)
        val endMs = stopTimestamp?.toLongOrNull()?.times(1000)
            ?: parseDateTimeMs(end)
        val programTitle = title?.takeIf { it.isNotBlank() } ?: return null
        if (startMs == null || endMs == null || endMs <= startMs) return null
        return EpgProgramEntity(
            sourceId = sourceId,
            streamId = streamId,
            title = programTitle,
            description = description?.takeIf { it.isNotBlank() },
            startMs = startMs,
            endMs = endMs,
            fetchedAt = fetchedAt,
        )
    }

    private fun parseDateTimeMs(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            format.parse(value)?.time
        }.getOrNull()
    }

    private fun playerApiUrl(baseUrl: String): String =
        "${normalizeBaseUrl(baseUrl)}/player_api.php"

    companion object {
        const val STALE_THRESHOLD_MS = 15 * 60 * 1000L
        const val PREFETCH_BATCH_SIZE = 50
    }
}
