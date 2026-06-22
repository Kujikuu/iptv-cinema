package com.tviptv.app.ui.player

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.tviptv.app.R
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.ui.common.ContentImageBindings.bindContentImage

class PlayerZapToastView(
    private val root: View,
    private val logoView: ImageView,
    private val titleView: TextView,
    private val numberView: TextView,
) {
    private var hideRunnable: Runnable? = null

    fun show(channel: Channel, channelNumber: Int?) {
        hideRunnable?.let { root.removeCallbacks(it) }
        titleView.text = channel.name
        numberView.text = channelNumber?.toString().orEmpty()
        numberView.visibility = if (channelNumber != null) View.VISIBLE else View.GONE

        logoView.bindContentImage(channel.logoUrl, channel.contentType, crossfade = false)

        root.visibility = View.VISIBLE
        root.alpha = 0f
        root.translationY = 32f
        root.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(200)
            .start()

        val runnable = Runnable { hide() }
        hideRunnable = runnable
        root.postDelayed(runnable, DISPLAY_MS)
    }

    fun preload(channel: Channel) {
        val logoUrl = channel.logoUrl ?: return
        logoView.bindContentImage(logoUrl, channel.contentType, crossfade = false)
    }

    fun hide() {
        hideRunnable?.let { root.removeCallbacks(it) }
        hideRunnable = null
        root.animate()
            .alpha(0f)
            .translationY(16f)
            .setDuration(150)
            .withEndAction {
                root.visibility = View.GONE
                root.translationY = 0f
                root.alpha = 1f
            }
            .start()
    }

    companion object {
        private const val DISPLAY_MS = 2000L
    }
}
