package com.eyedock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun CameraWall(
    cameras: List<CameraItem>,
    onCameraClick: (CameraItem) -> Unit,
    onAddCameraClick: () -> Unit,
    testTag: String = "camera_wall"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (cameras.isEmpty()) {
            Text(
                text = "No cameras yet",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onAddCameraClick) {
                Text("Add Camera")
            }
        } else {
            // Grid de câmeras seria implementado aqui
            Text("Camera grid with ${cameras.size} cameras")
        }
    }
}

data class CameraItem(
    val id: String,
    val name: String,
    val ip: String,
    val isOnline: Boolean
)
