package com.eyedock.app.domain.interfaces

import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.model.StreamProfile

/**
 * Interface para cliente ONVIF
 */
interface OnvifClient {
    
    /**
     * Descobre dispositivos ONVIF na rede
     */
    suspend fun discoverDevices(): List<CameraConnection>
    
    /**
     * Obtém informações do dispositivo
     */
    suspend fun getDeviceInformation(deviceUrl: String, username: String?, password: String?): DeviceInfo?
    
    /**
     * Obtém capacidades do dispositivo
     */
    suspend fun getCapabilities(deviceUrl: String, username: String?, password: String?): DeviceCapabilities?
    
    /**
     * Obtém perfis de mídia
     */
    suspend fun getProfiles(mediaUrl: String, username: String?, password: String?): List<StreamProfile>
    
    /**
     * Obtém URI do stream
     */
    suspend fun getStreamUri(mediaUrl: String, profileToken: String, username: String?, password: String?): String?
    
    /**
     * Testa conectividade ONVIF
     */
    suspend fun testConnection(deviceUrl: String, username: String?, password: String?): Boolean
}

/**
 * Informações do dispositivo ONVIF
 */
data class DeviceInfo(
    val manufacturer: String?,
    val model: String?,
    val firmwareVersion: String?,
    val serialNumber: String?,
    val hardwareId: String?
)

/**
 * Capacidades do dispositivo ONVIF
 */
data class DeviceCapabilities(
    val deviceService: String?,
    val mediaService: String?,
    val ptzService: String?,
    val hasPtz: Boolean = false,
    val hasImaging: Boolean = false,
    val hasAudio: Boolean = false
)
