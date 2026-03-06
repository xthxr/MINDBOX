package com.example.mindbox.ui.screen.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindbox.domain.model.Event
import com.example.mindbox.domain.model.Note
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val json = Json { ignoreUnknownKeys = true }
private fun parseJsonStringList(s: String): List<String> =
    runCatching { json.decodeFromString<List<String>>(s) }.getOrDefault(emptyList())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    onBack: () -> Unit,
    viewModel: EntryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is DetailUiState.Deleted) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.EventDetail -> EventDetailContent(state.event)
                is DetailUiState.NoteDetail -> NoteDetailContent(state.note)
                is DetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DetailUiState.Deleted -> Unit
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Memory") },
            text = { Text("Are you sure you want to delete this memory? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteEntry()
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EventDetailContent(event: Event) {
    val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailRow(label = "Title", value = event.title)
        DetailRow(label = "Type", value = event.eventType)
        DetailRow(label = "Date", value = event.date?.let { dateFormat.format(Date(it)) } ?: event.dateRaw ?: "—")
        if (!event.notes.isNullOrBlank()) DetailRow(label = "Notes", value = event.notes!!)
        val eventTags = parseJsonStringList(event.tags)
        if (eventTags.isNotEmpty()) DetailRow(label = "Tags", value = eventTags.joinToString(", "))
    }
}

@Composable
private fun NoteDetailContent(note: Note) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailRow(label = "Content", value = note.content)
        val noteTags = parseJsonStringList(note.tags)
        if (noteTags.isNotEmpty()) DetailRow(label = "Tags", value = noteTags.joinToString(", "))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
