package com.tviptv.app.ui.browse

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import com.tviptv.app.R
import com.tviptv.app.databinding.CardContentBinding
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.ui.common.ContentImageBindings.bindContentImage
import com.tviptv.app.ui.common.ContentImageBindings.contentTypeForImage

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
        }

        binding.cardFavorite.isVisible = card.isFavorite

        binding.cardImage.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.cardImage.bindContentImage(card.imageUrl, card.contentTypeForImage())

        root.contentDescription = listOfNotNull(card.title, card.subtitle, card.badge).joinToString(", ")

        root.setOnFocusChangeListener { _, hasFocus ->
            CardFocusHelper.applyContentCardFocus(binding.cardContainer, hasFocus, focusScale)
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
}
