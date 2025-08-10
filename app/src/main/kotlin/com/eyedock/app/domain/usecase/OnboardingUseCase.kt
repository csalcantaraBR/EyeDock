package com.eyedock.app.domain.usecase

import com.eyedock.app.data.repository.DeviceRepository
import com.eyedock.app.data.security.SecureCredentialStore
import com.eyedock.app.domain.interfaces.Normalizer
import com.eyedock.app.domain.interfaces.QrParser
import com.eyedock.app.domain.interfaces.RtspProber
import com.eyedock.app.domain.model.Auth
import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.model.QrPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class OnboardingState {
    object Idle : OnboardingState()
    object Scanning : OnboardingState()
    object Parsing : OnboardingState()
    object Normalizing : OnboardingState()
    object Discovering : OnboardingState()
    object Validating : OnboardingState()
    object Saving : OnboardingState()
    data class Error(val message: String) : OnboardingState()
    data class Success(val deviceId: String) : OnboardingState()
}

data class OnboardingResult(
    val deviceId: String,
    val rtspUri: String,
    val hasPtz: Boolean,
    val deviceName: String
)

class OnboardingUseCase(
    private val qrParser: QrParser,
    private val normalizer: Normalizer,
    private val rtspProber: RtspProber,
    private val deviceRepository: DeviceRepository,
    private val credentialStore: SecureCredentialStore
) {
    
    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val state: Flow<OnboardingState> = _state.asStateFlow()
    
    suspend fun processQrCode(qrText: String, deviceName: String, auth: Auth? = null): OnboardingResult? {
        return try {
            // 1. Scan QR
            _state.value = OnboardingState.Scanning
            val payload = qrParser.parse(qrText)
            
            // 2. Parse/Detect
            _state.value = OnboardingState.Parsing
            
            // 3. Normalize
            _state.value = OnboardingState.Normalizing
            val connection = normalizer.normalize(payload)
            
            // 4. Discover (se necessário)
            if (connection.onvifDeviceService != null) {
                _state.value = OnboardingState.Discovering
                // Implementar descoberta ONVIF se necessário
            }
            
            // 5. Validate
            _state.value = OnboardingState.Validating
            val rtspUri = connection.toRtspUri()
            validateRtspConnection(rtspUri, auth)
            
            // 6. Persist
            _state.value = OnboardingState.Saving
            val device = deviceRepository.addDevice(connection, deviceName)
            
            // Salvar credenciais se fornecidas
            if (auth != null) {
                credentialStore.saveCredentials(device.id, auth)
            }
            
            // 7. Success
            _state.value = OnboardingState.Success(device.id)
            
            OnboardingResult(
                deviceId = device.id,
                rtspUri = rtspUri,
                hasPtz = connection.ptz ?: false,
                deviceName = deviceName
            )
            
        } catch (e: Exception) {
            _state.value = OnboardingState.Error("Erro no onboarding: ${e.message}")
            null
        }
    }
    
    private suspend fun validateRtspConnection(rtspUri: String, auth: Auth?) {
        try {
            // Testar OPTIONS
            val options = rtspProber.options(rtspUri, auth)
            if (options.methods.isEmpty() && options.public.isEmpty()) {
                throw Exception("RTSP OPTIONS falhou - dispositivo não responde")
            }
            
            // Testar DESCRIBE
            val sdp = rtspProber.describe(rtspUri, auth)
            if (sdp.media.isEmpty()) {
                throw Exception("RTSP DESCRIBE falhou - nenhum stream disponível")
            }
            
        } catch (e: Exception) {
            throw Exception("Validação RTSP falhou: ${e.message}")
        }
    }
    
    fun resetState() {
        _state.value = OnboardingState.Idle
    }
}
