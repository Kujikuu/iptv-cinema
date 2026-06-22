package com.tviptv.app.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tviptv.app.R
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.ui.common.ContentImageBindings.bindContentImage

class PlayerChannelListPanel(
    private val panelRoot: View,
    private val recyclerView: RecyclerView,
    private val onChannelSelected: (Channel) -> Unit,
) {
    private val adapter = ChannelAdapter { onChannelSelected(it) }
    private var currentChannelId: String? = null

    init {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = adapter
    }

    fun show(channels: List<Channel>, currentIndex: Int, epgTitles: Map<String, String>) {
        currentChannelId = channels.getOrNull(currentIndex)?.id
        adapter.submit(channels, currentChannelId, epgTitles)
        panelRoot.isVisible = true
        panelRoot.alpha = 0f
        panelRoot.translationX = -panelRoot.width.toFloat().coerceAtLeast(-400f)
        panelRoot.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(200)
            .start()
        recyclerView.post {
            val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return@post
            lm.scrollToPositionWithOffset(currentIndex.coerceAtLeast(0), recyclerView.height / 3)
            lm.findViewByPosition(currentIndex)?.requestFocus()
                ?: recyclerView.requestFocus()
        }
    }

    fun hide() {
        panelRoot.animate()
            .alpha(0f)
            .translationX(-panelRoot.width.toFloat().coerceAtLeast(-400f))
            .setDuration(150)
            .withEndAction {
                panelRoot.isVisible = false
                panelRoot.translationX = 0f
                panelRoot.alpha = 1f
            }
            .start()
    }

    fun isVisible(): Boolean = panelRoot.isVisible

    private class ChannelAdapter(
        private val onSelect: (Channel) -> Unit,
    ) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

        private var channels: List<Channel> = emptyList()
        private var currentId: String? = null
        private var epgTitles: Map<String, String> = emptyMap()

        fun submit(channels: List<Channel>, currentId: String?, epgTitles: Map<String, String>) {
            this.channels = channels
            this.currentId = currentId
            this.epgTitles = epgTitles
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_player_channel, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(channels[position], channels[position].id == currentId, epgTitles)
        }

        override fun getItemCount(): Int = channels.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val logo: ImageView = itemView.findViewById(R.id.channel_logo)
            private val name: TextView = itemView.findViewById(R.id.channel_name)
            private val epg: TextView = itemView.findViewById(R.id.channel_epg)
            private val accent: View = itemView.findViewById(R.id.channel_accent)

            fun bind(channel: Channel, isCurrent: Boolean, epgTitles: Map<String, String>) {
                name.text = channel.name
                val epgTitle = epgTitles[channel.externalId ?: channel.id]
                epg.text = epgTitle ?: itemView.context.getString(R.string.player_no_program_info)
                accent.isVisible = isCurrent
                if (isCurrent) {
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
                } else {
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.on_surface))
                }
                logo.bindContentImage(channel.logoUrl, channel.contentType, crossfade = false)
                itemView.isFocusable = true
                itemView.setOnClickListener { onSelect(channel) }
            }
        }
    }
}
