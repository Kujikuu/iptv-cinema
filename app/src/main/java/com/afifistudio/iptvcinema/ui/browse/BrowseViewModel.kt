package com.afifistudio.iptvcinema.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afifistudio.iptvcinema.data.cache.CachedHomeFeed
import com.afifistudio.iptvcinema.data.cache.CategoryChannelsCache
import com.afifistudio.iptvcinema.data.cache.HomeFeedCache
import com.afifistudio.iptvcinema.data.cache.SectionFeedCache
import com.afifistudio.iptvcinema.data.cache.SeriesEpisodesCache
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.dao.FavoriteDao
import com.afifistudio.iptvcinema.data.local.dao.SectionImportStateDao
import com.afifistudio.iptvcinema.data.local.entity.FavoriteEntity
import com.afifistudio.iptvcinema.data.local.toDomain
import com.afifistudio.iptvcinema.data.platform.WatchNextPublisher
import com.afifistudio.iptvcinema.data.prefs.AppPreferences
import com.afifistudio.iptvcinema.data.repository.SourceImportCoordinator
import com.afifistudio.iptvcinema.data.repository.SourceRepository
import com.afifistudio.iptvcinema.data.repository.WatchHistoryRepository
import com.afifistudio.iptvcinema.data.repository.SourceRefreshPolicy
import com.afifistudio.iptvcinema.domain.model.ContinueWatchingItem
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.domain.model.IptvSourceConfig
import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.SectionImportState
import com.afifistudio.iptvcinema.domain.model.SourceType
import com.afifistudio.iptvcinema.domain.repository.IptvRepositoryFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CategoryRow(
    val category: Category,
    val channels: List<Channel>,
)

