package com.eyedock.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

data class RecordingSegment(
    val startTime: Long,
    val endTime: Long,
    val type: RecordingType
)

enum class RecordingType {
    CONTINUOUS, MOTION, MANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    cameraId: String? = null,
    cameraName: String = "Camera",
    onNavigateBack: () -> Unit = {}
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = currentTime }) }

    // Lista vazia - sem dados mockados
    val recordingSegments = remember { emptyList<RecordingSegment>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$cameraName - Timeline") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Calendar picker */ }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Video Player Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(16.dp)
                    .testTag("timeline_player"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "📹 PLAYBACK - ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Speed: ${playbackSpeed}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Play/Pause overlay
                    if (!isPlaying) {
                        IconButton(
                            onClick = { isPlaying = true },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Timeline
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("timeline"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Date display
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(selectedDate.time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 24-hour timeline
                    TimelineRuler(
                        currentTime = currentTime,
                        selectedDate = selectedDate.timeInMillis,
                        recordingSegments = recordingSegments,
                        onTimeSelected = { currentTime = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip back 10s
                IconButton(
                    onClick = { 
                        currentTime = maxOf(currentTime - 10000, selectedDate.timeInMillis)
                    }
                ) {
                    Icon(Icons.Default.Replay10, contentDescription = "Skip back 10s")
                }

                // Play/Pause
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(28.dp))
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Skip forward 10s
                IconButton(
                    onClick = { 
                        currentTime = minOf(currentTime + 10000, selectedDate.timeInMillis + 24 * 60 * 60 * 1000 - 1)
                    }
                ) {
                    Icon(Icons.Default.Forward10, contentDescription = "Skip forward 10s")
                }

                // Speed control
                TextButton(
                    onClick = { 
                        playbackSpeed = when (playbackSpeed) {
                            0.5f -> 1.0f
                            1.0f -> 2.0f
                            2.0f -> 4.0f
                            4.0f -> 0.5f
                            else -> 1.0f
                        }
                    }
                ) {
                    Text("${playbackSpeed}x")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Export clip */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Clip")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { /* TODO: Share */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { /* TODO: Open in library */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Library")
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun TimelineRuler(
    currentTime: Long,
    selectedDate: Long,
    recordingSegments: List<RecordingSegment>,
    onTimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clickable { /* Handle timeline clicks */ }
    ) {
        drawTimelineRuler(
            currentTime = currentTime,
            selectedDate = selectedDate,
            recordingSegments = recordingSegments,
            canvasSize = size
        )
    }
}

fun DrawScope.drawTimelineRuler(
    currentTime: Long,
    selectedDate: Long,
    recordingSegments: List<RecordingSegment>,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    val width = canvasSize.width
    val height = canvasSize.height
    val dayStartTime = selectedDate
    val dayEndTime = dayStartTime + 24 * 60 * 60 * 1000
    
    // Draw hour markers
    for (hour in 0..24) {
        val x = (hour / 24f) * width
        val time = dayStartTime + hour * 60 * 60 * 1000
        
        // Hour line
        drawLine(
            color = Color.Gray,
            start = Offset(x, height * 0.7f),
            end = Offset(x, height),
            strokeWidth = if (hour % 6 == 0) 3.dp.toPx() else 1.dp.toPx()
        )
    }
    
    // Draw recording segments
    recordingSegments.forEach { segment ->
        val startX = ((segment.startTime - dayStartTime).toFloat() / (dayEndTime - dayStartTime)) * width
        val endX = ((segment.endTime - dayStartTime).toFloat() / (dayEndTime - dayStartTime)) * width
        
        val segmentColor = when (segment.type) {
            RecordingType.CONTINUOUS -> Color.Blue
            RecordingType.MOTION -> Color.Red
            RecordingType.MANUAL -> Color.Green
        }
        
        drawLine(
            color = segmentColor,
            start = Offset(startX, height * 0.3f),
            end = Offset(endX, height * 0.3f),
            strokeWidth = 8.dp.toPx()
        )
    }
    
    // Draw current time indicator
    val currentX = ((currentTime - dayStartTime).toFloat() / (dayEndTime - dayStartTime)) * width
    drawLine(
        color = Color.Red,
        start = Offset(currentX, 0f),
        end = Offset(currentX, height),
        strokeWidth = 3.dp.toPx()
    )
    
    // Draw current time knob
    drawCircle(
        color = Color.Red,
        radius = 8.dp.toPx(),
        center = Offset(currentX, height * 0.5f)
    )
}
