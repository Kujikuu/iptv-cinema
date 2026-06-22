package com.tviptv.app.ui.player

import android.view.KeyEvent
import com.tviptv.app.domain.model.ContentType

sealed class PlayerKeyAction {
    data object Back : PlayerKeyAction()
    data object TogglePlayPause : PlayerKeyAction()
    data object ChannelPrevious : PlayerKeyAction()
    data object ChannelNext : PlayerKeyAction()
    data class Seek(val forward: Boolean, val amountMs: Long) : PlayerKeyAction()
    data object OpenChannelList : PlayerKeyAction()
    data object OpenInfoPanel : PlayerKeyAction()
    data object OpenVodDetails : PlayerKeyAction()
    data object ShowControls : PlayerKeyAction()
    data object Consume : PlayerKeyAction()
    data object PassThrough : PlayerKeyAction()
}

class PlayerRemoteController {

    fun handleKeyDown(
        keyCode: Int,
        overlayMode: PlayerOverlayMode,
        contentType: ContentType,
        overlayVisible: Boolean,
        seekBarFocused: Boolean,
        hasPrevious: Boolean,
        hasNext: Boolean,
    ): PlayerKeyAction {
        if (overlayMode == PlayerOverlayMode.ChannelList ||
            overlayMode == PlayerOverlayMode.TrackPicker ||
            overlayMode == PlayerOverlayMode.Settings ||
            overlayMode == PlayerOverlayMode.Info
        ) {
            return PlayerKeyAction.PassThrough
        }

        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            -> return PlayerKeyAction.TogglePlayPause

            KeyEvent.KEYCODE_DPAD_UP -> {
                if (contentType == ContentType.LIVE && overlayVisible) {
                    return PlayerKeyAction.OpenChannelList
                }
                return PlayerKeyAction.ShowControls
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (contentType == ContentType.LIVE && overlayVisible) {
                    return PlayerKeyAction.OpenInfoPanel
                }
                if (contentType != ContentType.LIVE && overlayVisible) {
                    return PlayerKeyAction.OpenVodDetails
                }
                return PlayerKeyAction.ShowControls
            }

            KeyEvent.KEYCODE_BACK -> return PlayerKeyAction.Back

            KeyEvent.KEYCODE_INFO -> return PlayerKeyAction.OpenInfoPanel

            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_MEDIA_REWIND,
            -> {
                if (seekBarFocused && overlayVisible) {
                    return PlayerKeyAction.PassThrough
                }
                if (contentType == ContentType.LIVE) {
                    return if (hasPrevious) PlayerKeyAction.ChannelPrevious else PlayerKeyAction.Consume
                }
                return PlayerKeyAction.Seek(forward = false, amountMs = SEEK_STEP_MS)
            }

            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            -> {
                if (seekBarFocused && overlayVisible) {
                    return PlayerKeyAction.PassThrough
                }
                if (contentType == ContentType.LIVE) {
                    return if (hasNext) PlayerKeyAction.ChannelNext else PlayerKeyAction.Consume
                }
                return PlayerKeyAction.Seek(forward = true, amountMs = SEEK_STEP_MS)
            }
        }

        if (!overlayVisible) {
            return PlayerKeyAction.ShowControls
        }
        return PlayerKeyAction.PassThrough
    }

    fun handleBack(overlayMode: PlayerOverlayMode): BackAction = when {
        overlayMode == PlayerOverlayMode.ChannelList ||
            overlayMode == PlayerOverlayMode.TrackPicker ||
            overlayMode == PlayerOverlayMode.Settings ||
            overlayMode == PlayerOverlayMode.Info ->
            BackAction.ClosePanel
        else -> BackAction.Exit
    }

    enum class BackAction {
        ClosePanel,
        Exit,
    }

    companion object {
        const val SEEK_STEP_MS = 10_000L
        const val SEEK_LONG_MS = 30_000L
    }
}
