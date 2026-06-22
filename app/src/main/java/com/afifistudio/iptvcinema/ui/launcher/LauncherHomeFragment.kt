package com.afifistudio.iptvcinema.ui.launcher

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.DateUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
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
import com.afifistudio.iptvcinema.domain.model.SourceType
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
import com.afifistudio.iptvcinema.ui.details.SeriesDetailsFragment
import com.afifistudio.iptvcinema.ui.details.ChannelDetailsFragment
import com.afifistudio.iptvcinema.ui.player.PlayerActivity
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.SectionImportStatus
import androidx.leanback.widget.HorizontalGridView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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
    private lateinit var favoritesAdapter: ArrayObjectAdapter
    private lateinit var recommendationsAdapter: ArrayObjectAdapter
    private var continueWatchingItems: List<ContinueWatchingItem> = emptyList()
    private var lastFocusedGridId: Int = R.id.launcher_grid
    private var hasRequestedInitialFocus = false
    private val sectionRefreshStatuses = mutableMapOf<BrowseSection, SectionRefreshStatus>()

    private val contentFocusHandler = object : ContentFocusHandler {
        override fun requestInitialFocus(): Boolean {
            val grid = binding.root.findViewById<HorizontalGridView>(lastFocusedGridId)
            if (grid != null && grid.isVisible && grid.isFocusable && grid.adapter != null && grid.adapter!!.itemCount > 0) {
                grid.requestFocus()
                scrollToGrid(grid)
                return true
            }
            binding.launcherGrid.post {
                if (_binding != null) {
                    binding.launcherGrid.selectedPosition = 0
                    binding.launcherGrid.requestFocus()
                    scrollToGrid(binding.launcherGrid)
                }
            }
            return true
        }

        override fun canFocusUpToChrome(): Boolean {
            return lastFocusedGridId == R.id.launcher_grid
        }

        override fun onChromeFocusGained() {
            scrollToGrid(binding.launcherGrid)
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
        setupFavoritesGrid()
        setupRecommendationsGrid()
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
            SectionLauncherCardPresenter(
                onItemClick = { sectionItem -> openSection(sectionItem) },
                onItemLongPress = { sectionItem -> showSectionActionMenu(sectionItem) },
            ),
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
            onLongPress = ::onEpisodeLongPress,
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

    private fun setupFavoritesGrid() {
        val contentCardPresenter = ContentCardPresenter(
            onLongPress = ::onEpisodeLongPress,
            onClick = { item -> handleContentClick(item) },
        )
        favoritesAdapter = ArrayObjectAdapter(contentCardPresenter)
        val bridgeAdapter = ItemBridgeAdapter(favoritesAdapter)
        FocusHighlightHelper.setupBrowseItemFocusHighlight(
            bridgeAdapter,
            FocusHighlight.ZOOM_FACTOR_NONE,
            false,
        )
        binding.favoritesGrid.adapter = bridgeAdapter
        binding.favoritesGrid.clipChildren = false
        binding.favoritesGrid.clipToPadding = false
        binding.favoritesGrid.horizontalSpacing =
            resources.getDimensionPixelSize(R.dimen.row_item_spacing)
        val cardHeight = resources.getDimensionPixelSize(R.dimen.launcher_cw_card_height)
        val focusInset = resources.getDimensionPixelSize(R.dimen.launcher_cw_focus_inset)
        binding.favoritesGrid.setRowHeight(cardHeight + focusInset)
        binding.favoritesGrid.isScrollEnabled = true
        val horizontalPadding =
            resources.getDimensionPixelSize(R.dimen.launcher_content_horizontal_padding)
        binding.favoritesGrid.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    private fun setupRecommendationsGrid() {
        val contentCardPresenter = ContentCardPresenter(
            onLongPress = ::onEpisodeLongPress,
            onClick = { item -> handleContentClick(item) },
        )
        recommendationsAdapter = ArrayObjectAdapter(contentCardPresenter)
        val bridgeAdapter = ItemBridgeAdapter(recommendationsAdapter)
        FocusHighlightHelper.setupBrowseItemFocusHighlight(
            bridgeAdapter,
            FocusHighlight.ZOOM_FACTOR_NONE,
            false,
        )
        binding.recommendationsGrid.adapter = bridgeAdapter
        binding.recommendationsGrid.clipChildren = false
        binding.recommendationsGrid.clipToPadding = false
        binding.recommendationsGrid.horizontalSpacing =
            resources.getDimensionPixelSize(R.dimen.row_item_spacing)
        val cardHeight = resources.getDimensionPixelSize(R.dimen.launcher_cw_card_height)
        val focusInset = resources.getDimensionPixelSize(R.dimen.launcher_cw_focus_inset)
        binding.recommendationsGrid.setRowHeight(cardHeight + focusInset)
        binding.recommendationsGrid.isScrollEnabled = true
        val horizontalPadding =
            resources.getDimensionPixelSize(R.dimen.launcher_content_horizontal_padding)
        binding.recommendationsGrid.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    private fun wireVerticalFocusNavigation() {
        binding.launcherGrid.setOnKeyInterceptListener { event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyInterceptListener false
            handleGridKeyDown(binding.launcherGrid, event.keyCode)
        }
        binding.launcherGrid.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) lastFocusedGridId = R.id.launcher_grid
        }

        binding.continueWatchingGrid.setOnKeyInterceptListener { event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyInterceptListener false
            handleGridKeyDown(binding.continueWatchingGrid, event.keyCode)
        }
        binding.continueWatchingGrid.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) lastFocusedGridId = R.id.continue_watching_grid
        }

        binding.favoritesGrid.setOnKeyInterceptListener { event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyInterceptListener false
            handleGridKeyDown(binding.favoritesGrid, event.keyCode)
        }
        binding.favoritesGrid.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) lastFocusedGridId = R.id.favorites_grid
        }

        binding.recommendationsGrid.setOnKeyInterceptListener { event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyInterceptListener false
            handleGridKeyDown(binding.recommendationsGrid, event.keyCode)
        }
        binding.recommendationsGrid.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) lastFocusedGridId = R.id.recommendations_grid
        }

        binding.launcherGrid.setOnChildViewHolderSelectedListener(
            object : OnChildViewHolderSelectedListener() {
                override fun onChildViewHolderSelected(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subposition: Int,
                ) {
                    if (child != null) {
                        scrollToGrid(binding.launcherGrid)
                    }
                }
            },
        )
        binding.continueWatchingGrid.setOnChildViewHolderSelectedListener(
            object : OnChildViewHolderSelectedListener() {
                override fun onChildViewHolderSelected(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subposition: Int,
                ) {
                    if (child != null) {
                        scrollToGrid(binding.continueWatchingGrid)
                    }
                }
            },
        )
        binding.favoritesGrid.setOnChildViewHolderSelectedListener(
            object : OnChildViewHolderSelectedListener() {
                override fun onChildViewHolderSelected(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subposition: Int,
                ) {
                    if (child != null) {
                        scrollToGrid(binding.favoritesGrid)
                    }
                }
            },
        )
        binding.recommendationsGrid.setOnChildViewHolderSelectedListener(
            object : OnChildViewHolderSelectedListener() {
                override fun onChildViewHolderSelected(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subposition: Int,
                ) {
                    if (child != null) {
                        scrollToGrid(binding.recommendationsGrid)
                    }
                }
            },
        )
    }

    private fun getVisibleGrids(): List<HorizontalGridView> = buildList {
        add(binding.launcherGrid)
        if (binding.continueWatchingSection.isVisible && continueWatchingAdapter.size() > 0) {
            add(binding.continueWatchingGrid)
        }
        if (binding.favoritesSection.isVisible && favoritesAdapter.size() > 0) {
            add(binding.favoritesGrid)
        }
        if (binding.recommendationsSection.isVisible && recommendationsAdapter.size() > 0) {
            add(binding.recommendationsGrid)
        }
    }

    private fun handleGridKeyDown(grid: HorizontalGridView, keyCode: Int): Boolean {
        val visibleGrids = getVisibleGrids()
        val index = visibleGrids.indexOf(grid)
        if (index == -1) return false

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (index == 0) {
                    lastFocusedGridId = grid.id
                    requestChromeFocus()
                    true
                } else {
                    val prevGrid = visibleGrids[index - 1]
                    lastFocusedGridId = prevGrid.id
                    prevGrid.requestFocus()
                    scrollToGrid(prevGrid)
                    true
                }
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (index < visibleGrids.lastIndex) {
                    val nextGrid = visibleGrids[index + 1]
                    lastFocusedGridId = nextGrid.id
                    nextGrid.requestFocus()
                    scrollToGrid(nextGrid)
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    private fun scrollToGrid(grid: HorizontalGridView) {
        val scrollView = binding.root as? NestedScrollView ?: return
        scrollView.post {
            if (_binding == null) return@post
            val targetY = when (grid.id) {
                R.id.launcher_grid -> 0
                R.id.continue_watching_grid -> binding.continueWatchingSection.top
                R.id.favorites_grid -> binding.favoritesSection.top
                R.id.recommendations_grid -> binding.recommendationsSection.top
                else -> 0
            }
            scrollView.smoothScrollTo(0, targetY)
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
                    renderFavorites(state)
                    renderRecommendations(state)
                    updateBackdrop(state)

                    if (!state.isBootstrapLoading && !hasRequestedInitialFocus) {
                        hasRequestedInitialFocus = true
                        binding.launcherGrid.post {
                            if (_binding != null) {
                                binding.launcherGrid.selectedPosition = 0
                                binding.launcherGrid.requestFocus()
                                scrollToGrid(binding.launcherGrid)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderFavorites(state: BrowseUiState) {
        val gridBinding = _binding ?: return
        val items = state.favorites
        if (items.isEmpty()) {
            gridBinding.favoritesSection.isVisible = false
            favoritesAdapter.clear()
            return
        }
        gridBinding.favoritesSection.isVisible = true
        gridBinding.favoritesHeader.text = getString(
            R.string.row_header_count,
            getString(R.string.favorites_row),
            items.size,
        )
        val favHasFocus = gridBinding.favoritesGrid.hasFocus()
        val focusedPosition = gridBinding.favoritesGrid.selectedPosition
        val list = items.map { item ->
            item.toBrowseItem(favoriteIds = setOf(item.id), context = requireContext()).copy(
                usePosterLayout = false,
                cardWidthRes = R.dimen.launcher_cw_card_width,
                cardHeightRes = R.dimen.launcher_cw_card_height,
            )
        }
        updateAdapterInPlace(favoritesAdapter, list)
        if (favHasFocus && favoritesAdapter.size() > 0) {
            gridBinding.favoritesGrid.post {
                val activeBinding = _binding ?: return@post
                activeBinding.favoritesGrid.selectedPosition = focusedPosition.coerceAtMost(favoritesAdapter.size() - 1)
                activeBinding.favoritesGrid.requestFocus()
            }
        }
    }

    private fun renderRecommendations(state: BrowseUiState) {
        val gridBinding = _binding ?: return
        val items = state.recommendedChannels
        if (items.isEmpty()) {
            gridBinding.recommendationsSection.isVisible = false
            recommendationsAdapter.clear()
            return
        }
        gridBinding.recommendationsSection.isVisible = true
        gridBinding.recommendationsHeader.text = getString(
            R.string.row_header_count,
            state.recommendedRowTitle,
            items.size,
        )
        val recHasFocus = gridBinding.recommendationsGrid.hasFocus()
        val focusedPosition = gridBinding.recommendationsGrid.selectedPosition
        val favoriteIds = state.favorites.map { it.id }.toSet()
        val list = items.map { item ->
            item.toBrowseItem(favoriteIds, context = requireContext()).copy(
                usePosterLayout = false,
                cardWidthRes = R.dimen.launcher_cw_card_width,
                cardHeightRes = R.dimen.launcher_cw_card_height,
            )
        }
        updateAdapterInPlace(recommendationsAdapter, list)
        if (recHasFocus && recommendationsAdapter.size() > 0) {
            gridBinding.recommendationsGrid.post {
                val activeBinding = _binding ?: return@post
                activeBinding.recommendationsGrid.selectedPosition = focusedPosition.coerceAtMost(recommendationsAdapter.size() - 1)
                activeBinding.recommendationsGrid.requestFocus()
            }
        }
    }

    private fun handleContentClick(item: BrowseItem) {
        val channel = item.channel ?: return
        when (channel.contentType) {
            ContentType.LIVE, ContentType.MOVIE -> {
                startActivity(PlayerActivity.createIntent(requireContext(), channel, channel.categoryId))
            }
            ContentType.SERIES -> replaceContent(SeriesDetailsFragment.newInstance(channel))
            ContentType.EPISODE -> replaceContent(ChannelDetailsFragment.newInstance(channel))
        }
    }

    private fun onEpisodeLongPress(item: BrowseItem) {
        val channel = item.channel ?: return
        if (channel.contentType != ContentType.EPISODE) return
        replaceContent(SeriesDetailsFragment.newInstance(channel.toSeriesChannel(), highlightEpisodeId = channel.id))
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
            refreshStatus = sectionRefreshStatuses[section] ?: SectionRefreshStatus.IDLE,
            iconRes = SectionIcons.forSection(section),
            previewImageUrl = null,
            previewFallbackRes = sectionPreviewFallback(section),
            isBrowseEnabled = false,
            disabledMessage = getString(R.string.section_still_loading, title),
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
        updateAdapterInPlace(gridAdapter, items)
        gridBinding.launcherGrid.post {
            val activeBinding = _binding ?: return@post
            if (gridAdapter.size() > 0 && gridHasFocus) {
                activeBinding.launcherGrid.requestFocus()
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
        val list = items.map { item ->
            item.toBrowseItem(requireContext(), favoriteIds).copy(
                usePosterLayout = false,
                cardWidthRes = R.dimen.launcher_cw_card_width,
                cardHeightRes = R.dimen.launcher_cw_card_height,
            )
        }.toMutableList()
        if (state.continueWatching.size >= CONTINUE_WATCHING_ROW_LIMIT) {
            list.add(
                BrowseItem(
                    id = ContinueWatchingRowHelper.SEE_ALL_ITEM_ID,
                    title = getString(R.string.continue_watching_see_all),
                    type = BrowseItemType.ACTION,
                ),
            )
        }
        updateAdapterInPlace(continueWatchingAdapter, list)
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
        val importState = state.sectionLoadStates[section]
        val itemCount = countForSection(state, section)
        val countLabel = sectionCountLabel(section, itemCount, importState?.status)
        val title = when (section) {
            BrowseSection.LIVE -> getString(R.string.section_live)
            BrowseSection.MOVIES -> getString(R.string.section_movies)
            BrowseSection.SERIES -> getString(R.string.section_series)
            BrowseSection.HOME -> getString(R.string.section_home)
        }
        val isBrowseEnabled = (importState == null || importState.status == SectionImportStatus.READY) && itemCount > 0
        val disabledMessage = disabledSectionMessage(title, itemCount, importState?.status)
        val updateLabel = when (importState?.status) {
            SectionImportStatus.QUEUED -> getString(R.string.section_import_queued)
            SectionImportStatus.LOADING -> getString(R.string.section_import_loading)
            SectionImportStatus.ERROR -> getString(R.string.section_import_failed)
            SectionImportStatus.READY,
            null -> lastUpdate
        }
        return SectionLauncherItem(
            section = section,
            title = title,
            countLabel = countLabel,
            lastUpdateLabel = updateLabel,
            refreshStatus = sectionRefreshStatuses[section] ?: SectionRefreshStatus.IDLE,
            iconRes = SectionIcons.forSection(section),
            previewImageUrl = previewForContentType(state, contentType),
            previewFallbackRes = sectionPreviewFallback(section),
            isBrowseEnabled = isBrowseEnabled,
            disabledMessage = disabledMessage,
        )
    }

    private fun countForSection(state: BrowseUiState, section: BrowseSection): Int =
        when (section) {
            BrowseSection.LIVE -> state.liveCount
            BrowseSection.MOVIES -> state.movieCount
            BrowseSection.SERIES -> state.seriesCount
            BrowseSection.HOME -> 0
        }

    private fun sectionCountLabel(
        section: BrowseSection,
        count: Int,
        status: SectionImportStatus?,
    ): String {
        return when (status) {
            SectionImportStatus.QUEUED -> getString(R.string.section_import_queued)
            SectionImportStatus.LOADING -> getString(R.string.loading)
            SectionImportStatus.ERROR -> getString(R.string.section_import_failed)
            SectionImportStatus.READY,
            null -> when (section) {
                BrowseSection.LIVE -> getString(R.string.launcher_count_channels, count)
                BrowseSection.MOVIES -> getString(R.string.launcher_count_titles, count)
                BrowseSection.SERIES -> getString(R.string.launcher_count_series, count)
                BrowseSection.HOME -> ""
            }
        }
    }

    private fun disabledSectionMessage(
        title: String,
        count: Int,
        status: SectionImportStatus?,
    ): String? {
        return when (status) {
            SectionImportStatus.QUEUED -> getString(R.string.section_still_queued, title)
            SectionImportStatus.LOADING -> getString(R.string.section_still_loading, title)
            SectionImportStatus.ERROR -> getString(R.string.section_load_failed, title)
            SectionImportStatus.READY,
            null -> if (count <= 0) getString(R.string.section_not_ready, title) else null
        }
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

    private fun openSection(item: SectionLauncherItem) {
        if (!item.isBrowseEnabled) {
            Toast.makeText(
                requireContext(),
                item.disabledMessage ?: getString(R.string.section_not_ready, item.title),
                Toast.LENGTH_SHORT,
            ).show()
            requestSectionCardFocus(item.section)
            return
        }
        openSection(item.section)
    }

    private fun openSection(section: BrowseSection) {
        browseViewModel.selectSection(section)
        replaceContent(CategoryGridFragment.newInstance(fromLauncher = true))
    }

    private fun showSectionActionMenu(item: SectionLauncherItem) {
        val dialog = Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_section_action, null)
        val selectedSource = browseViewModel.uiState.value.sources.firstOrNull {
            it.id == browseViewModel.uiState.value.selectedSourceId
        }
        val usesPlaylistFallback = selectedSource?.type != SourceType.XTREAM

        view.findViewById<TextView>(R.id.section_action_title).text = item.title
        view.findViewById<TextView>(R.id.section_action_subtitle).text = if (usesPlaylistFallback) {
            getString(R.string.section_refresh_playlist_fallback)
        } else {
            getString(R.string.section_refresh_section_only)
        }
        val refreshAction = view.findViewById<TextView>(R.id.section_action_refresh)
        refreshAction.text = if (usesPlaylistFallback) {
            getString(R.string.section_refresh_playlist)
        } else {
            sectionRefreshActionLabel(item.section)
        }
        view.findViewById<TextView>(R.id.section_action_browse).text =
            getString(R.string.section_browse_action, item.title)

        refreshAction.setOnClickListener {
            dialog.dismiss()
            refreshSectionFromCard(item.section)
        }
        view.findViewById<TextView>(R.id.section_action_browse).setOnClickListener {
            dialog.dismiss()
            openSection(item)
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.setOnDismissListener { requestSectionCardFocus(item.section) }
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.56f)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            val width = resources.getDimensionPixelSize(R.dimen.section_action_dialog_width)
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        refreshAction.requestFocus()
    }

    private fun refreshSectionFromCard(section: BrowseSection) {
        setSectionRefreshStatus(section, SectionRefreshStatus.REFRESHING)
        browseViewModel.refreshSection(section) { result ->
            if (_binding == null || !isAdded) return@refreshSection
            val status = if (result.isSuccess) SectionRefreshStatus.SUCCESS else SectionRefreshStatus.ERROR
            setSectionRefreshStatus(section, status)
            val message = if (result.isSuccess) {
                getString(R.string.section_refresh_updated, sectionTitle(section))
            } else {
                getString(R.string.section_refresh_failed)
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            viewLifecycleOwner.lifecycleScope.launch {
                delay(SECTION_REFRESH_STATUS_RESET_MS)
                if (sectionRefreshStatuses[section] == status) {
                    setSectionRefreshStatus(section, SectionRefreshStatus.IDLE)
                }
            }
        }
    }

    private fun setSectionRefreshStatus(section: BrowseSection, status: SectionRefreshStatus) {
        if (status == SectionRefreshStatus.IDLE) {
            sectionRefreshStatuses.remove(section)
        } else {
            sectionRefreshStatuses[section] = status
        }
        renderSections(browseViewModel.uiState.value)
        requestSectionCardFocus(section)
    }

    private fun requestSectionCardFocus(section: BrowseSection) {
        val position = when (section) {
            BrowseSection.LIVE -> 0
            BrowseSection.MOVIES -> 1
            BrowseSection.SERIES -> 2
            BrowseSection.HOME -> 0
        }
        val activeBinding = _binding ?: return
        activeBinding.launcherGrid.post {
            val postedBinding = _binding ?: return@post
            if (gridAdapter.size() <= position) return@post
            postedBinding.launcherGrid.selectedPosition = position
            postedBinding.launcherGrid.requestFocus()
            scrollToGrid(postedBinding.launcherGrid)
        }
    }

    private fun sectionRefreshActionLabel(section: BrowseSection): String = when (section) {
        BrowseSection.LIVE -> getString(R.string.section_refresh_live)
        BrowseSection.MOVIES -> getString(R.string.section_refresh_movies)
        BrowseSection.SERIES -> getString(R.string.section_refresh_series)
        BrowseSection.HOME -> getString(R.string.action_refresh)
    }

    private fun sectionTitle(section: BrowseSection): String = when (section) {
        BrowseSection.LIVE -> getString(R.string.section_live)
        BrowseSection.MOVIES -> getString(R.string.section_movies)
        BrowseSection.SERIES -> getString(R.string.section_series)
        BrowseSection.HOME -> getString(R.string.section_home)
    }

    private fun updateAdapterInPlace(adapter: ArrayObjectAdapter, newItems: List<Any>) {
        if (adapter.size() == newItems.size) {
            for (i in 0 until newItems.size) {
                val oldItem = adapter.get(i)
                val newItem = newItems[i]
                if (oldItem != newItem) {
                    adapter.replace(i, newItem)
                }
            }
        } else {
            adapter.clear()
            newItems.forEach { adapter.add(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val SECTION_COUNT = 3
        private const val CONTINUE_WATCHING_ROW_LIMIT = 15
        private const val SECTION_REFRESH_STATUS_RESET_MS = 2_400L
    }
}
