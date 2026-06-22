package com.afifistudio.iptvcinema.ui.common

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

class TvNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    override fun computeScrollDeltaToGetChildRectOnScreen(rect: Rect): Int {
        // Return 0 to prevent NestedScrollView from automatically scrolling to focused children.
        // We handle scrolling manually.
        return 0
    }
}
