package com.afifistudio.iptvcinema.ui.categories

import androidx.leanback.widget.ListRow
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.PresenterSelector
import com.afifistudio.iptvcinema.ui.browse.ContinueWatchingRowHelper
import com.afifistudio.iptvcinema.ui.browse.StreamingRowPresenter

class CategoryGridPresenterSelector : PresenterSelector() {

    private val categoryRowPresenter = CategoryGridRowPresenter()
    private val streamingRowPresenter = StreamingRowPresenter()

    override fun getPresenter(item: Any?): Presenter {
        val row = item as? ListRow ?: return categoryRowPresenter
        return if (ContinueWatchingRowHelper.isContinueWatchingRow(row)) {
            streamingRowPresenter
        } else {
            categoryRowPresenter
        }
    }

    override fun getPresenters(): Array<Presenter> =
        arrayOf(categoryRowPresenter, streamingRowPresenter)
}
