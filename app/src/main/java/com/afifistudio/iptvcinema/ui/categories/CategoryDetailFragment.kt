package com.afifistudio.iptvcinema.ui.categories

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.BaseGridView
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.FocusHighlightHelper
import androidx.leanback.widget.ItemBridgeAdapter
import androidx.leanback.widget.Presenter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.databinding.FragmentCategoryDetailBinding
import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.ui.ContentFocusHandler
import com.afifistudio.iptvcinema.ui.HomeChromeHost
import com.afifistudio.iptvcinema.ui.HomeChromeMode
import com.afifistudio.iptvcinema.ui.common.TvFocusCoordinator
import com.afifistudio.iptvcinema.ui.registerContentFocusHandler
import com.afifistudio.iptvcinema.ui.requestChromeFocus
import com.afifistudio.iptvcinema.ui.unregisterContentFocusHandler
import com.afifistudio.iptvcinema.ui.browse.BrowseItem
import com.afifistudio.iptvcinema.ui.browse.BrowseItemType
import com.afifistudio.iptvcinema.ui.browse.ContentCardPresenter
import com.afifistudio.iptvcinema.ui.browse.PosterGridCardPresenter
import com.afifistudio.iptvcinema.ui.details.ChannelDetailsFragment
import com.afifistudio.iptvcinema.ui.details.SeriesDetailsFragment
import com.afifistudio.iptvcinema.ui.player.PlayerActivity
import com.afifistudio.iptvcinema.data.cache.SeriesEpisodesLoader
import com.afifistudio.iptvcinema.ui.replaceContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CategoryDetailFragment : Fragment() {

    @Inject lateinit var seriesEpisodesLoader: SeriesEpisodesLoader

    private val viewModel: CategoryDetailViewModel by viewModels()
    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var gridAdapter: ArrayObjectAdapter
    private lateinit var bridgeAdapter: ItemBridgeAdapter
    private val posterCardPresenter = PosterGridCardPresenter()
    private val liveCardPresenter = ContentCardPresenter()
    private lateinit var contentType: ContentType
    private var renderedChannelIds: List<String> = emptyList()
    private var renderedFavoriteIds: Set<String> = emptySet()
    private var gridColumns = POSTER_GRID_COLUMNS
    private var hasInitialFocus = false

    private val contentFocusHandler = object : ContentFocusHandler {
        override fun requestInitialFocus(): Boolean {
            if (_binding == null || gridAdapter.size() == 0) return false
            val grid = binding.categoryDetailGrid
            grid.selectedPosition = 0
            return grid.requestFocus()
        }

        override fun canFocusUpToChrome(): Boolean =
            TvFocusCoordinator.isFirstGridRow(binding.categoryDetailGrid.selectedPosition, gridColumns)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setPadding(
            0,
            resources.getDimensionPixelSize(R.dimen.browse_content_top_padding),
            0,
            0,
        )
        val category = readCategoryArg()
        val sourceUpdatedAt = requireArguments().getLong(ARG_SOURCE_UPDATED_AT, 0L)
        contentType = category.contentType
        setupGrid(category.contentType, sourceUpdatedAt)
        viewModel.load(category, sourceUpdatedAt)
        observeState()
        (activity as? HomeChromeHost)?.apply {
            setChromeVisible(true)
            setChromeMode(HomeChromeMode.BROWSE)
        }
    }

    override fun onResume() {
        super.onResume()
        registerContentFocusHandler(contentFocusHandler)
    }

    override fun onPause() {
        unregisterContentFocusHandler()
        super.onPause()
    }

    private fun requestGridFocus() {
        val grid = binding.categoryDetailGrid
        grid.post {
            if (_binding == null || gridAdapter.size() == 0) return@post
            grid.selectedPosition = 0
            if (grid.childCount > 0) {
                grid.requestFocus()
            } else {
                grid.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                    override fun onLayoutChange(
                        v: View?,
                        left: Int, top: Int, right: Int, bottom: Int,
                        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
                    ) {
                        grid.removeOnLayoutChangeListener(this)
                        grid.requestFocus()
                    }
                })
            }
        }
    }

    private fun setupGrid(contentType: ContentType, sourceUpdatedAt: Long) {
        val columns = gridColumnsFor(contentType)
        gridColumns = columns
        val cardPresenter = cardPresenterFor(contentType)

        gridAdapter = ArrayObjectAdapter(cardPresenter)
        bridgeAdapter = ItemBridgeAdapter(gridAdapter)
        FocusHighlightHelper.setupBrowseItemFocusHighlight(
            bridgeAdapter,
            FocusHighlight.ZOOM_FACTOR_NONE,
            false,
        )

        val bottomPadding = resources.getDimensionPixelSize(R.dimen.category_detail_grid_bottom_padding)
        val rowSpacing = if (contentType == ContentType.LIVE) {
            resources.getDimensionPixelSize(R.dimen.row_item_spacing)
        } else {
            resources.getDimensionPixelSize(R.dimen.poster_grid_spacing)
        }

        binding.categoryDetailGrid.apply {
            setNumColumns(columns)
            verticalSpacing = rowSpacing
            clipChildren = false
            clipToPadding = true
            adapter = bridgeAdapter
            setPadding(paddingLeft, 0, paddingRight, bottomPadding)
            setWindowAlignment(BaseGridView.WINDOW_ALIGN_LOW_EDGE)
            setWindowAlignmentOffset(0)
            setItemAlignmentOffset(0)
            setItemAlignmentOffsetPercent(0f)
        }

        TvFocusCoordinator.wireGridUp(
            gridView = binding.categoryDetailGrid,
            columns = columns,
            selectedPositionProvider = { binding.categoryDetailGrid.selectedPosition },
            requestChromeFocus = { requestChromeFocus() },
        )

        binding.categoryDetailGrid.addOnChildViewHolderSelectedListener(
            object : androidx.leanback.widget.OnChildViewHolderSelectedListener() {
                override fun onChildViewHolderSelected(
                    parent: androidx.recyclerview.widget.RecyclerView,
                    child: androidx.recyclerview.widget.RecyclerView.ViewHolder?,
                    position: Int,
                    subposition: Int,
                ) {
                    val state = viewModel.uiState.value
                    if (state.hasMore && !state.isLoadingMore && position >= state.channels.size - LOAD_MORE_THRESHOLD) {
                        viewModel.loadNextPage()
                    }
                    if (contentType == ContentType.SERIES) {
                        state.channels.getOrNull(position)?.let { channel ->
                            viewLifecycleOwner.lifecycleScope.launch {
                                seriesEpisodesLoader.prefetchIfAbsent(channel, sourceUpdatedAt)
                            }
                        }
                    }
                }
            },
        )

        bridgeAdapter.setAdapterListener(object : ItemBridgeAdapter.AdapterListener() {
            override fun onBind(viewHolder: ItemBridgeAdapter.ViewHolder) {
                viewHolder.itemView.setOnClickListener {
                    val browseItem = viewHolder.item as? BrowseItem ?: return@setOnClickListener
                    browseItem.channel?.let(::openContent)
                }
            }
        })
    }

    private fun cardPresenterFor(contentType: ContentType): Presenter =
        if (contentType == ContentType.LIVE) liveCardPresenter else posterCardPresenter

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderChannels(state)
                }
            }
        }
    }

    private fun renderChannels(state: CategoryDetailUiState) {
        if (state.isLoading && state.channels.isEmpty()) {
            return
        }

        binding.categoryDetailTitle.text = state.categoryName
        if (state.totalCount > 0) {
            binding.categoryDetailCount.text = getString(R.string.category_count, state.totalCount)
            binding.categoryDetailCount.isVisible = true
        } else {
            binding.categoryDetailCount.isVisible = false
        }

        val channelIds = state.channels.map { it.id }
        if (channelIds == renderedChannelIds && state.favoriteIds == renderedFavoriteIds) {
            return
        }

        if (renderedChannelIds.isNotEmpty() &&
            channelIds.size > renderedChannelIds.size &&
            channelIds.take(renderedChannelIds.size) == renderedChannelIds &&
            state.favoriteIds == renderedFavoriteIds
        ) {
            state.channels
                .drop(renderedChannelIds.size)
                .forEach { channel ->
                    gridAdapter.add(channel.toBrowseItem(state.favoriteIds, state.contentType))
                }
            renderedChannelIds = channelIds
            return
        }

        gridAdapter.clear()
        state.channels.forEach { channel ->
            gridAdapter.add(channel.toBrowseItem(state.favoriteIds, state.contentType))
        }
        renderedChannelIds = channelIds
        renderedFavoriteIds = state.favoriteIds
        if (!hasInitialFocus && state.channels.isNotEmpty()) {
            hasInitialFocus = true
            requestGridFocus()
        }
    }

    private fun openContent(channel: Channel) {
        when (channel.contentType) {
            ContentType.LIVE, ContentType.MOVIE -> {
                startActivity(PlayerActivity.createIntent(requireContext(), channel, channel.categoryId))
            }
            ContentType.SERIES -> replaceContent(SeriesDetailsFragment.newInstance(channel))
            ContentType.EPISODE -> replaceContent(ChannelDetailsFragment.newInstance(channel))
        }
    }

    private fun readCategoryArg(): Category {
        val args = requireArguments()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args.getParcelable(ARG_CATEGORY, Category::class.java)
        } else {
            @Suppress("DEPRECATION")
            args.getParcelable(ARG_CATEGORY)
        } ?: throw IllegalArgumentException("Category required")
    }

    private fun Channel.toBrowseItem(
        favoriteIds: Set<String>,
        contentType: ContentType,
    ): BrowseItem =
        BrowseItem(
            id = "channel_$id",
            title = name,
            subtitle = null,
            imageUrl = logoUrl,
            type = BrowseItemType.CHANNEL,
            channel = this,
            badge = badgeFor(contentType),
            usePosterLayout = contentType != ContentType.LIVE,
            isFavorite = id in favoriteIds,
        )

    private fun badgeFor(contentType: ContentType): String = when (contentType) {
        ContentType.LIVE -> getString(R.string.live_badge)
        ContentType.MOVIE -> getString(R.string.movie_badge)
        ContentType.SERIES -> getString(R.string.series_badge)
        ContentType.EPISODE -> getString(R.string.episode_badge)
    }

    private fun gridColumnsFor(contentType: ContentType): Int = when (contentType) {
        ContentType.LIVE -> LIVE_GRID_COLUMNS
        else -> POSTER_GRID_COLUMNS
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderedChannelIds = emptyList()
        renderedFavoriteIds = emptySet()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY = "arg_category"
        private const val ARG_SOURCE_UPDATED_AT = "arg_source_updated_at"
        private const val LIVE_GRID_COLUMNS = 3
        private const val POSTER_GRID_COLUMNS = 3
        private const val LOAD_MORE_THRESHOLD = 12

        fun newInstance(category: Category, sourceUpdatedAt: Long): CategoryDetailFragment =
            CategoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CATEGORY, category)
                    putLong(ARG_SOURCE_UPDATED_AT, sourceUpdatedAt)
                }
            }
    }
}
