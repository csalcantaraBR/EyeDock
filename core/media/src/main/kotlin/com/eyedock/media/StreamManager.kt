package com.eyedock.media

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * StreamManager para EyeDock
 * 
 * Gerencia conexões com fallback entre múltiplos streams RTSP
 */
@Singleton
class StreamManager @Inject constructor(
    private val rtspClient: RtspClient
) {

    private val activeStreams = mutableSetOf<String>()

    /**
     * Adiciona uma stream à lista de streams ativas
     */
    fun addStream(streamUrl: String): Boolean {
        return try {
            activeStreams.add(streamUrl)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Remove uma stream da lista de streams ativas
     */
    fun removeStream(streamUrl: String): Boolean {
        return try {
            activeStreams.remove(streamUrl)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtém o número de streams ativas
     */
    fun getActiveStreamCount(): Int {
        return activeStreams.size
    }

    /**
     * Conecta com fallback automático entre streams
     */
    suspend fun connectWithFallback(config: CameraConfig): StreamConnectionResult {
        val rtspPaths = listOf("/onvif1", "/onvif2")
        val startTime = System.currentTimeMillis()
        
        for (path in rtspPaths) {
            try {
                val rtspUrl = "rtsp://${config.ip}:${config.rtspPort}$path"
                val result = rtspClient.connect(rtspUrl)
                
                if (result.isSuccess) {
                    val totalTime = System.currentTimeMillis() - startTime
                    return StreamConnectionResult(
                        isConnected = true,
                        successfulPath = path,
                        connectionTimeMs = totalTime,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                // Continua para próximo path
                continue
            }
        }
        
        // Se chegou aqui, todos os paths falharam
        val totalTime = System.currentTimeMillis() - startTime
        return StreamConnectionResult(
            isConnected = false,
            successfulPath = null,
            connectionTimeMs = totalTime,
            errorMessage = "Failed to connect to any stream path"
        )
    }
}

/**
 * Configuração da câmera para conexão
 */
data class CameraConfig(
    val ip: String,
    val rtspPort: Int = 554,
    val user: String,
    val password: String,
    val onvifPort: Int = 5000
)

/**
 * Resultado da conexão com fallback
 */
data class StreamConnectionResult(
    val isConnected: Boolean,
    val successfulPath: String?,
    val connectionTimeMs: Long,
    val errorMessage: String?
)
