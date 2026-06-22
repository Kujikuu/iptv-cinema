package com.tviptv.app.ui.player

import android.view.KeyEvent
import com.tviptv.app.domain.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlayerRemoteControllerTest {

    private lateinit var controller: PlayerRemoteController

    @Before
    fun setUp() {
        controller = PlayerRemoteController()
    }

    @Test
    fun playPause_alwaysToggles_evenWhenOverlayHidden() {
        val action = controller.handleKeyDown(
            keyCode = KeyEvent.KEYCODE_DPAD_CENTER,
            overlayMode = PlayerOverlayMode.Hidden,
            contentType = ContentType.LIVE,
            overlayVisible = false,
            seekBarFocused = false,
            hasPrevious = true,
            hasNext = true,
        )
        assertEquals(PlayerKeyAction.TogglePlayPause, action)
    }

    @Test
    fun live_left_zapsPrevious() {
        val action = controller.handleKeyDown(
            keyCode = KeyEvent.KEYCODE_DPAD_LEFT,
            overlayMode = PlayerOverlayMode.Controls,
            contentType = ContentType.LIVE,
            overlayVisible = true,
            seekBarFocused = false,
            hasPrevious = true,
            hasNext = true,
        )
        assertEquals(PlayerKeyAction.ChannelPrevious, action)
    }

    @Test
    fun vod_overlayVisible_left_seeks() {
        val action = controller.handleKeyDown(
            keyCode = KeyEvent.KEYCODE_DPAD_LEFT,
            overlayMode = PlayerOverlayMode.Controls,
            contentType = ContentType.MOVIE,
            overlayVisible = true,
            seekBarFocused = false,
            hasPrevious = false,
            hasNext = false,
        )
        assertTrue(action is PlayerKeyAction.Seek)
        assertEquals(false, (action as PlayerKeyAction.Seek).forward)
    }

    @Test
    fun vod_overlayHidden_left_seeksImmediately() {
        val action = controller.handleKeyDown(
            keyCode = KeyEvent.KEYCODE_DPAD_LEFT,
            overlayMode = PlayerOverlayMode.Hidden,
            contentType = ContentType.MOVIE,
            overlayVisible = false,
            seekBarFocused = false,
            hasPrevious = false,
            hasNext = false,
        )
        assertTrue(action is PlayerKeyAction.Seek)
        assertEquals(false, (action as PlayerKeyAction.Seek).forward)
    }

    @Test
    fun vod_seekBarFocused_passesThroughLeftRight() {
        val left = controller.handleKeyDown(
            keyCode = KeyEvent.KEYCODE_DPAD_LEFT,
            overlayMode = PlayerOverlayMode.Controls,
            contentType = ContentType.MOVIE,
            overlayVisible = true,
            seekBarFocused = true,
            hasPrevious = false,
            hasNext = false,
        )
        val right = controller.handleKeyDown(
            keyCode = KeyEvent.KEYCODE_DPAD_RIGHT,
            overlayMode = PlayerOverlayMode.Controls,
            contentType = ContentType.MOVIE,
            overlayVisible = true,
            seekBarFocused = true,
            hasPrevious = false,
            hasNext = false,
        )
        assertEquals(PlayerKeyAction.PassThrough, left)
        assertEquals(PlayerKeyAction.PassThrough, right)
    }

    @Test
    fun vod_down_opensDetailsWhenOverlayVisible() {
        val action = controller.handleKeyDown(
            keyCode = KeyEvent.KEYCODE_DPAD_DOWN,
            overlayMode = PlayerOverlayMode.Controls,
            contentType = ContentType.MOVIE,
            overlayVisible = true,
            seekBarFocused = false,
            hasPrevious = false,
            hasNext = false,
        )
        assertEquals(PlayerKeyAction.OpenVodDetails, action)
    }

    @Test
    fun live_up_opensChannelList() {
        val action = controller.handleKeyDown(
            keyCode = KeyEvent.KEYCODE_DPAD_UP,
            overlayMode = PlayerOverlayMode.Controls,
            contentType = ContentType.LIVE,
            overlayVisible = true,
            seekBarFocused = false,
            hasPrevious = true,
            hasNext = true,
        )
        assertEquals(PlayerKeyAction.OpenChannelList, action)
    }

    @Test
    fun back_closesPanelWhenSubPanelOpen() {
        val action = controller.handleBack(PlayerOverlayMode.ChannelList)
        assertEquals(PlayerRemoteController.BackAction.ClosePanel, action)
    }

    @Test
    fun back_exitsPlayerWhenNoPanelOpen() {
        val action = controller.handleBack(PlayerOverlayMode.Controls)
        assertEquals(PlayerRemoteController.BackAction.Exit, action)
    }

    @Test
    fun back_exitsWhenOverlayHidden() {
        val action = controller.handleBack(PlayerOverlayMode.Hidden)
        assertEquals(PlayerRemoteController.BackAction.Exit, action)
    }
}
