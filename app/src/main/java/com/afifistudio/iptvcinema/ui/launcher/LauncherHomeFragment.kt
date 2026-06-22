package com.afifistudio.iptvcinema.ui.launcher

import android.os.Bundle
import android.text.format.DateUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.FocusHighlightHelper
import androidx.leanback.widget.ItemBridgeAdapter
import androidx.leanback.widget.OnChildViewHolderSelectedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.databinding.FragmentLauncherHomeBinding
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.ContinueWatchingItem
import com.afifistudio.iptvcinema.ui.ContentFocusHandler
import com.afifistudio.iptvcinema.ui.HomeChromeHost
import com.afifistudio.iptvcinema.ui.HomeChromeMode
import com.afifistudio.iptvcinema.ui.registerContentFocusHandler
import com.afifistudio.iptvcinema.ui.requestChromeFocus
import com.afifistudio.iptvcinema.ui.unregisterContentFocusHandler
import com.afifistudio.iptvcinema.ui.browse.BrowseItem
import com.afifistudio.iptvcinema.ui.browse.BrowseItemType
import com.afifistudio.iptvcinema.ui.browse.BrowseSection
import com.afifistudio.iptvcinema.ui.browse.BrowseUiState
import com.afifistudio.iptvcinema.ui.browse.BrowseViewModel
import com.afifistudio.iptvcinema.ui.browse.ContinueWatchingNavigator
import com.afifistudio.iptvcinema.ui.browse.ContinueWatchingRowHelper
import com.afifistudio.iptvcinema.ui.browse.ContentCardPresenter
import com.afifistudio.iptvcinema.ui.browse.toBrowseItem
import com.afifistudio.iptvcinema.ui.categories.CategoryGridFragment
import com.afifistudio.iptvcinema.ui.common.SectionIcons
import com.afifistudio.iptvcinema.ui.replaceContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LauncherHomeFragment : Fragment() {

    @Inject lateinit var continueWatchingNavigator: ContinueWatchingNavigator

    private val browseViewModel: BrowseViewModel by viewModels({ requireActivity() })
    private var _binding: FragmentLauncherHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var gridAdapter: ArrayObjectAdapter
    private lateinit var continueWatchingAdapter: ArrayObjectAdapter
    private var continueWatchingItems: List<ContinueWatchingItem> = emptyList()
    private var lastContentFocusTarget = ContentFocusTarget.SectionGrid
    private var hasRequestedInitialFocus = false

    private enum class ContentFocusTarget {
        SectionGrid,
        ContinueWatching,
    }

    private val contentFocusHandler = object : ContentFocusHandler {
        override fun requestInitialFocus(): Boolean {
            return when (lastContentFocusTarget) {
                ContentFocusTarget.ContinueWatching -> {
                    if (binding.continueWatchingSection.isVisible && continueWatchingAdapter.size() > 0) {
                        focusContinueWatchingRow()
                        true
                    } else {
                        focusSectionGrid()
                    }
                }
                ContentFocusTarget.SectionGrid -> focusSectionGrid()
            }
        }

        override fun canFocusUpToChrome(): Boolean = true

        override fun onChromeFocusGained() {
            scrollToSectionRow()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLauncherHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hasRequestedInitialFocus = false
        setupGrid()
        setupContinueWatchingGrid()
        wireVerticalFocusNavigation()
        showPlaceholderSections()
        observeState()
        (activity as? HomeChromeHost)?.apply {
            setChromeVisible(true)
            setChromeMode(HomeChromeMode.LAUNCHER)
        }
    }

    override fun onResume() {
        super.onResume()
        browseViewModel.refreshContinueWatching()
        registerContentFocusHandler(contentFocusHandler)
        (activity as? HomeChromeHost)?.apply {
            setChromeVisible(true)
            setChromeMode(HomeChromeMode.LAUNCHER)
        }
    }

    override fun onPause() {
        unregisterContentFocusHandler()
        super.onPause()
    }

    private fun setupGrid() {
        gridAdapter = ArrayObjectAdapter(
            SectionLauncherCardPresenter { sectionItem ->
                openSection(sectionItem.section)
            },
        )
        val bridgeAdapter = ItemBridgeAdapter(gridAdapter)
        FocusHighlightHelper.setupBrowseItemFocusHighlight(
            bridgeAdapter,
            FocusHighlight.ZOOM_FACTOR_NONE,
            false,
        )
        binding.launcherGrid.adapter = bridgeAdapter
        binding.launcherGrid.clipChildren = false
        binding.launcherGrid.clipToPadding = false
        binding.launcherGrid.horizontalSpacing =
            resources.getDimensionPixelSize(R.dimen.launcher_card_spacing)
        val cardHeight = resources.getDimensionPixelSize(R.dimen.launcher_card_height)
        val focusInset = resources.getDimensionPixelSize(R.dimen.launcher_card_focus_inset)
        binding.launcherGrid.setRowHeight(cardHeight + focusInset)
        binding.launcherGrid.isScrollEnabled = false
        applySectionGridPadding()
    }

    private fun applySectionGridPadding() {
        val cardWidth = resources.getDimensionPixelSize(R.dimen.launcher_card_width)
        val spacing = resources.getDimensionPixelSize(R.dimen.launcher_card_spacing)
        val minHorizontal = resources.getDimensionPixelSize(R.dimen.launcher_content_horizontal_padding)
        val totalCardsWidth = cardWidth * SECTION_COUNT + spacing * (SECTION_COUNT - 1)
        val sidePadding = ((resources.displayMetrics.widthPixels - totalCardsWidth) / 2)
            .coerceAtLeast(minHorizontal)
        binding.launcherGrid.setPadding(sidePadding, 0, sidePadding, 0)
    }

    private fun setupContinueWatchingGrid() {
        val contentCardPresenter = ContentCardPresenter(
            onClick = { item -> handleContinueWatchingClick(item) },
        )
        continueWatchingAdapter = ArrayObjectAdapter(contentCardPresenter)
        val bridgeAdapter = ItemBridgeAdapter(continueWatchingAdapter)
        FocusHighlightHelper.setupBrowseItemFocusHighlight(
            bridgeAdapter,
            FocusHighlight.ZOOM_FACTOR_NONE,
            false,
        )
        binding.continueWatchingGrid.adapter = bridgeAdapter
        binding.continueWatchingGrid.clipChildren = false
        binding.continueWatchingGrid.clipToPadding = false
        binding.continueWatchingGrid.horizontalSpacing =
            resources.getDimensionPixelSize(R.dimen.row_item_spacing)
        val cardHeight = resources.getDimensionPixelSize(R.dimen.launcher_cw_card_height)
        val focusInset = resources.getDimensionPixelSize(R.dimen.launcher_cw_focus_inset)
        binding.continueWatchingGrid.setRowHeight(cardHeight + focusInset)
        binding.continueWatchingGrid.isScrollEnabled = true
        val horizontalPadding =
            resources.getDimensionPixelSize(R.dimen.launcher_content_horizontal_padding)
        binding.continueWatchingGrid.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    private fun wireVerticalFocusNavigation() {
        binding.launcherGrid.setOnKeyInterceptListener { event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyInterceptListener false
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    lastContentFocusTarget = ContentFocusTarget.SectionGrid
                    requestChromeFocus()
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (binding.continueWatchingSection.isVisible && continueWatchingAdapter.size() > 0) {
                        lastContentFocusTarget = ContentFocusTarget.ContinueWatching
                        focusContinueWatchingRow()
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
        binding.launcherGrid.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) lastContentFocusTarget = ContentFocusTarget.SectionGrid
        }
        binding.continueWatchingGrid.setOnKeyInterceptListener { event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyInterceptListener false
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    lastContentFocusTarget = ContentFocusTarget.SectionGrid
                    binding.launcherGrid.requestFocus()
                    scrollToSectionRow()
                    true
                }
                else -> false
            }
        }
        binding.continueWatchingGrid.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) lastContentFocusTarget = ContentFocusTarget.ContinueWatching
        }
        binding.continueWatchingGrid.setOnChildViewHolderSelectedListener(
            object : OnChildViewHolderSelectedListener() {
                override fun onChildViewHolderSelected(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subposition: Int,
                ) {
                    scrollToContinueWatching()
                }
            },
        )
        binding.launcherGrid.setOnChildViewHolderSelectedListener(
            object : OnChildViewHolderSelectedListener() {
                override fun onChildViewHolderSelected(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subposition: Int,
                ) {
                    if (child != null) {
                        scrollToSectionRow()
                    }
                }
            },
        )
    }

    private fun focusContinueWatchingRow() {
        binding.continueWatchingGrid.post {
            if (_binding == null || continueWatchingAdapter.size() == 0) return@post
            binding.continueWatchingGrid.selectedPosition = 0
            binding.continueWatchingGrid.requestFocus()
            scrollToContinueWatching()
        }
    }

    private fun focusSectionGrid(): Boolean {
        binding.launcherGrid.post {
            if (_binding == null || gridAdapter.size() == 0) return@post
            binding.launcherGrid.selectedPosition = 0
            binding.launcherGrid.requestFocus()
            scrollToSectionRow()
        }
        return true
    }

    private fun scrollToContinueWatching() {
        val scrollView = binding.root as? NestedScrollView ?: return
        scrollView.post {
            scrollView.smoothScrollTo(0, binding.continueWatchingSection.top)
        }
    }

    private fun scrollToSectionRow() {
        val scrollView = binding.root as? NestedScrollView ?: return
        scrollView.post {
            scrollView.smoothScrollTo(0, 0)
        }
    }

    private fun handleContinueWatchingClick(item: BrowseItem) {
        if (ContinueWatchingRowHelper.isSeeAllItem(item)) {
            continueWatchingNavigator.openSeeAll(this)
            return
        }
        val cwItem = continueWatchingItems.firstOrNull {
            "cw_${it.channel.id}_${it.channel.contentType.name}" == item.id
        } ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            continueWatchingNavigator.openItem(this@LauncherHomeFragment, cwItem)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                browseViewModel.uiState.collect { state ->
                    if (_binding == null) return@collect
                    renderSections(state)
                    renderContinueWatching(state)
                    updateBackdrop(state)
                }
            }
        }
    }

    private fun showPlaceholderSections() {
        gridAdapter.clear()
        listOf(BrowseSection.LIVE, BrowseSection.MOVIES, BrowseSection.SERIES).forEach { section ->
            gridAdapter.add(buildPlaceholderItem(section))
        }
    }

    private fun buildPlaceholderItem(section: BrowseSection): SectionLauncherItem {
        val title = when (section) {
            BrowseSection.LIVE -> getString(R.string.section_live)
            BrowseSection.MOVIES -> getString(R.string.section_movies)
            BrowseSection.SERIES -> getString(R.string.section_series)
            BrowseSection.HOME -> getString(R.string.section_home)
        }
        return SectionLauncherItem(
            section = section,
            title = title,
            countLabel = getString(R.string.loading),
            lastUpdateLabel = null,
            iconRes = SectionIcons.forSection(section),
            previewImageUrl = null,
            previewFallbackRes = sectionPreviewFallback(section),
        )
    }

    private fun renderSections(state: BrowseUiState) {
        val gridBinding = _binding ?: return
        val lastUpdate = formatLastUpdate(state.sourceUpdatedAt)
        val items = listOf(
            buildItem(state, BrowseSection.LIVE, ContentType.LIVE, lastUpdate),
            buildItem(state, BrowseSection.MOVIES, ContentType.MOVIE, lastUpdate),
            buildItem(state, BrowseSection.SERIES, ContentType.SERIES, lastUpdate),
        )
        val gridHasFocus = gridBinding.launcherGrid.hasFocus()
        gridAdapter.clear()
        items.forEach { gridAdapter.add(it) }
        gridBinding.launcherGrid.post {
            val activeBinding = _binding ?: return@post
            if (gridAdapter.size() > 0) {
                activeBinding.launcherGrid.selectedPosition = 0
                if (!hasRequestedInitialFocus) {
                    hasRequestedInitialFocus = true
                    activeBinding.launcherGrid.requestFocus()
                } else if (gridHasFocus) {
                    activeBinding.launcherGrid.requestFocus()
                }
            }
        }
    }

    private fun renderContinueWatching(state: BrowseUiState) {
        val gridBinding = _binding ?: return
        val items = state.continueWatching
        continueWatchingItems = items
        if (items.isEmpty()) {
            gridBinding.continueWatchingSection.isVisible = false
            continueWatchingAdapter.clear()
            (activity as? HomeChromeHost)?.setChromeFooterVisible(true)
            return
        }
        gridBinding.continueWatchingSection.isVisible = true
        (activity as? HomeChromeHost)?.setChromeFooterVisible(false)
        gridBinding.continueWatchingHeader.text = getString(
            R.string.row_header_count,
            getString(R.string.continue_watching_row),
            items.size,
        )
        val favoriteIds = state.favorites.map { it.id }.toSet()
        val cwHasFocus = gridBinding.continueWatchingGrid.hasFocus()
        val focusedPosition = gridBinding.continueWatchingGrid.selectedPosition
        continueWatchingAdapter.clear()
        items.forEach { item ->
            continueWatchingAdapter.add(
                item.toBrowseItem(requireContext(), favoriteIds).copy(
                    usePosterLayout = false,
                    cardWidthRes = R.dimen.launcher_cw_card_width,
                    cardHeightRes = R.dimen.launcher_cw_card_height,
                ),
            )
        }
        if (state.continueWatching.size >= CONTINUE_WATCHING_ROW_LIMIT) {
            continueWatchingAdapter.add(
                BrowseItem(
                    id = ContinueWatchingRowHelper.SEE_ALL_ITEM_ID,
                    title = getString(R.string.continue_watching_see_all),
                    type = BrowseItemType.ACTION,
                ),
            )
        }
        if (cwHasFocus && continueWatchingAdapter.size() > 0) {
            gridBinding.continueWatchingGrid.post {
                val activeBinding = _binding ?: return@post
                activeBinding.continueWatchingGrid.selectedPosition = focusedPosition.coerceAtMost(continueWatchingAdapter.size() - 1)
                activeBinding.continueWatchingGrid.requestFocus()
            }
        }
        wireSectionCardFocusDown()
    }

    private fun wireSectionCardFocusDown() {
        if (!binding.continueWatchingSection.isVisible) return
        binding.launcherGrid.post {
            for (i in 0 until binding.launcherGrid.childCount) {
                val child = binding.launcherGrid.getChildAt(i) ?: continue
                child.nextFocusDownId = R.id.continue_watching_grid
            }
        }
    }

    private fun buildItem(
        state: BrowseUiState,
        section: BrowseSection,
        contentType: ContentType,
        lastUpdate: String?,
    ): SectionLauncherItem {
        val countLabel = when (section) {
            BrowseSection.LIVE -> getString(R.string.launcher_count_channels, state.liveCount)
            BrowseSection.MOVIES -> getString(R.string.launcher_count_titles, state.movieCount)
            BrowseSection.SERIES -> getString(R.string.launcher_count_series, state.seriesCount)
            BrowseSection.HOME -> ""
        }
        val title = when (section) {
            BrowseSection.LIVE -> getString(R.string.section_live)
            BrowseSection.MOVIES -> getString(R.string.section_movies)
            BrowseSection.SERIES -> getString(R.string.section_series)
            BrowseSection.HOME -> getString(R.string.section_home)
        }
        return SectionLauncherItem(
            section = section,
            title = title,
            countLabel = countLabel,
            lastUpdateLabel = lastUpdate,
            iconRes = SectionIcons.forSection(section),
            previewImageUrl = previewForContentType(state, contentType),
            previewFallbackRes = sectionPreviewFallback(section),
        )
    }

    private fun sectionPreviewFallback(section: BrowseSection): Int = when (section) {
        BrowseSection.LIVE -> R.drawable.launcher_preview_live
        BrowseSection.MOVIES -> R.drawable.launcher_preview_movies
        BrowseSection.SERIES -> R.drawable.launcher_preview_series
        BrowseSection.HOME -> R.drawable.launcher_preview_live
    }

    private fun previewForContentType(state: BrowseUiState, contentType: ContentType): String? =
        state.homeCategoryHighlights
            .firstOrNull { it.category.contentType == contentType }
            ?.previewImageUrl
            ?: state.continueWatching
                .firstOrNull { it.channel.contentType == contentType }
                ?.channel
                ?.logoUrl

    private fun formatLastUpdate(updatedAt: Long): String? {
        if (updatedAt <= 0L) return null
        return DateUtils.getRelativeTimeSpanString(
            updatedAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        ).toString()
    }

    private fun updateBackdrop(state: BrowseUiState) {
        if (_binding == null || !isAdded) return
        val imageUrl = state.featuredChannel?.logoUrl
            ?: state.continueWatching.firstOrNull()?.channel?.logoUrl
            ?: state.homeCategoryHighlights.firstOrNull()?.previewImageUrl
        (activity as? HomeChromeHost)?.setBackdropImageUrl(imageUrl)
    }

    private fun openSection(section: BrowseSection) {
        browseViewModel.selectSection(section)
        replaceContent(CategoryGridFragment.newInstance(fromLauncher = true))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val SECTION_COUNT = 3
        private const val CONTINUE_WATCHING_ROW_LIMIT = 15
    }
}
