package com.tviptv.app.ui.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import com.tviptv.app.R
import com.tviptv.app.databinding.CardCategoryBinding
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.ui.common.ContentImageBindings.bindContentImage

class CategoryCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = CardCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card = item as? BrowseItem ?: return
        val root = viewHolder.view
        val binding = CardCategoryBinding.bind(root)
        val context = root.context

        val width = context.resources.getDimensionPixelSize(R.dimen.card_width_category)
        val height = context.resources.getDimensionPixelSize(R.dimen.card_height_category)
        root.layoutParams = root.layoutParams.apply {
            this.width = width
            this.height = height
        }
        binding.categoryCardContainer.layoutParams = binding.categoryCardContainer.layoutParams.apply {
            this.width = width
            this.height = height
        }

        binding.categoryName.text = card.title
        binding.categoryCount.text = card.channelCount?.let { count ->
            context.getString(R.string.category_count, count)
        }.orEmpty()
        binding.categoryCount.isVisible = card.channelCount != null

        val contentType = card.category?.contentType
        if (contentType != null) {
            binding.categoryTypeBadge.isVisible = true
            binding.categoryTypeBadge.text = typeBadgeLabel(context, contentType)
        } else {
            binding.categoryTypeBadge.isVisible = false
        }

        bindPreview(binding, card.previewImageUrls.firstOrNull() ?: card.imageUrl, contentType ?: ContentType.LIVE)

        binding.categoryHint.visibility = View.INVISIBLE
        binding.categoryAccentBar.setBackgroundResource(R.drawable.category_accent_bar_muted)

        root.contentDescription = listOfNotNull(
            card.title,
            binding.categoryCount.text?.toString(),
            binding.categoryTypeBadge.takeIf { it.isVisible }?.text?.toString(),
        ).joinToString(", ")

        root.setOnFocusChangeListener { view, hasFocus ->
            CardFocusHelper.applyCategoryFocus(view, binding.categoryAccentBar, binding.categoryHint, hasFocus)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val root = viewHolder.view
        val binding = CardCategoryBinding.bind(root)
        binding.categoryPreview.setImageDrawable(null)
        binding.categoryName.text = null
        binding.categoryCount.text = null
        binding.categoryTypeBadge.isVisible = false
        binding.categoryHint.visibility = View.INVISIBLE
        binding.categoryAccentBar.setBackgroundResource(R.drawable.category_accent_bar_muted)
        root.setOnFocusChangeListener(null)
        CardFocusHelper.resetFocus(root)
    }

    private fun bindPreview(binding: CardCategoryBinding, url: String?, contentType: ContentType) {
        binding.categoryPreview.bindContentImage(url, contentType)
        binding.categoryPreview.alpha = 1f
    }

    private fun typeBadgeLabel(context: android.content.Context, contentType: ContentType): String =
        when (contentType) {
            ContentType.LIVE -> context.getString(R.string.live_badge)
            ContentType.MOVIE -> context.getString(R.string.movie_badge)
            ContentType.SERIES -> context.getString(R.string.series_badge)
            ContentType.EPISODE -> context.getString(R.string.episode_badge)
        }
}
