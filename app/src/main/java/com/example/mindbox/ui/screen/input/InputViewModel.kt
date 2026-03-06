package com.example.mindbox.ui.screen.input

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindbox.domain.usecase.ExtractMetadataUseCase
import com.example.mindbox.domain.usecase.GenerateEmbeddingUseCase
import com.example.mindbox.domain.usecase.SaveRawEntryUseCase
import com.example.mindbox.domain.usecase.SaveStructuredEntryUseCase
import com.example.mindbox.nlp.ConfidenceTier
import com.example.mindbox.nlp.ExtractionResult
import com.example.mindbox.nlp.confidenceTier
import com.example.mindbox.voice.VoiceRecognitionManager
import com.example.mindbox.voice.VoiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class InputUiState {
    object Idle : InputUiState()
    object Recording : InputUiState()
    data class Extracted(val result: ExtractionResult) : InputUiState()
    data class Saving(val result: ExtractionResult) : InputUiState()
    object Saved : InputUiState()
    data class Error(val message: String) : InputUiState()
}

@HiltViewModel
class InputViewModel @Inject constructor(
    private val saveRawEntryUseCase: SaveRawEntryUseCase,
    private val extractMetadataUseCase: ExtractMetadataUseCase,
    private val saveStructuredEntryUseCase: SaveStructuredEntryUseCase,
    private val generateEmbeddingUseCase: GenerateEmbeddingUseCase,
    private val voiceRecognitionManager: VoiceRecognitionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<InputUiState>(InputUiState.Idle)
    val uiState: StateFlow<InputUiState> = _uiState

    private val _partialVoiceText = MutableStateFlow("")
    val partialVoiceText: StateFlow<String> = _partialVoiceText

    fun processText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                extractMetadataUseCase(text)
            }
            when (result.confidenceTier()) {
                ConfidenceTier.HIGH -> {
                    // Auto-save without confirmation
                    saveEntry(text, result, source = "TEXT")
                }
                ConfidenceTier.MEDIUM, ConfidenceTier.LOW -> {
                    // Show preview card for confirmation
                    _uiState.value = InputUiState.Extracted(result)
                }
            }
        }
    }

    fun confirmAndSave(result: ExtractionResult, editedResult: ExtractionResult? = null) {
        val finalResult = editedResult ?: result
        viewModelScope.launch {
            saveEntry(finalResult.rawText, finalResult, source = "TEXT")
        }
    }

    fun cancelInput() {
        _uiState.value = InputUiState.Idle
        _partialVoiceText.value = ""
    }

    fun startVoiceRecognition() {
        viewModelScope.launch(Dispatchers.Main) {
            voiceRecognitionManager.listen()
                .collect { state ->
                    when (state) {
                        is VoiceState.Listening -> _uiState.value = InputUiState.Recording
                        is VoiceState.Partial -> _partialVoiceText.value = state.text
                        is VoiceState.Final -> {
                            _partialVoiceText.value = ""
                            processText(state.text)
                        }
                        is VoiceState.Error -> _uiState.value = InputUiState.Error(state.message)
                        VoiceState.Idle -> if (_uiState.value is InputUiState.Recording) {
                            _uiState.value = InputUiState.Idle
                        }
                        else -> Unit
                    }
                }
        }
    }

    private suspend fun saveEntry(text: String, result: ExtractionResult, source: String) {
        _uiState.value = InputUiState.Saving(result)
        try {
            val rawId = saveRawEntryUseCase(text, source)
            val saveResult = saveStructuredEntryUseCase(rawId, result)
            // Generate embedding in background
            viewModelScope.launch(Dispatchers.Default) {
                generateEmbeddingUseCase(text, saveResult.eventId, "EVENT")
            }
            _uiState.value = InputUiState.Saved
        } catch (e: Exception) {
            _uiState.value = InputUiState.Error(e.message ?: "Save failed")
        }
    }
}
