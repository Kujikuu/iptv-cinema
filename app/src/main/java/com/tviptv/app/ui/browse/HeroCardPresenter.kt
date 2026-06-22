package com.tviptv.app.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import com.tviptv.app.R
import com.tviptv.app.databinding.CardHeroCinematicBinding
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.ui.common.ContentImageBindings.bindContentImage
import com.tviptv.app.ui.common.ContentImageBindings.contentTypeForImage

class HeroCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = CardHeroCinematicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card = item as? BrowseItem ?: return
        val root = viewHolder.view
        val binding = CardHeroCinematicBinding.bind(root)
        val context = root.context

        val widthRes = if (card.usePosterLayout) R.dimen.card_width_poster else R.dimen.card_width_hero
        val heightRes = if (card.usePosterLayout) R.dimen.card_height_poster else R.dimen.card_height_hero
        val width = if (card.usePosterLayout) {
            context.resources.getDimensionPixelSize(widthRes)
        } else {
            context.resources.getDimensionPixelSize(R.dimen.card_width_hero)
        }
        val height = if (card.usePosterLayout) {
            context.resources.getDimensionPixelSize(heightRes)
        } else {
            context.resources.getDimensionPixelSize(R.dimen.card_height_hero)
        }

        binding.heroCardContainer.layoutParams = binding.heroCardContainer.layoutParams.apply {
            this.width = width
            this.height = height
        }
        root.layoutParams = root.layoutParams.apply {
            this.width = width
            this.height = height
        }

        binding.heroTitle.text = card.title
        binding.heroSubtitle.text = card.subtitle.orEmpty()
        binding.heroSubtitle.isVisible = !card.subtitle.isNullOrBlank()

        if (card.channel?.contentType == ContentType.LIVE) {
            binding.heroBadge.isVisible = true
            binding.heroBadge.text = card.badge ?: context.getString(R.string.live_badge)
        } else {
            binding.heroBadge.isVisible = !card.badge.isNullOrBlank()
            binding.heroBadge.text = card.badge.orEmpty()
            binding.heroBadge.setBackgroundResource(R.drawable.badge_chip_bg)
        }

        binding.heroImage.bindContentImage(card.imageUrl, card.contentTypeForImage())

        root.contentDescription = "${card.title}, ${card.subtitle}"

        binding.heroHint.visibility = android.view.View.INVISIBLE
        root.setOnFocusChangeListener { view, hasFocus ->
            CardFocusHelper.applyHeroFocus(view, binding.heroImage, hasFocus)
            binding.heroHint.visibility = if (hasFocus) android.view.View.VISIBLE else android.view.View.INVISIBLE
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val root = viewHolder.view
        val binding = CardHeroCinematicBinding.bind(root)
        binding.heroImage.setImageDrawable(null)
        binding.heroTitle.text = null
        binding.heroSubtitle.text = null
        binding.heroBadge.isVisible = false
        binding.heroHint.visibility = android.view.View.INVISIBLE
        binding.heroImage.scaleX = 1f
        binding.heroImage.scaleY = 1f
        root.setOnFocusChangeListener(null)
        CardFocusHelper.resetFocus(root)
    }
}
