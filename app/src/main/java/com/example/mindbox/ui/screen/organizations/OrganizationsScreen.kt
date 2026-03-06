package com.example.mindbox.ui.screen.organizations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindbox.domain.model.Organization
import com.example.mindbox.ui.component.EmptyStateView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }
private fun parseJsonStringList(s: String): List<String> =
    runCatching { json.decodeFromString<List<String>>(s) }.getOrDefault(emptyList())

@Composable
fun OrganizationsScreen(
    viewModel: OrgViewModel = hiltViewModel()
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
            placeholder = { Text("Search organizations…") },
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
            uiState.organizations.isEmpty() -> {
                EmptyStateView(message = "No organizations yet. They'll appear when you save entries mentioning companies or organizations.")
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.organizations, key = { it.id }) { org ->
                        OrgCard(org = org)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrgCard(org: Organization, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = org.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!org.orgType.isNullOrBlank()) {
                    Text(
                        text = org.orgType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val aliases = parseJsonStringList(org.aliases)
                if (aliases.isNotEmpty()) {
                    Text(
                        text = "Also: ${aliases.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
