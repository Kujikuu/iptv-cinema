package com.tviptv.app.ui.browse

import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow

object BrowseSectionRowHelper {

    fun buildSectionRow(
        state: BrowseUiState,
        sectionTabPresenter: SectionTabPresenter,
        sectionsHeader: String,
        sectionTitleProvider: (BrowseSection) -> String,
        sectionCountLabelProvider: (BrowseSection, BrowseUiState) -> String?,
    ): ListRow {
        val adapter = ArrayObjectAdapter(sectionTabPresenter)
        BrowseSection.entries.forEach { section ->
            adapter.add(
                BrowseItem(
                    id = "section_${section.name}",
                    title = sectionTitleProvider(section),
                    subtitle = sectionCountLabelProvider(section, state),
                    type = BrowseItemType.SECTION,
                    section = section,
                    selected = state.selectedSection == section,
                ),
            )
        }
        return ListRow(HeaderItem(SECTIONS_HEADER_ID, sectionsHeader), adapter)
    }

    const val SECTIONS_HEADER_ID = 105L
}
