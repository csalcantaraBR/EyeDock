package com.eyedock.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.eyedock.app.camera.QrScannerView
import com.eyedock.app.viewmodels.QrScanViewModel
import com.eyedock.app.viewmodels.QrScanUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    onNavigateBack: () -> Unit,
    onQrCodeScanned: (String) -> Unit,
    viewModel: QrScanViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Observar mudanças no estado
    LaunchedEffect(uiState) {
        when (uiState) {
            is QrScanUiState.Success -> {
                val cameraConnection = (uiState as QrScanUiState.Success).cameraConnection
                // Converter CameraConnection para string para compatibilidade
                val qrString = "rtsp://${cameraConnection.user ?: ""}:${cameraConnection.pass ?: ""}@${cameraConnection.ip}:${cameraConnection.port}${cameraConnection.path}"
                onQrCodeScanned(qrString)
            }
            is QrScanUiState.Error -> {
                // Mostrar erro (pode ser implementado com Snackbar)
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // QR Scanner com câmera real
            QrScannerView(
                onQrDetected = { qrContent ->
                    viewModel.processQrCode(qrContent)
                },
                onTorchToggle = {
                    // Torch toggle é gerenciado internamente pelo QrScannerView
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Overlay de estado
            when (uiState) {
                is QrScanUiState.Processing -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Processing QR Code...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                is QrScanUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = (uiState as QrScanUiState.Error).message,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                else -> {}
            }
            
            // Botão de teste (apenas em debug)
            if (uiState is QrScanUiState.Idle) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = { 
                            // Simulate QR code detection for testing
                            viewModel.processQrCode("rtsp://admin:password@192.168.1.100:554/stream1")
                        }
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test QR Code (Demo)")
                    }
                }
            }
        }
    }
}
