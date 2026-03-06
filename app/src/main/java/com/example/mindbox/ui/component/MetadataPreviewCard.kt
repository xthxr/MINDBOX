package com.example.mindbox.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.mindbox.nlp.ExtractionResult
import com.example.mindbox.nlp.confidenceTier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MetadataPreviewCard(
    result: ExtractionResult,
    onConfirm: (ExtractionResult) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var eventTitle by remember { mutableStateOf(result.eventType ?: "") }
    var orgName by remember { mutableStateOf(result.orgName ?: "") }
    var peopleText by remember { mutableStateOf(result.people.joinToString(", ")) }

    val dateFormatted = result.dateEpoch?.let {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(it))
    } ?: result.dateRaw ?: "Not detected"

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Extracted Information",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            ConfidenceBar(
                score = result.confidence,
                tier = result.confidenceTier(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Date (read-only display)
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Date:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(80.dp)
                )
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Event type (editable)
            OutlinedTextField(
                value = eventTitle,
                onValueChange = { eventTitle = it },
                label = { Text("Event Type") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Org name (editable)
            OutlinedTextField(
                value = orgName,
                onValueChange = { orgName = it },
                label = { Text("Organization") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // People (editable, comma separated)
            OutlinedTextField(
                value = peopleText,
                onValueChange = { peopleText = it },
                label = { Text("People (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val editedResult = result.copy(
                        orgName = orgName.trim().ifBlank { null },
                        people = peopleText.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    )
                    onConfirm(editedResult)
                }) {
                    Text("Confirm & Save")
                }
            }
        }
    }
}
