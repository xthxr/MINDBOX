package com.example.mindbox.ui.screen.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindbox.domain.model.Person
import com.example.mindbox.domain.repository.IPeopleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PeopleUiState(
    val people: List<Person> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val peopleRepository: IPeopleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PeopleUiState> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                peopleRepository.getAllPeopleFlow()
                    .map { PeopleUiState(people = it, isLoading = false) }
            } else {
                flow {
                    emit(PeopleUiState(people = peopleRepository.searchByName(query), isLoading = false))
                }
            }
        }
        .catch { emit(PeopleUiState(error = it.message, isLoading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PeopleUiState())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deletePerson(id: Long) {
        viewModelScope.launch {
            peopleRepository.deletePerson(id)
        }
    }
}
