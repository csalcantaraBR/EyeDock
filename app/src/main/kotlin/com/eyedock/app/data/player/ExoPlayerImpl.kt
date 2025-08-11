package com.eyedock.app.data.player

import android.content.Context
import android.view.View
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.PlayerView
import com.eyedock.app.domain.interfaces.Player as PlayerInterface
import com.eyedock.app.domain.interfaces.PlayerState
import com.eyedock.app.domain.interfaces.PlayerEventListener
import com.eyedock.app.domain.interfaces.StreamStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoPlayerImpl @Inject constructor(
    private val context: Context
) : PlayerInterface {
    
    private var exoPlayer: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var eventListener: PlayerEventListener? = null
    private val streamStatsFlow = MutableStateFlow(StreamStats())
    private var currentStreamUrl: String? = null
    private var isInitialized = false
    
    override fun initialize() {
        try {
            if (isInitialized) return
            
            exoPlayer = ExoPlayer.Builder(context)
                .setMediaSourceFactory(DefaultMediaSourceFactory(context))
                .build()
                .apply {
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            val state = when (playbackState) {
                                Player.STATE_IDLE -> PlayerState.IDLE
                                Player.STATE_BUFFERING -> PlayerState.BUFFERING
                                Player.STATE_READY -> PlayerState.READY
                                Player.STATE_ENDED -> PlayerState.ENDED
                                else -> PlayerState.ERROR
                            }
                            eventListener?.onPlayerStateChanged(state)
                        }
                        
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            val errorMessage = when {
                                error.message?.contains("404") == true -> "Source error: 404 Not Found"
                                error.message?.contains("connection") == true -> "Source error: Connection failed"
                                error.message?.contains("timeout") == true -> "Source error: Connection timeout"
                                error.message?.contains("failed") == true -> "Source error: ${error.message}"
                                else -> "Player error: ${error.message ?: "Unknown error"}"
                            }
                            eventListener?.onError(errorMessage)
                        }
                        
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (isPlaying) {
                                eventListener?.onFirstFrame()
                            }
                        }
                        
                        override fun onLoadingChanged(isLoading: Boolean) {
                            if (isLoading) {
                                eventListener?.onBuffering(0)
                            }
                        }
                    })
                }
            
            playerView = PlayerView(context).apply {
                player = exoPlayer
                useController = false // We'll use our own controls
            }
            
            isInitialized = true
        } catch (e: Exception) {
            eventListener?.onError("Failed to initialize player: ${e.message}")
        }
    }
    
    override fun setStreamUrl(url: String, username: String?, password: String?) {
        try {
            if (!isInitialized) {
                initialize()
            }
            
            currentStreamUrl = url
            
            // Stop current playback if any
            exoPlayer?.stop()
            
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            
        } catch (e: Exception) {
            eventListener?.onError("Failed to set stream URL: ${e.message}")
        }
    }
    
    override fun play() {
        try {
            if (!isInitialized) {
                eventListener?.onError("Player not initialized")
                return
            }
            exoPlayer?.play()
        } catch (e: Exception) {
            eventListener?.onError("Failed to play: ${e.message}")
        }
    }
    
    override fun pause() {
        try {
            exoPlayer?.pause()
        } catch (e: Exception) {
            eventListener?.onError("Failed to pause: ${e.message}")
        }
    }
    
    override fun stop() {
        try {
            exoPlayer?.stop()
        } catch (e: Exception) {
            eventListener?.onError("Failed to stop: ${e.message}")
        }
    }
    
    override fun release() {
        try {
            exoPlayer?.release()
            exoPlayer = null
            playerView = null
            currentStreamUrl = null
            isInitialized = false
        } catch (e: Exception) {
            // Ignore errors during release
        }
    }
    
    override fun getPlayerView(): View {
        if (!isInitialized) {
            initialize()
        }
        return playerView ?: throw IllegalStateException("Player not initialized")
    }
    
    override fun setEventListener(listener: PlayerEventListener) {
        this.eventListener = listener
    }
    
    override fun getPlayerState(): PlayerState {
        return try {
            when (exoPlayer?.playbackState) {
                Player.STATE_IDLE -> PlayerState.IDLE
                Player.STATE_BUFFERING -> PlayerState.BUFFERING
                Player.STATE_READY -> if (exoPlayer?.isPlaying == true) PlayerState.PLAYING else PlayerState.PAUSED
                Player.STATE_ENDED -> PlayerState.ENDED
                else -> PlayerState.ERROR
            }
        } catch (e: Exception) {
            PlayerState.ERROR
        }
    }
    
    override fun getStreamStats(): Flow<StreamStats> {
        // TODO: Implement real stream statistics collection
        return streamStatsFlow
    }
    
    override fun setBufferSize(bufferMs: Long) {
        // Configure buffer size for low latency
        exoPlayer?.let { player ->
            // Set buffer parameters for RTSP streaming
            // Note: ExoPlayer doesn't expose direct buffer configuration
            // This would need to be implemented through custom MediaSource
        }
    }
    
    override fun setAudioEnabled(enabled: Boolean) {
        try {
            exoPlayer?.volume = if (enabled) 1.0f else 0.0f
        } catch (e: Exception) {
            eventListener?.onError("Failed to set audio: ${e.message}")
        }
    }
    
    override fun setVolume(volume: Float) {
        try {
            exoPlayer?.volume = volume.coerceIn(0.0f, 1.0f)
        } catch (e: Exception) {
            eventListener?.onError("Failed to set volume: ${e.message}")
        }
    }
}
