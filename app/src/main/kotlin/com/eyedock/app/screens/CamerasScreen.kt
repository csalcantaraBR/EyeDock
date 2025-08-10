package com.eyedock.app.screens

import androidx.compose.foundation.background
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.eyedock.app.viewmodels.CamerasViewModel
import com.eyedock.app.viewmodels.CameraCardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamerasScreen(
    onAddCamera: () -> Unit = {},
    onCameraClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CamerasViewModel = hiltViewModel()
) {
    var gridColumns by remember { mutableIntStateOf(2) }
    var showDiscoveryDialog by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    val isDiscovering by viewModel.isDiscovering.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCameras()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Câmeras") },
                actions = {
                    IconButton(onClick = onAddCamera) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Câmera")
                    }
                    IconButton(onClick = { /* TODO: Implementar scan QR */ }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCamera,
                modifier = Modifier.testTag("add_camera_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Camera")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Error handling
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.cameras.isEmpty()) {
                EmptyCamerasState(
                    onAddCamera = onAddCamera,
                    onDiscoverCameras = { viewModel.discoverCameras() },
                    isDiscovering = isDiscovering,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CameraWall(
                    cameras = uiState.cameras,
                    gridColumns = gridColumns,
                    onCameraClick = onCameraClick,
                    onCameraLongClick = { cameraId ->
                        // Show camera options dialog
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("camera_wall")
                )
            }
        }
    }

    // Discovery dialog
    if (showDiscoveryDialog) {
        AlertDialog(
            onDismissRequest = { showDiscoveryDialog = false },
            title = { Text("Discover Cameras") },
            text = { 
                Text(
                    if (isDiscovering) "Searching for cameras on your network..." 
                    else "This will search your local network for ONVIF/RTSP cameras."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.discoverCameras()
                        showDiscoveryDialog = false
                    },
                    enabled = !isDiscovering
                ) {
                    if (isDiscovering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Discover")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscoveryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyCamerasState(
    onAddCamera: () -> Unit,
    onDiscoverCameras: () -> Unit,
    isDiscovering: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Videocam,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No cameras found",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first IP camera to get started",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddCamera,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Camera")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onDiscoverCameras,
            enabled = !isDiscovering,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isDiscovering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Discover Cameras")
        }
    }
}

@Composable
fun CameraWall(
    cameras: List<CameraCardState>,
    gridColumns: Int,
    onCameraClick: (String) -> Unit,
    onCameraLongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(cameras) { camera ->
            CameraCard(
                camera = camera,
                onClick = { onCameraClick(camera.id) },
                onLongClick = { onCameraLongClick(camera.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCard(
    camera: CameraCardState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (camera.isOnline) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = when {
                                camera.isOnline -> Color.Green
                                camera.status == "Connecting" -> Color(0xFFFFA500)
                                else -> Color.Red
                            },
                            shape = MaterialTheme.shapes.small
                        )
                )
                IconButton(
                    onClick = onLongClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Camera options",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Camera info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = camera.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = camera.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Stats
            if (camera.isOnline) {
                Column {
                    Text(
                        text = "IP: ${camera.ipAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
