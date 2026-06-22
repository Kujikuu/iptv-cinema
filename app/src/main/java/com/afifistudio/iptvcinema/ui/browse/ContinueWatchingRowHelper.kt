package com.afifistudio.iptvcinema.ui.browse

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.data.cache.SeriesEpisodesLoader
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.SourceDao
import com.afifistudio.iptvcinema.data.local.toDomain
import com.afifistudio.iptvcinema.data.repository.WatchHistoryRepository
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContinueWatchingItem
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.firstEpisode
import com.afifistudio.iptvcinema.domain.model.toChannel
import com.afifistudio.iptvcinema.ui.details.ChannelDetailsFragment
import com.afifistudio.iptvcinema.ui.details.SeriesDetailsFragment
import com.afifistudio.iptvcinema.ui.library.LibraryListFragment
import com.afifistudio.iptvcinema.ui.library.LibraryType
import com.afifistudio.iptvcinema.ui.player.PlayerActivity
import com.afifistudio.iptvcinema.ui.replaceContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

object ContinueWatchingRowHelper {
    const val HEADER_ID = 9_001L
    const val SEE_ALL_ITEM_ID = "cw_see_all"

    fun buildRow(
        context: Context,
        header: String,
        items: List<ContinueWatchingItem>,
        favoriteIds: Set<String>,
        totalCount: Int,
        rowLimit: Int,
        contentCardPresenter: ContentCardPresenter,
    ): ListRow {
        val adapter = ArrayObjectAdapter(contentCardPresenter)
        items.forEach { item ->
            adapter.add(item.toBrowseItem(context, favoriteIds))
        }
        if (totalCount > rowLimit) {
            adapter.add(
                BrowseItem(
                    id = SEE_ALL_ITEM_ID,
                    title = context.getString(R.string.continue_watching_see_all),
                    type = BrowseItemType.ACTION,
                ),
            )
        }
        val headerText = context.getString(R.string.row_header_count, header, items.size)
        return ListRow(HeaderItem(HEADER_ID, headerText), adapter)
    }

    fun isContinueWatchingRow(row: Any?): Boolean =
        row is ListRow && row.headerItem.id == HEADER_ID

    fun isSeeAllItem(item: BrowseItem?): Boolean = item?.id == SEE_ALL_ITEM_ID
}

@Singleton
class ContinueWatchingNavigator @Inject constructor(
    private val channelDao: ChannelDao,
    private val sourceDao: SourceDao,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val seriesEpisodesLoader: SeriesEpisodesLoader,
) {
    suspend fun openItem(fragment: Fragment, item: ContinueWatchingItem) {
        when (item.channel.contentType) {
            ContentType.MOVIE, ContentType.LIVE -> playChannel(fragment, item.channel)
            ContentType.EPISODE -> playEpisode(fragment, item)
            ContentType.SERIES -> playSeries(fragment, item)
        }
    }

    fun openSeeAll(fragment: Fragment) {
        fragment.replaceContent(LibraryListFragment.newInstance(LibraryType.CONTINUE_WATCHING))
    }

    private suspend fun playSeries(fragment: Fragment, item: ContinueWatchingItem) {
        val series = resolveSeriesChannel(item) ?: run {
            fragment.replaceContent(ChannelDetailsFragment.newInstance(item.channel))
            return
        }
        val seriesId = series.externalId ?: series.id
        val latestEpisode = watchHistoryRepository.getLatestEpisodeForSeries(
            sourceId = series.sourceId,
            seriesId = seriesId,
        )
        if (latestEpisode != null) {
            playEpisode(fragment, latestEpisode)
            return
        }
        playFirstEpisode(fragment, series)
    }

    private suspend fun playFirstEpisode(fragment: Fragment, series: Channel) {
        val seriesId = series.externalId ?: series.id
        val sourceUpdatedAt = sourceDao.getById(series.sourceId)?.updatedAt ?: 0L
        val episodes = runCatching {
            withContext(Dispatchers.IO) {
                seriesEpisodesLoader.loadEpisodes(series, sourceUpdatedAt)
            }
        }.getOrNull().orEmpty()
        val first = firstEpisode(episodes)
        if (first == null) {
            fragment.replaceContent(SeriesDetailsFragment.newInstance(series))
            return
        }
        playChannel(fragment, first.toChannel(series))
    }

    private fun playEpisode(fragment: Fragment, item: ContinueWatchingItem) {
        playChannel(fragment, item.channel)
    }

    private fun playChannel(fragment: Fragment, channel: Channel) {
        fragment.startActivity(
            PlayerActivity.createIntent(
                fragment.requireContext(),
                channel,
                channel.categoryId,
            ),
        )
    }

    private suspend fun resolveSeriesChannel(item: ContinueWatchingItem): Channel? {
        if (item.channel.contentType == ContentType.SERIES) {
            return item.channel
        }
        val seriesId = item.seriesId ?: item.channel.seriesId ?: return null
        return withContext(Dispatchers.IO) {
            channelDao.getByExternalId(
                item.channel.sourceId,
                seriesId,
                ContentType.SERIES,
            )?.toDomain(item.seriesName ?: item.channel.seriesName)
        }
    }
}
