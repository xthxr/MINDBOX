package com.example.mindbox.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mindbox.domain.model.Event
import com.example.mindbox.domain.model.RawEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToInput: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToEntry: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPeople: () -> Unit,
    onNavigateToOrgs: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MindBox") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { viewModel.requestSync() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Memory") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onNavigateToInput
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("People") },
                    selected = false,
                    onClick = onNavigateToPeople
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Business, contentDescription = null) },
                    label = { Text("Organizations") },
                    selected = false,
                    onClick = onNavigateToOrgs
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.recentEvents.isEmpty() && uiState.recentEntries.isEmpty()) {
                    item { EmptyStateCard(onNavigateToInput) }
                } else {
                    if (uiState.recentEvents.isNotEmpty()) {
                        item {
                            Text(
                                "Recent Events",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(uiState.recentEvents, key = { "event_${it.id}" }) { event ->
                            EventCard(event = event, onClick = { onNavigateToEntry(event.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (event.eventType != "OTHER") {
                    AssistChip(
                        onClick = {},
                        label = { Text(event.eventType, style = MaterialTheme.typography.labelSmall) }
                    )
                }
                event.date?.let { dateMs ->
                    Text(
                        dateFormat.format(Date(dateMs)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(onNavigateToInput: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Text("No memories yet", style = MaterialTheme.typography.titleMedium)
            Text("Tap 'Add Memory' to start building your personal memory system", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onNavigateToInput) { Text("Add First Memory") }
        }
    }
}
