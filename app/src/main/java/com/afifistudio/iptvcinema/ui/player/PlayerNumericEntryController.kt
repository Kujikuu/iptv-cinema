package com.afifistudio.iptvcinema.ui.player

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.TextView
import androidx.core.view.isVisible

class PlayerNumericEntryController(
    private val overlay: TextView,
    private val onSubmit: (Int) -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var buffer = StringBuilder()
    private var submitRunnable: Runnable? = null

    fun handleDigit(digit: Int) {
        if (digit !in 0..9) return
        if (buffer.length >= MAX_DIGITS) {
            buffer.clear()
        }
        buffer.append(digit)
        overlay.text = buffer.toString()
        overlay.isVisible = true
        scheduleSubmit()
    }

    fun cancel() {
        submitRunnable?.let { handler.removeCallbacks(it) }
        submitRunnable = null
        buffer.clear()
        overlay.isVisible = false
    }

    fun isActive(): Boolean = overlay.isVisible

    private fun scheduleSubmit() {
        submitRunnable?.let { handler.removeCallbacks(it) }
        val runnable = Runnable {
            val number = buffer.toString().toIntOrNull()
            cancel()
            if (number != null && number > 0) {
                onSubmit(number)
            }
        }
        submitRunnable = runnable
        handler.postDelayed(runnable, SUBMIT_DELAY_MS)
    }

    companion object {
        private const val MAX_DIGITS = 4
        private const val SUBMIT_DELAY_MS = 1500L

        fun isDigitKey(keyCode: Int): Boolean = when (keyCode) {
            KeyEvent.KEYCODE_0,
            KeyEvent.KEYCODE_1,
            KeyEvent.KEYCODE_2,
            KeyEvent.KEYCODE_3,
            KeyEvent.KEYCODE_4,
            KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_6,
            KeyEvent.KEYCODE_7,
            KeyEvent.KEYCODE_8,
            KeyEvent.KEYCODE_9,
            -> true
            else -> false
        }

        fun digitFromKey(keyCode: Int): Int = when (keyCode) {
            KeyEvent.KEYCODE_0 -> 0
            KeyEvent.KEYCODE_1 -> 1
            KeyEvent.KEYCODE_2 -> 2
            KeyEvent.KEYCODE_3 -> 3
            KeyEvent.KEYCODE_4 -> 4
            KeyEvent.KEYCODE_5 -> 5
            KeyEvent.KEYCODE_6 -> 6
            KeyEvent.KEYCODE_7 -> 7
            KeyEvent.KEYCODE_8 -> 8
            KeyEvent.KEYCODE_9 -> 9
            else -> -1
        }
    }
}
