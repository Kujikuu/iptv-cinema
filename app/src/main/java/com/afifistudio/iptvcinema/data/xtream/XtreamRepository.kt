package com.afifistudio.iptvcinema.data.xtream

import com.afifistudio.iptvcinema.data.local.dao.CategoryDao
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.SourceDao
import com.afifistudio.iptvcinema.data.local.entity.CategoryEntity
import com.afifistudio.iptvcinema.data.local.entity.ChannelEntity
import com.afifistudio.iptvcinema.data.local.toDomain
import com.afifistudio.iptvcinema.data.prefs.CredentialStore
import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.Episode
import com.afifistudio.iptvcinema.domain.model.SourceType
import com.afifistudio.iptvcinema.domain.repository.IptvRepository
import com.afifistudio.iptvcinema.util.buildXtreamLiveUrl
import com.afifistudio.iptvcinema.util.buildXtreamMovieUrl
import com.afifistudio.iptvcinema.util.buildXtreamSeriesEpisodeUrl
import com.afifistudio.iptvcinema.data.player.PlayerEpgRepository
import com.afifistudio.iptvcinema.util.normalizeBaseUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamRepository @Inject constructor(
    private val sourceDao: SourceDao,
    private val categoryDao: CategoryDao,
    private val channelDao: ChannelDao,
    private val credentialStore: CredentialStore,
    private val xtreamApi: XtreamApi,
    private val epgRepository: PlayerEpgRepository,
) : IptvRepository {

    override suspend fun getCategories(
        sourceId: Long,
        contentType: ContentType?,
    ): Result<List<Category>> = runCatching {
        categoryDao.getBySource(sourceId, contentType).map { it.toDomain() }
    }

    override suspend fun getChannels(
        sourceId: Long,
        categoryId: String?,
        contentType: ContentType?,
    ): Result<List<Channel>> = runCatching {
        val categories = categoryDao.getBySource(sourceId, contentType).associate { it.externalId to it.name }
        channelDao.getBySource(sourceId, categoryId, contentType).map { entity ->
            entity.toDomain(categories[entity.categoryId])
        }
    }

    override suspend fun resolveStreamUrl(channel: Channel): Result<String> = runCatching {
        val source = sourceDao.getById(channel.sourceId)
            ?: throw IllegalStateException("Source not found")
        val password = credentialStore.getPassword(source.id)
            ?: throw IllegalStateException("Missing credentials")
        val streamId = channel.externalId
            ?: throw IllegalStateException("Missing stream id")
        when (channel.contentType) {
            ContentType.LIVE -> buildXtreamLiveUrl(
                baseUrl = source.url.orEmpty(),
                username = source.username.orEmpty(),
                password = password,
                streamId = streamId,
            )
            ContentType.MOVIE -> buildXtreamMovieUrl(
                baseUrl = source.url.orEmpty(),
                username = source.username.orEmpty(),
                password = password,
                streamId = streamId,
                extension = channel.containerExtension ?: "mp4",
            )
            ContentType.EPISODE -> buildXtreamSeriesEpisodeUrl(
                baseUrl = source.url.orEmpty(),
                username = source.username.orEmpty(),
                password = password,
                episodeId = streamId,
                extension = channel.containerExtension ?: "mp4",
            )
            ContentType.SERIES -> throw IllegalStateException("Select an episode to play")
        }
    }

    override suspend fun refreshSource(sourceId: Long): Result<Unit> = runCatching {
        val source = sourceDao.getById(sourceId)
            ?: throw IllegalArgumentException("Source not found")
        if (source.type != SourceType.XTREAM) {
            throw IllegalArgumentException("Not an Xtream source")
        }
        val password = credentialStore.getPassword(sourceId)
            ?: throw IllegalStateException("Missing credentials")
        cacheFromApi(source.url.orEmpty(), source.username.orEmpty(), password, sourceId)
        sourceDao.touch(sourceId)
    }

    override suspend fun getSeriesEpisodes(sourceId: Long, seriesId: String): Result<List<Episode>> =
        runCatching {
            val source = sourceDao.getById(sourceId)
                ?: throw IllegalStateException("Source not found")
            val password = credentialStore.getPassword(sourceId)
                ?: throw IllegalStateException("Missing credentials")
            val series = channelDao.getByExternalId(sourceId, seriesId, ContentType.SERIES)
                ?: throw IllegalStateException("Series not found")
            val response = xtreamApi.getSeriesInfo(
                playerApiUrl(source.url.orEmpty()),
                source.username.orEmpty(),
                password,
                seriesId = seriesId,
            )
            response.episodes.orEmpty()
                .flatMap { (seasonNumber, episodes) ->
                    episodes.map { episode ->
                        Episode(
                            id = episode.id,
                            title = episode.title,
                            seasonNumber = seasonNumber.toIntOrNull() ?: 0,
                            episodeNumber = episode.episodeNum ?: 0,
                            containerExtension = episode.containerExtension ?: "mp4",
                            sourceId = sourceId,
                            seriesId = seriesId,
                            seriesName = series.name,
                            imageUrl = series.logoUrl,
                        )
                    }
                }
                .sortedWith(
                    compareByDescending<Episode> { it.seasonNumber }
                        .thenByDescending { it.episodeNumber },
                )
        }

    suspend fun validateCredentials(baseUrl: String, username: String, password: String) {
        val apiUrl = playerApiUrl(baseUrl)
        val response = xtreamApi.authenticate(apiUrl, username, password)
        val authorized = response.userInfo?.auth == 1 ||
            response.userInfo?.status.equals("Active", ignoreCase = true)
        if (!authorized) {
            throw IllegalStateException("Authentication failed")
        }
    }

    suspend fun cacheFromApi(baseUrl: String, username: String, password: String, sourceId: Long) {
        val apiUrl = playerApiUrl(baseUrl)

        val liveCategories = xtreamApi.getLiveCategories(apiUrl, username, password)
        val vodCategories = xtreamApi.getVodCategories(apiUrl, username, password)
        val seriesCategories = xtreamApi.getSeriesCategories(apiUrl, username, password)

        categoryDao.deleteBySource(sourceId)
        channelDao.deleteBySource(sourceId)

        val categories = buildList {
            addAll(
                liveCategories.map {
                    CategoryEntity(
                        sourceId = sourceId,
                        externalId = it.categoryId,
                        name = it.categoryName,
                        contentType = ContentType.LIVE,
                    )
                },
            )
            addAll(
                vodCategories.map {
                    CategoryEntity(
                        sourceId = sourceId,
                        externalId = it.categoryId,
                        name = it.categoryName,
                        contentType = ContentType.MOVIE,
                    )
                },
            )
            addAll(
                seriesCategories.map {
                    CategoryEntity(
                        sourceId = sourceId,
                        externalId = it.categoryId,
                        name = it.categoryName,
                        contentType = ContentType.SERIES,
                    )
                },
            )
        }
        categoryDao.insertAll(categories)

        val liveStreams = xtreamApi.getLiveStreams(apiUrl, username, password)
        insertMappedBatches(liveStreams) { stream ->
            ChannelEntity(
                sourceId = sourceId,
                externalId = stream.streamId.toString(),
                name = stream.name,
                logoUrl = stream.streamIcon?.takeIf { it.isNotBlank() },
                categoryId = stream.categoryId,
                streamUrl = null,
                sortOrder = 0,
                contentType = ContentType.LIVE,
                addedAt = stream.added?.toLongOrNull(),
            )
        }

        val vodStreams = xtreamApi.getVodStreams(apiUrl, username, password)
        insertMappedBatches(vodStreams) { stream ->
            ChannelEntity(
                sourceId = sourceId,
                externalId = stream.streamId.toString(),
                name = stream.name,
                logoUrl = stream.streamIcon?.takeIf { it.isNotBlank() },
                categoryId = stream.categoryId,
                streamUrl = null,
                sortOrder = 0,
                contentType = ContentType.MOVIE,
                containerExtension = stream.containerExtension?.takeIf { it.isNotBlank() },
                addedAt = stream.added?.toLongOrNull(),
            )
        }

        val seriesList = xtreamApi.getSeries(apiUrl, username, password)
        insertMappedBatches(seriesList) { series ->
            ChannelEntity(
                sourceId = sourceId,
                externalId = series.seriesId.toString(),
                name = series.name,
                logoUrl = series.cover?.takeIf { it.isNotBlank() },
                categoryId = series.categoryId,
                streamUrl = null,
                sortOrder = 0,
                contentType = ContentType.SERIES,
                addedAt = series.lastModified?.toLongOrNull(),
            )
        }

        val liveStreamIds = liveStreams.map { it.streamId.toString() }
        epgRepository.prefetchBatch(
            sourceId,
            liveStreamIds.take(PlayerEpgRepository.PREFETCH_BATCH_SIZE),
        )
    }

    private suspend fun <T> insertMappedBatches(
        items: List<T>,
        mapper: (T) -> ChannelEntity,
    ) {
        items.chunked(INSERT_BATCH_SIZE).forEach { batch ->
            channelDao.insertAll(batch.map(mapper))
        }
    }

    private fun playerApiUrl(baseUrl: String): String =
        "${normalizeBaseUrl(baseUrl)}/player_api.php"

    companion object {
        private const val INSERT_BATCH_SIZE = 400
    }
}
