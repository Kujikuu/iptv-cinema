package com.afifistudio.iptvcinema.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val categoryId: String?,
    val categoryName: String?,
    val streamUrl: String?,
    val sourceId: Long,
    val externalId: String?,
    val contentType: ContentType = ContentType.LIVE,
    val containerExtension: String? = null,
    val plot: String? = null,
    val addedAt: Long? = null,
    val seriesId: String? = null,
    val seriesName: String? = null,
    val channelNumber: Int? = null,
) : Parcelable {
    fun toEpisodeChannel(episode: Episode): Channel = copy(
        id = episode.id,
        name = episode.title,
        externalId = episode.id,
        contentType = ContentType.EPISODE,
        containerExtension = episode.containerExtension,
        seriesId = seriesId,
        seriesName = seriesName,
    )

    /**
     * Reconstructs a SERIES-type [Channel] from an EPISODE channel.
     * The episode's [seriesId] is used as both the id and externalId so that
     * [SeriesDetailsFragment] can load the correct episode list.
     */
    fun toSeriesChannel(): Channel = copy(
        id = seriesId ?: id,
        name = seriesName ?: name,
        externalId = seriesId ?: externalId,
        contentType = ContentType.SERIES,
        containerExtension = null,
        seriesId = null,
        seriesName = null,
    )
}
