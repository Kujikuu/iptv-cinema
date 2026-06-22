package com.afifistudio.iptvcinema.ui.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.databinding.CardSectionLauncherBinding
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.ImageRequestSize
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.bindContentImage
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.toContentTypeForImage

class SectionLauncherCardPresenter(
    private val onItemClick: (SectionLauncherItem) -> Unit,
    private val onItemLongPress: (SectionLauncherItem) -> Unit,
) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = CardSectionLauncherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card = item as? SectionLauncherItem ?: return
        val root = viewHolder.view
        val binding = CardSectionLauncherBinding.bind(root)
        val context = root.context

        val width = context.resources.getDimensionPixelSize(R.dimen.launcher_card_width)
        val height = context.resources.getDimensionPixelSize(R.dimen.launcher_card_height)
        root.layoutParams = root.layoutParams.apply {
            this.width = width
            this.height = height
        }

        binding.sectionTitleIcon.setImageResource(card.iconRes)
        binding.sectionTitle.text = card.title
        binding.sectionCount.text = card.countLabel

        val statusLabel = when (card.refreshStatus) {
            SectionRefreshStatus.IDLE -> card.lastUpdateLabel
            SectionRefreshStatus.REFRESHING -> context.getString(R.string.section_refreshing)
            SectionRefreshStatus.SUCCESS -> context.getString(R.string.section_updated_now)
            SectionRefreshStatus.ERROR -> context.getString(R.string.section_refresh_failed)
        }
        if (statusLabel.isNullOrBlank()) {
            binding.sectionUpdateRow.isVisible = false
        } else {
            binding.sectionUpdateRow.isVisible = true
            binding.sectionUpdateBadge.text = statusLabel
        }
        binding.sectionUpdateIcon.isVisible = card.refreshStatus != SectionRefreshStatus.REFRESHING
        binding.sectionUpdateProgress.isVisible = card.refreshStatus == SectionRefreshStatus.REFRESHING

        binding.sectionPreview.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        binding.sectionPreview.bindContentImage(
            card.previewImageUrl,
            card.section.toContentTypeForImage(),
            requestSize = ImageRequestSize(width, height),
        )
        binding.sectionPreview.alpha = 1f
        binding.sectionPreview.scaleX = 1f
        binding.sectionPreview.scaleY = 1f

        binding.sectionFocusBar.alpha = 0f
        binding.sectionCardContainer.foreground = null
        root.contentDescription = listOfNotNull(
            card.title,
            card.countLabel,
            statusLabel,
            card.disabledMessage,
            context.getString(R.string.section_refresh_hint),
        )
            .joinToString(", ")

        root.nextFocusDownId = R.id.continue_watching_grid

        root.setOnFocusChangeListener { view, hasFocus ->
            val scale = if (hasFocus) FOCUS_SCALE else 1f
            view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(FOCUS_MS)
                .start()
            binding.sectionPreview.animate()
                .scaleX(if (hasFocus) IMAGE_FOCUS_SCALE else 1f)
                .scaleY(if (hasFocus) IMAGE_FOCUS_SCALE else 1f)
                .setDuration(FOCUS_MS)
                .start()
            binding.sectionFocusBar.animate()
                .alpha(if (hasFocus) 1f else 0f)
                .setDuration(FOCUS_MS)
                .start()
            binding.sectionUpdateRow.animate()
                .alpha(if (hasFocus) 1f else 0.76f)
                .setDuration(FOCUS_MS)
                .start()
            binding.sectionCardContainer.foreground = if (hasFocus) {
                ContextCompat.getDrawable(context, R.drawable.launcher_card_border)
            } else {
                null
            }
        }

        root.setOnClickListener { onItemClick(card) }
        root.setOnLongClickListener {
            onItemLongPress(card)
            true
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val root = viewHolder.view
        val binding = CardSectionLauncherBinding.bind(root)
        binding.sectionPreview.setImageDrawable(null)
        binding.sectionTitleIcon.setImageDrawable(null)
        binding.sectionTitle.text = null
        binding.sectionCount.text = null
        binding.sectionUpdateRow.isVisible = false
        binding.sectionUpdateIcon.isVisible = true
        binding.sectionUpdateProgress.isVisible = false
        binding.sectionFocusBar.alpha = 0f
        binding.sectionCardContainer.foreground = null
        binding.sectionPreview.scaleX = 1f
        binding.sectionPreview.scaleY = 1f
        binding.sectionUpdateRow.alpha = 1f
        root.scaleX = 1f
        root.scaleY = 1f
        root.setOnFocusChangeListener(null)
        root.setOnClickListener(null)
        root.setOnLongClickListener(null)
    }

    companion object {
        private const val FOCUS_SCALE = 1.055f
        private const val IMAGE_FOCUS_SCALE = 1.045f
        private const val FOCUS_MS = 170L
    }
}
