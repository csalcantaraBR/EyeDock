package com.eyedock.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * GREEN PHASE - Implementação mínima de TimelineScrubber
 * 
 * Controle de timeline para navegação em gravações
 */
@Composable
fun TimelineScrubber(
    currentPosition: Long,
    totalDuration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "timeline"
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }
    
    val progress = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .semantics { 
                contentDescription = "Timeline scrubber for video navigation" 
            }
    ) {
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = formatTime(totalDuration),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Timeline track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            dragPosition = newProgress
                            onSeek((newProgress * totalDuration).toLong())
                        },
                        onDragEnd = {
                            isDragging = false
                        }
                    ) { _, _ ->
                        // Continue dragging
                        val newProgress = dragPosition.coerceIn(0f, 1f)
                        onSeek((newProgress * totalDuration).toLong())
                    }
                }
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            
            // Progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isDragging) dragPosition else progress)
                    .height(4.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            
            // Thumb
            Box(
                modifier = Modifier
                    .offset(x = ((if (isDragging) dragPosition else progress) * 300).dp)
                    .size(16.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 24h ruler
        TimelineRuler(
            totalDuration = totalDuration,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Régua de timeline mostrando marcações de tempo
 */
@Composable
private fun TimelineRuler(
    totalDuration: Long,
    modifier: Modifier = Modifier
) {
    val hourMarks = (0..24).map { hour ->
        val timestamp = hour * 60 * 60 * 1000L // Convert hours to milliseconds
        val progress = if (totalDuration > 0) timestamp.toFloat() / totalDuration else 0f
        TimelineMarker(hour.toString().padStart(2, '0') + ":00", progress.coerceIn(0f, 1f))
    }
    
    Canvas(
        modifier = modifier.height(30.dp)
    ) {
        hourMarks.forEach { marker ->
            val x = marker.progress * size.width
            
            // Draw tick mark
            drawLine(
                color = Color.Gray,
                start = Offset(x, 0f),
                end = Offset(x, size.height * 0.3f),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
    
    // Hour labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("00:00", "06:00", "12:00", "18:00", "24:00").forEach { time ->
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Controles de playback
 */
@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "playback_controls"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Seek backward 10s
        IconButton(
            onClick = onSeekBackward,
            modifier = Modifier.semantics { 
                contentDescription = "Seek backward 10 seconds" 
            }
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_media_rew),
                contentDescription = null
            )
        }
        
        // Play/Pause
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(56.dp)
                .semantics { 
                    contentDescription = if (isPlaying) "Pause" else "Play" 
                }
        ) {
            Icon(
                painter = painterResource(
                    if (isPlaying) 
                        android.R.drawable.ic_media_pause 
                    else 
                        android.R.drawable.ic_media_play
                ),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Seek forward 10s
        IconButton(
            onClick = onSeekForward,
            modifier = Modifier.semantics { 
                contentDescription = "Seek forward 10 seconds" 
            }
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_media_ff),
                contentDescription = null
            )
        }
        
        // Speed control
        OutlinedButton(
            onClick = {
                val newSpeed = when (playbackSpeed) {
                    0.5f -> 1.0f
                    1.0f -> 2.0f
                    2.0f -> 4.0f
                    else -> 0.5f
                }
                onSpeedChange(newSpeed)
            },
            modifier = Modifier.semantics { 
                contentDescription = "Playback speed ${playbackSpeed}x" 
            }
        ) {
            Text("${playbackSpeed}x")
        }
    }
}

/**
 * Ações de export/share
 */
@Composable
fun TimelineActions(
    onExportClip: () -> Unit,
    onShare: () -> Unit,
    onOpenInLibrary: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "timeline_actions"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = onExportClip,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_save),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Clip")
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_share),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share")
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        OutlinedButton(
            onClick = onOpenInLibrary,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_gallery),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Library")
        }
    }
}

/**
 * Helper functions
 */
private fun formatTime(timeMs: Long): String {
    val hours = timeMs / (60 * 60 * 1000)
    val minutes = (timeMs % (60 * 60 * 1000)) / (60 * 1000)
    val seconds = (timeMs % (60 * 1000)) / 1000
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private data class TimelineMarker(
    val label: String,
    val progress: Float
)

@Preview
@Composable
private fun TimelineScrubberPreview() {
    MaterialTheme {
        TimelineScrubber(
            currentPosition = 3600000L, // 1 hour
            totalDuration = 86400000L, // 24 hours
            onSeek = { }
        )
    }
}

@Preview
@Composable
private fun PlaybackControlsPreview() {
    MaterialTheme {
        PlaybackControls(
            isPlaying = true,
            playbackSpeed = 1.0f,
            onPlayPause = { },
            onSpeedChange = { },
            onSeekBackward = { },
            onSeekForward = { }
        )
    }
}
