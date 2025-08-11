package com.eyedock.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eyedock.app.viewmodels.LiveViewViewModel
import com.eyedock.app.viewmodels.LiveViewUiState
import com.eyedock.app.domain.interfaces.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveViewScreen(
    cameraId: String?,
    onNavigateBack: () -> Unit,
    viewModel: LiveViewViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    
    LaunchedEffect(cameraId) {
        cameraId?.let { viewModel.connectToCamera(it) }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnect()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live View") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            // Camera info
            when (val currentState = uiState) {
                is LiveViewUiState.Connected -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Camera: ${currentState.cameraConnection.ip}",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "RTSP: ${currentState.cameraConnection.proto}://${currentState.cameraConnection.ip}:${currentState.cameraConnection.port}${currentState.cameraConnection.path}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
            
            // Video player area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (val currentState = uiState) {
                        LiveViewUiState.Idle -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Videocam,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Ready to Connect",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        LiveViewUiState.Connecting -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Connecting...",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        is LiveViewUiState.Connected -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Real video player
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // AndroidView to render ExoPlayer
                                        AndroidView(
                                            factory = { context ->
                                                // Get the player view from the injected player
                                                val player = viewModel.getPlayer()
                                                player.getPlayerView()
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        
                                        // Overlay for status information
                                        if (currentState.connectionStatus != "Live") {
                                            Card(
                                                modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .padding(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                                )
                                            ) {
                                                Text(
                                                    text = currentState.connectionStatus,
                                                    modifier = Modifier.padding(8.dp),
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Status indicators
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Connection status
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Connected",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    
                                    // Recording status
                                    if (currentState.isRecording) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.FiberManualRecord,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Recording",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Mute status
                                    if (currentState.isMuted) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.VolumeOff,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Muted",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Play/Pause button
                                Button(
                                    onClick = { viewModel.togglePlayPause() }
                                ) {
                                    Icon(
                                        if (currentState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (currentState.isPlaying) "Pause" else "Play")
                                }
                            }
                        }
                        
                        is LiveViewUiState.Error -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Connection Error",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentState.message,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { 
                                        cameraId?.let { viewModel.connectToCamera(it) }
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retry Connection")
                                }
                            }
                        }
                        
                        LiveViewUiState.SnapshotTaken -> {
                            // Show snapshot taken message briefly
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(2000)
                                // Reset to connected state
                            }
                        }
                    }
                }
            }
            
            // Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        enabled = uiState is LiveViewUiState.Connected
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.takeSnapshot() },
                        enabled = uiState is LiveViewUiState.Connected
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = "Snapshot")
                    }
                    
                    IconButton(
                        onClick = { viewModel.toggleRecording() },
                        enabled = uiState is LiveViewUiState.Connected
                    ) {
                        Icon(
                            Icons.Default.FiberManualRecord,
                            contentDescription = "Start/Stop Recording",
                            tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        enabled = uiState is LiveViewUiState.Connected
                    ) {
                        Icon(
                            if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute/Unmute"
                        )
                    }
                    
                    IconButton(
                        onClick = { /* TODO: Fullscreen */ },
                        enabled = uiState is LiveViewUiState.Connected
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen")
                    }
                }
            }
        }
    }
}
