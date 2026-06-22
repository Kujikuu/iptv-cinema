package com.afifistudio.iptvcinema.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afifistudio.iptvcinema.R

data class PlayerListOption(
    val label: String,
    val id: String,
    val selected: Boolean = false,
)

class PlayerListPanel(
    private val panelRoot: View,
    private val titleView: TextView,
    private val recyclerView: RecyclerView,
    private val onOptionSelected: (PlayerListOption) -> Unit,
) {
    private val adapter = OptionAdapter { onOptionSelected(it) }

    init {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = adapter
    }

    fun show(title: String, options: List<PlayerListOption>) {
        titleView.text = title
        adapter.submit(options)
        panelRoot.isVisible = true
        panelRoot.alpha = 0f
        panelRoot.animate().alpha(1f).setDuration(150).start()
        recyclerView.post {
            val selectedIndex = options.indexOfFirst { it.selected }.coerceAtLeast(0)
            (recyclerView.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(selectedIndex, recyclerView.height / 3)
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            layoutManager?.findViewByPosition(selectedIndex)?.requestFocus()
                ?: recyclerView.getChildAt(0)?.requestFocus()
                ?: recyclerView.requestFocus()
        }
    }

    fun hide() {
        panelRoot.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                panelRoot.isVisible = false
                panelRoot.alpha = 1f
            }
            .start()
    }

    fun isVisible(): Boolean = panelRoot.isVisible

    private class OptionAdapter(
        private val onSelect: (PlayerListOption) -> Unit,
    ) : RecyclerView.Adapter<OptionAdapter.ViewHolder>() {

        private var options: List<PlayerListOption> = emptyList()

        fun submit(options: List<PlayerListOption>) {
            this.options = options
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_player_list_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(options[position])
        }

        override fun getItemCount(): Int = options.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val label: TextView = itemView.findViewById(R.id.option_label)
            private val check: View = itemView.findViewById(R.id.option_check)

            fun bind(option: PlayerListOption) {
                label.text = option.label
                check.isVisible = option.selected
                if (option.selected) {
                    label.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
                } else {
                    label.setTextColor(ContextCompat.getColor(itemView.context, R.color.on_surface))
                }
                itemView.isFocusable = true
                itemView.setOnClickListener { onSelect(option) }
            }
        }
    }
}
