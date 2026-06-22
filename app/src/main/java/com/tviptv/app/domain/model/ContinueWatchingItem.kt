package com.tviptv.app.domain.model

data class ContinueWatchingItem(
    val channel: Channel,
    val watchedAt: Long,
    val playbackPositionMs: Long,
    val durationMs: Long,
    val seriesId: String? = null,
    val seriesName: String? = null,
    val seriesPosterUrl: String? = null,
) {
    val progressFraction: Float?
        get() = when {
            channel.contentType == ContentType.LIVE -> null
            durationMs <= 0L || playbackPositionMs <= 0L -> null
            else -> (playbackPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }

    val hasWatchProgress: Boolean
        get() {
            val fraction = progressFraction ?: return false
            return fraction >= MIN_VISIBLE_PROGRESS
        }

    val isNearlyComplete: Boolean
        get() = durationMs > 0L &&
            playbackPositionMs >= durationMs - RESUME_END_BUFFER_MS

    companion object {
        const val RESUME_END_BUFFER_MS = 30_000L
        const val MIN_VISIBLE_PROGRESS = 0.01f
    }
}
