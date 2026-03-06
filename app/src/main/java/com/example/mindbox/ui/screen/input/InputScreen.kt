package com.example.mindbox.ui.screen.input

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mindbox.nlp.ExtractionResult
import com.example.mindbox.ui.component.ConfidenceBar
import com.example.mindbox.ui.component.MetadataPreviewCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit,
    viewModel: InputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val partialVoice by viewModel.partialVoiceText.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startVoiceRecognition()
    }

    LaunchedEffect(uiState) {
        if (uiState is InputUiState.Saved) onSaveComplete()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Memory") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            when (val state = uiState) {
                is InputUiState.Idle, is InputUiState.Recording -> {
                    OutlinedTextField(
                        value = if (uiState is InputUiState.Recording) partialVoice else inputText,
                        onValueChange = { inputText = it },
                        label = { Text("What do you want to remember?") },
                        placeholder = { Text("e.g. Had my interview at OpenAI on Feb 17, 2025") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 10,
                        enabled = uiState !is InputUiState.Recording
                    )

                    if (uiState is InputUiState.Recording) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Listening...", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Voice")
                        }
                        Button(
                            onClick = { viewModel.processText(inputText) },
                            modifier = Modifier.weight(1f),
                            enabled = inputText.isNotBlank()
                        ) {
                            Text("Analyze")
                        }
                    }
                }

                is InputUiState.Extracted -> {
                    MetadataPreviewCard(
                        result = state.result,
                        onConfirm = { edited -> viewModel.confirmAndSave(state.result, edited) },
                        onCancel = { viewModel.cancelInput() }
                    )
                }

                is InputUiState.Saving -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Saving your memory...")
                        }
                    }
                }

                is InputUiState.Error -> {
                    Text(
                        "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { viewModel.cancelInput() }) { Text("Try Again") }
                }

                is InputUiState.Saved -> {
                    // Handled by LaunchedEffect
                }
            }
        }
    }
}
