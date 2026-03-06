package com.example.mindbox.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindbox.data.remote.FirebaseAuthManager
import com.example.mindbox.domain.usecase.SyncUseCase
import com.example.mindbox.security.EncryptedPreferences
import com.example.mindbox.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val biometricEnabled: Boolean = false,
    val currentUid: String? = null,
    val isSyncing: Boolean = false,
    val lastSyncFormatted: String = "Never",
    val syncMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val encryptedPreferences: EncryptedPreferences,
    private val firebaseAuthManager: FirebaseAuthManager,
    private val syncUseCase: SyncUseCase,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val biometricEnabled = encryptedPreferences.isBiometricEnabled()
        val uid = encryptedPreferences.getFirebaseUid()
        val lastSync = encryptedPreferences.getLastSyncTimestamp()
        val formatted = if (lastSync == 0L) "Never"
        else java.text.SimpleDateFormat("d MMM yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(lastSync))
        _uiState.value = SettingsUiState(
            biometricEnabled = biometricEnabled,
            currentUid = uid,
            lastSyncFormatted = formatted
        )
    }

    fun toggleBiometric(enabled: Boolean) {
        encryptedPreferences.setBiometricEnabled(enabled)
        _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
    }

    fun triggerSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncMessage = null)
            try {
                syncUseCase()
                _uiState.value = _uiState.value.copy(isSyncing = false, syncMessage = "Sync scheduled")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSyncing = false, syncMessage = "Sync failed: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            firebaseAuthManager.signOut()
            encryptedPreferences.clearFirebaseUid()
            _uiState.value = _uiState.value.copy(currentUid = null)
        }
    }
}
