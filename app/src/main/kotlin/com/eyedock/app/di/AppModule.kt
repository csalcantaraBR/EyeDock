package com.eyedock.app.di

import android.content.Context
import com.eyedock.app.data.local.CameraDatabase
import com.eyedock.app.data.local.dao.CameraDao
import com.eyedock.app.data.repository.CameraRepository
import com.eyedock.app.domain.interfaces.QrParser
import com.eyedock.app.data.qr.QrParserImpl
import com.eyedock.app.domain.interfaces.Player
import com.eyedock.app.data.player.ExoPlayerImpl

/**
 * Manual dependency injection module for the EyeDock application.
 * This replaces Hilt for now and provides all necessary dependencies.
 */
object AppModule {
    
    private var database: CameraDatabase? = null
    private var cameraDao: CameraDao? = null
    private var cameraRepository: CameraRepository? = null
    private var qrParser: QrParser? = null
    private var player: Player? = null
    
    /**
     * Initialize the database and all dependencies
     */
    fun initialize(context: Context) {
        if (database == null) {
            database = CameraDatabase.getDatabase(context)
            cameraDao = database!!.cameraDao()
            cameraRepository = CameraRepository(cameraDao!!)
            qrParser = QrParserImpl()
            player = ExoPlayerImpl(context)
        }
    }
    
    /**
     * Get the camera repository instance
     */
    fun getCameraRepository(): CameraRepository {
        return cameraRepository ?: throw IllegalStateException("AppModule not initialized. Call initialize() first.")
    }
    
    /**
     * Get the QR parser instance
     */
    fun getQrParser(): QrParser {
        return qrParser ?: throw IllegalStateException("AppModule not initialized. Call initialize() first.")
    }
    
    /**
     * Get the camera DAO instance
     */
    fun getCameraDao(): CameraDao {
        return cameraDao ?: throw IllegalStateException("AppModule not initialized. Call initialize() first.")
    }
    
    /**
     * Get the player instance
     */
    fun getPlayer(): Player {
        return player ?: throw IllegalStateException("AppModule not initialized. Call initialize() first.")
    }
    
    /**
     * Clear all dependencies (useful for testing)
     */
    fun clear() {
        database = null
        cameraDao = null
        cameraRepository = null
        qrParser = null
        player?.release()
        player = null
    }
}
