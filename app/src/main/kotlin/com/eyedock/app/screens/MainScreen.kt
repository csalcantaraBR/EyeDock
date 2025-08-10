package com.eyedock.app.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToAddCamera: () -> Unit = {},
    onNavigateToLiveView: (String) -> Unit = {},
    onNavigateToTimeline: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EyeDock") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                    label = { Text("Cameras") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    label = { Text("Alerts") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = null) },
                    label = { Text("Library") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onNavigateToAddCamera,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_camera_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Camera")
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> CamerasScreen(
                onAddCamera = onNavigateToAddCamera,
                onCameraClick = onNavigateToLiveView,
                modifier = Modifier.padding(paddingValues)
            )
            1 -> AlertsScreen(
                onOpenRecording = { cameraId, timestamp ->
                    onNavigateToTimeline(cameraId)
                },
                modifier = Modifier.padding(paddingValues)
            )
            2 -> LibraryScreen(
                onPlayRecording = { recordingId ->
                    // TODO: Navigate to specific recording playback
                    onNavigateToTimeline("1") // For now, just go to timeline
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
