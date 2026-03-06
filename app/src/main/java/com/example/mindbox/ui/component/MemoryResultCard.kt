package com.example.mindbox.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mindbox.query.MemoryResult
import com.example.mindbox.query.SearchSource
import com.example.mindbox.ui.theme.SourceSemantic
import com.example.mindbox.ui.theme.SourceStructured
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SourceBadge(source: SearchSource, modifier: Modifier = Modifier) {
    val (label, color) = when (source) {
        SearchSource.STRUCTURED -> "SQL" to SourceStructured
        SearchSource.SEMANTIC -> "Semantic" to SourceSemantic
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun MemoryResultCard(
    result: MemoryResult,
    source: SearchSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = result.displayText.take(80).let {
                        if (result.displayText.length > 80) "$it…" else it
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SourceBadge(source = source)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.sourceTable.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
