package com.tviptv.app.data.cache

import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.Episode
import com.tviptv.app.domain.repository.IptvRepositoryFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeriesEpisodesLoader @Inject constructor(
    private val repositoryFactory: IptvRepositoryFactory,
    private val seriesEpisodesCache: SeriesEpisodesCache,
) {

    suspend fun loadEpisodes(series: Channel, sourceUpdatedAt: Long): List<Episode> {
        val seriesId = series.externalId.orEmpty().ifBlank { series.id }
        seriesEpisodesCache.get(series.sourceId, seriesId, sourceUpdatedAt)?.let { return it }
        val episodes = repositoryFactory.forSource(series.sourceId)
            .getSeriesEpisodes(series.sourceId, seriesId)
            .getOrThrow()
        seriesEpisodesCache.put(series.sourceId, seriesId, sourceUpdatedAt, episodes)
        return episodes
    }

    suspend fun prefetchIfAbsent(series: Channel, sourceUpdatedAt: Long) {
        val seriesId = series.externalId.orEmpty().ifBlank { series.id }
        if (seriesEpisodesCache.get(series.sourceId, seriesId, sourceUpdatedAt) != null) return
        loadEpisodes(series, sourceUpdatedAt)
    }

    fun peekCached(sourceId: Long, seriesId: String, sourceUpdatedAt: Long): List<Episode>? =
        seriesEpisodesCache.get(sourceId, seriesId, sourceUpdatedAt)
}
