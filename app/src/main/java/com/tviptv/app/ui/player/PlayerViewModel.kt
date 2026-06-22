package com.tviptv.app.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tviptv.app.data.cache.SeriesEpisodesLoader
import com.tviptv.app.data.local.dao.ChannelDao
import com.tviptv.app.data.local.dao.FavoriteDao
import com.tviptv.app.data.local.dao.SourceDao
import com.tviptv.app.data.local.entity.FavoriteEntity
import com.tviptv.app.data.local.toDomain
import com.tviptv.app.data.player.PlayerEpgRepository
import com.tviptv.app.data.prefs.AppPreferences
import com.tviptv.app.data.repository.WatchHistoryRepository
import com.tviptv.app.domain.model.Channel
import com.tviptv.app.domain.model.ContentType
import com.tviptv.app.domain.model.Episode
import com.tviptv.app.domain.model.NowNextEpg
import com.tviptv.app.domain.model.findNextEpisode
import com.tviptv.app.domain.model.toChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EpgUiState(
    val nowTitle: String? = null,
    val nowDescription: String? = null,
    val nowProgress: Float = 0f,
    val nowStartTime: String? = null,
    val nowEndTime: String? = null,
    val nextTitle: String? = null,
    val nextStartTime: String? = null,
)

data class ChannelListUiState(
    val channels: List<Channel> = emptyList(),
    val currentIndex: Int = 0,
    val epgTitles: Map<String, String> = emptyMap(),
)