data class BrowseUiState(
    val isLoading: Boolean = true,
    val isBootstrapLoading: Boolean = true,
    val isHomeRowsLoading: Boolean = false,
    val isSectionCacheWarming: Boolean = false,
    val error: String? = null,
    val sources: List<IptvSourceConfig> = emptyList(),
    val selectedSourceId: Long? = null,
    val selectedSection: BrowseSection = BrowseSection.HOME,
    val featuredChannel: Channel? = null,
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val favorites: List<Channel> = emptyList(),
    val recommendedChannels: List<Channel> = emptyList(),
    val recommendedRowTitle: String = "",
    val categoryRows: List<CategoryRow> = emptyList(),
    val categorySummaries: List<CategorySummary> = emptyList(),
    val homeCategoryHighlights: List<CategorySummary> = emptyList(),
    val liveCount: Int = 0,
    val movieCount: Int = 0,
    val seriesCount: Int = 0,
    val sectionLoadStates: Map<BrowseSection, SectionImportState> = emptyMap(),
    val sourceUpdatedAt: Long = 0L,
    val activeSourceName: String = "",
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val repositoryFactory: IptvRepositoryFactory,
    private val sourceRefreshPolicy: SourceRefreshPolicy,
    private val sectionFeedCache: SectionFeedCache,
    private val categoryChannelsCache: CategoryChannelsCache,
    private val homeFeedCache: HomeFeedCache,
    private val seriesEpisodesCache: SeriesEpisodesCache,
    private val channelDao: ChannelDao,
    private val favoriteDao: FavoriteDao,
    private val sectionImportStateDao: SectionImportStateDao,
    private val sourceImportCoordinator: SourceImportCoordinator,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val appPreferences: AppPreferences,
    private val watchNextPublisher: WatchNextPublisher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()
    private var sectionWarmJob: kotlinx.coroutines.Job? = null
    private var sectionImportStateJob: Job? = null

    init {
        loadInitial()
    }

    fun loadInitial() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isBootstrapLoading = true,
                    isHomeRowsLoading = false,
                    error = null,
                )
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    val sources = sourceRepository.getSources()
                    if (sources.isEmpty()) {
                        sectionImportStateJob?.cancel()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isBootstrapLoading = false,
                                isHomeRowsLoading = false,
                                sources = emptyList(),
                                sectionLoadStates = emptyMap(),
                                error = null,
                            )
                        }
                        return@withContext
                    }
                    val preferredId = appPreferences.getSelectedSourceId()
                    val sourceId = sources.firstOrNull { it.id == preferredId }?.id ?: sources.first().id
                    selectSourceInternal(sourceId, sources, _uiState.value.selectedSection)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBootstrapLoading = false,
                        isHomeRowsLoading = false,
                        error = error.message ?: "Failed to load",
                    )
                }
            }
        }
    }

    fun selectSource(sourceId: Long) {
        viewModelScope.launch {
            val sources = _uiState.value.sources.ifEmpty { sourceRepository.getSources() }
            withContext(Dispatchers.IO) {
                appPreferences.setSelectedSourceId(sourceId)
                selectSourceInternal(sourceId, sources, _uiState.value.selectedSection)
            }
        }
    }

    fun cycleSource() {
        val sources = _uiState.value.sources
        if (sources.size <= 1) return
        val currentId = _uiState.value.selectedSourceId ?: return
        val currentIndex = sources.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        val nextSource = sources[(currentIndex + 1) % sources.size]
        selectSource(nextSource.id)
    }

    fun deleteSource(sourceId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isBootstrapLoading = true,
                    isHomeRowsLoading = false,
                    error = null,
                )
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    sourceRepository.deleteSource(sourceId)
                    invalidateSourceCaches(sourceId)
                }
                val sources = sourceRepository.getSources()
                if (sources.isEmpty()) {
                    appPreferences.setSelectedSourceId(null)
                    sectionImportStateJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isBootstrapLoading = false,
                            isHomeRowsLoading = false,
                            sources = emptyList(),
                            selectedSourceId = null,
                            sectionLoadStates = emptyMap(),
                            activeSourceName = "",
                        )
                    }
                } else {
                    val nextId = sources.first().id
                    appPreferences.setSelectedSourceId(nextId)
                    selectSourceInternal(nextId, sources, BrowseSection.HOME)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBootstrapLoading = false,
                        isHomeRowsLoading = false,
                        error = error.message ?: "Failed to delete source",
                    )
                }
            }
        }
    }

    fun clearWatchHistory() {
        val sourceId = _uiState.value.selectedSourceId ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                watchHistoryRepository.clearForSource(sourceId)
            }
            refreshContinueWatching()
            withContext(Dispatchers.IO) {
                runCatching { watchNextPublisher.sync() }
            }
        }
    }

    fun selectSection(section: BrowseSection) {
        val sourceId = _uiState.value.selectedSourceId ?: return
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val sourceUpdatedAt = sourceUpdatedAtFor(sourceId)
                    sectionFeedCache.get(sourceId, section, sourceUpdatedAt)?.let { cached ->
                        _uiState.update {
                            it.copy(
                                selectedSection = section,
                                isLoading = false,
                                isBootstrapLoading = false,
                                categorySummaries = cached,
                                error = null,
                            )
                        }
                        return@withContext
                    }
                }
                _uiState.update {
                    it.copy(
                        selectedSection = section,
                        isLoading = true,
                        isBootstrapLoading = false,
                        categorySummaries = emptyList(),
                        error = null,
                    )
                }
                withContext(Dispatchers.IO) {
                    reloadFeedData(sourceId, section)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBootstrapLoading = false,
                        error = error.message ?: "Failed to load section",
                    )
                }
            }
        }
    }

    fun markHomeSection() {
        if (_uiState.value.selectedSection == BrowseSection.HOME) return
        _uiState.update { it.copy(selectedSection = BrowseSection.HOME) }
    }

    fun refreshCurrentSource() {
        val sourceId = _uiState.value.selectedSourceId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isBootstrapLoading = true,
                    isHomeRowsLoading = false,
                    error = null,
                )
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    repositoryFactory.forSource(sourceId).refreshSource(sourceId).getOrThrow()
                    invalidateSourceCaches(sourceId)
                    val sources = sourceRepository.getSources()
                    selectSourceInternal(sourceId, sources, _uiState.value.selectedSection)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBootstrapLoading = false,
                        isHomeRowsLoading = false,
                        error = error.message ?: "Refresh failed",
                    )
                }
            }
        }
    }

    fun refreshSection(section: BrowseSection, onResult: (Result<Unit>) -> Unit = {}) {
        val sourceId = _uiState.value.selectedSourceId ?: return
        val contentType = section.contentType ?: return
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val sourceType = sourceRepository.getSource(sourceId)?.type
                    if (sourceType == SourceType.XTREAM) {
                        sourceImportCoordinator.refreshSection(sourceId, contentType).getOrThrow()
                    } else {
                        repositoryFactory.forSource(sourceId)
                            .refreshSection(sourceId, contentType)
                            .getOrThrow()
                    }
                    if (sourceType == com.afifistudio.iptvcinema.domain.model.SourceType.XTREAM) {
                        invalidateSectionCaches(sourceId, section)
                    } else {
                        invalidateSourceCaches(sourceId)
                    }
                    val sources = sourceRepository.getSources()
                    _uiState.update { it.copy(sources = sources) }
                    reloadFeedData(sourceId, BrowseSection.HOME)
                }
            }.fold(
                onSuccess = { onResult(Result.success(Unit)) },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isBootstrapLoading = false,
                            isHomeRowsLoading = false,
                            error = error.message ?: "Refresh failed",
                        )
                    }
                    onResult(Result.failure(error))
                },
            )
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            val wasFavorite = withContext(Dispatchers.IO) {
                favoriteDao.isFavorite(channel.sourceId, channel.id, channel.contentType)
            }
            withContext(Dispatchers.IO) {
                if (wasFavorite) {
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
            }
            applyFavoriteToggleLocally(channel, wasFavorite = wasFavorite)
            categoryChannelsCache.patchFavorite(
                sourceId = channel.sourceId,
                channelId = channel.id,
                contentType = channel.contentType,
                isFavorite = !wasFavorite,
            )
        }
    }

    private fun applyFavoriteToggleLocally(channel: Channel, wasFavorite: Boolean) {
        _uiState.update { state ->
            val favorites = if (wasFavorite) {
                state.favorites.filterNot { it.id == channel.id && it.contentType == channel.contentType }
            } else {
                listOf(channel) + state.favorites.filterNot {
                    it.id == channel.id && it.contentType == channel.contentType
                }
            }
            val featuredChannel = state.featuredChannel
                ?: state.continueWatching.firstOrNull()?.channel
                ?: favorites.firstOrNull()
            state.copy(
                favorites = favorites,
                featuredChannel = featuredChannel,
            )
        }
        val sourceId = channel.sourceId
        val sourceUpdatedAt = _uiState.value.sourceUpdatedAt
        homeFeedCache.get(sourceId, sourceUpdatedAt)?.let { cached ->
            val favorites = if (wasFavorite) {
                cached.favorites.filterNot { it.id == channel.id && it.contentType == channel.contentType }
            } else {
                listOf(channel) + cached.favorites.filterNot {
                    it.id == channel.id && it.contentType == channel.contentType
                }
            }
            homeFeedCache.put(
                sourceId = sourceId,
                sourceUpdatedAt = sourceUpdatedAt,
                feed = cached.copy(
                    favorites = favorites,
                    featuredChannel = cached.featuredChannel
                        ?: cached.continueWatching.firstOrNull()?.channel
                        ?: favorites.firstOrNull(),
                ),
            )
        }
    }

    suspend fun isFavorite(channel: Channel): Boolean =
        favoriteDao.isFavorite(channel.sourceId, channel.id, channel.contentType)

    suspend fun getAllContinueWatching(sourceId: Long): List<ContinueWatchingItem> {
        val categoryNames = categoryNameMapFor(sourceId)
        return watchHistoryRepository.getRecentContinueWatching(
            sourceId = sourceId,
            contentType = null,
            limit = CONTINUE_WATCHING_LIBRARY_LIMIT,
            categoryNames = categoryNames,
        )
    }

    fun continueWatchingForSection(section: BrowseSection): List<ContinueWatchingItem> {
        val items = _uiState.value.continueWatching
        return when (section) {
            BrowseSection.HOME -> items
            BrowseSection.LIVE -> items.filter { it.channel.contentType == ContentType.LIVE }
            BrowseSection.MOVIES -> items.filter { it.channel.contentType == ContentType.MOVIE }
            BrowseSection.SERIES -> items.filter {
                it.channel.contentType == ContentType.EPISODE ||
                    it.channel.contentType == ContentType.SERIES
            }
        }
    }

    fun refreshContinueWatching() {
        val sourceId = _uiState.value.selectedSourceId ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val categoryNames = categoryNameMapFor(sourceId)
                val continueWatching = watchHistoryRepository.getRecentContinueWatching(
                    sourceId = sourceId,
                    contentType = null,
                    limit = CONTINUE_WATCHING_LIMIT,
                    categoryNames = categoryNames,
                )
                val sourceUpdatedAt = sourceUpdatedAtFor(sourceId)
                val favorites = _uiState.value.favorites
                val featuredChannel = continueWatching.firstOrNull()?.channel
                    ?: favorites.firstOrNull()
                    ?: _uiState.value.featuredChannel
                _uiState.update {
                    it.copy(
                        continueWatching = continueWatching,
                        featuredChannel = featuredChannel,
                    )
                }
                homeFeedCache.get(sourceId, sourceUpdatedAt)?.let { cached ->
                    homeFeedCache.put(
                        sourceId = sourceId,
                        sourceUpdatedAt = sourceUpdatedAt,
                        feed = cached.copy(
                            continueWatching = continueWatching,
                            featuredChannel = featuredChannel ?: cached.featuredChannel,
                        ),
                    )
                }
            }
        }
    }

    fun rememberLastWatched(channel: Channel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                watchHistoryRepository.rememberLastWatched(channel)
            }
            refreshContinueWatching()
        }
    }

    private suspend fun categoryNameMapFor(sourceId: Long): Map<String?, String?> =
        repositoryFactory.forSource(sourceId)
            .getCategories(sourceId, null)
            .getOrNull()
            .orEmpty()
            .associate { it.id to it.name }

    private fun observeSectionImportStates(sourceId: Long) {
        sectionImportStateJob?.cancel()
        sectionImportStateJob = viewModelScope.launch {
            sectionImportStateDao.observeBySource(sourceId).collectLatest { states ->
                if (_uiState.value.selectedSourceId != sourceId) return@collectLatest
                if (states.isEmpty()) {
                    _uiState.update { it.copy(sectionLoadStates = emptyMap()) }
                    return@collectLatest
                }
                val mappedStates = states
                    .map { it.toDomain() }
                    .mapNotNull { state ->
                        browseSectionForContentType(state.contentType)?.let { section -> section to state }
                    }
                    .toMap()
                _uiState.update { it.copy(sectionLoadStates = mappedStates) }
                withContext(Dispatchers.IO) {
                    val refreshedSources = sourceRepository.getSources()
                    _uiState.update { it.copy(sources = refreshedSources) }
                    publishSourceCounts(sourceId)
                    if (_uiState.value.selectedSection == BrowseSection.HOME) {
                        reloadFeedData(sourceId, BrowseSection.HOME)
                    }
                }
            }
        }
    }

    private suspend fun ensureXtreamInitialImportQueued(sourceId: Long) {
        val states = sectionImportStateDao.getBySource(sourceId)
        if (states.isEmpty()) {
            sourceImportCoordinator.queueXtreamInitialImport(sourceId)
        } else {
            sourceImportCoordinator.ensurePendingImportsRunning(sourceId)
        }
    }

    private fun browseSectionForContentType(contentType: ContentType): BrowseSection? =
        when (contentType) {
            ContentType.LIVE -> BrowseSection.LIVE
            ContentType.MOVIE -> BrowseSection.MOVIES
            ContentType.SERIES -> BrowseSection.SERIES
            ContentType.EPISODE -> null
        }

    suspend fun getAllFavorites(sourceId: Long): List<Channel> {
        val categoryNames = repositoryFactory.forSource(sourceId)
            .getCategories(sourceId, null)
            .getOrNull()
            .orEmpty()
            .associate { it.id to it.name }
        return favoriteDao.getFavoriteChannels(sourceId, contentType = null)
            .map { it.toDomain(categoryNames[it.categoryId]) }
    }

    private suspend fun loadContinueWatching(
        sourceId: Long,
        categoryNames: Map<String?, String?>,
    ): List<ContinueWatchingItem> = watchHistoryRepository.getRecentContinueWatching(
        sourceId = sourceId,
        contentType = null,
        limit = CONTINUE_WATCHING_LIMIT,
        categoryNames = categoryNames,
    )

    private suspend fun selectSourceInternal(
        sourceId: Long,
        sources: List<IptvSourceConfig>,
        section: BrowseSection,
    ) {
        _uiState.update {
            it.copy(
                isLoading = true,
                isBootstrapLoading = true,
                isHomeRowsLoading = false,
                sources = sources,
                selectedSourceId = sourceId,
                selectedSection = section,
                error = null,
            )
        }
        appPreferences.setSelectedSourceId(sourceId)
        observeSectionImportStates(sourceId)

        runCatching {
            val repository = repositoryFactory.forSource(sourceId)
            val source = sourceRepository.getSource(sourceId)
            val channelCount = channelDao.countAllBySource(sourceId)
            val updatedAt = source?.updatedAt ?: 0L
            val isEmptyXtreamSource = source?.type == SourceType.XTREAM && channelCount == 0
            if (isEmptyXtreamSource) {
                ensureXtreamInitialImportQueued(sourceId)
            } else if (sourceRefreshPolicy.shouldRefreshFromNetwork(channelCount, updatedAt)) {
                repository.refreshSource(sourceId).getOrThrow()
                invalidateSourceCaches(sourceId)
            }
            val refreshedSources = sourceRepository.getSources()
            _uiState.update { it.copy(sources = refreshedSources) }
            reloadFeedData(sourceId, section)
            sectionWarmJob?.cancel()
            sectionWarmJob = viewModelScope.launch(Dispatchers.IO) {
                warmSectionCaches(sourceId)
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isBootstrapLoading = false,
                    isHomeRowsLoading = false,
                    isSectionCacheWarming = false,
                    error = error.message ?: "Failed to load source",
                )
            }
        }
    }

    private suspend fun reloadFeedData(sourceId: Long, section: BrowseSection) {
        publishSourceCounts(sourceId)

        val repository = repositoryFactory.forSource(sourceId)
        val isHome = section == BrowseSection.HOME

        if (!isHome) {
            val contentType = section.contentType
            val sourceUpdatedAt = sourceUpdatedAtFor(sourceId)
            val categories = repository.getCategories(sourceId, contentType).getOrThrow()
            val categorySummaries = loadCategorySummaries(
                sourceId = sourceId,
                categories = categories,
                contentType = contentType,
                previewLimit = CATEGORY_GRID_PREVIEW_LIMIT,
            )
            sectionFeedCache.put(sourceId, section, sourceUpdatedAt, categorySummaries)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isBootstrapLoading = false,
                    error = null,
                    selectedSection = section,
                    categorySummaries = categorySummaries,
                    categoryRows = emptyList(),
                    sourceUpdatedAt = sourceUpdatedAt,
                )
            }
            return
        }

        val sourceUpdatedAt = sourceUpdatedAtFor(sourceId)
        publishHomeShell(sourceId, section, sourceUpdatedAt)
        homeFeedCache.get(sourceId, sourceUpdatedAt)?.let { cached ->
            val activeSource = _uiState.value.sources.firstOrNull { it.id == sourceId }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isBootstrapLoading = false,
                    isHomeRowsLoading = false,
                    error = null,
                    selectedSection = section,
                    favorites = cached.favorites,
                    continueWatching = cached.continueWatching,
                    recommendedChannels = cached.recommendedChannels,
                    recommendedRowTitle = cached.recommendedRowTitle,
                    categoryRows = emptyList(),
                    categorySummaries = emptyList(),
                    homeCategoryHighlights = cached.homeCategoryHighlights,
                    featuredChannel = cached.featuredChannel,
                    sourceUpdatedAt = activeSource?.updatedAt ?: sourceUpdatedAt,
                    activeSourceName = activeSource?.name.orEmpty(),
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                isBootstrapLoading = false,
                isHomeRowsLoading = true,
                error = null,
                selectedSection = section,
            )
        }

        val categoryNameMap = categoryNameMapFor(sourceId)

        val favorites = favoriteDao.getFavoriteChannels(sourceId, contentType = null).map { entity ->
            entity.toDomain(categoryNameMap[entity.categoryId])
        }
        val continueWatching = loadContinueWatching(sourceId, categoryNameMap)
        val homeCategoryHighlights = loadHomeCategoryHighlights(sourceId, repository)

        val recentItem = continueWatching.firstOrNull()?.channel ?: favorites.firstOrNull()
        var recommended: List<Channel> = emptyList()
        var rowTitle = "Recommended For You"
        if (recentItem != null && !recentItem.categoryId.isNullOrBlank()) {
            val categoryName = recentItem.categoryName ?: categoryNameMap[recentItem.categoryId] ?: ""
            rowTitle = if (categoryName.isNotBlank()) "Because you watched $categoryName" else "Recommended For You"
            recommended = channelDao.getByCategory(
                sourceId = sourceId,
                categoryId = recentItem.categoryId,
                contentType = recentItem.contentType,
                limit = 15
            ).map { it.toDomain(categoryName) }
             .filterNot { it.id == recentItem.id }
        }
        if (recommended.isEmpty()) {
            val highlight = homeCategoryHighlights.firstOrNull()
            if (highlight != null) {
                rowTitle = "Featured in ${highlight.category.name}"
                recommended = channelDao.getByCategory(
                    sourceId = sourceId,
                    categoryId = highlight.category.id,
                    contentType = highlight.category.contentType,
                    limit = 15
                ).map { it.toDomain(highlight.category.name) }
            }
        }

        val featuredChannel = continueWatching.firstOrNull()?.channel
            ?: favorites.firstOrNull()
            ?: featuredFromHighlights(sourceId, homeCategoryHighlights)

        val activeSource = _uiState.value.sources.firstOrNull { it.id == sourceId }

        homeFeedCache.put(
            sourceId = sourceId,
            sourceUpdatedAt = sourceUpdatedAt,
            feed = CachedHomeFeed(
                favorites = favorites,
                continueWatching = continueWatching,
                homeCategoryHighlights = homeCategoryHighlights,
                featuredChannel = featuredChannel,
                recommendedChannels = recommended,
                recommendedRowTitle = rowTitle,
            ),
        )

        _uiState.update {
            it.copy(
                isLoading = false,
                isBootstrapLoading = false,
                isHomeRowsLoading = false,
                error = null,
                selectedSection = section,
                favorites = favorites,
                continueWatching = continueWatching,
                recommendedChannels = recommended,
                recommendedRowTitle = rowTitle,
                categoryRows = emptyList(),
                categorySummaries = emptyList(),
                homeCategoryHighlights = homeCategoryHighlights,
                featuredChannel = featuredChannel,
                sourceUpdatedAt = activeSource?.updatedAt ?: sourceUpdatedAt,
                activeSourceName = activeSource?.name.orEmpty(),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { watchNextPublisher.sync() }
        }
    }

    private suspend fun warmSectionCaches(sourceId: Long) {
        _uiState.update { it.copy(isSectionCacheWarming = true) }
        val sourceUpdatedAt = sourceUpdatedAtFor(sourceId)
        val repository = repositoryFactory.forSource(sourceId)
        try {
            listOf(BrowseSection.LIVE, BrowseSection.MOVIES, BrowseSection.SERIES).forEach { section ->
                if (sectionFeedCache.get(sourceId, section, sourceUpdatedAt) != null) return@forEach
                val contentType = section.contentType
                val categories = repository.getCategories(sourceId, contentType).getOrNull().orEmpty()
                if (categories.isEmpty()) return@forEach
                val summaries = loadCategorySummaries(
                    sourceId = sourceId,
                    categories = categories,
                    contentType = contentType,
                    previewLimit = CATEGORY_GRID_PREVIEW_LIMIT,
                )
                sectionFeedCache.put(sourceId, section, sourceUpdatedAt, summaries)
            }
        } finally {
            _uiState.update { it.copy(isSectionCacheWarming = false) }
        }
    }

    private suspend fun publishSourceCounts(sourceId: Long) {
        val liveCount = channelDao.countBySource(sourceId, ContentType.LIVE)
        val movieCount = channelDao.countBySource(sourceId, ContentType.MOVIE)
        val seriesCount = channelDao.countBySource(sourceId, ContentType.SERIES)
        val activeSource = _uiState.value.sources.firstOrNull { it.id == sourceId }
        _uiState.update {
            it.copy(
                liveCount = liveCount,
                movieCount = movieCount,
                seriesCount = seriesCount,
                sourceUpdatedAt = activeSource?.updatedAt ?: 0L,
                activeSourceName = activeSource?.name.orEmpty(),
            )
        }
    }

    private fun publishHomeShell(sourceId: Long, section: BrowseSection, sourceUpdatedAt: Long) {
        val activeSource = _uiState.value.sources.firstOrNull { it.id == sourceId }
        _uiState.update {
            it.copy(
                isLoading = false,
                isBootstrapLoading = false,
                selectedSection = section,
                sourceUpdatedAt = activeSource?.updatedAt ?: sourceUpdatedAt,
                activeSourceName = activeSource?.name.orEmpty(),
                error = null,
            )
        }
    }

    private suspend fun featuredFromHighlights(
        sourceId: Long,
        highlights: List<CategorySummary>,
    ): Channel? {
        val category = highlights.firstOrNull()?.category ?: return null
        return channelDao
            .getByCategory(sourceId, category.id, category.contentType, limit = 1)
            .firstOrNull()
            ?.toDomain(category.name)
    }

    private suspend fun loadCategorySummaries(
        sourceId: Long,
        categories: List<Category>,
        contentType: ContentType?,
        previewLimit: Int = CATEGORY_PREVIEW_LIMIT,
    ): List<CategorySummary> {
        if (contentType == null) return emptyList()
        val counts = channelDao.getCategoryCounts(sourceId, contentType)
            .associateBy { it.categoryId }
        val sorted = categories.mapNotNull { category ->
            val stats = counts[category.id] ?: return@mapNotNull null
            if (stats.count == 0) return@mapNotNull null
            CategorySummary(
                category = category,
                channelCount = stats.count,
                previewImageUrls = emptyList(),
                latestAddedAt = stats.latestAddedAt ?: 0L,
            )
        }.sortedWith(
            compareByDescending<CategorySummary> { it.latestAddedAt }
                .thenByDescending { it.channelCount }
                .thenBy { it.category.name.lowercase() },
        )
        if (previewLimit <= 0) return sorted
        return sorted.map { summary ->
            val previews = channelDao.getPreviewLogosForCategory(
                sourceId = sourceId,
                categoryId = summary.category.id,
                contentType = contentType,
                limit = previewLimit,
            ).mapNotNull { it.logoUrl }.distinct()
            summary.copy(previewImageUrls = previews)
        }
    }

    private suspend fun loadHomeCategoryHighlights(
        sourceId: Long,
        repository: com.afifistudio.iptvcinema.domain.repository.IptvRepository,
    ): List<CategorySummary> = coroutineScope {
        listOf(ContentType.LIVE, ContentType.MOVIE, ContentType.SERIES).map { type ->
            async {
                val categories = repository.getCategories(sourceId, type).getOrNull().orEmpty()
                loadCategorySummaries(sourceId, categories, type, previewLimit = CATEGORY_PREVIEW_LIMIT)
                    .take(HOME_CATEGORIES_PER_TYPE)
            }
        }.awaitAll()
            .flatten()
            .take(HOME_CATEGORY_HIGHLIGHT_LIMIT)
    }

    private suspend fun sourceUpdatedAtFor(sourceId: Long): Long =
        sourceRepository.getSource(sourceId)?.updatedAt ?: 0L

    private fun invalidateSourceCaches(sourceId: Long) {
        sectionFeedCache.clearSource(sourceId)
        categoryChannelsCache.clearSource(sourceId)
        homeFeedCache.clearSource(sourceId)
        seriesEpisodesCache.clearSource(sourceId)
    }

    private fun invalidateSectionCaches(sourceId: Long, section: BrowseSection) {
        sectionFeedCache.clearSection(sourceId, section)
        section.contentType?.let { contentType ->
            categoryChannelsCache.clearContentType(sourceId, contentType)
            if (contentType == ContentType.SERIES) {
                seriesEpisodesCache.clearSource(sourceId)
            }
        }
        homeFeedCache.clearSource(sourceId)
    }

    companion object {
        private const val CATEGORY_PREVIEW_LIMIT = 4
        private const val CATEGORY_GRID_PREVIEW_LIMIT = 1
        private const val CONTINUE_WATCHING_LIMIT = 15
        private const val CONTINUE_WATCHING_LIBRARY_LIMIT = 50
        private const val HOME_CATEGORIES_PER_TYPE = 3
        private const val HOME_CATEGORY_HIGHLIGHT_LIMIT = 9
    }
}
