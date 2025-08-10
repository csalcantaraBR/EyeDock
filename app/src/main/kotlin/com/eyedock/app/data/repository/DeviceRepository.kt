package com.eyedock.app.data.repository

import com.eyedock.app.domain.model.CameraConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

data class Device(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val brand: String? = null,
    val model: String? = null,
    val uid: String? = null,
    val lastKnownIp: String? = null,
    val rtspHost: String? = null,
    val rtspPort: Int? = null,
    val rtspPath: String? = null,
    val hasPtz: Boolean = false,
    val onvifDeviceService: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

class DeviceRepository {
    
    private val devices = MutableStateFlow<List<Device>>(emptyList())
    
    fun getAllDevices(): Flow<List<Device>> = devices.asStateFlow()
    
    suspend fun addDevice(connection: CameraConnection, name: String): Device {
        val device = Device(
            name = name,
            brand = connection.meta["brand"],
            model = connection.meta["model"],
            uid = connection.uid,
            lastKnownIp = connection.ip,
            rtspHost = connection.ip,
            rtspPort = connection.port,
            rtspPath = connection.path,
            hasPtz = connection.ptz ?: false,
            onvifDeviceService = connection.onvifDeviceService
        )
        
        val currentDevices = devices.value.toMutableList()
        currentDevices.add(device)
        devices.value = currentDevices
        
        return device
    }
    
    suspend fun updateDevice(device: Device) {
        val currentDevices = devices.value.toMutableList()
        val index = currentDevices.indexOfFirst { it.id == device.id }
        
        if (index >= 0) {
            currentDevices[index] = device.copy(updatedAt = System.currentTimeMillis())
            devices.value = currentDevices
        }
    }
    
    suspend fun deleteDevice(deviceId: String) {
        val currentDevices = devices.value.toMutableList()
        currentDevices.removeAll { it.id == deviceId }
        devices.value = currentDevices
    }
    
    suspend fun getDevice(deviceId: String): Device? {
        return devices.value.find { it.id == deviceId }
    }
    
    suspend fun getDeviceByUid(uid: String): Device? {
        return devices.value.find { it.uid == uid }
    }
    
    suspend fun updateDeviceIp(deviceId: String, newIp: String) {
        val device = getDevice(deviceId) ?: return
        updateDevice(device.copy(
            lastKnownIp = newIp,
            rtspHost = newIp
        ))
    }
    
    suspend fun getDevicesByIp(ip: String): List<Device> {
        return devices.value.filter { it.lastKnownIp == ip || it.rtspHost == ip }
    }
}
