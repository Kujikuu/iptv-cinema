package com.afifistudio.iptvcinema.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.databinding.CardPosterGridBinding
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.bindContentImage
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.contentTypeForImage

class PosterGridCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = CardPosterGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card = item as? BrowseItem ?: return
        val binding = CardPosterGridBinding.bind(viewHolder.view)
        val context = viewHolder.view.context
        val width = context.resources.getDimensionPixelSize(R.dimen.card_width_poster_grid)
        val imageHeight = context.resources.getDimensionPixelSize(R.dimen.card_poster_image_height)
        val slotHeight = context.resources.getDimensionPixelSize(R.dimen.card_height_poster_grid)
        val focusInset = context.resources.getDimensionPixelSize(R.dimen.card_focus_inset)

        viewHolder.view.layoutParams = viewHolder.view.layoutParams.apply {
            this.width = width
            this.height = slotHeight + focusInset
        }
        (binding.posterFocusTarget.layoutParams as ViewGroup.MarginLayoutParams).apply {
            this.width = width
            this.height = imageHeight + focusInset / 2
            topMargin = focusInset / 2
        }
        binding.cardContainer.layoutParams = binding.cardContainer.layoutParams.apply {
            this.width = width
            this.height = imageHeight
        }

        binding.cardTitle.text = card.title

        if (card.badge.isNullOrBlank()) {
            binding.cardBadge.isVisible = false
        } else {
            binding.cardBadge.isVisible = true
            binding.cardBadge.text = card.badge
        }

        binding.cardFavorite.isVisible = card.isFavorite

        binding.cardImage.bindContentImage(card.imageUrl, card.contentTypeForImage())

        binding.posterFocusTarget.contentDescription = listOfNotNull(card.title, card.badge).joinToString(", ")

        viewHolder.view.setOnFocusChangeListener { _, hasFocus ->
            CardFocusHelper.applyPosterGridFocus(binding.cardContainer, hasFocus)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val binding = CardPosterGridBinding.bind(viewHolder.view)
        binding.cardImage.setImageDrawable(null)
        binding.cardTitle.text = null
        binding.cardBadge.isVisible = false
        binding.cardFavorite.isVisible = false
        viewHolder.view.setOnFocusChangeListener(null)
        CardFocusHelper.resetFocus(binding.cardContainer)
    }
}
