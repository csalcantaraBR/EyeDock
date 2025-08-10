package com.eyedock.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyedock.app.data.repository.Device
import com.eyedock.app.data.repository.DeviceRepository
import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.data.security.SecureCredentialStore
import com.eyedock.app.domain.interfaces.OnvifClient
import com.eyedock.app.domain.model.Auth
import com.eyedock.app.domain.usecase.OnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CamerasViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val credentialStore: SecureCredentialStore,
    private val onvifClient: OnvifClient,
    private val onboardingUseCase: OnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CamerasUiState())
    val uiState: StateFlow<CamerasUiState> = _uiState.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    init {
        loadCameras()
    }

    fun loadCameras() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                deviceRepository.getAllDevices().collect { devices ->
                    _uiState.update {
                        it.copy(
                            cameras = devices.map { device ->
                                CameraCardState(
                                    id = device.id,
                                    name = device.name,
                                    status = "Online", // TODO: Implementar verificação real
                                    isOnline = true, // TODO: Implementar verificação real
                                    lastSeen = device.updatedAt,
                                    ipAddress = device.lastKnownIp ?: device.rtspHost ?: "",
                                    rtspUrl = buildRtspUrl(device)
                                )
                            },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Erro ao carregar câmeras: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun discoverCameras() {
        viewModelScope.launch {
            _isDiscovering.value = true
            try {
                val endpoints = onvifClient.probe(2500)
                
                if (endpoints.isEmpty()) {
                    _uiState.update {
                        it.copy(error = "Nenhuma câmera ONVIF encontrada na rede")
                    }
                } else {
                    _uiState.update {
                        it.copy(error = "Encontradas ${endpoints.size} câmeras ONVIF. Use o QR Code para adicionar.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Erro na descoberta: ${e.message}")
                }
            } finally {
                _isDiscovering.value = false
            }
        }
    }

    fun processQrCode(qrText: String, deviceName: String, auth: Auth?) {
        viewModelScope.launch {
            try {
                val result = onboardingUseCase.processQrCode(qrText, deviceName, auth)
                if (result != null) {
                    // Recarregar lista de câmeras
                    loadCameras()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Erro ao processar QR Code: ${e.message}")
                }
            }
        }
    }

    fun addCamera(name: String, ipAddress: String, rtspUrl: String, auth: Auth?) {
        viewModelScope.launch {
            try {
                // Criar conexão básica
                val connection = com.eyedock.app.domain.model.CameraConnection(
                    proto = "rtsp",
                    ip = ipAddress,
                    port = 554,
                    user = auth?.username,
                    pass = auth?.password,
                    path = extractPathFromRtspUrl(rtspUrl),
                    meta = mapOf("source" to "manual")
                )

                val device = deviceRepository.addDevice(connection, name)
                
                if (auth != null) {
                    credentialStore.saveCredentials(device.id, auth)
                }

                // Recarregar lista
                loadCameras()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Erro ao adicionar câmera: ${e.message}")
                }
            }
        }
    }

    fun deleteCamera(cameraId: String) {
        viewModelScope.launch {
            try {
                deviceRepository.deleteDevice(cameraId)
                credentialStore.removeCredentials(cameraId)
                loadCameras()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Erro ao deletar câmera: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun buildRtspUrl(device: Device): String {
        val protocol = "rtsp"
        val host = device.rtspHost ?: device.lastKnownIp ?: ""
        val port = device.rtspPort ?: 554
        val path = device.rtspPath ?: ""
        
        return "$protocol://$host:$port$path"
    }

    private fun extractPathFromRtspUrl(rtspUrl: String): String? {
        return try {
            val uri = java.net.URI(rtspUrl)
            uri.path
        } catch (e: Exception) {
            null
        }
    }
}

data class CameraCardState(
    val id: String,
    val name: String,
    val status: String,
    val isOnline: Boolean,
    val lastSeen: Long?,
    val ipAddress: String,
    val rtspUrl: String
)

data class CamerasUiState(
    val cameras: List<CameraCardState> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
