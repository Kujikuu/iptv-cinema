package com.tviptv.app.ui.browse

import android.view.View
import androidx.core.content.ContextCompat
import com.tviptv.app.R

object CardFocusHelper {

    const val CONTENT_FOCUS_SCALE = 1.06f
    private const val FOCUS_SCALE = CONTENT_FOCUS_SCALE
    private const val HERO_FOCUS_SCALE = 1.02f
    private const val FOCUS_ANIMATION_MS = 150L

    fun applyContentCardFocus(view: View, hasFocus: Boolean, scale: Float = FOCUS_SCALE) {
        applyFocus(view, hasFocus, scale)
    }

    fun applyPosterGridFocus(view: View, hasFocus: Boolean) {
        applyFocus(view, hasFocus, 1.05f)
    }

    fun applyHeroFocus(view: View, imageView: View, hasFocus: Boolean) {
        applyFocus(view, hasFocus, FOCUS_SCALE)
        val imageScale = if (hasFocus) HERO_FOCUS_SCALE else 1f
        imageView.animate()
            .scaleX(imageScale)
            .scaleY(imageScale)
            .setDuration(FOCUS_ANIMATION_MS)
            .start()
    }

    fun applyCategoryFocus(view: View, accentBar: View, hintView: View, hasFocus: Boolean) {
        applyFocus(view, hasFocus, FOCUS_SCALE)
        if (hasFocus) {
            accentBar.setBackgroundResource(R.drawable.category_accent_bar)
            hintView.visibility = View.VISIBLE
        } else {
            accentBar.setBackgroundResource(R.drawable.category_accent_bar_muted)
            hintView.visibility = View.INVISIBLE
        }
    }

    fun applySectionTabFocus(view: View, hasFocus: Boolean) {
        view.foreground = if (hasFocus) {
            ContextCompat.getDrawable(view.context, R.drawable.card_focus_glow)
        } else {
            null
        }
    }

    private fun applyFocus(view: View, hasFocus: Boolean, scale: Float) {
        val targetScale = if (hasFocus) scale else 1f
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(FOCUS_ANIMATION_MS)
            .start()
        view.elevation = if (hasFocus) {
            view.resources.getDimension(R.dimen.card_focus_elevation)
        } else {
            0f
        }
        view.foreground = if (hasFocus) {
            ContextCompat.getDrawable(view.context, R.drawable.card_focus_glow)
        } else {
            null
        }
    }

    fun resetFocus(view: View) {
        view.foreground = null
        view.elevation = 0f
        view.scaleX = 1f
        view.scaleY = 1f
    }
}
