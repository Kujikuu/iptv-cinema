package com.tviptv.app.domain.model

private val episodePlaybackOrder = compareBy<Episode> { it.seasonNumber }
    .thenBy { it.episodeNumber }
    .thenBy { it.id }

fun findNextEpisode(episodes: List<Episode>, currentEpisodeId: String): Episode? {
    if (episodes.isEmpty()) return null
    val ordered = episodes.sortedWith(episodePlaybackOrder)
    val currentIndex = ordered.indexOfFirst { it.id == currentEpisodeId }
    if (currentIndex < 0 || currentIndex >= ordered.lastIndex) return null
    return ordered[currentIndex + 1]
}

fun firstEpisode(episodes: List<Episode>): Episode? =
    episodes.minWithOrNull(episodePlaybackOrder)

fun Episode.toChannel(series: Channel): Channel = Channel(
    id = id,
    name = title,
    logoUrl = imageUrl ?: series.logoUrl,
    categoryId = series.categoryId,
    categoryName = series.categoryName,
    streamUrl = null,
    sourceId = sourceId,
    externalId = id,
    contentType = ContentType.EPISODE,
    containerExtension = containerExtension,
    seriesId = series.id,
    seriesName = series.name,
)
