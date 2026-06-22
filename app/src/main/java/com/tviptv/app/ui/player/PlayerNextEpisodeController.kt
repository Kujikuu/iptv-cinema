package com.tviptv.app.ui.player

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.tviptv.app.R
import com.tviptv.app.domain.model.Episode

class PlayerNextEpisodeController(
    private val root: View,
    private val titleView: TextView,
    private val countdownView: TextView,
    private val playNowButton: TextView,
    private val cancelButton: TextView,
    private val onPlayNow: () -> Unit,
    private val onCancel: () -> Unit,
) {
    init {
        playNowButton.setOnClickListener { onPlayNow() }
        cancelButton.setOnClickListener { onCancel() }
    }

    private var wasVisible = false

    fun bind(state: NextEpisodeUiState, autoplayEnabled: Boolean) {
        val episode = state.nextEpisode
        val countdown = state.countdownSeconds
        val visible = autoplayEnabled &&
            episode != null &&
            countdown != null &&
            !state.isAutoplayDismissed
        root.isVisible = visible
        if (!visible) {
            wasVisible = false
            return
        }

        titleView.text = formatEpisodeTitle(root.context, episode!!)
        countdownView.text = root.context.getString(
            R.string.player_next_episode_countdown,
            countdown!!,
        )
        if (!wasVisible) {
            wasVisible = true
            playNowButton.post { playNowButton.requestFocus() }
        }
    }

    fun isVisible(): Boolean = root.isVisible

    private fun formatEpisodeTitle(context: android.content.Context, episode: Episode): String =
        context.getString(
            R.string.player_next_episode_title,
            episode.seasonNumber,
            episode.episodeNumber,
            episode.title,
        )
}
