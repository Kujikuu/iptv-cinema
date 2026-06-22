package com.afifistudio.iptvcinema.ui.browse

import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter

class StreamingRowPresenter : ListRowPresenter() {

    init {
        selectEffectEnabled = false
        headerPresenter = StreamingRowHeaderPresenter()
    }

    override fun isUsingDefaultListSelectEffect(): Boolean = false

    override fun initializeRowViewHolder(holder: RowPresenter.ViewHolder) {
        super.initializeRowViewHolder(holder)
        val rowViewHolder = holder as ViewHolder
        rowViewHolder.gridView.horizontalSpacing =
            rowViewHolder.view.resources.getDimensionPixelSize(com.afifistudio.iptvcinema.R.dimen.row_item_spacing)
        rowViewHolder.gridView.setPadding(
            rowViewHolder.view.resources.getDimensionPixelSize(com.afifistudio.iptvcinema.R.dimen.row_padding_horizontal),
            rowViewHolder.gridView.paddingTop,
            rowViewHolder.view.resources.getDimensionPixelSize(com.afifistudio.iptvcinema.R.dimen.row_padding_horizontal),
            rowViewHolder.gridView.paddingBottom,
        )
    }
}
