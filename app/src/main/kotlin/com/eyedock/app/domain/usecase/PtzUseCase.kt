package com.eyedock.app.domain.usecase

import com.eyedock.app.data.repository.DeviceRepository
import com.eyedock.app.data.security.SecureCredentialStore
import com.eyedock.app.domain.interfaces.OnvifClient
import com.eyedock.app.domain.model.Auth
import com.eyedock.app.domain.model.OnvifEndpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PtzUseCase(
    private val onvifClient: OnvifClient,
    private val deviceRepository: DeviceRepository,
    private val credentialStore: SecureCredentialStore
) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    suspend fun onPanTilt(deviceId: String, deltaX: Float, deltaY: Float) {
        try {
            val device = deviceRepository.getDevice(deviceId) ?: return
            val auth = credentialStore.getCredentials(deviceId)
            
            if (device.onvifDeviceService != null) {
                val endpoint = OnvifEndpoint(
                    ip = device.lastKnownIp ?: device.rtspHost ?: return,
                    deviceService = device.onvifDeviceService
                )
                
                onvifClient.ptzContinuousMove(endpoint, vx = deltaX, vy = deltaY, vz = 0f, auth)
            }
        } catch (e: Exception) {
            // Log error
        }
    }
    
    suspend fun onZoom(deviceId: String, delta: Float) {
        try {
            val device = deviceRepository.getDevice(deviceId) ?: return
            val auth = credentialStore.getCredentials(deviceId)
            
            if (device.onvifDeviceService != null) {
                val endpoint = OnvifEndpoint(
                    ip = device.lastKnownIp ?: device.rtspHost ?: return,
                    deviceService = device.onvifDeviceService
                )
                
                onvifClient.ptzContinuousMove(endpoint, vx = 0f, vy = 0f, vz = delta, auth)
            }
        } catch (e: Exception) {
            // Log error
        }
    }
    
    suspend fun onGestureEnd(deviceId: String) {
        try {
            val device = deviceRepository.getDevice(deviceId) ?: return
            val auth = credentialStore.getCredentials(deviceId)
            
            if (device.onvifDeviceService != null) {
                val endpoint = OnvifEndpoint(
                    ip = device.lastKnownIp ?: device.rtspHost ?: return,
                    deviceService = device.onvifDeviceService
                )
                
                onvifClient.ptzStop(endpoint, auth)
            }
        } catch (e: Exception) {
            // Log error
        }
    }
    
    fun onPanTiltAsync(deviceId: String, deltaX: Float, deltaY: Float) {
        scope.launch {
            onPanTilt(deviceId, deltaX, deltaY)
        }
    }
    
    fun onZoomAsync(deviceId: String, delta: Float) {
        scope.launch {
            onZoom(deviceId, delta)
        }
    }
    
    fun onGestureEndAsync(deviceId: String) {
        scope.launch {
            onGestureEnd(deviceId)
        }
    }
    
    suspend fun hasPtzSupport(deviceId: String): Boolean {
        return try {
            val device = deviceRepository.getDevice(deviceId)
            device?.hasPtz == true && device.onvifDeviceService != null
        } catch (e: Exception) {
            false
        }
    }
}
