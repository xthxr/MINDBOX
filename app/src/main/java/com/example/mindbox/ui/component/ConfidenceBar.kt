package com.example.mindbox.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mindbox.nlp.ConfidenceTier
import com.example.mindbox.ui.theme.ConfidenceHigh
import com.example.mindbox.ui.theme.ConfidenceLow
import com.example.mindbox.ui.theme.ConfidenceMedium

@Composable
fun ConfidenceBar(
    score: Float,
    tier: ConfidenceTier,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val color = when (tier) {
        ConfidenceTier.HIGH -> ConfidenceHigh
        ConfidenceTier.MEDIUM -> ConfidenceMedium
        ConfidenceTier.LOW -> ConfidenceLow
    }
    val label = when (tier) {
        ConfidenceTier.HIGH -> "High confidence"
        ConfidenceTier.MEDIUM -> "Medium confidence"
        ConfidenceTier.LOW -> "Low confidence"
    }

    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
                Text(
                    text = "${(score * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        LinearProgressIndicator(
            progress = { score },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
