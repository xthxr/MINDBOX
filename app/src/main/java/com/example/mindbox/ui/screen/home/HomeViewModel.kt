package com.example.mindbox.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindbox.domain.model.Event
import com.example.mindbox.domain.model.RawEntry
import com.example.mindbox.domain.repository.IEntryRepository
import com.example.mindbox.domain.repository.IEventRepository
import com.example.mindbox.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(
    val recentEntries: List<RawEntry> = emptyList(),
    val recentEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val entryCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val entryRepository: IEntryRepository,
    private val eventRepository: IEventRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        entryRepository.getAllEntriesFlow().map { it.take(20) },
        eventRepository.getAllEventsFlow().map { it.take(20) }
    ) { entries, events ->
        HomeUiState(
            recentEntries = entries,
            recentEvents = events,
            entryCount = entries.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun requestSync() = syncScheduler.scheduleImmediate()
}
