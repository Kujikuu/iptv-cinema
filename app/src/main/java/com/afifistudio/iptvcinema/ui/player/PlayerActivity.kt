package com.afifistudio.iptvcinema.ui.player

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.data.prefs.AppPreferences
import com.afifistudio.iptvcinema.databinding.ActivityPlayerBinding
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.Episode
import com.afifistudio.iptvcinema.domain.repository.IptvRepositoryFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    @Inject
    lateinit var repositoryFactory: IptvRepositoryFactory

    @Inject
    lateinit var appPreferences: AppPreferences

    private val playerViewModel: PlayerViewModel by viewModels()

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var channel: Channel
    private var categoryId: String? = null
    private var currentStreamUrl: String? = null
    private var resizeFit = true
    private var pendingResumePositionMs = 0L
    private var activeListPanelType: ListPanelType? = null
    private val toastHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var toastRunnable: Runnable? = null
    private var videoOnlyFallbackAttempted = false
    private val triedAudioTrackKeys = mutableSetOf<String>()

    private lateinit var overlayController: PlayerOverlayController
    private lateinit var remoteController: PlayerRemoteController
    private lateinit var seekController: PlayerSeekController
    private lateinit var zapToast: PlayerZapToastView
    private lateinit var channelListPanel: PlayerChannelListPanel
    private lateinit var listPanel: PlayerListPanel
    private lateinit var nextEpisodeController: PlayerNextEpisodeController
    private lateinit var numericEntryController: PlayerNumericEntryController

    private enum class ListPanelType {
        Subtitles,
        Audio,
        Settings,
        Episodes,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        channel = intent.getParcelableExtra(EXTRA_CHANNEL)
            ?: run {
                finish()
                return
            }
        categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID) ?: channel.categoryId

        initControllers()
        setupBackHandler()

        playerViewModel.initialize(channel, categoryId)
        bindChannelInfo(channel)
        setupControls()
        initializePlayer()
        loadStream(channel, showOverlay = false)
        observeViewModel()
    }

    private fun initControllers() {
        overlayController = PlayerOverlayController(
            overlayRoot = binding.playerOverlay,
            controlsRoot = binding.playerControlsRoot,
            onModeChanged = { mode ->
                playerViewModel.setOverlayMode(mode)
                if (mode == PlayerOverlayMode.Hidden) {
                    updatePauseOverlay()
                }
            },
        )
        remoteController = PlayerRemoteController()
        seekController = PlayerSeekController(
            seekContainer = binding.playerSeekContainer,
            timeBar = binding.playerTimeBar,
            positionLabel = binding.playerPosition,
            durationLabel = binding.playerDuration,
            seekPreviewLabel = binding.playerSeekPreview,
        )
        zapToast = PlayerZapToastView(
            root = binding.playerZapToast.root,
            logoView = binding.playerZapToast.zapLogo,
            titleView = binding.playerZapToast.zapTitle,
            numberView = binding.playerZapToast.zapNumber,
        )
        channelListPanel = PlayerChannelListPanel(
            panelRoot = binding.playerChannelListPanel,
            recyclerView = binding.playerChannelList,
            onChannelSelected = { selected ->
                val index = playerViewModel.playlist.value.indexOfFirst { it.id == selected.id }
                if (index >= 0) {
                    playerViewModel.selectChannelAtIndex(index)?.let { switchToChannel(it, showToast = true) }
                }
                closeChannelList()
            },
        )
        listPanel = PlayerListPanel(
            panelRoot = binding.playerListPanel,
            titleView = binding.playerListPanelTitle,
            recyclerView = binding.playerListPanelRecycler,
            onOptionSelected = { option -> handleListOption(option) },
        )
        nextEpisodeController = PlayerNextEpisodeController(
            root = binding.playerNextEpisodeCard,
            titleView = binding.playerNextEpisodeTitle,
            countdownView = binding.playerNextEpisodeCountdown,
            playNowButton = binding.playerNextEpisodePlay,
            cancelButton = binding.playerNextEpisodeCancel,
            onPlayNow = { playNextEpisodeFromAutoplay() },
            onCancel = { playerViewModel.dismissAutoplay() },
        )
        numericEntryController = PlayerNumericEntryController(
            overlay = binding.playerNumericEntry,
            onSubmit = { number -> zapToChannelNumber(number) },
        )
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    performBackAction()
                }
            },
        )
    }

    private fun performBackAction() {
        if (nextEpisodeController.isVisible()) {
            playerViewModel.dismissAutoplay()
            return
        }
        when (remoteController.handleBack(playerViewModel.overlayMode.value)) {
            PlayerRemoteController.BackAction.ClosePanel -> {
                closeAllPanels()
                showControls()
            }
            PlayerRemoteController.BackAction.Exit -> finish()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        saveCurrentPosition()
        player?.pause()
        playerViewModel.stopPositionSaving()
        overlayController.release()
        toastRunnable?.let { toastHandler.removeCallbacks(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    private fun setupControls() {
        binding.retryButton.setOnClickListener {
            loadStream(playerViewModel.currentChannel() ?: channel)
        }
        binding.playerPrev.setOnClickListener { onPrevClicked() }
        binding.playerNext.setOnClickListener { onNextClicked() }
        binding.playerPlayPause.setOnClickListener { togglePlayPause() }
        binding.playerFavorite.setOnClickListener {
            playerViewModel.toggleFavorite { updateFavoriteIcon(it) }
        }
        binding.playerSubtitles.setOnClickListener { showSubtitlePanel() }
        binding.playerAudio.setOnClickListener { showAudioPanel() }
        binding.playerSettings.setOnClickListener { showSettingsPanel() }
        binding.playerEpisodes.setOnClickListener { showEpisodesPanel() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    playerViewModel.isFavorite.collect { updateFavoriteIcon(it) }
                }
                launch {
                    playerViewModel.epgState.collect { updateEpgUi(it) }
                }
                launch {
                    playerViewModel.savedPositionMs.collect { pendingResumePositionMs = it }
                }
                launch {
                    var previousCountdown: Int? = null
                    playerViewModel.nextEpisodeUiState.collect { state ->
                        nextEpisodeController.bind(
                            state = state,
                            autoplayEnabled = appPreferences.isAutoplayNextEpisodeEnabled(),
                        )
                        val countdown = state.countdownSeconds
                        val priorCountdown = previousCountdown
                        if (priorCountdown != null && priorCountdown > 0 && countdown == 0) {
                            playNextEpisodeFromAutoplay()
                        }
                        previousCountdown = countdown
                    }
                }
            }
        }
    }

    private fun playNextEpisodeFromAutoplay() {
        playerViewModel.playNextEpisodeNow()?.let { nextChannel ->
            switchToChannel(nextChannel, showToast = false)
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            seekController.bindPlayer(exoPlayer)
            exoPlayer.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        binding.buffering.visibility =
                            if (playbackState == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                        if (playbackState == Player.STATE_READY) {
                            applyResumePosition(exoPlayer)
                            updatePlayPauseIcon()
                            seekController.updateFromPlayer()
                            overlayController.scheduleHide()
                            startPositionSaving()
                        }
                        if (playbackState == Player.STATE_ENDED) {
                            playerViewModel.consumeAutoplayOnEnded()?.let { nextChannel ->
                                switchToChannel(nextChannel, showToast = false)
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updatePlayPauseIcon()
                        seekController.updateFromPlayer()
                    }

                    override fun onEvents(player: Player, events: Player.Events) {
                        if (events.containsAny(
                                Player.EVENT_PLAYBACK_STATE_CHANGED,
                                Player.EVENT_IS_PLAYING_CHANGED,
                                Player.EVENT_TIMELINE_CHANGED,
                                Player.EVENT_POSITION_DISCONTINUITY,
                            )
                        ) {
                            seekController.updateFromPlayer()
                        }
                        if (channel.contentType == ContentType.EPISODE &&
                            events.containsAny(
                                Player.EVENT_PLAYBACK_STATE_CHANGED,
                                Player.EVENT_IS_PLAYING_CHANGED,
                                Player.EVENT_TIMELINE_CHANGED,
                                Player.EVENT_POSITION_DISCONTINUITY,
                            )
                        ) {
                            reportEpisodePlaybackProgress(exoPlayer)
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        if (handlePlaybackError(error)) return
                        showError(playbackErrorMessage(error))
                    }
                },
            )
        }
    }

    private fun reportEpisodePlaybackProgress(exoPlayer: ExoPlayer) {
        if (channel.contentType != ContentType.EPISODE) return
        playerViewModel.onPlaybackProgress(exoPlayer.currentPosition, exoPlayer.duration)
    }

    private fun bindChannelInfo(activeChannel: Channel) {
        channel = activeChannel
        binding.playerTitle.text = activeChannel.name

        val isLive = activeChannel.contentType == ContentType.LIVE
        binding.playerLiveBadgeRow.isVisible = isLive
        if (isLive) {
            binding.playerTypeBadge.text = getString(R.string.live_badge)
            binding.playerSubtitle.text = activeChannel.categoryName.orEmpty()
        } else {
            val typeLabel = when (activeChannel.contentType) {
                ContentType.MOVIE -> getString(R.string.movie_badge)
                ContentType.EPISODE -> getString(R.string.episode_badge)
                ContentType.SERIES -> getString(R.string.series_badge)
                ContentType.LIVE -> getString(R.string.live_badge)
            }
            binding.playerSubtitle.text = listOfNotNull(
                activeChannel.categoryName?.takeIf { it.isNotBlank() },
                typeLabel,
            ).joinToString(" · ")
        }

        val isVod = !isLive
        binding.playerSeekContainer.isVisible = isVod
        binding.playerEpgRow.isVisible = isLive
        binding.playerEpisodes.isVisible = activeChannel.contentType == ContentType.EPISODE
        seekController.setVisible(isVod)
        updateTransportButtons(isLive)

        playerViewModel.refreshEpg(activeChannel)
    }

    private fun updateTransportButtons(isLive: Boolean) {
        if (isLive) {
            binding.playerPrev.setImageResource(R.drawable.ic_player_prev)
            binding.playerNext.setImageResource(R.drawable.ic_player_next)
            binding.playerPrev.contentDescription = getString(R.string.player_prev)
            binding.playerNext.contentDescription = getString(R.string.player_next)
        } else {
            binding.playerPrev.setImageResource(R.drawable.ic_player_replay_10)
            binding.playerNext.setImageResource(R.drawable.ic_player_forward_10)
            binding.playerPrev.contentDescription = getString(R.string.player_rewind)
            binding.playerNext.contentDescription = getString(R.string.player_forward)
        }
    }

    private fun onPrevClicked() {
        if (channel.contentType == ContentType.LIVE) {
            switchChannel(previous = true)
        } else {
            seekController.seekBy(-PlayerRemoteController.SEEK_STEP_MS)
            showControls()
        }
    }

    private fun onNextClicked() {
        if (channel.contentType == ContentType.LIVE) {
            switchChannel(previous = false)
        } else {
            seekController.seekBy(PlayerRemoteController.SEEK_STEP_MS)
            showControls()
        }
    }

    private fun loadStream(activeChannel: Channel, showOverlay: Boolean = false) {
        binding.errorContainer.visibility = View.GONE
        binding.buffering.visibility = View.VISIBLE
        if (showOverlay) showControls()
        playerViewModel.rememberLastWatched(activeChannel)
        lifecycleScope.launch {
            runCatching {
                val repository = repositoryFactory.forSource(activeChannel.sourceId)
                repository.resolveStreamUrl(activeChannel).getOrThrow()
            }.onSuccess { url ->
                playUrl(url)
            }.onFailure { error ->
                showError(error.message ?: getString(R.string.error_playback))
            }
        }
    }

    private fun playUrl(url: String) {
        currentStreamUrl = url
        videoOnlyFallbackAttempted = false
        triedAudioTrackKeys.clear()
        binding.errorContainer.visibility = View.GONE
        player?.setMediaItem(MediaItem.fromUri(url))
        player?.prepare()
        player?.playWhenReady = true
    }

    private fun handlePlaybackError(error: PlaybackException): Boolean {
        val fallback = currentStreamUrl?.let(::tsFallbackUrl)
        if (fallback != null && fallback != currentStreamUrl) {
            playUrl(fallback)
            return true
        }

        val exoPlayer = player ?: return false
        if (isAudioDecoderError(error)) {
            if (tryNextAudioTrack(exoPlayer)) return true
            if (!videoOnlyFallbackAttempted) {
                videoOnlyFallbackAttempted = true
                val resumePosition = exoPlayer.currentPosition
                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                    .build()
                exoPlayer.prepare()
                exoPlayer.seekTo(resumePosition)
                exoPlayer.playWhenReady = true
                return true
            }
        }
        return false
    }

    private fun isAudioDecoderError(error: PlaybackException): Boolean {
        if (error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED) return true
        var cause: Throwable? = error
        while (cause != null) {
            if (cause.message.orEmpty().contains("MediaCodecAudioRenderer", ignoreCase = true)) {
                return true
            }
            cause = cause.cause
        }
        return false
    }

    private fun tryNextAudioTrack(exoPlayer: ExoPlayer): Boolean {
        val resumePosition = exoPlayer.currentPosition
        exoPlayer.currentTracks.groups
            .filter { it.type == C.TRACK_TYPE_AUDIO }
            .forEach { group ->
                for (trackIndex in 0 until group.length) {
                    if (group.isTrackSelected(trackIndex)) {
                        triedAudioTrackKeys.add(audioTrackKey(group.mediaTrackGroup, trackIndex))
                    }
                }
            }
        val candidates = exoPlayer.currentTracks.groups
            .filter { it.type == C.TRACK_TYPE_AUDIO }
            .flatMap { group ->
                (0 until group.length).map { trackIndex ->
                    group.mediaTrackGroup to trackIndex
                }
            }
        val next = candidates.firstOrNull { (group, trackIndex) ->
            audioTrackKey(group, trackIndex) !in triedAudioTrackKeys
        } ?: return false

        triedAudioTrackKeys.add(audioTrackKey(next.first, next.second))
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(TrackSelectionOverride(next.first, listOf(next.second)))
            .build()
        exoPlayer.prepare()
        exoPlayer.seekTo(resumePosition)
        exoPlayer.playWhenReady = true
        return true
    }

    private fun audioTrackKey(group: androidx.media3.common.TrackGroup, trackIndex: Int): String =
        "${group.id}:${group.hashCode()}:$trackIndex"

    private fun playbackErrorMessage(error: PlaybackException): String {
        if (isAudioDecoderError(error)) {
            return getString(R.string.error_playback_audio)
        }
        if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
            error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ||
            error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
        ) {
            return getString(R.string.error_playback_source)
        }
        return getString(R.string.error_playback)
    }

    private fun applyResumePosition(exoPlayer: ExoPlayer) {
        if (channel.contentType == ContentType.LIVE) return
        val duration = exoPlayer.duration
        val position = pendingResumePositionMs
        if (position > RESUME_MIN_MS && duration > 0 && position < duration - RESUME_END_BUFFER_MS) {
            exoPlayer.seekTo(position)
            pendingResumePositionMs = 0L
        }
    }

    private fun startPositionSaving() {
        playerViewModel.startPositionSaving(
            channel = channel,
            getPosition = { player?.currentPosition ?: 0L },
            getDuration = { player?.duration ?: 0L },
        )
    }

    private fun saveCurrentPosition() {
        val exoPlayer = player ?: return
        if (channel.contentType == ContentType.LIVE) return
        playerViewModel.savePosition(channel, exoPlayer.currentPosition, exoPlayer.duration)
    }

    private fun switchChannel(previous: Boolean) {
        val nextChannel = if (previous) {
            playerViewModel.previousChannel()
        } else {
            playerViewModel.nextChannel()
        } ?: return
        switchToChannel(nextChannel, showToast = true)
    }

    private fun switchToChannel(nextChannel: Channel, showToast: Boolean) {
        saveCurrentPosition()
        bindChannelInfo(nextChannel)
        if (showToast) {
            val index = playerViewModel.currentIndex.value
            zapToast.show(nextChannel, index + 1)
            preloadAdjacentChannels()
        }
        loadStream(nextChannel)
        if (nextChannel.contentType == ContentType.EPISODE) {
            playerViewModel.prepareAutoplayForChannel(nextChannel)
        }
    }

    private fun preloadAdjacentChannels() {
        val playlist = playerViewModel.playlist.value
        val index = playerViewModel.currentIndex.value
        playlist.getOrNull(index - 1)?.let { zapToast.preload(it) }
        playlist.getOrNull(index + 1)?.let { zapToast.preload(it) }
    }

    private fun togglePlayPause() {
        player?.let { exoPlayer ->
            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        }
        updatePauseOverlay()
        showControls()
    }

    private fun updatePlayPauseIcon() {
        val playing = player?.isPlaying == true
        binding.playerPlayPause.setImageResource(
            if (playing) R.drawable.ic_player_pause else R.drawable.ic_player_play,
        )
        updatePauseOverlay()
    }

    private fun updatePauseOverlay() {
        val playing = player?.isPlaying == true
        val showPauseGlyph = !playing && !overlayController.isOverlayVisible()
        binding.playerPauseOverlay.isVisible = showPauseGlyph
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        binding.playerFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_player_favorite_filled else R.drawable.ic_player_favorite,
        )
        binding.playerFavorite.imageTintList = ColorStateList.valueOf(
            getColor(if (isFavorite) R.color.accent else R.color.on_surface),
        )
    }

    private fun updateEpgUi(state: EpgUiState) {
        if (channel.contentType != ContentType.LIVE) {
            binding.playerEpgRow.isVisible = false
            return
        }
        val hasEpg = !state.nowTitle.isNullOrBlank()
        binding.playerEpgRow.isVisible = hasEpg
        if (!hasEpg) return
        binding.playerEpgNow.text = state.nowTitle
        binding.playerEpgProgress.progress = (state.nowProgress * 100).toInt()
    }

    private fun showSubtitlePanel() {
        val exoPlayer = player ?: return
        val textGroups = exoPlayer.currentTracks.groups.filter {
            it.type == C.TRACK_TYPE_TEXT && it.isSupported
        }
        if (textGroups.isEmpty()) {
            showPlayerToast(getString(R.string.player_no_subtitles))
            return
        }
        val options = mutableListOf<PlayerListOption>()
        val hasActiveText = textGroups.any { group ->
            (0 until group.length).any { group.isTrackSelected(it) }
        }
        options.add(
            PlayerListOption(
                label = getString(R.string.player_off),
                id = "off",
                selected = !hasActiveText,
            ),
        )
        textGroups.forEach { group ->
            for (trackIndex in 0 until group.length) {
                val format = group.getTrackFormat(trackIndex)
                val label = format.label ?: format.language ?: "Track ${trackIndex + 1}"
                options.add(
                    PlayerListOption(
                        label = label,
                        id = "text:$trackIndex:${group.mediaTrackGroup.hashCode()}",
                        selected = group.isTrackSelected(trackIndex),
                    ),
                )
            }
        }
        activeListPanelType = ListPanelType.Subtitles
        overlayController.cancelHideTimer()
        listPanel.show(getString(R.string.player_subtitles), options)
        playerViewModel.setOverlayMode(PlayerOverlayMode.TrackPicker)
    }

    private fun showAudioPanel() {
        val exoPlayer = player ?: return
        val audioGroups = exoPlayer.currentTracks.groups.filter {
            it.type == C.TRACK_TYPE_AUDIO && it.isSupported
        }
        if (audioGroups.isEmpty()) {
            showPlayerToast(getString(R.string.player_no_audio_tracks))
            return
        }
        val options = mutableListOf<PlayerListOption>()
        audioGroups.forEach { group ->
            for (trackIndex in 0 until group.length) {
                val format = group.getTrackFormat(trackIndex)
                val label = format.label ?: format.language ?: "Audio ${trackIndex + 1}"
                options.add(
                    PlayerListOption(
                        label = label,
                        id = "audio:$trackIndex:${group.mediaTrackGroup.hashCode()}",
                        selected = group.isTrackSelected(trackIndex),
                    ),
                )
            }
        }
        activeListPanelType = ListPanelType.Audio
        overlayController.cancelHideTimer()
        listPanel.show(getString(R.string.player_audio), options)
        playerViewModel.setOverlayMode(PlayerOverlayMode.TrackPicker)
    }

    private fun showSettingsPanel() {
        val fitSelected = resizeFit
        val speed = player?.playbackParameters?.speed ?: 1f
        val options = listOf(
            PlayerListOption("0.75x", "speed:0.75", speed == 0.75f),
            PlayerListOption("1.0x", "speed:1.0", speed == 1.0f),
            PlayerListOption("1.25x", "speed:1.25", speed == 1.25f),
            PlayerListOption("1.5x", "speed:1.5", speed == 1.5f),
            PlayerListOption("2.0x", "speed:2.0", speed == 2.0f),
            PlayerListOption(
                getString(R.string.player_fit_letterbox),
                "resize:fit",
                fitSelected,
            ),
            PlayerListOption(
                getString(R.string.player_fill_screen),
                "resize:fill",
                !fitSelected,
            ),
        )
        activeListPanelType = ListPanelType.Settings
        overlayController.cancelHideTimer()
        listPanel.show(getString(R.string.player_settings), options)
        playerViewModel.setOverlayMode(PlayerOverlayMode.Settings)
    }

    private fun showEpisodesPanel() {
        val list = playerViewModel.episodes.value
        if (list.isEmpty()) {
            showPlayerToast("No episodes available")
            return
        }
        val options = list.map { episode ->
            val label = "S${episode.seasonNumber} E${episode.episodeNumber} · ${episode.title}"
            PlayerListOption(
                label = label,
                id = "episode:${episode.id}",
                selected = episode.id == channel.id
            )
        }
        activeListPanelType = ListPanelType.Episodes
        overlayController.cancelHideTimer()
        listPanel.show(getString(R.string.player_episodes), options)
        playerViewModel.setOverlayMode(PlayerOverlayMode.Episodes)
    }

    private fun applyEpisodesOption(option: PlayerListOption) {
        val episodeId = option.id.removePrefix("episode:")
        val episode = playerViewModel.episodes.value.find { it.id == episodeId } ?: return
        val nextChannel = playerViewModel.getEpisodeChannel(episode) ?: return
        switchToChannel(nextChannel, showToast = false)
    }

    private fun handleListOption(option: PlayerListOption) {
        val exoPlayer = player ?: return
        when (activeListPanelType) {
            ListPanelType.Subtitles -> applySubtitleOption(exoPlayer, option)
            ListPanelType.Audio -> applyAudioOption(exoPlayer, option)
            ListPanelType.Settings -> applySettingsOption(option)
            ListPanelType.Episodes -> applyEpisodesOption(option)
            null -> Unit
        }
        closeListPanel()
        showControls()
    }

    private fun applySubtitleOption(exoPlayer: ExoPlayer, option: PlayerListOption) {
        if (option.id == "off") {
            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .build()
            return
        }
        val textGroups = exoPlayer.currentTracks.groups.filter {
            it.type == C.TRACK_TYPE_TEXT && it.isSupported
        }
        val trackIndex = option.id.removePrefix("text:").substringBefore(":").toIntOrNull() ?: return
        val groupHash = option.id.substringAfterLast(":").toIntOrNull()
        val group = textGroups.find { it.mediaTrackGroup.hashCode() == groupHash } ?: return
        if (trackIndex !in 0 until group.length) return
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(trackIndex)))
            .build()
    }

    private fun applyAudioOption(exoPlayer: ExoPlayer, option: PlayerListOption) {
        val trackIndex = option.id.removePrefix("audio:").substringBefore(":").toIntOrNull() ?: return
        val groupHash = option.id.substringAfterLast(":").toIntOrNull()
        val audioGroups = exoPlayer.currentTracks.groups.filter {
            it.type == C.TRACK_TYPE_AUDIO && it.isSupported
        }
        val group = audioGroups.find { it.mediaTrackGroup.hashCode() == groupHash } ?: return
        if (trackIndex !in 0 until group.length) return
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(trackIndex)))
            .build()
    }

    private fun applySettingsOption(option: PlayerListOption) {
        when {
            option.id.startsWith("speed:") -> {
                val speed = option.id.removePrefix("speed:").toFloatOrNull() ?: return
                player?.playbackParameters = PlaybackParameters(speed)
            }
            option.id == "resize:fit" -> applyResize(fit = true)
            option.id == "resize:fill" -> applyResize(fit = false)
        }
    }

    private fun applyResize(fit: Boolean) {
        resizeFit = fit
        binding.playerView.resizeMode = if (fit) {
            AspectRatioFrameLayout.RESIZE_MODE_FIT
        } else {
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
    }

    private fun openChannelList() {
        overlayController.setMode(PlayerOverlayMode.Hidden, animate = true)
        val state = playerViewModel.channelListState.value
        channelListPanel.show(state.channels, state.currentIndex, state.epgTitles)
        overlayController.cancelHideTimer()
        playerViewModel.setOverlayMode(PlayerOverlayMode.ChannelList)
    }

    private fun closeChannelList() {
        channelListPanel.hide()
        playerViewModel.setOverlayMode(PlayerOverlayMode.Controls)
    }

    private fun closeListPanel() {
        listPanel.hide()
        activeListPanelType = null
        playerViewModel.setOverlayMode(PlayerOverlayMode.Controls)
    }

    private fun closeAllPanels() {
        if (channelListPanel.isVisible()) closeChannelList()
        if (listPanel.isVisible()) closeListPanel()
        if (binding.playerInfoPanel.isVisible) {
            binding.playerInfoPanel.isVisible = false
            playerViewModel.setOverlayMode(PlayerOverlayMode.Hidden)
            updatePauseOverlay()
        }
    }

    private fun hideControls() {
        overlayController.setMode(PlayerOverlayMode.Hidden, animate = true)
        playerViewModel.setOverlayMode(PlayerOverlayMode.Hidden)
        updatePauseOverlay()
    }

    private fun openInfoPanel() {
        overlayController.setMode(PlayerOverlayMode.Hidden, animate = false)
        val epg = playerViewModel.epgState.value
        binding.playerInfoTitle.text = epg.nowTitle ?: channel.name
        val timeRange = listOfNotNull(epg.nowStartTime, epg.nowEndTime)
            .joinToString(" – ")
            .takeIf { it.isNotBlank() }
        val metaParts = buildList {
            timeRange?.let { add(it) }
            channel.categoryName?.takeIf { it.isNotBlank() }?.let { add(it) }
            epg.nextTitle?.let { next ->
                add(
                    getString(
                        R.string.player_epg_next,
                        next,
                        epg.nextStartTime.orEmpty(),
                    ),
                )
            }
        }
        binding.playerInfoMeta.text = metaParts.joinToString("\n")
        binding.playerInfoDescription.text = epg.nowDescription
            ?: getString(R.string.player_info_no_description)
        binding.playerInfoPanel.isVisible = true
        playerViewModel.setOverlayMode(PlayerOverlayMode.Info)
    }

    private fun zapToChannelNumber(number: Int) {
        if (channel.contentType != ContentType.LIVE) return
        lifecycleScope.launch {
            val target = playerViewModel.resolveChannelByNumber(number) ?: run {
                showPlayerToast(getString(R.string.player_channel_not_found, number))
                return@launch
            }
            val index = playerViewModel.playlist.value.indexOfFirst { it.id == target.id }
            if (index >= 0) {
                playerViewModel.selectChannelAtIndex(index)?.let { switchToChannel(it, showToast = true) }
            } else {
                switchToChannel(target, showToast = true)
            }
        }
    }

    private fun openVodDetailsPanel() {
        val plot = channel.plot?.trim().orEmpty()
        if (plot.isEmpty()) {
            showPlayerToast(getString(R.string.player_info_no_description))
            return
        }
        overlayController.setMode(PlayerOverlayMode.Hidden, animate = false)
        binding.playerInfoTitle.text = channel.name
        binding.playerInfoMeta.text = binding.playerSubtitle.text
        binding.playerInfoDescription.text = plot
        binding.playerInfoPanel.isVisible = true
        playerViewModel.setOverlayMode(PlayerOverlayMode.Info)
    }

    private fun showPlayerToast(message: String) {
        toastRunnable?.let { toastHandler.removeCallbacks(it) }
        binding.playerToast.text = message
        binding.playerToast.alpha = 1f
        binding.playerToast.isVisible = true
        val runnable = Runnable {
            binding.playerToast.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    binding.playerToast.isVisible = false
                    binding.playerToast.alpha = 1f
                }
                .start()
        }
        toastRunnable = runnable
        toastHandler.postDelayed(runnable, 2500L)
    }

    private fun showError(message: String) {
        binding.buffering.visibility = View.GONE
        binding.errorContainer.visibility = View.VISIBLE
        binding.errorMessage.text = message
        showControls()
        overlayController.cancelHideTimer()
    }

    private fun showControls() {
        binding.playerPauseOverlay.isVisible = false
        overlayController.showControls { binding.playerPlayPause.requestFocus() }
        playerViewModel.setOverlayMode(PlayerOverlayMode.Controls)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event)
        }

        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            performBackAction()
            return true
        }

        if (nextEpisodeController.isVisible()) {
            return super.dispatchKeyEvent(event)
        }

        if (channel.contentType == ContentType.LIVE &&
            PlayerNumericEntryController.isDigitKey(event.keyCode)
        ) {
            if (channelListPanel.isVisible() || listPanel.isVisible() || binding.playerInfoPanel.isVisible) {
                return super.dispatchKeyEvent(event)
            }
            numericEntryController.handleDigit(
                PlayerNumericEntryController.digitFromKey(event.keyCode),
            )
            return true
        }

        if (channelListPanel.isVisible() || listPanel.isVisible()) {
            return super.dispatchKeyEvent(event)
        }

        val action = remoteController.handleKeyDown(
            keyCode = event.keyCode,
            overlayMode = playerViewModel.overlayMode.value,
            contentType = channel.contentType,
            overlayVisible = overlayController.isOverlayVisible(),
            seekBarFocused = seekController.isSeekBarFocused(),
            hasPrevious = playerViewModel.hasPrevious(),
            hasNext = playerViewModel.hasNext(),
        )

        return when (action) {
            is PlayerKeyAction.Back -> {
                performBackAction()
                true
            }
            is PlayerKeyAction.TogglePlayPause -> {
                togglePlayPause()
                true
            }
            is PlayerKeyAction.ChannelPrevious -> {
                switchChannel(previous = true)
                true
            }
            is PlayerKeyAction.ChannelNext -> {
                switchChannel(previous = false)
                true
            }
            is PlayerKeyAction.Seek -> {
                seekController.seekBy(if (action.forward) action.amountMs else -action.amountMs)
                if (overlayController.isOverlayVisible()) {
                    showControls()
                }
                true
            }
            is PlayerKeyAction.OpenChannelList -> {
                openChannelList()
                true
            }
            is PlayerKeyAction.OpenInfoPanel -> {
                openInfoPanel()
                true
            }
            is PlayerKeyAction.OpenVodDetails -> {
                openVodDetailsPanel()
                true
            }
            is PlayerKeyAction.ShowControls -> {
                showControls()
                true
            }
            is PlayerKeyAction.Consume -> true
            is PlayerKeyAction.PassThrough -> {
                if (!overlayController.isOverlayVisible()) {
                    showControls()
                } else {
                    overlayController.resetHideTimer()
                }
                super.dispatchKeyEvent(event)
            }
        }
    }

    private fun tsFallbackUrl(m3u8Url: String): String? {
        if (!m3u8Url.endsWith(".m3u8", ignoreCase = true)) return null
        return m3u8Url.dropLast(5) + ".ts"
    }

    companion object {
        private const val EXTRA_CHANNEL = "extra_channel"
        private const val EXTRA_CATEGORY_ID = "extra_category_id"
        private const val RESUME_MIN_MS = 5_000L
        private const val RESUME_END_BUFFER_MS = 30_000L

        fun createIntent(
            context: Context,
            channel: Channel,
            categoryId: String? = channel.categoryId,
        ): Intent = Intent(context, PlayerActivity::class.java)
            .putExtra(EXTRA_CHANNEL, channel)
            .putExtra(EXTRA_CATEGORY_ID, categoryId)
    }
}
