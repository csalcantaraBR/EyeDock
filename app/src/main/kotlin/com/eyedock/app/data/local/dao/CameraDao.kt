package com.eyedock.app.data.local.dao

import androidx.room.*
import com.eyedock.app.data.local.entity.CameraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CameraDao {
    
    @Query("SELECT * FROM cameras ORDER BY name ASC")
    fun getAllCameras(): Flow<List<CameraEntity>>
    
    @Query("SELECT * FROM cameras WHERE id = :cameraId")
    suspend fun getCameraById(cameraId: Long): CameraEntity?
    
    @Query("SELECT * FROM cameras WHERE ip = :ip")
    suspend fun getCameraByIp(ip: String): CameraEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCamera(camera: CameraEntity): Long
    
    @Update
    suspend fun updateCamera(camera: CameraEntity)
    
    @Delete
    suspend fun deleteCamera(camera: CameraEntity)
    
    @Query("DELETE FROM cameras WHERE id = :cameraId")
    suspend fun deleteCameraById(cameraId: Long)
    
    @Query("UPDATE cameras SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :cameraId")
    suspend fun updateCameraStatus(cameraId: Long, isOnline: Boolean, lastSeen: java.util.Date?)
    
    @Query("SELECT COUNT(*) FROM cameras")
    suspend fun getCameraCount(): Int
}
