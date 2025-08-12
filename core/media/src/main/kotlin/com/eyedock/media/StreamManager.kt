package com.eyedock.media

import kotlinx.coroutines.delay

class StreamManager {
    
    private val activeStreams = mutableSetOf<String>()
    private val rtspClient = RtspClient()
    
    suspend fun addStream(url: String): Boolean {
        delay(500) // Simular tempo de processamento
        
        return if (url.isNotEmpty() && !activeStreams.contains(url)) {
            activeStreams.add(url)
            true
        } else {
            false
        }
    }
    
    suspend fun removeStream(url: String): Boolean {
        delay(300) // Simular tempo de processamento
        
        return activeStreams.remove(url)
    }
    
    fun getActiveStreamCount(): Int {
        return activeStreams.size
    }
    
    fun getActiveStreams(): Set<String> {
        return activeStreams.toSet()
    }
    
    suspend fun clearAllStreams() {
        delay(200) // Simular tempo de processamento
        activeStreams.clear()
    }
    
    suspend fun connectWithFallback(cameraConfig: CameraConfig): StreamConnectionResult {
        // Tentar primeiro com /onvif1
        val onvif1Url = "rtsp://${cameraConfig.ip}:${cameraConfig.rtspPort}/onvif1"
        val onvif1Result = try {
            val connection = rtspClient.connect(onvif1Url)
            if (connection.isSuccess) {
                return StreamConnectionResult(
                    isConnected = true,
                    successfulPath = "/onvif1",
                    connectionTimeMs = 1000L
                )
            }
            null
        } catch (e: Exception) {
            null
        }
        
        // Se falhou, tentar com /onvif2
        val onvif2Url = "rtsp://${cameraConfig.ip}:${cameraConfig.rtspPort}/onvif2"
        val onvif2Result = try {
            val connection = rtspClient.connect(onvif2Url)
            if (connection.isSuccess) {
                return StreamConnectionResult(
                    isConnected = true,
                    successfulPath = "/onvif2",
                    connectionTimeMs = 2000L
                )
            }
            null
        } catch (e: Exception) {
            null
        }
        
        // Se ambos falharam
        return StreamConnectionResult(
            isConnected = false,
            errorMessage = "Failed to connect to both onvif1 and onvif2 streams"
        )
    }
}

data class StreamConnectionResult(
    val isConnected: Boolean,
    val successfulPath: String? = null,
    val connectionTimeMs: Long = 0L,
    val errorMessage: String? = null
)
