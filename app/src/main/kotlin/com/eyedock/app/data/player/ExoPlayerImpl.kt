package com.eyedock.app.data.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.Surface
import com.eyedock.app.domain.interfaces.Player as DomainPlayer
import com.eyedock.app.domain.model.Auth
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

@UnstableApi
class ExoPlayerImpl(
    private val context: Context
) : DomainPlayer {
    
    private var exoPlayer: ExoPlayer? = null
    private var currentUri: String? = null
    private var currentAuth: Auth? = null
    
    override fun play(uri: String, auth: Auth?) {
        currentUri = uri
        currentAuth = auth
        
        try {
            // Criar ou reutilizar ExoPlayer
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(createMediaSourceFactory(auth))
                    .build()
            }
            
            // Configurar media source
            val mediaSource = createMediaSource(uri, auth)
            
            // Preparar e reproduzir
            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true
            }
            
        } catch (e: Exception) {
            // Log error
        }
    }
    
    override fun stop() {
        exoPlayer?.apply {
            stop()
            clearMediaItems()
        }
    }
    
    override fun snapshot(): ByteArray? {
        return try {
            // Implementar snapshot usando ExoPlayer
            // Esta é uma implementação simplificada
            // Em produção, seria necessário capturar o frame atual do vídeo
            null
        } catch (e: Exception) {
            null
        }
    }
    
    fun getPlayer(): ExoPlayer? = exoPlayer
    
    fun setSurface(surface: Surface) {
        exoPlayer?.setVideoSurface(surface)
    }
    
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
    
    private fun createMediaSourceFactory(auth: Auth?): MediaSource.Factory {
        val dataSourceFactory = createDataSourceFactory(auth)
        
        return ProgressiveMediaSource.Factory(dataSourceFactory)
    }
    
    private fun createMediaSource(uri: String, auth: Auth?): MediaSource {
        val dataSourceFactory = createDataSourceFactory(auth)
        
        return if (uri.startsWith("rtsp://")) {
            // RTSP Media Source - usar ProgressiveMediaSource como fallback
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
        } else {
            // Progressive Media Source para outros protocolos
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
        }
    }
    
    private fun createDataSourceFactory(auth: Auth?): HttpDataSource.Factory {
        return DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(10000) // 10 segundos timeout
            .setReadTimeoutMs(10000)
            .setAllowCrossProtocolRedirects(true)
            .apply {
                if (auth != null) {
                    setDefaultRequestProperties(
                        mapOf(
                            "Authorization" to "Basic ${
                                android.util.Base64.encodeToString(
                                    "${auth.username}:${auth.password}".toByteArray(),
                                    android.util.Base64.NO_WRAP
                                )
                            }"
                        )
                    )
                }
            }
    }
    
    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }
    
    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }
    
    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
    }
    
    fun addListener(listener: Player.Listener) {
        exoPlayer?.addListener(listener)
    }
    
    fun removeListener(listener: Player.Listener) {
        exoPlayer?.removeListener(listener)
    }
}
