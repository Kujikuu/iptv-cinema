package com.afifistudio.iptvcinema.ui.common

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.leanback.widget.HorizontalGridView
import androidx.leanback.widget.VerticalGridView

object TvFocusCoordinator {

    fun wireChromeHorizontal(searchView: View, settingsView: View) {
        searchView.nextFocusRightId = settingsView.id
        settingsView.nextFocusLeftId = searchView.id
    }

    fun wireChromeDown(vararg chromeViews: View, requestContentFocus: () -> Boolean) {
        val listener = View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                requestContentFocus()
            } else {
                false
            }
        }
        chromeViews.forEach { view ->
            view.setOnKeyListener(listener)
        }
    }

    fun wireLeanbackRowsUp(
        verticalGridView: VerticalGridView,
        selectedPositionProvider: () -> Int,
        canFocusUpProvider: () -> Boolean,
        requestChromeFocus: () -> Boolean,
    ) {
        verticalGridView.setOnKeyInterceptListener { event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyInterceptListener false
            if (event.keyCode != KeyEvent.KEYCODE_DPAD_UP) return@setOnKeyInterceptListener false
            if (selectedPositionProvider() != 0 || !canFocusUpProvider()) return@setOnKeyInterceptListener false
            requestChromeFocus()
        }
    }

    fun wireGridUp(
        gridView: VerticalGridView,
        columns: Int,
        selectedPositionProvider: () -> Int,
        requestChromeFocus: () -> Boolean,
    ) {
        gridView.setOnKeyInterceptListener { event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyInterceptListener false
            if (event.keyCode != KeyEvent.KEYCODE_DPAD_UP) return@setOnKeyInterceptListener false
            if (selectedPositionProvider() >= columns) return@setOnKeyInterceptListener false
            requestChromeFocus()
        }
    }

    fun wireViewUp(
        view: View,
        canFocusUpProvider: () -> Boolean,
        requestChromeFocus: () -> Boolean,
    ) {
        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_DPAD_UP &&
                canFocusUpProvider()
            ) {
                requestChromeFocus()
            } else {
                false
            }
        }
    }

    fun wireScrollableFocusScroll(
        scrollView: View,
        vararg focusableViews: View,
    ) {
        val listener = View.OnFocusChangeListener { focusedView, hasFocus ->
            if (!hasFocus) return@OnFocusChangeListener
            scrollView.post { scrollToShowView(scrollView, focusedView) }
        }
        focusableViews.forEach { it.onFocusChangeListener = listener }
    }

    fun focusHorizontalGridAt(gridView: HorizontalGridView, index: Int) {
        gridView.post {
            if (gridView.adapter?.itemCount == 0) return@post
            val target = index.coerceIn(0, (gridView.adapter?.itemCount ?: 1) - 1)
            gridView.selectedPosition = target
            gridView.requestFocus()
        }
    }

    fun findHorizontalGridView(root: View?): HorizontalGridView? {
        if (root is HorizontalGridView) return root
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                findHorizontalGridView(root.getChildAt(i))?.let { return it }
            }
        }
        return null
    }

    fun isFirstGridRow(selectedPosition: Int, columns: Int): Boolean =
        selectedPosition < columns.coerceAtLeast(1)

    private fun scrollToShowView(scrollView: View, target: View) {
        when (scrollView) {
            is NestedScrollView -> scrollView.smoothScrollTo(0, computeScrollTop(scrollView, target))
            is ScrollView -> scrollView.smoothScrollTo(0, computeScrollTop(scrollView, target))
        }
    }

    private fun computeScrollTop(scrollView: View, target: View): Int {
        var offset = 0
        var view: View? = target
        while (view != null && view !== scrollView) {
            offset += view.top
            view = view.parent as? View
        }
        val padding = when (scrollView) {
            is NestedScrollView -> scrollView.paddingTop
            is ScrollView -> scrollView.paddingTop
            else -> 0
        }
        return (offset - padding).coerceAtLeast(0)
    }
}
