package com.afifistudio.iptvcinema.data.m3u

import com.afifistudio.iptvcinema.data.local.dao.CategoryDao
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.SourceDao
import com.afifistudio.iptvcinema.data.local.entity.CategoryEntity
import com.afifistudio.iptvcinema.data.local.entity.ChannelEntity
import com.afifistudio.iptvcinema.data.local.toDomain
import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.Episode
import com.afifistudio.iptvcinema.domain.model.SourceType
import com.afifistudio.iptvcinema.domain.repository.IptvRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3uRepository @Inject constructor(
    private val sourceDao: SourceDao,
    private val categoryDao: CategoryDao,
    private val channelDao: ChannelDao,
    private val parser: M3uParser,
    private val okHttpClient: OkHttpClient,
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
    ): Result<List<Channel>> =
        runCatching {
            val categories = categoryDao.getBySource(sourceId, contentType).associate { it.externalId to it.name }
            channelDao.getBySource(sourceId, categoryId, contentType).map { entity ->
                entity.toDomain(categories[entity.categoryId])
            }
        }

    override suspend fun resolveStreamUrl(channel: Channel): Result<String> =
        channel.streamUrl?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Stream URL missing for ${channel.name}"))

    override suspend fun refreshSource(sourceId: Long): Result<Unit> = runCatching {
        val source = sourceDao.getById(sourceId)
            ?: throw IllegalArgumentException("Source not found")
        if (source.type != SourceType.M3U_URL && source.type != SourceType.M3U_FILE) {
            throw IllegalArgumentException("Not an M3U source")
        }

        val content = loadPlaylistContent(source.url.orEmpty(), source.type)
        val parsed = parser.parse(content)
        cacheParsedPlaylist(sourceId, parsed)
        sourceDao.touch(sourceId)
    }

    override suspend fun getSeriesEpisodes(sourceId: Long, seriesId: String): Result<List<Episode>> =
        Result.failure(UnsupportedOperationException("Series are not available for M3U playlists"))

    suspend fun validateAndParse(url: String, type: SourceType): ParsedM3uPlaylist =
        withContext(Dispatchers.IO) {
            parser.parse(loadPlaylistContent(url, type))
        }

    private suspend fun loadPlaylistContent(url: String, type: SourceType): String =
        withContext(Dispatchers.IO) {
            when (type) {
                SourceType.M3U_FILE -> File(url).readText()
                SourceType.M3U_URL -> {
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", USER_AGENT)
                        .build()
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IllegalStateException("HTTP ${response.code}")
                        }
                        response.body?.string()
                            ?: throw IllegalStateException("Empty playlist response")
                    }
                }
                SourceType.XTREAM -> throw IllegalArgumentException("Not M3U")
            }
        }

    private suspend fun cacheParsedPlaylist(sourceId: Long, parsed: ParsedM3uPlaylist) {
        categoryDao.deleteBySource(sourceId)
        channelDao.deleteBySource(sourceId)

        val categoryEntities = parsed.categories.map { (id, name) ->
            CategoryEntity(
                sourceId = sourceId,
                externalId = id,
                name = name,
                contentType = ContentType.LIVE,
            )
        }
        categoryDao.insertAll(categoryEntities)

        val channelEntities = parsed.entries.mapIndexed { index, entry ->
            val categoryId = entry.groupTitle ?: M3uParser.DEFAULT_CATEGORY
            ChannelEntity(
                sourceId = sourceId,
                externalId = entry.tvgId ?: "m3u_$index",
                name = entry.name,
                logoUrl = entry.logoUrl,
                categoryId = categoryId,
                streamUrl = entry.streamUrl,
                sortOrder = index,
                contentType = ContentType.LIVE,
                channelNumber = entry.channelNumber,
            )
        }
        channelDao.insertAll(channelEntities)
    }

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 TV IPTV/1.0"
    }
}
