package com.example.mindbox.ui.screen.people

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindbox.domain.model.Person
import com.example.mindbox.ui.component.EmptyStateView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }
private fun parseJsonStringList(s: String): List<String> =
    runCatching { json.decodeFromString<List<String>>(s) }.getOrDefault(emptyList())
private fun parseJsonLongList(s: String): List<Long> =
    runCatching { json.decodeFromString<List<Long>>(s) }.getOrDefault(emptyList())

@Composable
fun PeopleScreen(
    viewModel: PeopleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search people…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.people.isEmpty() -> {
                EmptyStateView(message = "No people found. They'll appear here when you save entries mentioning them.")
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.people, key = { it.id }) { person ->
                        PersonCard(person = person)
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonCard(person: Person, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium
                )
                val aliases = parseJsonStringList(person.aliases)
                if (aliases.isNotEmpty()) {
                    Text(
                        text = "Also known as: ${aliases.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val linkedEvents = parseJsonLongList(person.linkedEventIds)
                Text(
                    text = "${linkedEvents.size} linked event(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
