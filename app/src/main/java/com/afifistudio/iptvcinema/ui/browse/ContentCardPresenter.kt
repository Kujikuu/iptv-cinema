package com.afifistudio.iptvcinema.ui.browse

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.databinding.CardContentBinding
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.ImageRequestSize
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.bindContentImage
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.contentTypeForImage

class ContentCardPresenter(
    private val onLongPress: ((BrowseItem) -> Unit)? = null,
    private val onClick: ((BrowseItem) -> Unit)? = null,
) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = CardContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card = item as? BrowseItem ?: return
        val root = viewHolder.view
        val binding = CardContentBinding.bind(root)
        val context = root.context

        val widthRes = card.cardWidthRes
            ?: if (card.usePosterLayout) R.dimen.card_width_poster else R.dimen.card_width_standard
        val heightRes = card.cardHeightRes
            ?: if (card.usePosterLayout) R.dimen.card_height_poster else R.dimen.card_height_standard
        val width = context.resources.getDimensionPixelSize(widthRes)
        val height = context.resources.getDimensionPixelSize(heightRes)
        val isLauncherCwCard = card.cardHeightRes == R.dimen.launcher_cw_card_height
        val focusInset = context.resources.getDimensionPixelSize(
            if (isLauncherCwCard) R.dimen.launcher_cw_focus_inset else R.dimen.card_focus_inset,
        )
        val slotWidth = width + focusInset
        val slotHeight = height + focusInset
        val focusScale = if (isLauncherCwCard) 1.04f else CardFocusHelper.CONTENT_FOCUS_SCALE

        root.layoutParams = root.layoutParams.apply {
            this.width = slotWidth
            this.height = slotHeight
        }
        binding.cardContainer.layoutParams = FrameLayout.LayoutParams(width, height).apply {
            gravity = Gravity.CENTER
        }

        binding.cardTitle.text = card.title
        binding.cardTitle.maxLines = if (isLauncherCwCard) 1 else 2
        val subtitleText = card.progressLabel ?: card.subtitle.orEmpty()
        binding.cardSubtitle.text = subtitleText
        binding.cardSubtitle.isVisible = subtitleText.isNotBlank()

        val density = context.resources.displayMetrics.density
        val hasFocusNow = root.hasFocus()
        binding.cardSubtitle.alpha = if (hasFocusNow) 1f else 0f
        binding.cardSubtitle.translationY = if (hasFocusNow) 0f else 8f * density
        binding.cardImage.scaleX = if (hasFocusNow) IMAGE_FOCUS_SCALE else 1f
        binding.cardImage.scaleY = if (hasFocusNow) IMAGE_FOCUS_SCALE else 1f

        bindWatchProgress(binding, card)

        if (card.badge.isNullOrBlank()) {
            binding.cardBadge.isVisible = false
        } else {
            binding.cardBadge.isVisible = true
            binding.cardBadge.text = card.badge
            val badgeBg = if (card.channel?.contentType == ContentType.LIVE) {
                R.drawable.live_badge_chip_bg
            } else {
                R.drawable.badge_chip_bg
            }
            binding.cardBadge.setBackgroundResource(badgeBg)
            binding.cardBadge.alpha = if (hasFocusNow) 1f else 0.5f
        }

        binding.cardFavorite.isVisible = card.isFavorite
        binding.cardFavorite.alpha = if (hasFocusNow) 1f else 0.88f

        binding.cardImage.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.cardImage.bindContentImage(
            card.imageUrl,
            card.contentTypeForImage(),
            requestSize = ImageRequestSize(width, height),
        )

        root.contentDescription = listOfNotNull(card.title, card.subtitle, card.badge).joinToString(", ")

        root.setOnFocusChangeListener { _, hasFocus ->
            CardFocusHelper.applyContentCardFocus(binding.cardContainer, hasFocus, focusScale)
            binding.cardImage.animate()
                .scaleX(if (hasFocus) IMAGE_FOCUS_SCALE else 1f)
                .scaleY(if (hasFocus) IMAGE_FOCUS_SCALE else 1f)
                .setDuration(CardFocusHelper.FOCUS_ANIMATION_MS)
                .start()
            if (subtitleText.isNotBlank()) {
                val targetAlpha = if (hasFocus) 1f else 0f
                val targetTranslation = if (hasFocus) 0f else 8f * density
                binding.cardSubtitle.animate()
                    .alpha(targetAlpha)
                    .translationY(targetTranslation)
                    .setDuration(150)
                    .start()
            }
            if (!card.badge.isNullOrBlank()) {
                binding.cardBadge.animate()
                    .alpha(if (hasFocus) 1f else 0.5f)
                    .setDuration(150)
                    .start()
            }
            if (card.isFavorite) {
                binding.cardFavorite.animate()
                    .alpha(if (hasFocus) 1f else 0.88f)
                    .setDuration(CardFocusHelper.FOCUS_ANIMATION_MS)
                    .start()
            }
        }

        root.setOnLongClickListener {
            if (card.type == BrowseItemType.CHANNEL) {
                onLongPress?.invoke(card)
                true
            } else {
                false
            }
        }

        root.setOnClickListener {
            onClick?.invoke(card)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val root = viewHolder.view
        val binding = CardContentBinding.bind(root)
        binding.cardImage.setImageDrawable(null)
        binding.cardTitle.text = null
        binding.cardSubtitle.text = null
        binding.cardBadge.isVisible = false
        binding.cardFavorite.isVisible = false
        binding.cardProgressContainer.isVisible = false
        binding.cardImage.scaleX = 1f
        binding.cardImage.scaleY = 1f
        binding.cardFavorite.alpha = 1f
        root.setOnLongClickListener(null)
        root.setOnClickListener(null)
        root.setOnFocusChangeListener(null)
        CardFocusHelper.resetFocus(binding.cardContainer)
    }

    private fun bindWatchProgress(binding: CardContentBinding, card: BrowseItem) {
        val contentType = card.channel?.contentType
        val fraction = card.progressFraction
        val showProgress = fraction != null &&
            contentType != ContentType.LIVE &&
            (contentType == ContentType.MOVIE || contentType == ContentType.EPISODE)
        if (!showProgress) {
            binding.cardProgressContainer.isVisible = false
            return
        }
        binding.cardProgressContainer.isVisible = true
        binding.cardProgressContainer.post {
            val containerWidth = binding.cardProgressContainer.width
            if (containerWidth <= 0) return@post
            val fillWidth = (containerWidth * fraction!!).toInt()
                .coerceIn(1, containerWidth)
            binding.cardProgressFill.layoutParams =
                binding.cardProgressFill.layoutParams.apply {
                    width = fillWidth
            }
        }
    }

    companion object {
        private const val IMAGE_FOCUS_SCALE = 1.035f
    }
}
