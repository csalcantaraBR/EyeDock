package com.eyedock.app.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * QR Scanner usando CameraX e ML Kit
 * Implementação real para escanear QR codes de câmeras
 */
@Composable
fun QrScannerView(
    onQrDetected: (String) -> Unit,
    onTorchToggle: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "qr_scanner"
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    
    // Verificar permissão de câmera
    LaunchedEffect(Unit) {
        hasCameraPermission = context.checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    Box(modifier = modifier) {
        if (hasCameraPermission) {
            CameraPreview(
                onQrDetected = onQrDetected,
                isTorchOn = isTorchOn,
                lifecycleOwner = lifecycleOwner,
                context = context
            )
        } else {
            // Fallback quando não há permissão
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Please grant camera permission to scan QR codes",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Controles overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Align QR code from camera box or app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Torch toggle
                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        onTorchToggle()
                    }
                ) {
                    Icon(
                        imageVector = if (isTorchOn) {
                            Icons.Default.FlashOn
                        } else {
                            Icons.Default.FlashOff
                        },
                        contentDescription = "Toggle flashlight",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onQrDetected: (String) -> Unit,
    isTorchOn: Boolean,
    lifecycleOwner: LifecycleOwner,
    context: Context
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImage(imageProxy, barcodeScanner, onQrDetected)
                        }
                    }
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                    
                    // Configurar flash
                    camera.cameraControl.enableTorch(isTorchOn)
                    
                } catch (e: Exception) {
                    Log.e("QrScanner", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

private fun processImage(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQrDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        barcode.rawValue?.let { qrContent ->
                            Log.d("QrScanner", "QR Code detected: $qrContent")
                            onQrDetected(qrContent)
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

/**
 * Parser para QR codes de câmeras
 */
class QrCodeParser {
    
    /**
     * Parse QR code content para configuração de câmera
     */
    fun parseQrCode(qrContent: String): CameraQrConfig? {
        return try {
            // Tentar diferentes formatos de QR code
            
            // Formato 1: JSON direto
            if (qrContent.startsWith("{")) {
                parseJsonQrCode(qrContent)
            }
            // Formato 2: URL RTSP
            else if (qrContent.startsWith("rtsp://")) {
                parseRtspUrl(qrContent)
            }
            // Formato 3: String simples com IP
            else if (qrContent.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$"))) {
                parseSimpleIp(qrContent)
            }
            // Formato 4: String com IP:porta
            else if (qrContent.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d+$"))) {
                parseIpPort(qrContent)
            }
            else {
                null
            }
        } catch (e: Exception) {
            Log.e("QrCodeParser", "Error parsing QR code", e)
            null
        }
    }
    
    private fun parseJsonQrCode(jsonContent: String): CameraQrConfig? {
        return try {
            // Parse JSON básico (sem dependências externas)
            val ip = extractJsonValue(jsonContent, "ip")
            val port = extractJsonValue(jsonContent, "port")?.toIntOrNull() ?: 554
            val username = extractJsonValue(jsonContent, "user") ?: extractJsonValue(jsonContent, "username")
            val password = extractJsonValue(jsonContent, "pass") ?: extractJsonValue(jsonContent, "password")
            val name = extractJsonValue(jsonContent, "name")
            
            if (ip != null) {
                CameraQrConfig(
                    ip = ip,
                    port = port,
                    username = username,
                    password = password,
                    name = name ?: "Camera at $ip"
                )
            } else null
        } catch (e: Exception) {
            Log.e("QrCodeParser", "Error parsing JSON QR code", e)
            null
        }
    }
    
    private fun parseRtspUrl(rtspUrl: String): CameraQrConfig? {
        return try {
            // Parse RTSP URL: rtsp://username:password@ip:port/path
            val regex = Regex("rtsp://(?:([^:]+):([^@]+)@)?([^:/]+)(?::(\\d+))?(?:/(.+))?")
            val match = regex.find(rtspUrl)
            
            if (match != null) {
                val (username, password, ip, portStr, path) = match.destructured
                val port = portStr.toIntOrNull() ?: 554
                
                CameraQrConfig(
                    ip = ip,
                    port = port,
                    username = username.takeIf { it.isNotEmpty() },
                    password = password.takeIf { it.isNotEmpty() },
                    name = "Camera at $ip"
                )
            } else null
        } catch (e: Exception) {
            Log.e("QrCodeParser", "Error parsing RTSP URL", e)
            null
        }
    }
    
    private fun parseSimpleIp(ip: String): CameraQrConfig? {
        return CameraQrConfig(
            ip = ip,
            port = 554,
            username = null,
            password = null,
            name = "Camera at $ip"
        )
    }
    
    private fun parseIpPort(ipPort: String): CameraQrConfig? {
        val parts = ipPort.split(":")
        if (parts.size == 2) {
            val ip = parts[0]
            val port = parts[1].toIntOrNull() ?: 554
            
            return CameraQrConfig(
                ip = ip,
                port = port,
                username = null,
                password = null,
                name = "Camera at $ip:$port"
            )
        }
        return null
    }
    
    private fun extractJsonValue(json: String, key: String): String? {
        val pattern = Regex("\"$key\"\\s*:\\s*\"([^\"]+)\"")
        val match = pattern.find(json)
        return match?.groupValues?.get(1)
    }
}

/**
 * Configuração de câmera extraída do QR code
 */
data class CameraQrConfig(
    val ip: String,
    val port: Int,
    val username: String?,
    val password: String?,
    val name: String
)
