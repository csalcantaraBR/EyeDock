package com.eyedock.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

data class AlertEvent(
    val id: String,
    val type: AlertType,
    val cameraId: String,
    val cameraName: String,
    val timestamp: Long,
    val thumbnailUrl: String? = null,
    val confidence: Float = 0.85f,
    val isDismissed: Boolean = false
)

enum class AlertType {
    MOTION, SOUND, PERSON_DETECTED, VEHICLE_DETECTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onOpenRecording: (String, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(AlertFilter.ALL) }
    var selectedCamera by remember { mutableStateOf("all") }

    // Lista vazia - sem dados mockados
    val alerts = remember { emptyList<AlertEvent>() }

    val filteredAlerts = alerts.filter { alert ->
        val typeMatches = when (selectedFilter) {
            AlertFilter.ALL -> true
            AlertFilter.MOTION -> alert.type == AlertType.MOTION
            AlertFilter.SOUND -> alert.type == AlertType.SOUND
            AlertFilter.PERSON -> alert.type == AlertType.PERSON_DETECTED
            AlertFilter.VEHICLE -> alert.type == AlertType.VEHICLE_DETECTED
        }
        val cameraMatches = selectedCamera == "all" || alert.cameraId == selectedCamera
        typeMatches && cameraMatches && !alert.isDismissed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alerts") },
                actions = {
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Alert Settings")
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
            // Filter Controls
            AlertFilters(
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                selectedCamera = selectedCamera,
                onCameraChange = { selectedCamera = it },
                modifier = Modifier.padding(16.dp)
            )

            if (filteredAlerts.isEmpty()) {
                EmptyAlertsState(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("alerts"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredAlerts) { alert ->
                        AlertCard(
                            alert = alert,
                            onOpenRecording = { onOpenRecording(alert.cameraId, alert.timestamp) },
                            onDismiss = { /* TODO: Dismiss alert */ },
                            modifier = Modifier.testTag("alert_card_${alert.id}")
                        )
                    }
                }
            }
        }
    }
}

enum class AlertFilter {
    ALL, MOTION, SOUND, PERSON, VEHICLE
}

@Composable
fun AlertFilters(
    selectedFilter: AlertFilter,
    onFilterChange: (AlertFilter) -> Unit,
    selectedCamera: String,
    onCameraChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == AlertFilter.ALL,
                onClick = { onFilterChange(AlertFilter.ALL) },
                label = { Text("All") }
            )
            FilterChip(
                selected = selectedFilter == AlertFilter.MOTION,
                onClick = { onFilterChange(AlertFilter.MOTION) },
                label = { Text("Motion") },
                leadingIcon = {
                    Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
            FilterChip(
                selected = selectedFilter == AlertFilter.SOUND,
                onClick = { onFilterChange(AlertFilter.SOUND) },
                label = { Text("Sound") },
                leadingIcon = {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == AlertFilter.PERSON,
                onClick = { onFilterChange(AlertFilter.PERSON) },
                label = { Text("Person") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
            FilterChip(
                selected = selectedFilter == AlertFilter.VEHICLE,
                onClick = { onFilterChange(AlertFilter.VEHICLE) },
                label = { Text("Vehicle") },
                leadingIcon = {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertCard(
    alert: AlertEvent,
    onOpenRecording: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail placeholder
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
                        when (alert.type) {
                            AlertType.MOTION -> Icons.Default.DirectionsRun
                            AlertType.SOUND -> Icons.Default.VolumeUp
                            AlertType.PERSON_DETECTED -> Icons.Default.Person
                            AlertType.VEHICLE_DETECTED -> Icons.Default.DirectionsCar
                        },
                        contentDescription = null,
                        tint = when (alert.type) {
                            AlertType.MOTION -> Color(0xFFFFA500)
                            AlertType.SOUND -> Color.Blue
                            AlertType.PERSON_DETECTED -> Color.Green
                            AlertType.VEHICLE_DETECTED -> Color.Red
                        }
                    )
                }
            }

            // Alert info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = when (alert.type) {
                                AlertType.MOTION -> "Motion Detected"
                                AlertType.SOUND -> "Sound Detected"
                                AlertType.PERSON_DETECTED -> "Person Detected"
                                AlertType.VEHICLE_DETECTED -> "Vehicle Detected"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "${alert.cameraName} • ${formatRelativeTime(alert.timestamp)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (alert.confidence > 0) {
                            Text(
                                text = "Confidence: ${(alert.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenRecording,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Recording")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyAlertsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No alerts yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Motion and sound alerts from your cameras will appear here",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
