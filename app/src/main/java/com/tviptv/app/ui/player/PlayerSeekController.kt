package com.tviptv.app.ui.player

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.media3.common.Player
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlayerSeekController(
    private val seekContainer: View,
    private val timeBar: DefaultTimeBar,
    private val positionLabel: TextView,
    private val durationLabel: TextView,
    private val seekPreviewLabel: TextView,
) {
    private var player: Player? = null
    private var scrubbing = false

    fun bindPlayer(exoPlayer: Player) {
        player = exoPlayer
        timeBar.addListener(
            object : TimeBar.OnScrubListener {
                override fun onScrubStart(timeBar: TimeBar, position: Long) {
                    scrubbing = true
                }

                override fun onScrubMove(timeBar: TimeBar, position: Long) {
                    updateLabels(position, exoPlayer.duration)
                }

                override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                    scrubbing = false
                    if (!canceled) {
                        exoPlayer.seekTo(position)
                    }
                    updateFromPlayer()
                }
            },
        )
    }

    fun setVisible(visible: Boolean) {
        seekContainer.isVisible = visible
    }

    fun isSeekBarFocused(): Boolean = timeBar.isFocused

    fun updateFromPlayer() {
        val exoPlayer = player ?: return
        if (scrubbing) return
        val duration = exoPlayer.duration
        val position = exoPlayer.currentPosition.coerceAtLeast(0L)
        if (duration > 0) {
            timeBar.setDuration(duration)
            timeBar.setPosition(position)
            timeBar.setBufferedPosition(exoPlayer.bufferedPosition)
        }
        updateLabels(position, duration)
    }

    fun seekBy(deltaMs: Long) {
        val exoPlayer = player ?: return
        val duration = exoPlayer.duration
        if (duration <= 0) return
        val target = (exoPlayer.currentPosition + deltaMs).coerceIn(0L, duration)
        exoPlayer.seekTo(target)
        showSeekPreview(deltaMs)
        updateFromPlayer()
    }

    fun showSeekPreview(deltaMs: Long) {
        val sign = if (deltaMs >= 0) "+" else "-"
        val absMs = kotlin.math.abs(deltaMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(absMs)
        seekPreviewLabel.text = "$sign${seconds}s"
        seekPreviewLabel.isVisible = true
        seekPreviewLabel.animate()
            .alpha(1f)
            .setDuration(100)
            .withEndAction {
                seekPreviewLabel.animate()
                    .alpha(0f)
                    .setStartDelay(800)
                    .setDuration(200)
                    .withEndAction { seekPreviewLabel.isVisible = false }
                    .start()
            }
            .start()
    }

    private fun updateLabels(positionMs: Long, durationMs: Long) {
        positionLabel.text = formatTime(positionMs)
        durationLabel.text = if (durationMs > 0) formatTime(durationMs) else "--:--"
    }

    private fun formatTime(ms: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(ms)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
