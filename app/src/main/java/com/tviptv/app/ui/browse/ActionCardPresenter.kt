package com.tviptv.app.ui.browse

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.tviptv.app.R

class ActionCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            setBackgroundColor(ContextCompat.getColor(context, R.color.surface_elevated))
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card = item as? BrowseItem ?: return
        val cardView = viewHolder.view as ImageCardView
        cardView.titleText = card.title
        cardView.contentText = card.subtitle
        cardView.mainImageView?.setImageResource(R.drawable.card_placeholder)
        cardView.contentDescription = card.title

        cardView.setBackgroundColor(
            ContextCompat.getColor(cardView.context, R.color.surface_elevated),
        )

        cardView.setOnFocusChangeListener { view, hasFocus ->
            CardFocusHelper.applyContentCardFocus(view, hasFocus)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = null
        cardView.setOnFocusChangeListener(null)
        CardFocusHelper.resetFocus(cardView)
    }

    companion object {
        private const val CARD_WIDTH = 200
        private const val CARD_HEIGHT = 112
    }
}
