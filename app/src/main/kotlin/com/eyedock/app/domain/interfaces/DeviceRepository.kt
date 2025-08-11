package com.eyedock.app.domain.interfaces

import com.eyedock.app.domain.model.Device
import com.eyedock.app.domain.model.CameraConnection
import kotlinx.coroutines.flow.Flow

/**
 * Interface para repositório de dispositivos
 */
interface DeviceRepository {
    
    /**
     * Obtém todos os dispositivos
     */
    fun getAllDevices(): Flow<List<Device>>
    
    /**
     * Obtém um dispositivo por ID
     */
    suspend fun getDeviceById(id: String): Device?
    
    /**
     * Salva um dispositivo
     */
    suspend fun saveDevice(device: Device): String
    
    /**
     * Atualiza um dispositivo
     */
    suspend fun updateDevice(device: Device)
    
    /**
     * Remove um dispositivo
     */
    suspend fun deleteDevice(id: String)
    
    /**
     * Busca dispositivos por nome
     */
    suspend fun searchDevices(query: String): List<Device>
    
    /**
     * Obtém dispositivos online
     */
    fun getOnlineDevices(): Flow<List<Device>>
    
    /**
     * Atualiza status online/offline
     */
    suspend fun updateDeviceStatus(id: String, isOnline: Boolean)
    
    /**
     * Converte CameraConnection em Device
     */
    suspend fun createDeviceFromConnection(connection: CameraConnection, name: String): Device
    
    /**
     * Verifica se dispositivo existe
     */
    suspend fun deviceExists(uid: String?): Boolean
}
