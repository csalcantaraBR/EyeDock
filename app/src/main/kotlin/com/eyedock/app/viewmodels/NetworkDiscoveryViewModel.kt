package com.eyedock.app.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eyedock.app.domain.model.OnvifDevice
import com.eyedock.app.network.OnvifDiscovery
import com.eyedock.app.utils.Constants
import com.eyedock.app.utils.Logger
import com.eyedock.app.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Factory for NetworkDiscoveryViewModel that requires context
 */
class NetworkDiscoveryViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NetworkDiscoveryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NetworkDiscoveryViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * ViewModel for managing network discovery functionality.
 * Handles ONVIF device discovery and network scanning with real validation.
 */
class NetworkDiscoveryViewModel(
    private val context: Context
) : ViewModel() {

    private val onvifDiscovery = OnvifDiscovery(context)
    private val logger = Logger.withTag("NetworkDiscoveryViewModel")

    private val _uiState = MutableStateFlow<NetworkDiscoveryUiState>(NetworkDiscoveryUiState.Idle)
    val uiState: StateFlow<NetworkDiscoveryUiState> = _uiState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<OnvifDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<OnvifDevice>> = _discoveredDevices.asStateFlow()

    /**
     * Start network discovery process
     */
    fun startDiscovery() {
        viewModelScope.launch {
            try {
                Logger.d("Starting network discovery...")
                _uiState.value = NetworkDiscoveryUiState.Discovering
                
                // Check network availability
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    Logger.e("No network available")
                    _uiState.value = NetworkDiscoveryUiState.Error("No network connection available")
                    return@launch
                }
                
                // Get local subnet
                val subnet = NetworkUtils.getLocalSubnet(context)
                Logger.d("Local subnet: $subnet")
                
                // Start discovery with timeout
                withTimeout(45000L) { // 45 seconds timeout for thorough scan
                    // Use network scan instead of WS-Discovery for more reliable results
                    val devices = onvifDiscovery.scanNetworkForDevices()
                    Logger.i("Discovery completed: ${devices.size} devices found")
                    
                    if (devices.isNotEmpty()) {
                        _uiState.value = NetworkDiscoveryUiState.Success(devices)
                    } else {
                        _uiState.value = NetworkDiscoveryUiState.NoDevicesFound
                    }
                }
                
            } catch (e: TimeoutCancellationException) {
                Logger.e("Discovery timeout")
                _uiState.value = NetworkDiscoveryUiState.Error("Discovery timeout - no cameras found")
            } catch (e: Exception) {
                Logger.e("Discovery failed", e)
                _uiState.value = NetworkDiscoveryUiState.Error("Discovery failed: ${e.message}")
            }
        }
    }

    /**
     * Stop the discovery process
     */
    fun stopDiscovery() {
        _uiState.value = NetworkDiscoveryUiState.Idle
    }
    
    /**
     * Test camera IP detection
     */
    fun testCameraIp() {
        viewModelScope.launch {
            _uiState.value = NetworkDiscoveryUiState.Discovering
            
            try {
                logger.d("Testando detecção do IP real da câmera...")
                
                val cameraIp = NetworkUtils.findCameraIp(context)
                
                if (cameraIp != null) {
                    logger.i("IP real da câmera encontrado: $cameraIp")
                    _uiState.value = NetworkDiscoveryUiState.Error("Camera found at: $cameraIp")
                } else {
                    logger.w("Nenhuma câmera encontrada")
                    _uiState.value = NetworkDiscoveryUiState.Error("No camera found in network")
                }
                
            } catch (e: Exception) {
                logger.e("Erro ao testar IP da câmera", e)
                _uiState.value = NetworkDiscoveryUiState.Error("Error testing camera IP: ${e.message}")
            }
        }
    }
}

/**
 * UI states for network discovery
 */
sealed class NetworkDiscoveryUiState {
    object Idle : NetworkDiscoveryUiState()
    object Discovering : NetworkDiscoveryUiState()
    data class Success(val devices: List<OnvifDevice>) : NetworkDiscoveryUiState()
    object NoDevicesFound : NetworkDiscoveryUiState()
    data class Error(val message: String) : NetworkDiscoveryUiState()
}
