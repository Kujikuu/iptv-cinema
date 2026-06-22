package com.afifistudio.iptvcinema.ui.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.databinding.CardCategoryBinding
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.ImageRequestSize
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.bindContentImage

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

        bindPreview(
            binding = binding,
            url = card.previewImageUrls.firstOrNull() ?: card.imageUrl,
            contentType = contentType ?: ContentType.LIVE,
            requestSize = ImageRequestSize(width, height),
        )

        binding.categoryHint.visibility = View.INVISIBLE
        binding.categoryAccentBar.setBackgroundResource(R.drawable.category_accent_bar_muted)
        binding.categoryAccentBar.alpha = 0.55f

        root.contentDescription = listOfNotNull(
            card.title,
            binding.categoryCount.text?.toString(),
            binding.categoryTypeBadge.takeIf { it.isVisible }?.text?.toString(),
        ).joinToString(", ")

        root.setOnFocusChangeListener { view, hasFocus ->
            CardFocusHelper.applyCategoryFocus(view, binding.categoryAccentBar, binding.categoryHint, hasFocus)
            binding.categoryPreview.animate()
                .scaleX(if (hasFocus) IMAGE_FOCUS_SCALE else 1f)
                .scaleY(if (hasFocus) IMAGE_FOCUS_SCALE else 1f)
                .setDuration(CardFocusHelper.FOCUS_ANIMATION_MS)
                .start()
            binding.categoryCount.visibility = if (hasFocus) View.GONE else (if (card.channelCount != null) View.VISIBLE else View.GONE)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val root = viewHolder.view
        val binding = CardCategoryBinding.bind(root)
        binding.categoryPreview.setImageDrawable(null)
        binding.categoryName.text = null
        binding.categoryCount.text = null
        binding.categoryCount.visibility = View.VISIBLE
        binding.categoryTypeBadge.isVisible = false
        binding.categoryHint.visibility = View.INVISIBLE
        binding.categoryAccentBar.setBackgroundResource(R.drawable.category_accent_bar_muted)
        binding.categoryAccentBar.alpha = 0.55f
        binding.categoryPreview.scaleX = 1f
        binding.categoryPreview.scaleY = 1f
        root.setOnFocusChangeListener(null)
        CardFocusHelper.resetFocus(root)
    }

    private fun bindPreview(
        binding: CardCategoryBinding,
        url: String?,
        contentType: ContentType,
        requestSize: ImageRequestSize,
    ) {
        binding.categoryPreview.bindContentImage(url, contentType, requestSize = requestSize)
        binding.categoryPreview.alpha = 1f
    }

    private fun typeBadgeLabel(context: android.content.Context, contentType: ContentType): String =
        when (contentType) {
            ContentType.LIVE -> context.getString(R.string.live_badge)
            ContentType.MOVIE -> context.getString(R.string.movie_badge)
            ContentType.SERIES -> context.getString(R.string.series_badge)
            ContentType.EPISODE -> context.getString(R.string.episode_badge)
        }

    companion object {
        private const val IMAGE_FOCUS_SCALE = 1.035f
    }
}