data class NextEpisodeUiState(
    val nextEpisode: Episode? = null,
    val countdownSeconds: Int? = null,
    val isAutoplayDismissed: Boolean = false,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val channelDao: ChannelDao,
    private val sourceDao: SourceDao,
    private val favoriteDao: FavoriteDao,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val epgRepository: PlayerEpgRepository,
    private val seriesEpisodesLoader: SeriesEpisodesLoader,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    private val _playlist = MutableStateFlow<List<Channel>>(emptyList())
    val playlist: StateFlow<List<Channel>> = _playlist.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _overlayMode = MutableStateFlow(PlayerOverlayMode.Hidden)
    val overlayMode: StateFlow<PlayerOverlayMode> = _overlayMode.asStateFlow()

    private val _epgState = MutableStateFlow(EpgUiState())
    val epgState: StateFlow<EpgUiState> = _epgState.asStateFlow()

    private val _channelListState = MutableStateFlow(ChannelListUiState())
    val channelListState: StateFlow<ChannelListUiState> = _channelListState.asStateFlow()

    private val _savedPositionMs = MutableStateFlow(0L)
    val savedPositionMs: StateFlow<Long> = _savedPositionMs.asStateFlow()

    private val _nextEpisodeUiState = MutableStateFlow(NextEpisodeUiState())
    val nextEpisodeUiState: StateFlow<NextEpisodeUiState> = _nextEpisodeUiState.asStateFlow()

    private var positionSaveJob: Job? = null
    private var epgRefreshJob: Job? = null
    private var parentSeries: Channel? = null
    private var autoplayTriggered = false

    fun initialize(channel: Channel, categoryId: String?) {
        viewModelScope.launch {
            val entities = if (!categoryId.isNullOrBlank()) {
                channelDao.getByCategoryPage(
                    sourceId = channel.sourceId,
                    categoryId = categoryId,
                    contentType = channel.contentType,
                    limit = ZAPPING_PLAYLIST_LIMIT,
                    offset = 0,
                )
            } else {
                channelDao.getBySource(
                    sourceId = channel.sourceId,
                    contentType = channel.contentType,
                ).take(ZAPPING_PLAYLIST_LIMIT)
            }
            val channels = entities.map { entity ->
                entity.toDomain(channel.categoryName ?: entity.categoryId)
            }
            _playlist.value = channels
            val index = channels.indexOfFirst { it.id == channel.id }.coerceAtLeast(0)
            _currentIndex.value = index
            _channelListState.value = ChannelListUiState(channels = channels, currentIndex = index)
            refreshFavorite(channels.getOrNull(index) ?: channel)
            loadSavedPosition(channel)
            refreshEpg(channel)
            preloadAdjacentLogos(index, channels)
            initializeEpisodeAutoplay(channel)
        }
    }

    fun prepareAutoplayForChannel(channel: Channel) {
        viewModelScope.launch {
            autoplayTriggered = false
            initializeEpisodeAutoplay(channel)
        }
    }

    fun onPlaybackProgress(positionMs: Long, durationMs: Long) {
        if (!appPreferences.isAutoplayNextEpisodeEnabled()) return
        val state = _nextEpisodeUiState.value
        if (state.isAutoplayDismissed || state.nextEpisode == null) return
        if (durationMs <= 0L) return

        val remainingMs = durationMs - positionMs
        if (remainingMs > AUTOPLAY_TRIGGER_MS) {
            if (state.countdownSeconds != null) {
                _nextEpisodeUiState.update { it.copy(countdownSeconds = null) }
            }
            return
        }

        val seconds = ((remainingMs + 999L) / 1000L).toInt().coerceIn(0, AUTOPLAY_COUNTDOWN_SECONDS)
        if (state.countdownSeconds != seconds) {
            _nextEpisodeUiState.update { it.copy(countdownSeconds = seconds) }
        }
    }

    fun dismissAutoplay() {
        _nextEpisodeUiState.update { it.copy(isAutoplayDismissed = true, countdownSeconds = null) }
    }

    fun playNextEpisodeNow(): Channel? {
        if (autoplayTriggered) return null
        val nextChannel = resolveNextEpisodeChannel() ?: return null
        autoplayTriggered = true
        resetAutoplayState()
        return nextChannel
    }

    fun consumeAutoplayOnEnded(): Channel? {
        if (autoplayTriggered) return null
        if (!appPreferences.isAutoplayNextEpisodeEnabled()) return null
        val state = _nextEpisodeUiState.value
        if (state.isAutoplayDismissed || state.nextEpisode == null) return null
        val nextChannel = resolveNextEpisodeChannel() ?: return null
        autoplayTriggered = true
        resetAutoplayState()
        return nextChannel
    }

    fun isAutoplayOverlayVisible(): Boolean {
        val state = _nextEpisodeUiState.value
        return state.nextEpisode != null &&
            !state.isAutoplayDismissed &&
            state.countdownSeconds != null &&
            appPreferences.isAutoplayNextEpisodeEnabled()
    }

    private fun resolveNextEpisodeChannel(): Channel? {
        val nextEpisode = _nextEpisodeUiState.value.nextEpisode ?: return null
        val series = parentSeries ?: return null
        return nextEpisode.toChannel(series)
    }

    private suspend fun initializeEpisodeAutoplay(channel: Channel) {
        autoplayTriggered = false
        if (channel.contentType != ContentType.EPISODE) {
            parentSeries = null
            _nextEpisodeUiState.value = NextEpisodeUiState()
            return
        }
        val seriesId = channel.seriesId?.takeIf { it.isNotBlank() }
        if (seriesId == null) {
            parentSeries = null
            _nextEpisodeUiState.value = NextEpisodeUiState()
            return
        }
        val seriesEntity = channelDao.getByExternalId(
            sourceId = channel.sourceId,
            externalId = seriesId,
            contentType = ContentType.SERIES,
        )
        if (seriesEntity == null) {
            parentSeries = null
            _nextEpisodeUiState.value = NextEpisodeUiState()
            return
        }
        val series = seriesEntity.toDomain(channel.categoryName ?: seriesEntity.categoryId)
        parentSeries = series
        val source = sourceDao.getById(channel.sourceId)
        if (source == null) {
            _nextEpisodeUiState.value = NextEpisodeUiState()
            return
        }
        runCatching {
            seriesEpisodesLoader.loadEpisodes(series, source.updatedAt)
        }.onSuccess { episodes ->
            _nextEpisodeUiState.value = NextEpisodeUiState(
                nextEpisode = findNextEpisode(episodes, channel.id),
            )
        }.onFailure {
            _nextEpisodeUiState.value = NextEpisodeUiState()
        }
    }

    private fun resetAutoplayState() {
        _nextEpisodeUiState.value = NextEpisodeUiState()
        parentSeries = null
    }

    fun setOverlayMode(mode: PlayerOverlayMode) {
        _overlayMode.value = mode
    }

    fun currentChannel(): Channel? = _playlist.value.getOrNull(_currentIndex.value)

    fun hasNext(): Boolean = _currentIndex.value < _playlist.value.lastIndex

    fun hasPrevious(): Boolean = _currentIndex.value > 0

    fun nextChannel(): Channel? {
        if (!hasNext()) return null
        _currentIndex.update { it + 1 }
        val channel = currentChannel()
        if (channel != null) {
            onChannelChanged(channel)
        }
        return channel
    }

    fun previousChannel(): Channel? {
        if (!hasPrevious()) return null
        _currentIndex.update { it - 1 }
        val channel = currentChannel()
        if (channel != null) {
            onChannelChanged(channel)
        }
        return channel
    }

    fun selectChannelAtIndex(index: Int): Channel? {
        val channels = _playlist.value
        if (index !in channels.indices) return null
        _currentIndex.value = index
        val channel = channels[index]
        onChannelChanged(channel)
        return channel
    }

    suspend fun resolveChannelByNumber(number: Int): Channel? {
        if (number <= 0) return null
        val sourceId = currentChannel()?.sourceId ?: return null
        val fromDao = channelDao.getByChannelNumber(sourceId, ContentType.LIVE, number)?.toDomain()
            ?: channelDao.getLiveChannelBySortIndex(sourceId, number - 1)?.toDomain()
        return fromDao ?: _playlist.value.firstOrNull { it.channelNumber == number }
            ?: _playlist.value.getOrNull(number - 1)
    }

    fun toggleFavorite(onResult: (Boolean) -> Unit) {
        val channel = currentChannel() ?: return
        viewModelScope.launch {
            val currentlyFavorite = favoriteDao.isFavorite(
                channel.sourceId,
                channel.id,
                channel.contentType,
            )
            if (currentlyFavorite) {
                favoriteDao.delete(channel.sourceId, channel.id, channel.contentType)
            } else {
                favoriteDao.insert(
                    FavoriteEntity(
                        sourceId = channel.sourceId,
                        channelId = channel.id,
                        contentType = channel.contentType,
                    ),
                )
            }
            val updated = !currentlyFavorite
            _isFavorite.value = updated
            onResult(updated)
        }
    }

    fun refreshEpg(channel: Channel) {
        if (channel.contentType != ContentType.LIVE) {
            _epgState.value = EpgUiState()
            return
        }
        val streamId = channel.externalId ?: return
        epgRefreshJob?.cancel()
        epgRefreshJob = viewModelScope.launch {
            epgRepository.refreshIfNeeded(channel.sourceId, streamId)
            updateEpgUi(channel.sourceId, streamId)
            updateChannelListEpg(channel.sourceId)
        }
    }

    fun startPositionSaving(channel: Channel, getPosition: () -> Long, getDuration: () -> Long) {
        if (channel.contentType == ContentType.LIVE) return
        positionSaveJob?.cancel()
        positionSaveJob = viewModelScope.launch {
            while (isActive) {
                delay(POSITION_SAVE_INTERVAL_MS)
                savePosition(channel, getPosition(), getDuration())
            }
        }
    }

    fun stopPositionSaving() {
        positionSaveJob?.cancel()
        positionSaveJob = null
    }

    fun savePosition(channel: Channel, positionMs: Long, durationMs: Long) {
        if (channel.contentType == ContentType.LIVE) return
        viewModelScope.launch {
            watchHistoryRepository.savePosition(channel, positionMs, durationMs)
        }
    }

    fun rememberLastWatched(channel: Channel) {
        viewModelScope.launch {
            watchHistoryRepository.rememberLastWatched(channel)
        }
    }

    private suspend fun loadSavedPosition(channel: Channel) {
        if (channel.contentType == ContentType.LIVE) {
            _savedPositionMs.value = 0L
            return
        }
        _savedPositionMs.value = watchHistoryRepository.loadSavedPosition(channel)
    }

    private fun onChannelChanged(channel: Channel) {
        viewModelScope.launch {
            refreshFavorite(channel)
            loadSavedPosition(channel)
            refreshEpg(channel)
            _channelListState.update {
                it.copy(currentIndex = _currentIndex.value)
            }
            val index = _currentIndex.value
            preloadAdjacentLogos(index, _playlist.value)
        }
    }

    private fun preloadAdjacentLogos(index: Int, channels: List<Channel>) {
        _channelListState.update {
            it.copy(
                channels = channels,
                currentIndex = index,
            )
        }
    }

    private suspend fun refreshFavorite(channel: Channel) {
        _isFavorite.value = favoriteDao.isFavorite(
            channel.sourceId,
            channel.id,
            channel.contentType,
        )
    }

    private suspend fun updateEpgUi(sourceId: Long, streamId: String) {
        val nowNext = epgRepository.getNowNext(sourceId, streamId)
        _epgState.value = nowNext.toUiState()
    }

    private suspend fun updateChannelListEpg(sourceId: Long) {
        val channels = _playlist.value
        val titles = channels.associate { channel ->
            val streamId = channel.externalId ?: channel.id
            streamId to (epgRepository.getCurrentProgramTitle(sourceId, streamId) ?: "")
        }.filterValues { it.isNotBlank() }
        _channelListState.update { it.copy(epgTitles = titles) }
    }

    private fun NowNextEpg.toUiState(): EpgUiState {
        val nowMs = System.currentTimeMillis()
        val nowProgram = now
        val progress = if (nowProgram != null && nowProgram.endMs > nowProgram.startMs) {
            ((nowMs - nowProgram.startMs).toFloat() / (nowProgram.endMs - nowProgram.startMs))
                .coerceIn(0f, 1f)
        } else {
            0f
        }
        val nextStart = next?.startMs?.let { formatTime(it) }
        val nowStart = nowProgram?.startMs?.let { formatTime(it) }
        val nowEnd = nowProgram?.endMs?.let { formatTime(it) }
        return EpgUiState(
            nowTitle = nowProgram?.title,
            nowDescription = nowProgram?.description,
            nowProgress = progress,
            nowStartTime = nowStart,
            nowEndTime = nowEnd,
            nextTitle = next?.title,
            nextStartTime = nextStart,
        )
    }

    private fun formatTime(ms: Long): String {
        val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return format.format(java.util.Date(ms))
    }

    companion object {
        private const val ZAPPING_PLAYLIST_LIMIT = 500
        private const val POSITION_SAVE_INTERVAL_MS = 5000L
        const val AUTOPLAY_TRIGGER_MS = 15_000L
        const val AUTOPLAY_COUNTDOWN_SECONDS = 5
    }
}
