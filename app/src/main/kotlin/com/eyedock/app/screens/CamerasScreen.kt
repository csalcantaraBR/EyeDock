package com.eyedock.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eyedock.app.data.local.entity.CameraEntity
import com.eyedock.app.viewmodels.CamerasViewModel
import com.eyedock.app.viewmodels.CamerasUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamerasScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLiveView: (String) -> Unit,
    onNavigateToAddCamera: () -> Unit,
    viewModel: CamerasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameras by viewModel.cameras.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadCameras()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cameras") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddCamera) {
                        Icon(Icons.Default.Add, contentDescription = "Add Camera")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val currentState = uiState) {
            CamerasUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is CamerasUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
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
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCameras() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Your Cameras",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (cameras.isEmpty()) {
                        item {
                            EmptyState(
                                onAddCameraClick = onNavigateToAddCamera
                            )
                        }
                    } else {
                        items(cameras) { camera ->
                            CameraItem(
                                camera = camera,
                                onCameraClick = { onNavigateToLiveView(camera.id.toString()) },
                                onDeleteClick = { viewModel.deleteCamera(camera.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraItem(
    camera: CameraEntity,
    onCameraClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCameraClick
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
                tint = if (camera.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = camera.name,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${camera.protocol.uppercase()}://${camera.ip}:${camera.port}${camera.path}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (camera.manufacturer != null) {
                    Text(
                        text = camera.manufacturer,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (camera.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Camera",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    onAddCameraClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
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
            text = "No cameras yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first camera to get started",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddCameraClick
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Camera")
        }
    }
}
