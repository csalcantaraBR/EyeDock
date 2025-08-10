package com.eyedock.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

data class RecordingItem(
    val id: String,
    val cameraId: String,
    val cameraName: String,
    val filename: String,
    val timestamp: Long,
    val duration: Long, // in milliseconds
    val fileSize: Long, // in bytes
    val thumbnailUrl: String? = null,
    val recordingType: RecordingType = RecordingType.CONTINUOUS
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onPlayRecording: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedCamera by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }

    // Lista vazia - sem dados mockados
    val recordings = remember { emptyList<RecordingItem>() }

    val filteredRecordings = recordings.filter { recording ->
        val nameMatches = recording.cameraName.contains(searchQuery, ignoreCase = true) || 
                         recording.filename.contains(searchQuery, ignoreCase = true)
        val cameraMatches = selectedCamera == "all" || recording.cameraId == selectedCamera
        val dateMatches = selectedDate?.let { date ->
            val recordingDate = Calendar.getInstance().apply { timeInMillis = recording.timestamp }
            val selectedCal = Calendar.getInstance().apply { time = date }
            recordingDate.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
            recordingDate.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR)
        } ?: true
        
        nameMatches && cameraMatches && dateMatches
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                actions = {
                    IconButton(
                        onClick = { viewMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID }
                    ) {
                        Icon(
                            if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Change view mode"
                        )
                    }
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Library Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and Filters
            LibraryFilters(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                selectedCamera = selectedCamera,
                onCameraChange = { selectedCamera = it },
                modifier = Modifier.padding(16.dp)
            )

            if (filteredRecordings.isEmpty()) {
                EmptyLibraryState(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                when (viewMode) {
                    ViewMode.GRID -> {
                        RecordingsGrid(
                            recordings = filteredRecordings,
                            onPlayRecording = onPlayRecording,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("library")
                        )
                    }
                    ViewMode.LIST -> {
                        RecordingsList(
                            recordings = filteredRecordings,
                            onPlayRecording = onPlayRecording,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("library")
                        )
                    }
                }
            }
        }
    }
}

enum class ViewMode {
    GRID, LIST
}

@Composable
fun LibraryFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedDate: Date?,
    onDateChange: (Date?) -> Unit,
    selectedCamera: String,
    onCameraChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Search recordings") },
            placeholder = { Text("Camera name or filename") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date filter
            FilterChip(
                selected = selectedDate != null,
                onClick = { /* TODO: Show date picker */ },
                label = { 
                    Text(
                        selectedDate?.let { 
                            SimpleDateFormat("MMM dd", Locale.getDefault()).format(it) 
                        } ?: "Any date"
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )

            // Camera filter
            FilterChip(
                selected = selectedCamera != "all",
                onClick = { /* TODO: Show camera picker */ },
                label = { Text(if (selectedCamera == "all") "All cameras" else "Camera") },
                leadingIcon = {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
        }
    }
}

@Composable
fun RecordingsGrid(
    recordings: List<RecordingItem>,
    onPlayRecording: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recordings) { recording ->
            RecordingGridItem(
                recording = recording,
                onPlay = { onPlayRecording(recording.id) },
                modifier = Modifier.testTag("recording_item_${recording.id}")
            )
        }
    }
}

@Composable
fun RecordingsList(
    recordings: List<RecordingItem>,
    onPlayRecording: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recordings) { recording ->
            RecordingListItem(
                recording = recording,
                onPlay = { onPlayRecording(recording.id) },
                modifier = Modifier.testTag("recording_item_${recording.id}")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingGridItem(
    recording: RecordingItem,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onPlay,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Thumbnail
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.VideoFile,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    // Duration overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = formatDuration(recording.duration),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Recording type indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Surface(
                            color = when (recording.recordingType) {
                                RecordingType.MOTION -> Color.Red
                                RecordingType.MANUAL -> Color.Green
                                RecordingType.CONTINUOUS -> Color.Blue
                            }.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = when (recording.recordingType) {
                                    RecordingType.MOTION -> "M"
                                    RecordingType.MANUAL -> "●"
                                    RecordingType.CONTINUOUS -> "C"
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recording info
            Text(
                text = recording.cameraName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(recording.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatFileSize(recording.fileSize),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingListItem(
    recording: RecordingItem,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onPlay,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            Card(
                modifier = Modifier.size(64.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.VideoFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Recording info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recording.cameraName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = SimpleDateFormat("MMMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(recording.timestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = formatDuration(recording.duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatFileSize(recording.fileSize),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Actions
            Row {
                IconButton(onClick = { /* TODO: Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = { /* TODO: Download */ }) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                }
                IconButton(onClick = { /* TODO: Delete */ }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.VideoLibrary,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No recordings yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Recordings from your cameras will appear here. Start recording to build your library.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
        else -> String.format("%d:%02d", minutes, seconds % 60)
    }
}

fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        else -> String.format("%.1f KB", kb)
    }
}
