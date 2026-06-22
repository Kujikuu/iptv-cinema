package com.tviptv.app.ui.player

import android.view.KeyEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tviptv.app.domain.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerActivityKeyTest {

    private val remoteController = PlayerRemoteController()

    @Test
    fun backExitsPlayer() {
        assertEquals(
            PlayerRemoteController.BackAction.Exit,
            remoteController.handleBack(PlayerOverlayMode.Controls),
        )
        assertEquals(
            PlayerRemoteController.BackAction.Exit,
            remoteController.handleBack(PlayerOverlayMode.Hidden),
        )
    }

    @Test
    fun vodLeftSeeksWhenOverlayVisible() {
        val action = remoteController.handleKeyDown(
            keyCode = KeyEvent.KEYCODE_DPAD_LEFT,
            overlayMode = PlayerOverlayMode.Controls,
            contentType = ContentType.MOVIE,
            overlayVisible = true,
            seekBarFocused = false,
            hasPrevious = false,
            hasNext = false,
        )
        assertTrue(action is PlayerKeyAction.Seek)
    }

    @Test
    fun liveLeftZapsChannel() {
        val action = remoteController.handleKeyDown(
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
}
