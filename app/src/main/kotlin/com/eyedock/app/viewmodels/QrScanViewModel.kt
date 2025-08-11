package com.eyedock.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyedock.app.camera.QrCodeParser
import com.eyedock.app.camera.CameraQrConfig
import com.eyedock.app.di.AppModule
import com.eyedock.app.domain.interfaces.QrParser
import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.model.QrPayload
import com.eyedock.app.utils.Constants
import com.eyedock.app.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing QR code scanning functionality.
 * Handles QR code processing and validation.
 */
class QrScanViewModel : ViewModel() {

    private val qrParser: QrParser = AppModule.getQrParser()
    private val qrCodeParser = QrCodeParser()
    private val logger = Logger.withTag("QrScanViewModel")

    private val _uiState = MutableStateFlow<QrScanUiState>(QrScanUiState.Idle)
    val uiState: StateFlow<QrScanUiState> = _uiState.asStateFlow()

    /**
     * Process a QR code string and extract camera connection information
     */
    fun processQrCode(qrData: String) {
        viewModelScope.launch {
            _uiState.value = QrScanUiState.Processing

            try {
                logger.d("Processing QR code: $qrData")
                
                // Primeiro tentar o novo parser
                val cameraConfig = qrCodeParser.parseQrCode(qrData)
                if (cameraConfig != null) {
                    logger.i("QR code parsed successfully: ${cameraConfig.name}")
                    
                    // Converter para CameraConnection
                    val cameraConnection = CameraConnection(
                        proto = "rtsp",
                        ip = cameraConfig.ip,
                        port = cameraConfig.port,
                        user = cameraConfig.username,
                        pass = cameraConfig.password,
                        path = Constants.DEFAULT_RTSP_PATH,
                        meta = com.eyedock.app.domain.model.CameraMeta(source = "qr_scan")
                    )
                    
                    _uiState.value = QrScanUiState.Success(cameraConnection)
                    return@launch
                }
                
                // Fallback para o parser antigo
                logger.d("New parser failed, trying legacy parser")
                val payload = qrParser.parseQrCode(qrData)
                if (payload == null) {
                    _uiState.value = QrScanUiState.Error(Constants.ErrorMessages.INVALID_QR_CODE)
                    return@launch
                }

                if (!qrParser.isValidPayload(payload)) {
                    _uiState.value = QrScanUiState.Error("Invalid QR code content")
                    return@launch
                }

                val cameraConnection = qrParser.normalizePayload(payload)
                if (cameraConnection == null) {
                    _uiState.value = QrScanUiState.Error("Unable to parse camera connection")
                    return@launch
                }

                _uiState.value = QrScanUiState.Success(cameraConnection)

            } catch (e: Exception) {
                logger.e("Error processing QR code: ${e.message}")
                _uiState.value = QrScanUiState.Error("${Constants.ErrorMessages.QR_PROCESSING_ERROR}: ${e.message}")
            }
        }
    }

    /**
     * Reset the UI state to idle
     */
    fun resetState() {
        _uiState.value = QrScanUiState.Idle
    }
}

/**
 * UI states for the QR scan screen
 */
sealed class QrScanUiState {
    object Idle : QrScanUiState()
    object Processing : QrScanUiState()
    data class Success(val cameraConnection: CameraConnection) : QrScanUiState()
    data class Error(val message: String) : QrScanUiState()
}
