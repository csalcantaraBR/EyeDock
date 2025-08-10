package com.eyedock.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eyedock.app.screens.*
import com.eyedock.app.ui.scan.QrScanScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeDockNavigation(
    navController: NavHostController = rememberNavController()
) {
    val items = listOf(
        NavigationItem(
            route = "cameras",
            title = "Câmeras",
            icon = Icons.Default.Videocam
        ),
        NavigationItem(
            route = "liveview",
            title = "Live View",
            icon = Icons.Default.PlayArrow
        ),
        NavigationItem(
            route = "timeline",
            title = "Timeline",
            icon = Icons.Default.Timeline
        ),
        NavigationItem(
            route = "alerts",
            title = "Alertas",
            icon = Icons.Default.Notifications
        ),
        NavigationItem(
            route = "library",
            title = "Biblioteca",
            icon = Icons.Default.VideoLibrary
        ),
        NavigationItem(
            route = "settings",
            title = "Configurações",
            icon = Icons.Default.Settings
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "cameras",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("cameras") {
                CamerasScreen(
                    onAddCamera = { navController.navigate("add_camera") },
                    onCameraClick = { cameraId -> 
                        navController.navigate("liveview/$cameraId")
                    }
                )
            }
            
            composable("add_camera") {
                AddCameraScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("scan_qr") {
                QrScanScreen(
                    onQrScanned = { qrText ->
                        // Navegar para tela de configuração da câmera
                        navController.navigate("add_camera?qrText=$qrText")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("liveview/{cameraId}") { backStackEntry ->
                val cameraId = backStackEntry.arguments?.getString("cameraId")
                LiveViewScreen(
                    cameraId = cameraId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("liveview") {
                LiveViewScreen(
                    cameraId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("timeline") {
                TimelineScreen()
            }
            
            composable("alerts") {
                AlertsScreen()
            }
            
            composable("library") {
                LibraryScreen()
            }
            
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
