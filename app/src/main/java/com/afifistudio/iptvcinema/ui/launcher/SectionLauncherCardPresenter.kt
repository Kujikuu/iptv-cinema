package com.afifistudio.iptvcinema.ui.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.databinding.CardSectionLauncherBinding
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.bindContentImage
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.toContentTypeForImage

class SectionLauncherCardPresenter(
    private val onItemClick: (SectionLauncherItem) -> Unit,
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

        if (card.lastUpdateLabel.isNullOrBlank()) {
            binding.sectionUpdateRow.isVisible = false
        } else {
            binding.sectionUpdateRow.isVisible = true
            binding.sectionUpdateBadge.text = card.lastUpdateLabel
        }

        binding.sectionPreview.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        binding.sectionPreview.bindContentImage(
            card.previewImageUrl,
            card.section.toContentTypeForImage(),
        )
        binding.sectionPreview.alpha = 1f

        binding.sectionFocusBar.alpha = 0f
        binding.sectionCardContainer.foreground = null
        root.contentDescription = listOfNotNull(card.title, card.countLabel, card.lastUpdateLabel)
            .joinToString(", ")

        root.nextFocusDownId = R.id.continue_watching_grid

        root.setOnFocusChangeListener { view, hasFocus ->
            val scale = if (hasFocus) FOCUS_SCALE else 1f
            view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(FOCUS_MS)
                .start()
            binding.sectionFocusBar.animate()
                .alpha(if (hasFocus) 1f else 0f)
                .setDuration(FOCUS_MS)
                .start()
            binding.sectionCardContainer.foreground = if (hasFocus) {
                ContextCompat.getDrawable(context, R.drawable.launcher_card_border)
            } else {
                null
            }
        }

        root.setOnClickListener { onItemClick(card) }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val root = viewHolder.view
        val binding = CardSectionLauncherBinding.bind(root)
        binding.sectionPreview.setImageDrawable(null)
        binding.sectionTitleIcon.setImageDrawable(null)
        binding.sectionTitle.text = null
        binding.sectionCount.text = null
        binding.sectionUpdateRow.isVisible = false
        binding.sectionFocusBar.alpha = 0f
        binding.sectionCardContainer.foreground = null
        root.scaleX = 1f
        root.scaleY = 1f
        root.setOnFocusChangeListener(null)
        root.setOnClickListener(null)
    }

    companion object {
        private const val FOCUS_SCALE = 1.04f
        private const val FOCUS_MS = 150L
    }
}
