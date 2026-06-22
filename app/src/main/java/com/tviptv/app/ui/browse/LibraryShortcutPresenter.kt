package com.tviptv.app.ui.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.tviptv.app.R
import com.tviptv.app.databinding.CardLibraryShortcutBinding
import com.tviptv.app.ui.common.SectionIcons

class LibraryShortcutPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = CardLibraryShortcutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card = item as? BrowseItem ?: return
        val root = viewHolder.view
        val binding = CardLibraryShortcutBinding.bind(root)

        binding.libraryTitle.text = card.title
        binding.librarySubtitle.text = card.subtitle.orEmpty()
        binding.libraryIcon.setImageResource(
            card.section?.let { SectionIcons.forSection(it) } ?: R.drawable.ic_material_movie,
        )
        binding.libraryHint.visibility = View.INVISIBLE

        root.contentDescription = listOfNotNull(card.title, card.subtitle).joinToString(", ")

        root.setOnFocusChangeListener { view, hasFocus ->
            CardFocusHelper.applyContentCardFocus(view, hasFocus)
            binding.libraryHint.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val root = viewHolder.view
        val binding = CardLibraryShortcutBinding.bind(root)
        binding.libraryTitle.text = null
        binding.librarySubtitle.text = null
        binding.libraryHint.visibility = View.INVISIBLE
        root.setOnFocusChangeListener(null)
        CardFocusHelper.resetFocus(root)
    }
}
