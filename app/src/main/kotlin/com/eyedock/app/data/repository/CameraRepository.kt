package com.eyedock.app.data.repository

import com.eyedock.app.data.local.dao.CameraDao
import com.eyedock.app.data.local.entity.CameraEntity
import com.eyedock.app.domain.model.CameraConnection
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepository @Inject constructor(
    private val cameraDao: CameraDao
) {
    
    fun getAllCameras(): Flow<List<CameraEntity>> = cameraDao.getAllCameras()
    
    suspend fun getCameraById(cameraId: Long): CameraEntity? = cameraDao.getCameraById(cameraId)
    
    suspend fun getCameraByIp(ip: String): CameraEntity? = cameraDao.getCameraByIp(ip)
    
    suspend fun saveCamera(cameraConnection: CameraConnection, name: String): Long {
        val cameraEntity = CameraEntity(
            name = name,
            ip = cameraConnection.ip ?: "",
            port = cameraConnection.port ?: 554,
            username = cameraConnection.user,
            password = cameraConnection.pass,
            protocol = cameraConnection.proto ?: "rtsp",
            path = cameraConnection.path ?: "/",
            manufacturer = cameraConnection.meta?.manufacturer,
            model = cameraConnection.meta?.model,
            createdAt = Date(),
            updatedAt = Date()
        )
        
        return cameraDao.insertCamera(cameraEntity)
    }
    
    suspend fun updateCamera(camera: CameraEntity) {
        cameraDao.updateCamera(camera.copy(updatedAt = Date()))
    }
    
    suspend fun deleteCamera(cameraId: Long) {
        cameraDao.deleteCameraById(cameraId)
    }
    
    suspend fun updateCameraStatus(cameraId: Long, isOnline: Boolean) {
        val lastSeen = if (isOnline) Date() else null
        cameraDao.updateCameraStatus(cameraId, isOnline, lastSeen)
    }
    
    suspend fun getCameraCount(): Int = cameraDao.getCameraCount()
}
