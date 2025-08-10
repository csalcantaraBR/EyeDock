package com.eyedock.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyedock.app.data.repository.DeviceRepository
import com.eyedock.app.data.security.SecureCredentialStore
import com.eyedock.app.domain.interfaces.Player
import com.eyedock.app.domain.model.Auth
import com.eyedock.app.domain.usecase.PtzUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveViewViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val credentialStore: SecureCredentialStore,
    private val player: Player,
    private val ptzUseCase: PtzUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveViewUiState())
    val uiState: StateFlow<LiveViewUiState> = _uiState.asStateFlow()

    fun loadCamera(cameraId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                if (cameraId.isNullOrEmpty()) {
                    _uiState.update { 
                        it.copy(
                            error = "ID da câmera inválido",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                val device = deviceRepository.getDevice(cameraId)
                if (device == null) {
                    _uiState.update { 
                        it.copy(
                            error = "Câmera não encontrada",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                val auth = credentialStore.getCredentials(cameraId)
                val rtspUri = buildRtspUri(device, auth)
                
                _uiState.update { 
                    it.copy(
                        cameraId = cameraId,
                        cameraName = device.name,
                        rtspUri = rtspUri,
                        hasPtz = device.hasPtz,
                        isLoading = false
                    )
                }
                
                // Iniciar reprodução
                startPlayback(rtspUri, auth)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Erro ao carregar câmera: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun startPlayback(rtspUri: String, auth: Auth?) {
        try {
            player.play(rtspUri, auth)
            _uiState.update { 
                it.copy(
                    isConnected = true,
                    isPlaying = true
                )
            }
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    error = "Erro ao iniciar reprodução: ${e.message}",
                    isConnected = false,
                    isPlaying = false
                )
            }
        }
    }

    fun movePtz(x: Float, y: Float) {
        val cameraId = _uiState.value.cameraId ?: return
        
        if (_uiState.value.hasPtz) {
            ptzUseCase.onPanTiltAsync(cameraId, x, y)
        }
    }

    fun zoomPtz(delta: Float) {
        val cameraId = _uiState.value.cameraId ?: return
        
        if (_uiState.value.hasPtz) {
            ptzUseCase.onZoomAsync(cameraId, delta)
        }
    }

    fun stopPtz() {
        val cameraId = _uiState.value.cameraId ?: return
        
        if (_uiState.value.hasPtz) {
            ptzUseCase.onGestureEndAsync(cameraId)
        }
    }

    fun toggleMicrophone() {
        _uiState.update { 
            it.copy(isMicrophoneEnabled = !it.isMicrophoneEnabled)
        }
    }

    fun setMicrophoneVolume(volume: Float) {
        _uiState.update { it.copy(microphoneVolume = volume) }
    }

    fun toggleSpeaker() {
        _uiState.update { 
            it.copy(isSpeakerEnabled = !it.isSpeakerEnabled)
        }
    }

    fun setSpeakerVolume(volume: Float) {
        _uiState.update { it.copy(speakerVolume = volume) }
    }

    fun startRecording() {
        viewModelScope.launch {
            try {
                // TODO: Implementar gravação real
                _uiState.update { it.copy(isRecording = true) }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Erro ao iniciar gravação: ${e.message}")
                }
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                // TODO: Parar gravação real
                _uiState.update { it.copy(isRecording = false) }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Erro ao parar gravação: ${e.message}")
                }
            }
        }
    }

    fun takeSnapshot(): ByteArray? {
        return try {
            player.snapshot()
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(error = "Erro ao capturar snapshot: ${e.message}")
            }
            null
        }
    }

    fun stopPlayback() {
        try {
            player.stop()
            _uiState.update { 
                it.copy(
                    isConnected = false,
                    isPlaying = false
                )
            }
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(error = "Erro ao parar reprodução: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun buildRtspUri(device: com.eyedock.app.data.repository.Device, auth: Auth?): String {
        val protocol = "rtsp"
        val host = device.lastKnownIp ?: device.rtspHost ?: ""
        val port = device.rtspPort ?: 554
        val path = device.rtspPath ?: ""
        
        val authPart = if (auth != null) "${auth.username}:${auth.password}@" else ""
        
        return "$protocol://$authPart$host:$port$path"
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}

data class LiveViewUiState(
    val cameraId: String? = null,
    val cameraName: String? = null,
    val rtspUri: String? = null,
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val latency: String? = null,
    val bitrate: String? = null,
    val isRecording: Boolean = false,
    val hasPtz: Boolean = false,
    val isMicrophoneEnabled: Boolean = false,
    val microphoneVolume: Float = 0.5f,
    val isSpeakerEnabled: Boolean = true,
    val speakerVolume: Float = 0.7f
)
