package com.afifistudio.iptvcinema.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afifistudio.iptvcinema.data.local.ChannelSearchHelper
import com.afifistudio.iptvcinema.data.local.toDomain
import com.afifistudio.iptvcinema.data.repository.SourceRepository
import com.afifistudio.iptvcinema.domain.model.Channel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Channel> = emptyList(),
    val isSearching: Boolean = false,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val channelSearchHelper: ChannelSearchHelper,
    private val sourceRepository: SourceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        _uiState.update { it.copy(query = query, isSearching = query.isNotBlank()) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            runCatching {
                val sourceIds = sourceRepository.getSources().map { it.id }
                channelSearchHelper.search(
                    sourceIds = sourceIds,
                    query = query.trim(),
                    contentType = null,
                    limit = SEARCH_LIMIT,
                ).map { it.toDomain() }
            }.onSuccess { results ->
                _uiState.update { it.copy(results = results, isSearching = false) }
            }.onFailure {
                _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val SEARCH_LIMIT = 50
    }
}
