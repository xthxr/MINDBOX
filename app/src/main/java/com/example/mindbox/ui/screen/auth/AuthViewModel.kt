package com.example.mindbox.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindbox.data.remote.FirebaseAuthManager
import com.example.mindbox.security.EncryptedPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isBiometricRequired: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val encryptedPrefs: EncryptedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val biometricEnabled = encryptedPrefs.getBoolean(EncryptedPreferences.KEY_BIOMETRIC_ENABLED, false)
        if (firebaseAuthManager.isSignedIn && !biometricEnabled) {
            _uiState.value = AuthUiState(isAuthenticated = true)
        } else if (biometricEnabled) {
            _uiState.value = AuthUiState(isBiometricRequired = true)
        } else {
            signInAnonymously()
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            firebaseAuthManager.signInAnonymously()
                .onSuccess { _uiState.value = AuthUiState(isAuthenticated = true) }
                .onFailure { _uiState.value = AuthUiState(error = it.message) }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            firebaseAuthManager.signInWithEmail(email, password)
                .onSuccess { _uiState.value = AuthUiState(isAuthenticated = true) }
                .onFailure { e -> _uiState.value = AuthUiState(error = e.message, isLoading = false) }
        }
    }

    fun createAccount(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            firebaseAuthManager.createAccountWithEmail(email, password)
                .onSuccess { _uiState.value = AuthUiState(isAuthenticated = true) }
                .onFailure { e -> _uiState.value = AuthUiState(error = e.message, isLoading = false) }
        }
    }

    fun onBiometricSuccess() {
        _uiState.value = AuthUiState(isAuthenticated = true)
    }

    fun onBiometricFailed(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}
