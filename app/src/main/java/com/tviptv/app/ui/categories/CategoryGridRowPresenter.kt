package com.tviptv.app.ui.categories

import com.tviptv.app.R
import com.tviptv.app.ui.browse.StreamingRowHeaderPresenter
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.FocusHighlightHelper
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter

class CategoryGridRowPresenter : ListRowPresenter() {

    init {
        shadowEnabled = false
        selectEffectEnabled = false
        headerPresenter = StreamingRowHeaderPresenter()
    }

    override fun isUsingDefaultListSelectEffect(): Boolean = false

    override fun initializeRowViewHolder(holder: RowPresenter.ViewHolder) {
        super.initializeRowViewHolder(holder)
        val rowViewHolder = holder as ViewHolder
        val resources = rowViewHolder.view.resources
        val cardHeight = resources.getDimensionPixelSize(R.dimen.card_height_category)
        val focusInset = resources.getDimensionPixelSize(R.dimen.card_focus_inset)
        rowViewHolder.gridView.apply {
            horizontalSpacing = resources.getDimensionPixelSize(R.dimen.row_item_spacing)
            clipChildren = false
            clipToPadding = false
            setRowHeight(cardHeight + focusInset)
            setPadding(
                resources.getDimensionPixelSize(R.dimen.row_padding_horizontal),
                0,
                resources.getDimensionPixelSize(R.dimen.row_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.category_row_padding_bottom),
            )
        }
        FocusHighlightHelper.setupBrowseItemFocusHighlight(
            rowViewHolder.bridgeAdapter,
            FocusHighlight.ZOOM_FACTOR_NONE,
            false,
        )
    }
}
