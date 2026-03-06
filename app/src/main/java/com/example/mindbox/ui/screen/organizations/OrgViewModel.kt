package com.example.mindbox.ui.screen.organizations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindbox.domain.model.Organization
import com.example.mindbox.domain.repository.IOrgRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrgsUiState(
    val organizations: List<Organization> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class OrgViewModel @Inject constructor(
    private val orgRepository: IOrgRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<OrgsUiState> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                orgRepository.getAllOrgsFlow()
                    .map { OrgsUiState(organizations = it, isLoading = false) }
            } else {
                flow {
                    emit(OrgsUiState(organizations = orgRepository.searchByName(query), isLoading = false))
                }
            }
        }
        .catch { emit(OrgsUiState(error = it.message, isLoading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrgsUiState())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteOrg(id: Long) {
        viewModelScope.launch {
            orgRepository.deleteOrg(id)
        }
    }
}
