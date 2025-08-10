package com.eyedock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * GREEN PHASE - Implementação mínima de CameraWall
 * 
 * Grid de câmeras com estado vazio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraWall(
    cameras: List<CameraCardState>,
    onCameraClick: (String) -> Unit,
    onAddCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "camera_wall"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(testTag)
            .semantics { contentDescription = "Camera wall showing ${cameras.size} cameras" }
    ) {
        if (cameras.isEmpty()) {
            // Estado vazio
            EmptyCameraState(
                onAddCameraClick = onAddCameraClick,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Grid de câmeras
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cameras) { camera ->
                    CameraCard(
                        camera = camera,
                        onClick = { onCameraClick(camera.id) }
                    )
                }
            }
        }
    }
}

/**
 * Estado vazio para camera wall
 */
@Composable
private fun EmptyCameraState(
    onAddCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(android.R.drawable.ic_menu_camera),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No cameras yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add your first camera to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddCameraClick,
            modifier = Modifier.semantics { 
                contentDescription = "Add new camera button" 
            }
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_input_add),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Camera")
        }
    }
}

/**
 * Card individual de câmera
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraCard(
    camera: CameraCardState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .semantics { 
                contentDescription = "Camera ${camera.name}, ${if (camera.isOnline) "online" else "offline"}"
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Placeholder para thumbnail
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_camera),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Status overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = camera.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (camera.isOnline) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = if (camera.isOnline) camera.latency else "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Estado de um card de câmera
 */
data class CameraCardState(
    val id: String,
    val name: String,
    val isOnline: Boolean,
    val thumbnailUrl: String? = null,
    val latency: String = "0ms"
)

@Preview
@Composable
private fun CameraWallEmptyPreview() {
    MaterialTheme {
        CameraWall(
            cameras = emptyList(),
            onCameraClick = { },
            onAddCameraClick = { }
        )
    }
}

@Preview
@Composable
private fun CameraWallWithCamerasPreview() {
    MaterialTheme {
        CameraWall(
            cameras = listOf(
                CameraCardState("1", "Camera Hall", true, latency = "1.2s"),
                CameraCardState("2", "Camera Garden", false),
                CameraCardState("3", "Camera Garage", true, latency = "0.8s")
            ),
            onCameraClick = { },
            onAddCameraClick = { }
        )
    }
}
