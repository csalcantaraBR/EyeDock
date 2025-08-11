package com.eyedock.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eyedock.app.screens.MainScreen
import com.eyedock.app.screens.AddCameraScreen
import com.eyedock.app.screens.LiveViewScreen
import com.eyedock.app.screens.CamerasScreen
import com.eyedock.app.screens.SettingsScreen
import com.eyedock.app.screens.QrScanScreen

@Composable
fun EyeDockNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Tela principal
        composable("main") {
            MainScreen(
                onNavigateToAddCamera = { navController.navigate("add_camera") },
                onNavigateToCameras = { navController.navigate("cameras") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToLiveView = { cameraId -> 
                    navController.navigate("live_view/$cameraId")
                }
            )
        }
        
        // Adicionar câmera
        composable("add_camera") {
            AddCameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQrScan = { navController.navigate("qr_scan") }
            )
        }
        
        // Scanner QR
        composable("qr_scan") {
            QrScanScreen(
                onNavigateBack = { navController.popBackStack() },
                onQrCodeScanned = { qrData ->
                    // TODO: Processar QR code e navegar para configuração
                    navController.popBackStack()
                }
            )
        }
        
        // Visualização ao vivo
        composable("live_view/{cameraId}") { backStackEntry ->
            val cameraId = backStackEntry.arguments?.getString("cameraId")
            LiveViewScreen(
                cameraId = cameraId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Lista de câmeras
        composable("cameras") {
            CamerasScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLiveView = { cameraId ->
                    navController.navigate("live_view/$cameraId")
                }
            )
        }
        
        // Configurações
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
