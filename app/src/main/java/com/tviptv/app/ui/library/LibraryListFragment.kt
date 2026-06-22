package com.tviptv.app.ui.library

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.VerticalGridPresenter
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.lifecycleScope
import com.tviptv.app.R
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.domain.model.ContinueWatchingItem
import com.tviptv.app.ui.ContentFocusHandler
import com.tviptv.app.ui.HomeChromeHost
import com.tviptv.app.ui.common.TvFocusCoordinator
import com.tviptv.app.ui.registerContentFocusHandler
import com.tviptv.app.ui.requestChromeFocus
import com.tviptv.app.ui.unregisterContentFocusHandler
import com.tviptv.app.ui.browse.BrowseItem
import com.tviptv.app.ui.browse.BrowseItemType
import com.tviptv.app.ui.browse.BrowseViewModel
import com.tviptv.app.ui.browse.ContinueWatchingNavigator
import com.tviptv.app.ui.browse.ContentCardPresenter
import com.tviptv.app.ui.browse.toBrowseItem
import com.tviptv.app.ui.details.ChannelDetailsFragment
import com.tviptv.app.ui.details.SeriesDetailsFragment
import com.tviptv.app.ui.replaceContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LibraryListFragment : VerticalGridSupportFragment() {

    @Inject lateinit var continueWatchingNavigator: ContinueWatchingNavigator

    private val browseViewModel: BrowseViewModel by viewModels({ requireActivity() })
    private lateinit var libraryType: LibraryType
    private lateinit var gridAdapter: ArrayObjectAdapter
    private val contentCardPresenter = ContentCardPresenter()
    private var continueWatchingItems: List<ContinueWatchingItem> = emptyList()
    private var hasInitialFocus = false
    private val gridColumns = 4
    private var gridView: VerticalGridView? = null
    private var selectedGridPosition = 0

    private val contentFocusHandler = object : ContentFocusHandler {
        override fun requestInitialFocus(): Boolean {
            view?.post {
                val grid = gridView ?: return@post
                if ((adapter?.size() ?: 0) > 0) {
                    grid.selectedPosition = 0
                    grid.requestFocus()
                }
            }
            return true
        }

        override fun canFocusUpToChrome(): Boolean =
            TvFocusCoordinator.isFirstGridRow(selectedGridPosition, gridColumns)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryType = requireArguments().getSerializable(ARG_LIBRARY_TYPE) as? LibraryType
            ?: LibraryType.CONTINUE_WATCHING

        val gridPresenter = VerticalGridPresenter().apply { numberOfColumns = 4 }
        gridAdapter = ArrayObjectAdapter(contentCardPresenter)
        setGridPresenter(gridPresenter)
        adapter = gridAdapter

        title = when (libraryType) {
            LibraryType.CONTINUE_WATCHING -> getString(R.string.settings_continue_watching)
            LibraryType.FAVORITES -> getString(R.string.settings_favorites)
        }

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            val browseItem = item as? BrowseItem ?: return@OnItemViewClickedListener
            handleItemClick(browseItem)
        }

        loadLibrary()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            gridView = view.findViewById(androidx.leanback.R.id.browse_grid)
            gridView?.let { grid ->
                TvFocusCoordinator.wireGridUp(
                    gridView = grid,
                    columns = gridColumns,
                    selectedPositionProvider = { selectedGridPosition },
                    requestChromeFocus = { requestChromeFocus() },
                )
                grid.addOnChildViewHolderSelectedListener(
                    object : androidx.leanback.widget.OnChildViewHolderSelectedListener() {
                        override fun onChildViewHolderSelected(
                            parent: androidx.recyclerview.widget.RecyclerView,
                            child: androidx.recyclerview.widget.RecyclerView.ViewHolder?,
                            position: Int,
                            subposition: Int,
                        ) {
                            selectedGridPosition = position
                        }
                    },
                )
            }
        }
        (activity as? HomeChromeHost)?.setChromeVisible(true)
    }

    override fun onResume() {
        super.onResume()
        registerContentFocusHandler(contentFocusHandler)
        if (libraryType == LibraryType.CONTINUE_WATCHING) {
            browseViewModel.refreshContinueWatching()
            loadLibrary()
        }
    }

    override fun onPause() {
        unregisterContentFocusHandler()
        super.onPause()
    }

    private fun loadLibrary() {
        lifecycleScope.launch {
            val sourceId = browseViewModel.uiState.value.selectedSourceId ?: return@launch
            when (libraryType) {
                LibraryType.CONTINUE_WATCHING -> {
                    continueWatchingItems = browseViewModel.getAllContinueWatching(sourceId)
                    renderContinueWatching(continueWatchingItems)
                }
                LibraryType.FAVORITES -> {
                    renderFavorites(browseViewModel.getAllFavorites(sourceId))
                }
            }
        }
    }

    private fun renderContinueWatching(items: List<ContinueWatchingItem>) {
        val newAdapter = ArrayObjectAdapter(contentCardPresenter)
        if (items.isEmpty()) {
            newAdapter.add(emptyStateItem())
        } else {
            val favoriteIds = browseViewModel.uiState.value.favorites.map { it.id }.toSet()
            items.forEach { item ->
                newAdapter.add(item.toBrowseItem(requireContext(), favoriteIds))
            }
        }
        gridAdapter = newAdapter
        adapter = newAdapter
        requestInitialGridFocusIfNeeded(newAdapter.size())
    }

    private fun renderFavorites(channels: List<Channel>) {
        val newAdapter = ArrayObjectAdapter(contentCardPresenter)
        if (channels.isEmpty()) {
            newAdapter.add(emptyStateItem())
        } else {
            channels.forEach { channel ->
                newAdapter.add(channel.toBrowseItem(context = requireContext()))
            }
        }
        gridAdapter = newAdapter
        adapter = newAdapter
        requestInitialGridFocusIfNeeded(newAdapter.size())
    }

    private fun requestInitialGridFocusIfNeeded(itemCount: Int) {
        if (hasInitialFocus || itemCount == 0) return
        hasInitialFocus = true
        view?.post {
            val grid = gridView ?: return@post
            grid.selectedPosition = 0
            grid.requestFocus()
        }
    }

    private fun emptyStateItem(): BrowseItem =
        BrowseItem(
            id = "library_empty",
            title = getString(R.string.library_empty),
            type = BrowseItemType.ACTION,
        )

    private fun handleItemClick(browseItem: BrowseItem) {
        if (libraryType != LibraryType.CONTINUE_WATCHING) {
            browseItem.channel?.let { channel ->
                when (channel.contentType) {
                    ContentType.SERIES -> replaceContent(SeriesDetailsFragment.newInstance(channel))
                    else -> replaceContent(ChannelDetailsFragment.newInstance(channel))
                }
            }
            return
        }
        val cwItem = continueWatchingItems.firstOrNull {
            it.channel.id == browseItem.channel?.id &&
                it.channel.contentType == browseItem.channel?.contentType
        } ?: return
        lifecycleScope.launch {
            continueWatchingNavigator.openItem(this@LibraryListFragment, cwItem)
        }
    }

    companion object {
        private const val ARG_LIBRARY_TYPE = "arg_library_type"

        fun newInstance(type: LibraryType): LibraryListFragment =
            LibraryListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_LIBRARY_TYPE, type)
                }
            }
    }
}
