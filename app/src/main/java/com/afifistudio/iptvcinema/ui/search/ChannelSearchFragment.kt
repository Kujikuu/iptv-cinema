package com.afifistudio.iptvcinema.ui.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.ui.ContentFocusHandler
import com.afifistudio.iptvcinema.ui.HomeChromeHost
import com.afifistudio.iptvcinema.ui.common.TvFocusCoordinator
import com.afifistudio.iptvcinema.ui.registerContentFocusHandler
import com.afifistudio.iptvcinema.ui.requestChromeFocus
import com.afifistudio.iptvcinema.ui.unregisterContentFocusHandler
import com.afifistudio.iptvcinema.ui.browse.BrowseItem
import com.afifistudio.iptvcinema.ui.browse.BrowseItemType
import com.afifistudio.iptvcinema.ui.browse.ContentCardPresenter
import com.afifistudio.iptvcinema.ui.browse.StreamingRowPresenter
import com.afifistudio.iptvcinema.ui.details.ChannelDetailsFragment
import com.afifistudio.iptvcinema.ui.details.SeriesDetailsFragment
import com.afifistudio.iptvcinema.ui.replaceContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelSearchFragment : SearchSupportFragment() {

    private val viewModel: SearchViewModel by viewModels()
    private val rowsAdapter = ArrayObjectAdapter(StreamingRowPresenter())
    private val cardPresenter by lazy { ContentCardPresenter(onLongPress = ::onEpisodeLongPress) }
    private var resultsGridView: VerticalGridView? = null
    private var selectedResultsRow = 0

    private val contentFocusHandler = object : ContentFocusHandler {
        override fun requestInitialFocus(): Boolean {
            view?.post { focusSearchField() }
            return true
        }

        override fun canFocusUpToChrome(): Boolean =
            selectedResultsRow == 0 && rowsAdapter.size() > 0
    }

    private fun focusSearchField() {
        val editor = view?.findViewById<View>(androidx.leanback.R.id.lb_search_text_editor)
        if (editor?.requestFocus() == true) return
        view?.findViewById<View>(androidx.leanback.R.id.lb_search_bar)?.requestFocus()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSearchResultProvider(object : SearchResultProvider {
            override fun getResultsAdapter(): ObjectAdapter = rowsAdapter

            override fun onQueryTextChange(newQuery: String): Boolean {
                viewModel.search(newQuery)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.search(query)
                return true
            }
        })
        setOnItemViewClickedListener(
            OnItemViewClickedListener { _, item, _, _ ->
                val browseItem = item as? BrowseItem ?: return@OnItemViewClickedListener
                browseItem.channel?.let(::openContent)
            },
        )
        observeResults()
        wireChromeFocusBridge()
        (activity as? HomeChromeHost)?.setChromeVisible(true)
    }

    override fun onResume() {
        super.onResume()
        registerContentFocusHandler(contentFocusHandler)
    }

    override fun onPause() {
        unregisterContentFocusHandler()
        super.onPause()
    }

    private fun wireChromeFocusBridge() {
        view?.post {
            resultsGridView = view?.findViewById(androidx.leanback.R.id.browse_grid)
            resultsGridView?.let { grid ->
                TvFocusCoordinator.wireLeanbackRowsUp(
                    verticalGridView = grid,
                    selectedPositionProvider = { selectedResultsRow },
                    canFocusUpProvider = { selectedResultsRow == 0 && rowsAdapter.size() > 0 },
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
                            selectedResultsRow = position
                        }
                    },
                )
            }
        }
    }

    private fun observeResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderResults(state.results)
                }
            }
        }
    }

    private fun renderResults(results: List<Channel>) {
        rowsAdapter.clear()
        if (results.isEmpty()) {
            return
        }
        val adapter = ArrayObjectAdapter(cardPresenter)
        results.forEach { channel ->
            adapter.add(
                BrowseItem(
                    id = "search_${channel.id}",
                    title = channel.name,
                    subtitle = channel.categoryName,
                    imageUrl = channel.logoUrl,
                    type = BrowseItemType.CHANNEL,
                    channel = channel,
                    badge = badgeFor(channel.contentType),
                    usePosterLayout = channel.contentType != ContentType.LIVE,
                ),
            )
        }
        rowsAdapter.add(ListRow(HeaderItem(0, getString(R.string.search_title)), adapter))
    }

    private fun openContent(channel: Channel) {
        val fragment = when (channel.contentType) {
            ContentType.SERIES -> SeriesDetailsFragment.newInstance(channel)
            else -> ChannelDetailsFragment.newInstance(channel)
        }
        replaceContent(fragment)
    }

    private fun onEpisodeLongPress(item: BrowseItem) {
        val channel = item.channel ?: return
        if (channel.contentType != ContentType.EPISODE) return
        replaceContent(SeriesDetailsFragment.newInstance(channel.toSeriesChannel(), highlightEpisodeId = channel.id))
    }

    private fun badgeFor(contentType: ContentType): String = when (contentType) {
        ContentType.LIVE -> getString(R.string.live_badge)
        ContentType.MOVIE -> getString(R.string.movie_badge)
        ContentType.SERIES -> getString(R.string.series_badge)
        ContentType.EPISODE -> getString(R.string.episode_badge)
    }
}
