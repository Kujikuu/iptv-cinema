package com.afifistudio.iptvcinema.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afifistudio.iptvcinema.data.cache.CachedCategoryChannels
import com.afifistudio.iptvcinema.data.cache.CategoryChannelsCache
import com.afifistudio.iptvcinema.data.cache.CategoryContentLoader
import com.afifistudio.iptvcinema.data.local.dao.ChannelDao
import com.afifistudio.iptvcinema.data.local.toDomain
import com.afifistudio.iptvcinema.domain.model.Category
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CategoryDetailUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val categoryName: String = "",
    val contentType: ContentType = ContentType.LIVE,
    val channels: List<Channel> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val channelDao: ChannelDao,
    private val categoryChannelsCache: CategoryChannelsCache,
    private val contentLoader: CategoryContentLoader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    private var activeCategory: Category? = null
    private var loadedCount = 0
    private var sourceUpdatedAt = 0L

    fun load(category: Category, sourceUpdatedAt: Long) {
        activeCategory = category
        this.sourceUpdatedAt = sourceUpdatedAt

        categoryChannelsCache.get(category, sourceUpdatedAt)?.let { cached ->
            restoreFromCache(cached)
            return
        }

        loadedCount = 0
        _uiState.value = CategoryDetailUiState(
            isLoading = true,
            categoryName = category.name,
            contentType = category.contentType,
        )
        loadNextPage()
    }

    fun loadNextPage() {
        val category = activeCategory ?: return
        val current = _uiState.value
        if (current.isLoadingMore || (!current.hasMore && loadedCount > 0)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = loadedCount == 0,
                    isLoadingMore = loadedCount > 0,
                    error = null,
                )
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    if (loadedCount == 0) {
                        val firstPage = contentLoader.loadFirstPage(category)
                        PageLoadResult(
                            channels = firstPage.channels,
                            favoriteIds = firstPage.favoriteIds,
                            totalCount = firstPage.totalCount,
                        )
                    } else {
                        val page = channelDao.getByCategoryPage(
                            sourceId = category.sourceId,
                            categoryId = category.id,
                            contentType = category.contentType,
                            limit = CategoryContentLoader.PAGE_SIZE,
                            offset = loadedCount,
                        )
                        PageLoadResult(
                            channels = page.map { entity -> entity.toDomain(category.name) },
                            favoriteIds = current.favoriteIds,
                            totalCount = current.totalCount,
                        )
                    }
                }
            }.onSuccess { result ->
                loadedCount += result.channels.size
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        channels = it.channels + result.channels,
                        favoriteIds = result.favoriteIds,
                        totalCount = result.totalCount,
                        hasMore = loadedCount < result.totalCount,
                    )
                }
                persistCache(category)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = error.message ?: "Failed to load category",
                    )
                }
            }
        }
    }

    private fun restoreFromCache(cached: CachedCategoryChannels) {
        loadedCount = cached.loadedCount
        _uiState.value = CategoryDetailUiState(
            isLoading = false,
            categoryName = cached.categoryName,
            contentType = cached.contentType,
            channels = cached.channels,
            favoriteIds = cached.favoriteIds,
            totalCount = cached.totalCount,
            hasMore = cached.hasMore,
        )
    }

    private fun persistCache(category: Category) {
        val state = _uiState.value
        categoryChannelsCache.put(
            category = category,
            sourceUpdatedAt = sourceUpdatedAt,
            data = CachedCategoryChannels(
                channels = state.channels,
                favoriteIds = state.favoriteIds,
                totalCount = state.totalCount,
                loadedCount = loadedCount,
                hasMore = state.hasMore,
                contentType = state.contentType,
                categoryName = state.categoryName,
            ),
        )
    }

    private data class PageLoadResult(
        val channels: List<Channel>,
        val favoriteIds: Set<String>,
        val totalCount: Int,
    )
}
