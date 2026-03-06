package com.example.mindbox.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun VoiceInputButton(
    isListening: Boolean,
    onClick: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isListening) {
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                content = {}
            )
        }

        FloatingActionButton(
            onClick = if (isListening) onStop else onClick,
            containerColor = if (isListening)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop recording" else "Start voice input",
                tint = if (isListening)
                    MaterialTheme.colorScheme.onError
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
