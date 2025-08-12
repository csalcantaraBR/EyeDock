package com.eyedock.media

import kotlinx.coroutines.delay
import java.util.regex.Pattern

class RtspClient {
    
    fun isValidRtspUrl(url: String): Boolean {
        val rtspPattern = Pattern.compile("^rtsp://[^\\s]+$")
        return rtspPattern.matcher(url).matches()
    }
    
    suspend fun connect(url: String): ConnectionResult {
        delay(1000) // Simular tempo de conexão
        
        return if (isValidRtspUrl(url)) {
            ConnectionResult.Success(
                connectionId = "conn_${System.currentTimeMillis()}",
                url = url,
                isConnected = true
            )
        } else {
            ConnectionResult.Failure("URL RTSP inválida: $url")
        }
    }
    
    suspend fun disconnect(connectionId: String): Boolean {
        delay(500) // Simular tempo de desconexão
        return true
    }
    
    suspend fun getStreamInfo(url: String): StreamInfo? {
        delay(800) // Simular tempo de análise
        
        return if (isValidRtspUrl(url)) {
            StreamInfo(
                url = url,
                codec = "H.264",
                resolution = "1920x1080",
                frameRate = 25,
                bitrate = 2048,
                isValid = true
            )
        } else null
    }
    
    suspend fun monitorStability(durationMs: Long): StabilityResult {
        delay(durationMs)
        return StabilityResult(
            wasStable = true,
            uptimePercentage = 99.5,
            disconnectionCount = 0
        )
    }
}

data class StabilityResult(
    val wasStable: Boolean,
    val uptimePercentage: Double,
    val disconnectionCount: Int
)

sealed class ConnectionResult {
    data class Success(
        val connectionId: String,
        val url: String,
        val isConnected: Boolean
    ) : ConnectionResult()
    
    data class Failure(val error: String) : ConnectionResult()
    
    val isSuccess: Boolean
        get() = this is Success
}

data class StreamInfo(
    val url: String,
    val codec: String,
    val resolution: String,
    val frameRate: Int,
    val bitrate: Int,
    val isValid: Boolean
)
