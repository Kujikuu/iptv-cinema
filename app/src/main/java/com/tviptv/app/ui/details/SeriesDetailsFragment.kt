package com.tviptv.app.ui.details

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
import com.tviptv.app.R
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.domain.model.Episode
import com.tviptv.app.domain.model.toChannel
import com.tviptv.app.ui.ContentFocusHandler
import com.tviptv.app.ui.common.TvFocusCoordinator
import com.tviptv.app.ui.registerContentFocusHandler
import com.tviptv.app.ui.requestChromeFocus
import com.tviptv.app.ui.unregisterContentFocusHandler
import com.tviptv.app.ui.browse.ActionCardPresenter
import com.tviptv.app.ui.browse.BrowseItem
import com.tviptv.app.ui.browse.BrowseItemType
import com.tviptv.app.ui.browse.BrowseViewModel
import com.tviptv.app.ui.browse.ContentCardPresenter
import com.tviptv.app.ui.browse.HeroCardPresenter
import com.tviptv.app.ui.browse.StreamingRowPresenter
import com.tviptv.app.ui.browse.toBrowseItem
import com.tviptv.app.ui.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SeriesDetailsFragment : RowsSupportFragment() {

    private val viewModel: SeriesDetailsViewModel by viewModels()
    private val browseViewModel: BrowseViewModel by viewModels({ requireActivity() })
    private lateinit var series: Channel
    private var highlightEpisodeId: String? = null
    private lateinit var rowsAdapter: ArrayObjectAdapter
    private val contentCardPresenter = ContentCardPresenter()
    private val heroCardPresenter = HeroCardPresenter()
    private val actionCardPresenter = ActionCardPresenter()
    private var renderedFingerprint: String? = null
    private var pendingHighlightEpisodeId: String? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        series = requireArguments().getParcelable(ARG_SERIES)
            ?: throw IllegalArgumentException("Series required")
        highlightEpisodeId = arguments?.getString(ARG_HIGHLIGHT_EPISODE_ID)

        rowsAdapter = ArrayObjectAdapter(StreamingRowPresenter())
        adapter = rowsAdapter

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            val browseItem = item as? BrowseItem ?: return@OnItemViewClickedListener
            val episode = browseItem.channel ?: return@OnItemViewClickedListener
            browseViewModel.rememberLastWatched(episode)
            startActivity(PlayerActivity.createIntent(requireContext(), episode, episode.categoryId))
        }

        viewModel.load(series, browseViewModel.uiState.value.sourceUpdatedAt)
        observeState()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TvFocusCoordinator.wireLeanbackRowsUp(
            verticalGridView = verticalGridView,
            selectedPositionProvider = { selectedPosition },
            canFocusUpProvider = { selectedPosition == 0 },
            requestChromeFocus = { requestChromeFocus() },
        )
    }

    override fun onResume() {
        super.onResume()
        registerContentFocusHandler(contentFocusHandler)
        browseViewModel.refreshContinueWatching()
    }

    override fun onPause() {
        unregisterContentFocusHandler()
        super.onPause()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: SeriesDetailsUiState) {
        val fingerprint = buildSeriesFingerprint(state)
        if (fingerprint == renderedFingerprint) {
            return
        }
        renderedFingerprint = fingerprint

        rowsAdapter.clear()

        if (state.error != null && state.episodes.isEmpty()) {
            rowsAdapter.add(buildMessageRow(state.error))
            return
        }

        if (state.isLoading) {
            return
        }

        rowsAdapter.add(buildOverviewRow(state.series ?: series))

        val continueWatchingByEpisodeId = browseViewModel.uiState.value.continueWatching
            .filter { it.channel.contentType == ContentType.EPISODE }
            .associateBy { it.channel.id }

        var highlightRowIndex = -1
        state.episodes
            .sortedWith(
                compareByDescending<Episode> { it.seasonNumber }
                    .thenByDescending { it.episodeNumber },
            )
            .groupBy { it.seasonNumber }
            .toSortedMap(reverseOrder())
            .forEach { (season, episodes) ->
                val rowIndex = rowsAdapter.size()
                val row = buildSeasonRow(season, episodes, continueWatchingByEpisodeId)
                rowsAdapter.add(row)
                if (highlightEpisodeId != null &&
                    episodes.any { it.id == highlightEpisodeId }
                ) {
                    highlightRowIndex = rowIndex
                    pendingHighlightEpisodeId = highlightEpisodeId
                }
            }

        if (highlightRowIndex >= 0) {
            val targetRow = highlightRowIndex
            val targetEpisodeId = pendingHighlightEpisodeId
            view?.post {
                setSelectedPosition(targetRow, false)
                focusEpisodeInRow(targetRow, targetEpisodeId)
            }
            highlightEpisodeId = null
        } else if (!hasInitialFocus && rowsAdapter.size() > 0) {
            hasInitialFocus = true
            view?.post {
                setSelectedPosition(0, false)
                verticalGridView?.requestFocus()
            }
        }
    }

    private fun focusEpisodeInRow(rowIndex: Int, episodeId: String?) {
        if (episodeId.isNullOrBlank()) return
        val row = rowsAdapter.get(rowIndex) as? ListRow ?: return
        val rowAdapter = row.adapter as? ArrayObjectAdapter ?: return
        val episodeIndex = (0 until rowAdapter.size()).firstOrNull { index ->
            (rowAdapter.get(index) as? BrowseItem)?.channel?.id == episodeId
        } ?: return
        setSelectedPosition(rowIndex, false)
        verticalGridView.post {
            val rowView = verticalGridView.findViewHolderForAdapterPosition(rowIndex)?.itemView
            val gridView = TvFocusCoordinator.findHorizontalGridView(rowView)
            if (gridView != null) {
                TvFocusCoordinator.focusHorizontalGridAt(gridView, episodeIndex)
            }
        }
    }

    private fun buildSeriesFingerprint(state: SeriesDetailsUiState): String = buildString {
        append(state.isLoading)
        append('|')
        append(state.error)
        append('|')
        append(state.series?.id)
        append('|')
        append(highlightEpisodeId)
        append('|')
        state.episodes.joinTo(this, separator = ",") { it.id }
    }

    private fun buildOverviewRow(series: Channel): ListRow {
        val adapter = ArrayObjectAdapter(heroCardPresenter)
        adapter.add(
            BrowseItem(
                id = "series_overview",
                title = series.name,
                subtitle = series.plot ?: series.categoryName,
                imageUrl = series.logoUrl,
                type = BrowseItemType.HERO,
                channel = series,
                badge = getString(R.string.series_badge),
                usePosterLayout = true,
            ),
        )
        return ListRow(HeaderItem(OVERVIEW_HEADER_ID, series.name), adapter)
    }

    private fun buildSeasonRow(
        seasonNumber: Int,
        episodes: List<Episode>,
        continueWatchingByEpisodeId: Map<String, com.tviptv.app.domain.model.ContinueWatchingItem>,
    ): ListRow {
        val adapter = ArrayObjectAdapter(contentCardPresenter)
        val favoriteIds = browseViewModel.uiState.value.favorites.map { it.id }.toSet()
        episodes.forEach { episode ->
            val episodeChannel = episode.toChannel(series)
            val cwItem = continueWatchingByEpisodeId[episode.id]
            if (cwItem != null) {
                adapter.add(cwItem.toBrowseItem(requireContext(), favoriteIds))
            } else {
                adapter.add(
                    BrowseItem(
                        id = "episode_${episode.id}",
                        title = episode.title,
                        subtitle = getString(
                            R.string.season_episode_label,
                            seasonNumber,
                            episode.episodeNumber,
                        ),
                        imageUrl = episode.imageUrl,
                        type = BrowseItemType.CHANNEL,
                        channel = episodeChannel,
                        badge = getString(R.string.episode_badge),
                    ),
                )
            }
        }
        return ListRow(
            HeaderItem(seasonNumber.toLong(), getString(R.string.season_header, seasonNumber)),
            adapter,
        )
    }

    private fun buildMessageRow(message: String): ListRow {
        val adapter = ArrayObjectAdapter(actionCardPresenter)
        adapter.add(
            BrowseItem(
                id = "series_error",
                title = message,
                type = BrowseItemType.ACTION,
            ),
        )
        return ListRow(HeaderItem(ERROR_HEADER_ID, getString(R.string.error_loading)), adapter)
    }

    companion object {
        private const val ARG_SERIES = "arg_series"
        private const val ARG_HIGHLIGHT_EPISODE_ID = "arg_highlight_episode_id"
        private const val OVERVIEW_HEADER_ID = 200L
        private const val ERROR_HEADER_ID = 201L

        fun newInstance(
            series: Channel,
            highlightEpisodeId: String? = null,
        ): SeriesDetailsFragment =
            SeriesDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SERIES, series)
                    putString(ARG_HIGHLIGHT_EPISODE_ID, highlightEpisodeId)
                }
            }
    }
}
