package com.afifistudio.iptvcinema.util

fun normalizeBaseUrl(url: String): String {
    var normalized = url.trim()
    if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
        normalized = "http://$normalized"
    }
    return normalized.trimEnd('/')
}

fun buildXtreamLiveUrl(
    baseUrl: String,
    username: String,
    password: String,
    streamId: String,
    extension: String = "m3u8",
): String = "${normalizeBaseUrl(baseUrl)}/live/$username/$password/$streamId.$extension"

fun buildXtreamMovieUrl(
    baseUrl: String,
    username: String,
    password: String,
    streamId: String,
    extension: String,
): String = "${normalizeBaseUrl(baseUrl)}/movie/$username/$password/$streamId.$extension"

fun buildXtreamSeriesEpisodeUrl(
    baseUrl: String,
    username: String,
    password: String,
    episodeId: String,
    extension: String,
): String = "${normalizeBaseUrl(baseUrl)}/series/$username/$password/$episodeId.$extension"

fun buildXtreamStreamUrl(
    baseUrl: String,
    username: String,
    password: String,
    streamId: String,
    extension: String = "m3u8",
): String = buildXtreamLiveUrl(baseUrl, username, password, streamId, extension)
