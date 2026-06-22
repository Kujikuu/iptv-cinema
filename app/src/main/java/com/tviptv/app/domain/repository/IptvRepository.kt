package com.tviptv.app.domain.repository

import com.tviptv.app.domain.model.Category
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.domain.model.Episode

interface IptvRepository {
    suspend fun getCategories(sourceId: Long, contentType: ContentType? = null): Result<List<Category>>
    suspend fun getChannels(
        sourceId: Long,
        categoryId: String?,
        contentType: ContentType? = null,
    ): Result<List<Channel>>
    suspend fun resolveStreamUrl(channel: Channel): Result<String>
    suspend fun refreshSource(sourceId: Long): Result<Unit>
    suspend fun getSeriesEpisodes(sourceId: Long, seriesId: String): Result<List<Episode>>
}
