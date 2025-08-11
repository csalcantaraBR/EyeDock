package com.eyedock.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyedock.app.data.repository.CameraRepository
import com.eyedock.app.di.AppModule
import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.model.CameraMeta
import com.eyedock.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.Socket
import java.util.regex.Pattern
import com.eyedock.app.utils.Logger

/**
 * ViewModel for managing camera addition functionality.
 * Handles form data, connection testing, and camera saving.
 */
class AddCameraViewModel : ViewModel() {

    private val cameraRepository: CameraRepository = AppModule.getCameraRepository()
    private val IP_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    )

    private val _uiState = MutableStateFlow<AddCameraUiState>(AddCameraUiState.Idle)
    val uiState: StateFlow<AddCameraUiState> = _uiState.asStateFlow()

    private val _formData = MutableStateFlow(CameraFormData())
    val formData: StateFlow<CameraFormData> = _formData.asStateFlow()

    /**
     * Update a specific field in the form data
     */
    fun updateField(field: String, value: String) {
        _formData.value = _formData.value.copy(
            name = if (field == "name") value else _formData.value.name,
            ip = if (field == "ip") value else _formData.value.ip,
            port = if (field == "port") value else _formData.value.port,
            user = if (field == "user") value else _formData.value.user,
            password = if (field == "password") value else _formData.value.password
        )
    }

    /**
     * Pre-fill the form with camera data (e.g., from network discovery)
     */
    fun prefillForm(ip: String, port: Int = Constants.DEFAULT_RTSP_PORT, name: String? = null) {
        Logger.d("Prefilling form with IP: $ip, port: $port, name: $name")
        _formData.value = _formData.value.copy(
            ip = ip,
            port = port.toString(),
            name = name ?: "Camera at $ip"
        )
        Logger.d("Form data updated: ${_formData.value}")
    }

    /**
     * Test connection to the camera
     */
    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = AddCameraUiState.TestingConnection
            
            try {
                val form = _formData.value
                
                // Validate IP address
                if (!isValidIpAddress(form.ip)) {
                    _uiState.value = AddCameraUiState.ConnectionError(Constants.ErrorMessages.INVALID_IP_ADDRESS)
                    return@launch
                }
                
                // Validate port
                val port = form.port.toIntOrNull() ?: Constants.DEFAULT_RTSP_PORT
                if (!isValidPort(port)) {
                    _uiState.value = AddCameraUiState.ConnectionError(Constants.ErrorMessages.INVALID_PORT)
                    return@launch
                }
                
                // Test network connectivity
                val isReachable = InetAddress.getByName(form.ip).isReachable(Constants.CONNECTION_TIMEOUT_MS)
                if (!isReachable) {
                    _uiState.value = AddCameraUiState.ConnectionError(Constants.ErrorMessages.CAMERA_NOT_REACHABLE)
                    return@launch
                }
                
                // Test port connectivity
                val socket = Socket()
                socket.connect(java.net.InetSocketAddress(form.ip, port), Constants.CONNECTION_TIMEOUT_MS)
                socket.close()
                
                _uiState.value = AddCameraUiState.ConnectionSuccess(Constants.SuccessMessages.CONNECTION_SUCCESS)
                
            } catch (e: Exception) {
                _uiState.value = AddCameraUiState.ConnectionError("${Constants.ErrorMessages.CONNECTION_FAILED}: ${e.message}")
            }
        }
    }

    /**
     * Save the camera to the database
     */
    fun saveCamera() {
        viewModelScope.launch {
            _uiState.value = AddCameraUiState.Saving
            
            try {
                val form = _formData.value
                
                // Validate required fields
                if (form.name.isBlank() || form.ip.isBlank()) {
                    _uiState.value = AddCameraUiState.SaveError("${Constants.ErrorMessages.NAME_REQUIRED} and ${Constants.ErrorMessages.IP_REQUIRED}")
                    return@launch
                }
                
                if (!isValidIpAddress(form.ip)) {
                    _uiState.value = AddCameraUiState.SaveError(Constants.ErrorMessages.INVALID_IP_ADDRESS)
                    return@launch
                }
                
                val port = form.port.toIntOrNull() ?: Constants.DEFAULT_RTSP_PORT
                if (!isValidPort(port)) {
                    _uiState.value = AddCameraUiState.SaveError(Constants.ErrorMessages.INVALID_PORT)
                    return@launch
                }
                
                // Create camera connection
                val cameraConnection = CameraConnection(
                    proto = "rtsp",
                    ip = form.ip,
                    port = port,
                    user = form.user.takeIf { it.isNotBlank() },
                    pass = form.password.takeIf { it.isNotBlank() },
                    path = Constants.DEFAULT_RTSP_PATH,
                    meta = CameraMeta(source = "manual")
                )
                
                // Save to database
                val cameraId = cameraRepository.saveCamera(cameraConnection, form.name)
                
                _uiState.value = AddCameraUiState.SaveSuccess(cameraConnection)
                
            } catch (e: Exception) {
                _uiState.value = AddCameraUiState.SaveError("${Constants.ErrorMessages.SAVE_FAILED}: ${e.message}")
            }
        }
    }

    /**
     * Reset the UI state to idle
     */
    fun resetState() {
        _uiState.value = AddCameraUiState.Idle
    }

    /**
     * Validate IP address format
     */
    private fun isValidIpAddress(ip: String): Boolean {
        return IP_PATTERN.matcher(ip).matches()
    }

    /**
     * Validate port number
     */
    private fun isValidPort(port: Int): Boolean {
        return port in 1..65535
    }
}

/**
 * Form data for camera addition
 */
data class CameraFormData(
    val name: String = "",
    val ip: String = "",
    val port: String = Constants.DEFAULT_RTSP_PORT.toString(),
    val user: String = "",
    val password: String = ""
)

/**
 * UI states for the add camera screen
 */
sealed class AddCameraUiState {
    object Idle : AddCameraUiState()
    object TestingConnection : AddCameraUiState()
    object Saving : AddCameraUiState()
    data class ConnectionSuccess(val message: String) : AddCameraUiState()
    data class ConnectionError(val message: String) : AddCameraUiState()
    data class SaveSuccess(val cameraConnection: CameraConnection) : AddCameraUiState()
    data class SaveError(val message: String) : AddCameraUiState()
}
