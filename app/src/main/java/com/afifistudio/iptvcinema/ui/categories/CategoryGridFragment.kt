package com.afifistudio.iptvcinema.ui.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.ContinueWatchingItem
import com.afifistudio.iptvcinema.ui.ContentFocusHandler
import com.afifistudio.iptvcinema.ui.HomeChromeHost
import com.afifistudio.iptvcinema.ui.HomeChromeMode
import com.afifistudio.iptvcinema.ui.common.TvFocusCoordinator
import com.afifistudio.iptvcinema.ui.registerContentFocusHandler
import com.afifistudio.iptvcinema.ui.requestChromeFocus
import com.afifistudio.iptvcinema.ui.unregisterContentFocusHandler
import com.afifistudio.iptvcinema.ui.browse.ActionCardPresenter
import com.afifistudio.iptvcinema.ui.browse.BrowseItem
import com.afifistudio.iptvcinema.ui.browse.BrowseItemType
import com.afifistudio.iptvcinema.ui.browse.BrowseSection
import com.afifistudio.iptvcinema.ui.browse.BrowseSectionRowHelper
import com.afifistudio.iptvcinema.ui.browse.BrowseUiState
import com.afifistudio.iptvcinema.ui.browse.BrowseViewModel
import com.afifistudio.iptvcinema.ui.browse.CategoryCardPresenter
import com.afifistudio.iptvcinema.ui.browse.ContinueWatchingNavigator
import com.afifistudio.iptvcinema.ui.browse.ContinueWatchingRowHelper
import com.afifistudio.iptvcinema.ui.browse.ContentCardPresenter
import com.afifistudio.iptvcinema.ui.browse.SectionTabPresenter
import com.afifistudio.iptvcinema.ui.browse.toBrowseItem
import com.afifistudio.iptvcinema.data.cache.CategoryPrefetcher
import com.afifistudio.iptvcinema.ui.replaceContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CategoryGridFragment : RowsSupportFragment() {

    @Inject lateinit var categoryPrefetcher: CategoryPrefetcher
    @Inject lateinit var continueWatchingNavigator: ContinueWatchingNavigator

    private val browseViewModel: BrowseViewModel by viewModels({ requireActivity() })
    private var fromLauncher: Boolean = false
    private lateinit var rowsAdapter: ArrayObjectAdapter
    private val presenterSelector = CategoryGridPresenterSelector()
    private val categoryCardPresenter = CategoryCardPresenter()
    private val contentCardPresenter = ContentCardPresenter()
    private val actionCardPresenter = ActionCardPresenter()
    private val sectionTabPresenter = SectionTabPresenter()
    private var renderedFingerprint: String? = null
    private var sectionContinueWatching: List<ContinueWatchingItem> = emptyList()
    private var hasInitialFocus = false

    private val contentFocusHandler = object : ContentFocusHandler {
        override fun requestInitialFocus(): Boolean {
            val grid = verticalGridView ?: return false
            if (adapter == null || adapter.size() == 0) return false
            setSelectedPosition(0, false)
            return grid.requestFocus()
        }

        override fun canFocusUpToChrome(): Boolean = selectedPosition == 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fromLauncher = arguments?.getBoolean(ARG_FROM_LAUNCHER, false) ?: false
        applyBrowseInsets(view)
        setupUi()
        wireChromeFocusBridge()
        observeState()
        applyBrowseChrome()
    }

    override fun onResume() {
        super.onResume()
        registerContentFocusHandler(contentFocusHandler)
        browseViewModel.refreshContinueWatching()
        applyBrowseChrome()
    }

    override fun onPause() {
        unregisterContentFocusHandler()
        super.onPause()
    }

    override fun onDestroyView() {
        renderedFingerprint = null
        hasInitialFocus = false
        super.onDestroyView()
    }

    private fun wireChromeFocusBridge() {
        TvFocusCoordinator.wireLeanbackRowsUp(
            verticalGridView = verticalGridView,
            selectedPositionProvider = { selectedPosition },
            canFocusUpProvider = { selectedPosition == 0 },
            requestChromeFocus = { requestChromeFocus() },
        )
    }

    private fun applyBrowseInsets(view: View) {
        view.setPadding(
            0,
            resources.getDimensionPixelSize(R.dimen.browse_content_top_padding),
            0,
            resources.getDimensionPixelSize(R.dimen.browse_content_bottom_padding),
        )
    }

    private fun applyBrowseChrome() {
        (activity as? HomeChromeHost)?.apply {
            setChromeVisible(true)
            setChromeMode(HomeChromeMode.BROWSE)
        }
    }

    private fun setupUi() {
        rowsAdapter = ArrayObjectAdapter(presenterSelector)
        adapter = rowsAdapter

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            handleItemClick(item)
        }

        onItemViewSelectedListener =
            androidx.leanback.widget.OnItemViewSelectedListener { _, item, _, _ ->
                val browseItem = item as? BrowseItem ?: return@OnItemViewSelectedListener
                browseItem.category?.let { category ->
                    categoryPrefetcher.prefetch(
                        category,
                        browseViewModel.uiState.value.sourceUpdatedAt,
                    )
                }
            }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                browseViewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: BrowseUiState) {
        if (state.sources.isEmpty()) {
            return
        }

        val fingerprint = buildGridFingerprint(state)
        if (fingerprint == renderedFingerprint) {
            return
        }
        renderedFingerprint = fingerprint

        sectionContinueWatching = browseViewModel.continueWatchingForSection(state.selectedSection)
        val newAdapter = ArrayObjectAdapter(presenterSelector)
        if (!fromLauncher) {
            newAdapter.add(buildSectionRow(state))
        }

        if (sectionContinueWatching.isNotEmpty()) {
            val favoriteIds = state.favorites.map { it.id }.toSet()
            newAdapter.add(
                ContinueWatchingRowHelper.buildRow(
                    context = requireContext(),
                    header = getString(R.string.continue_watching_row),
                    items = sectionContinueWatching,
                    favoriteIds = favoriteIds,
                    totalCount = state.continueWatching.size,
                    rowLimit = CONTINUE_WATCHING_ROW_LIMIT,
                    contentCardPresenter = contentCardPresenter,
                ),
            )
        }

        if (state.isLoading && state.categorySummaries.isEmpty()) {
            newAdapter.add(buildLoadingRow())
        } else {
            renderCategoryRows(state, state.selectedSection, newAdapter)

            if (state.error != null && state.categorySummaries.isEmpty()) {
                newAdapter.add(buildErrorRow(state.error))
            } else if (state.categorySummaries.isEmpty()) {
                newAdapter.add(buildEmptyRow())
            }
        }

        rowsAdapter = newAdapter
        adapter = newAdapter

        if (newAdapter.size() > 0 && !hasInitialFocus) {
            view?.post {
                setSelectedPosition(0, false)
                verticalGridView?.requestFocus()
            }
            hasInitialFocus = true
        }
    }

    private fun buildGridFingerprint(state: BrowseUiState): String = buildString {
        append(fromLauncher)
        append('|')
        append(state.selectedSection.name)
        append('|')
        append(state.isLoading)
        append('|')
        append(state.error)
        append('|')
        append(state.liveCount)
        append('|')
        append(state.movieCount)
        append('|')
        append(state.seriesCount)
        append('|')
        browseViewModel.continueWatchingForSection(state.selectedSection).joinTo(
            this,
            prefix = "cw:",
            separator = ",",
        ) {
            "${it.channel.id}:${it.playbackPositionMs}"
        }
        append('|')
        state.categorySummaries.joinTo(this, separator = ";") {
            "${it.category.id}:${it.channelCount}:${it.previewImageUrl}"
        }
    }

    private fun buildLoadingRow(): ListRow {
        val adapter = ArrayObjectAdapter(actionCardPresenter)
        adapter.add(
            BrowseItem(
                id = "category_loading",
                title = getString(R.string.loading),
                type = BrowseItemType.ACTION,
            ),
        )
        return ListRow(HeaderItem(LOADING_HEADER_ID, ""), adapter)
    }

    private fun renderCategoryRows(
        state: BrowseUiState,
        section: BrowseSection,
        adapter: ArrayObjectAdapter,
    ) {
        val sectionLabel = sectionTitle(section)
        val countLabel = sectionCountLabel(section, state)
        val gridHeader = if (countLabel.isNullOrBlank()) {
            sectionLabel
        } else {
            "$sectionLabel · $countLabel"
        }
        var rowIndex = 0
        state.categorySummaries.chunked(CATEGORY_COLUMNS).forEach { chunk ->
            val rowAdapter = ArrayObjectAdapter(categoryCardPresenter)
            chunk.forEach { summary ->
                rowAdapter.add(summary.toBrowseItem())
            }
            val header = if (rowIndex == 0) gridHeader else ""
            adapter.add(ListRow(HeaderItem(rowIndex.toLong(), header), rowAdapter))
            rowIndex++
        }
    }

    private fun buildSectionRow(state: BrowseUiState): ListRow =
        BrowseSectionRowHelper.buildSectionRow(
            state = state,
            sectionTabPresenter = sectionTabPresenter,
            sectionsHeader = getString(R.string.browse_sections),
            sectionTitleProvider = ::sectionTitle,
            sectionCountLabelProvider = ::sectionCountLabel,
        )

    private fun handleItemClick(item: Any?) {
        val browseItem = item as? BrowseItem ?: return
        when {
            ContinueWatchingRowHelper.isSeeAllItem(browseItem) -> {
                continueWatchingNavigator.openSeeAll(this)
            }
            browseItem.type == BrowseItemType.CHANNEL -> {
                val cwItem = sectionContinueWatching.firstOrNull {
                    it.channel.id == browseItem.channel?.id &&
                        it.channel.contentType == browseItem.channel?.contentType
                }
                if (cwItem != null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        continueWatchingNavigator.openItem(this@CategoryGridFragment, cwItem)
                    }
                }
            }
            browseItem.type == BrowseItemType.SECTION -> browseItem.section?.let(::handleSectionClick)
            browseItem.type == BrowseItemType.CATEGORY -> browseItem.category?.let(::openCategoryDetail)
        }
    }

    private fun handleSectionClick(section: BrowseSection) {
        if (section == BrowseSection.HOME) {
            browseViewModel.selectSection(BrowseSection.HOME)
            parentFragmentManager.popBackStack()
            return
        }
        browseViewModel.selectSection(section)
    }

    private fun openCategoryDetail(category: Category) {
        replaceContent(
            CategoryDetailFragment.newInstance(
                category,
                browseViewModel.uiState.value.sourceUpdatedAt,
            ),
        )
    }

    private fun buildErrorRow(error: String): ListRow {
        val adapter = ArrayObjectAdapter(actionCardPresenter)
        adapter.add(
            BrowseItem(
                id = "category_error",
                title = getString(R.string.error_banner),
                subtitle = error,
                type = BrowseItemType.ACTION,
            ),
        )
        return ListRow(HeaderItem(ERROR_HEADER_ID, getString(R.string.error_loading)), adapter)
    }

    private fun buildEmptyRow(): ListRow {
        val adapter = ArrayObjectAdapter(actionCardPresenter)
        adapter.add(
            BrowseItem(
                id = "category_empty",
                title = getString(R.string.no_categories),
                type = BrowseItemType.ACTION,
            ),
        )
        return ListRow(HeaderItem(EMPTY_HEADER_ID, ""), adapter)
    }

    private fun sectionTitle(section: BrowseSection): String = when (section) {
        BrowseSection.HOME -> getString(R.string.section_home)
        BrowseSection.LIVE -> getString(R.string.section_live)
        BrowseSection.MOVIES -> getString(R.string.section_movies)
        BrowseSection.SERIES -> getString(R.string.section_series)
    }

    private fun sectionCountLabel(section: BrowseSection, state: BrowseUiState): String? =
        when (section) {
            BrowseSection.HOME -> null
            BrowseSection.LIVE -> getString(R.string.section_count, state.liveCount)
            BrowseSection.MOVIES -> getString(R.string.section_count, state.movieCount)
            BrowseSection.SERIES -> getString(R.string.section_count, state.seriesCount)
        }

    companion object {
        private const val ARG_FROM_LAUNCHER = "arg_from_launcher"
        private const val CATEGORY_COLUMNS = 3
        private const val ERROR_HEADER_ID = 300L
        private const val EMPTY_HEADER_ID = 301L
        private const val LOADING_HEADER_ID = 302L
        private const val CONTINUE_WATCHING_ROW_LIMIT = 15

        fun newInstance(fromLauncher: Boolean = false): CategoryGridFragment =
            CategoryGridFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_FROM_LAUNCHER, fromLauncher)
                }
            }
    }
}
