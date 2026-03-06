package com.example.mindbox.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindbox.domain.usecase.QueryMemoryUseCase
import com.example.mindbox.query.MemoryResult
import com.example.mindbox.query.QueryResponse
import com.example.mindbox.query.SearchSource
import com.example.mindbox.voice.VoiceRecognitionManager
import com.example.mindbox.voice.VoiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val results: List<MemoryResult> = emptyList(),
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val searchSource: SearchSource? = null,
    val queryMs: Long = 0,
    val error: String? = null,
    val hasSearched: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val queryMemoryUseCase: QueryMemoryUseCase,
    private val voiceRecognitionManager: VoiceRecognitionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = SearchUiState(isLoading = true)
            try {
                val response = queryMemoryUseCase(query)
                _uiState.value = SearchUiState(
                    results = response.results,
                    searchSource = response.source,
                    queryMs = response.queryMs,
                    hasSearched = true
                )
            } catch (e: Exception) {
                _uiState.value = SearchUiState(error = e.message, hasSearched = true)
            }
        }
    }

    fun startVoiceSearch() {
        viewModelScope.launch(Dispatchers.Main) {
            voiceRecognitionManager.listen()
                .collect { state ->
                    when (state) {
                        is VoiceState.Listening -> _uiState.value = SearchUiState(isListening = true)
                        is VoiceState.Final -> {
                            _uiState.value = SearchUiState(isListening = false)
                            search(state.text)
                        }
                        is VoiceState.Error -> _uiState.value = SearchUiState(
                            error = state.message, isListening = false
                        )
                        else -> Unit
                    }
                }
        }
    }
}
