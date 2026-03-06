package com.example.mindbox.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindbox.domain.model.Event
import com.example.mindbox.domain.model.Note
import com.example.mindbox.domain.repository.IEventRepository
import com.example.mindbox.domain.repository.INoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class EventDetail(val event: Event) : DetailUiState()
    data class NoteDetail(val note: Note) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
    data object Deleted : DetailUiState()
}

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    private val eventRepository: IEventRepository,
    private val noteRepository: INoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: Long = checkNotNull(savedStateHandle["entryId"])
    private val entryType: String = savedStateHandle["entryType"] ?: "events"

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    init {
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            try {
                when (entryType) {
                    "events" -> {
                        val event = eventRepository.getById(entryId)
                        _uiState.value = if (event != null) DetailUiState.EventDetail(event)
                        else DetailUiState.Error("Event not found")
                    }
                    "notes" -> {
                        val note = noteRepository.getById(entryId)
                        _uiState.value = if (note != null) DetailUiState.NoteDetail(note)
                        else DetailUiState.Error("Note not found")
                    }
                    else -> _uiState.value = DetailUiState.Error("Unknown entry type")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteEntry() {
        viewModelScope.launch {
            try {
                when (val state = _uiState.value) {
                    is DetailUiState.EventDetail -> eventRepository.deleteEvent(state.event.id)
                    is DetailUiState.NoteDetail -> noteRepository.deleteNote(state.note.id)
                    else -> return@launch
                }
                _uiState.value = DetailUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Delete failed")
            }
        }
    }
}
