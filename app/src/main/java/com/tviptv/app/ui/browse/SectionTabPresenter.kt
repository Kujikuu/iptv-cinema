package com.tviptv.app.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import com.tviptv.app.R
import com.tviptv.app.databinding.SectionTabBinding

class SectionTabPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = SectionTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card = item as? BrowseItem ?: return
        val root = viewHolder.view
        val binding = SectionTabBinding.bind(root)
        val context = root.context

        binding.sectionTabLabel.text = card.title
        applyTabStyle(binding, card.selected)

        root.contentDescription = card.title
        root.setOnFocusChangeListener { view, hasFocus ->
            CardFocusHelper.applySectionTabFocus(view, hasFocus)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val root = viewHolder.view
        val binding = SectionTabBinding.bind(root)
        binding.sectionTabLabel.text = null
        root.setOnFocusChangeListener(null)
        CardFocusHelper.resetFocus(root)
    }

    private fun applyTabStyle(binding: SectionTabBinding, selected: Boolean) {
        val context = binding.root.context
        if (selected) {
            binding.sectionTabLabel.setBackgroundResource(R.drawable.section_tab_selected_bg)
            binding.sectionTabLabel.setTextColor(ContextCompat.getColor(context, R.color.on_primary))
        } else {
            binding.sectionTabLabel.setBackgroundResource(R.drawable.section_tab_bg)
            binding.sectionTabLabel.setTextColor(ContextCompat.getColor(context, R.color.on_surface))
        }
    }
}
