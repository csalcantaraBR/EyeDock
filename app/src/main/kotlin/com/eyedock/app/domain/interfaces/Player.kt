package com.eyedock.app.domain.interfaces

import android.view.View
import kotlinx.coroutines.flow.Flow

/**
 * Interface para player de vídeo
 */
interface Player {
    
    /**
     * Inicializa o player
     */
    fun initialize()
    
    /**
     * Configura a URL do stream
     */
    fun setStreamUrl(url: String, username: String? = null, password: String? = null)
    
    /**
     * Inicia a reprodução
     */
    fun play()
    
    /**
     * Pausa a reprodução
     */
    fun pause()
    
    /**
     * Para a reprodução
     */
    fun stop()
    
    /**
     * Libera recursos
     */
    fun release()
    
    /**
     * Obtém a view do player
     */
    fun getPlayerView(): View
    
    /**
     * Define listener de eventos
     */
    fun setEventListener(listener: PlayerEventListener)
    
    /**
     * Obtém o estado atual
     */
    fun getPlayerState(): PlayerState
    
    /**
     * Obtém estatísticas do stream
     */
    fun getStreamStats(): Flow<StreamStats>
    
    /**
     * Configura buffer
     */
    fun setBufferSize(bufferMs: Long)
    
    /**
     * Habilita/desabilita áudio
     */
    fun setAudioEnabled(enabled: Boolean)
    
    /**
     * Define volume
     */
    fun setVolume(volume: Float)
}

/**
 * Estados do player
 */
enum class PlayerState {
    IDLE,
    PREPARING,
    READY,
    PLAYING,
    PAUSED,
    BUFFERING,
    ERROR,
    ENDED
}

/**
 * Eventos do player
 */
interface PlayerEventListener {
    fun onPlayerStateChanged(state: PlayerState)
    fun onError(error: String)
    fun onBuffering(percent: Int)
    fun onFirstFrame()
}

/**
 * Estatísticas do stream
 */
data class StreamStats(
    val bitrate: Long = 0,
    val frameRate: Float = 0f,
    val packetLoss: Float = 0f,
    val latency: Long = 0,
    val jitter: Long = 0,
    val resolution: String? = null,
    val codec: String? = null
)
