package com.afifistudio.iptvcinema.ui.player

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible

class PlayerOverlayController(
    private val overlayRoot: View,
    private val controlsRoot: View,
    private val onModeChanged: (PlayerOverlayMode) -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var mode = PlayerOverlayMode.Hidden

    private val hideRunnable = Runnable {
        if (mode == PlayerOverlayMode.Controls) {
            setMode(PlayerOverlayMode.Hidden, animate = true)
        }
    }

    fun currentMode(): PlayerOverlayMode = mode

    fun isOverlayVisible(): Boolean = mode != PlayerOverlayMode.Hidden

    fun setMode(newMode: PlayerOverlayMode, animate: Boolean = false) {
        if (mode == newMode) return
        mode = newMode
        when (newMode) {
            PlayerOverlayMode.Hidden -> {
                cancelHideTimer()
                if (animate) {
                    overlayRoot.animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_MS)
                        .withEndAction {
                            overlayRoot.visibility = View.GONE
                            overlayRoot.alpha = 1f
                        }
                        .start()
                } else {
                    overlayRoot.visibility = View.GONE
                }
            }
            else -> {
                overlayRoot.visibility = View.VISIBLE
                controlsRoot.isVisible = newMode == PlayerOverlayMode.Controls
                if (animate) {
                    overlayRoot.alpha = 0f
                    overlayRoot.animate()
                        .alpha(1f)
                        .setDuration(ANIMATION_MS)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                } else {
                    overlayRoot.alpha = 1f
                }
                if (newMode == PlayerOverlayMode.Controls) {
                    scheduleHide()
                } else {
                    cancelHideTimer()
                }
            }
        }
        onModeChanged(newMode)
    }

    fun showControls(focusPlayPause: () -> Unit) {
        setMode(PlayerOverlayMode.Controls, animate = true)
        focusPlayPause()
        scheduleHide()
    }

    fun scheduleHide() {
        if (mode != PlayerOverlayMode.Controls) return
        cancelHideTimer()
        handler.postDelayed(hideRunnable, HIDE_DELAY_MS)
    }

    fun cancelHideTimer() {
        handler.removeCallbacks(hideRunnable)
    }

    fun resetHideTimer() {
        scheduleHide()
    }

    fun release() {
        handler.removeCallbacks(hideRunnable)
    }

    companion object {
        private const val HIDE_DELAY_MS = 5000L
        private const val ANIMATION_MS = 150L
    }
}
