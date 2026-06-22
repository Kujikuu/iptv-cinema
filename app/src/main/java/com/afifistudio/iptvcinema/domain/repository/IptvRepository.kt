package com.afifistudio.iptvcinema.domain.repository

import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.Episode

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
