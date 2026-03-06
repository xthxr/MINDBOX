package com.example.mindbox.ui.screen.auth

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mindbox.security.BiometricAuthManager
import com.example.mindbox.security.BiometricResult

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showEmailForm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthSuccess()
    }

    // Trigger biometric if required
    LaunchedEffect(uiState.isBiometricRequired) {
        if (uiState.isBiometricRequired) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            BiometricAuthManager().authenticate(
                activity = activity,
                onResult = { result ->
                    when (result) {
                        is BiometricResult.Success -> viewModel.onBiometricSuccess()
                        is BiometricResult.Error -> viewModel.onBiometricFailed(result.message)
                        BiometricResult.NotAvailable, BiometricResult.NotEnrolled ->
                            viewModel.signInAnonymously()
                    }
                }
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.isBiometricRequired && !showEmailForm) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text("MindBox", style = MaterialTheme.typography.headlineLarge)
                Text("Authenticate to access your memories", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = {
                    val activity = context as? FragmentActivity ?: return@Button
                    BiometricAuthManager().authenticate(activity) { result ->
                        when (result) {
                            is BiometricResult.Success -> viewModel.onBiometricSuccess()
                            is BiometricResult.Error -> viewModel.onBiometricFailed(result.message)
                            else -> viewModel.signInAnonymously()
                        }
                    }
                }) {
                    Text("Unlock with Biometrics")
                }
                TextButton(onClick = { showEmailForm = true }) {
                    Text("Sign in with email instead")
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Text("MindBox", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = { viewModel.signInWithEmail(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && password.isNotBlank()
                ) { Text("Sign In") }

                OutlinedButton(
                    onClick = { viewModel.createAccount(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && password.isNotBlank()
                ) { Text("Create Account") }

                TextButton(onClick = { viewModel.signInAnonymously() }) {
                    Text("Continue anonymously")
                }
            }
        }
    }
}
