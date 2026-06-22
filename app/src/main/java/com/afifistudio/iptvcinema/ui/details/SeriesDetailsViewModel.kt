package com.afifistudio.iptvcinema.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afifistudio.iptvcinema.data.cache.SeriesEpisodesLoader
import com.afifistudio.iptvcinema.domain.model.Channel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SeriesDetailsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val series: Channel? = null,
    val episodes: List<com.afifistudio.iptvcinema.domain.model.Episode> = emptyList(),
)

@HiltViewModel
class SeriesDetailsViewModel @Inject constructor(
    private val seriesEpisodesLoader: SeriesEpisodesLoader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesDetailsUiState())
    val uiState: StateFlow<SeriesDetailsUiState> = _uiState.asStateFlow()

    fun load(series: Channel, sourceUpdatedAt: Long) {
        val seriesId = series.externalId.orEmpty().ifBlank { series.id }
        seriesEpisodesLoader.peekCached(series.sourceId, seriesId, sourceUpdatedAt)?.let { cached ->
            _uiState.value = SeriesDetailsUiState(
                isLoading = false,
                series = series,
                episodes = cached,
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = SeriesDetailsUiState(
                isLoading = true,
                error = null,
                series = series,
            )
            runCatching {
                withContext(Dispatchers.IO) {
                    seriesEpisodesLoader.loadEpisodes(series, sourceUpdatedAt)
                }
            }.onSuccess { episodes ->
                _uiState.value = SeriesDetailsUiState(
                    isLoading = false,
                    series = series,
                    episodes = episodes,
                )
            }.onFailure { error ->
                _uiState.value = SeriesDetailsUiState(
                    isLoading = false,
                    series = series,
                    error = error.message ?: "Failed to load episodes",
                )
            }
        }
    }
}
