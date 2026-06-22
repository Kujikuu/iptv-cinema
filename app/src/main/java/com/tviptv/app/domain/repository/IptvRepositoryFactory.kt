package com.tviptv.app.domain.repository

import com.tviptv.app.data.local.dao.SourceDao
import com.tviptv.app.data.m3u.M3uRepository
import com.tviptv.app.data.xtream.XtreamRepository
import com.tviptv.app.domain.model.SourceType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IptvRepositoryFactory @Inject constructor(
    private val sourceDao: SourceDao,
    private val m3uRepository: M3uRepository,
    private val xtreamRepository: XtreamRepository,
) {
    suspend fun forSource(sourceId: Long): IptvRepository {
        val source = sourceDao.getById(sourceId)
            ?: throw IllegalArgumentException("Source not found: $sourceId")
        return when (source.type) {
            SourceType.M3U_URL, SourceType.M3U_FILE -> m3uRepository
            SourceType.XTREAM -> xtreamRepository
        }
    }
}
