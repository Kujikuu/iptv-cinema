package com.tviptv.app.data.xtream

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class XtreamAuthResponse(
    @Json(name = "user_info") val userInfo: XtreamUserInfo? = null,
)

@JsonClass(generateAdapter = true)
data class XtreamUserInfo(
    @Json(name = "auth") val auth: Int? = null,
    @Json(name = "status") val status: String? = null,
)

@JsonClass(generateAdapter = true)
data class XtreamCategoryDto(
    @Json(name = "category_id") val categoryId: String,
    @Json(name = "category_name") val categoryName: String,
)

@JsonClass(generateAdapter = true)
data class XtreamStreamDto(
    @Json(name = "stream_id") val streamId: Int,
    @Json(name = "name") val name: String,
    @Json(name = "stream_icon") val streamIcon: String? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    @Json(name = "added") val added: String? = null,
)

@JsonClass(generateAdapter = true)
data class XtreamVodStreamDto(
    @Json(name = "stream_id") val streamId: Int,
    @Json(name = "name") val name: String,
    @Json(name = "stream_icon") val streamIcon: String? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    @Json(name = "container_extension") val containerExtension: String? = null,
    @Json(name = "plot") val plot: String? = null,
    @Json(name = "added") val added: String? = null,
)

@JsonClass(generateAdapter = true)
data class XtreamSeriesDto(
    @Json(name = "series_id") val seriesId: Int,
    @Json(name = "name") val name: String,
    @Json(name = "cover") val cover: String? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    @Json(name = "plot") val plot: String? = null,
    @Json(name = "last_modified") val lastModified: String? = null,
)

@JsonClass(generateAdapter = true)
data class XtreamSeriesInfoResponse(
    @Json(name = "episodes") val episodes: Map<String, List<XtreamEpisodeDto>>? = null,
)

@JsonClass(generateAdapter = true)
data class XtreamEpisodeDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "episode_num") val episodeNum: Int? = null,
    @Json(name = "container_extension") val containerExtension: String? = null,
)

@JsonClass(generateAdapter = true)
data class XtreamShortEpgResponse(
    @Json(name = "epg_listings") val epgListings: List<XtreamEpgEntryDto>? = null,
)

@JsonClass(generateAdapter = true)
data class XtreamEpgEntryDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "start_timestamp") val startTimestamp: String? = null,
    @Json(name = "stop_timestamp") val stopTimestamp: String? = null,
    @Json(name = "start") val start: String? = null,
    @Json(name = "end") val end: String? = null,
)
