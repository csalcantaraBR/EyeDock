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
import com.eyedock.app.screens.ManualSetupScreen
import com.eyedock.app.screens.NetworkDiscoveryScreen
import com.eyedock.app.screens.CloudBackupScreen
import com.eyedock.app.utils.Logger

object EyeDockNavigation {
    const val TAG = "EyeDockNavigation"
}

@Composable
fun EyeDockNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onNavigateToAddCamera = { navController.navigate("add_camera") },
                onNavigateToCameras = { navController.navigate("cameras") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToLiveView = { cameraId ->
                    navController.navigate("live_view/$cameraId")
                },
                onNavigateToCloudBackup = {
                    navController.navigate("cloud_backup")
                }
            )
        }
        
        // Adicionar câmera
        composable("add_camera") {
            AddCameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQrScan = { navController.navigate("qr_scan") },
                onNavigateToManualSetup = { navController.navigate("manual_setup") },
                onNavigateToNetworkDiscovery = { navController.navigate("network_discovery") }
            )
        }
        
        // Setup manual
        composable("manual_setup") {
            ManualSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onCameraAdded = { 
                    // Navegar para a lista de câmeras após adicionar
                    navController.navigate("cameras") {
                        // Limpar o back stack para evitar voltar para a tela de adicionar
                        popUpTo("main") { inclusive = false }
                    }
                }
            )
        }
        
        // Setup manual com IP pré-preenchido
        composable("manual_setup/{cameraIp}") { backStackEntry ->
            val cameraIp = backStackEntry.arguments?.getString("cameraIp")
            ManualSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onCameraAdded = { 
                    // Navegar para a lista de câmeras após adicionar
                    navController.navigate("cameras") {
                        // Limpar o back stack para evitar voltar para a tela de adicionar
                        popUpTo("main") { inclusive = false }
                    }
                },
                prefillIp = cameraIp
            )
        }
        
        // Network Discovery
        composable("network_discovery") {
            NetworkDiscoveryScreen(
                onNavigateToAddCamera = { cameraIp ->
                    // Navegar para setup manual com IP pré-preenchido
                    Logger.d(EyeDockNavigation.TAG, "Camera selected from network discovery: $cameraIp")
                    navController.navigate("manual_setup/$cameraIp")
                },
                onNavigateBack = { navController.popBackStack() }
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
                onNavigateToAddCamera = { navController.navigate("add_camera") },
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

        composable("cloud_backup") {
            CloudBackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
