package com.eyedock.app.data.player

import android.content.Context
import android.view.View
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
    
    override fun initialize() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
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
                    eventListener?.onError(error.message ?: "Unknown error")
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        eventListener?.onFirstFrame()
                    }
                }
            })
        }
        
        playerView = PlayerView(context).apply {
            player = exoPlayer
        }
    }
    
    override fun setStreamUrl(url: String, username: String?, password: String?) {
        val mediaItem = if (username != null && password != null) {
            // TODO: Implementar autenticação RTSP
            MediaItem.fromUri(url)
        } else {
            MediaItem.fromUri(url)
        }
        
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
    }
    
    override fun play() {
        exoPlayer?.play()
    }
    
    override fun pause() {
        exoPlayer?.pause()
    }
    
    override fun stop() {
        exoPlayer?.stop()
    }
    
    override fun release() {
        exoPlayer?.release()
        exoPlayer = null
        playerView = null
    }
    
    override fun getPlayerView(): View {
        return playerView ?: throw IllegalStateException("Player not initialized")
    }
    
    override fun setEventListener(listener: PlayerEventListener) {
        this.eventListener = listener
    }
    
    override fun getPlayerState(): PlayerState {
        return when (exoPlayer?.playbackState) {
            Player.STATE_IDLE -> PlayerState.IDLE
            Player.STATE_BUFFERING -> PlayerState.BUFFERING
            Player.STATE_READY -> if (exoPlayer?.isPlaying == true) PlayerState.PLAYING else PlayerState.PAUSED
            Player.STATE_ENDED -> PlayerState.ENDED
            else -> PlayerState.ERROR
        }
    }
    
    override fun getStreamStats(): Flow<StreamStats> {
        // TODO: Implementar coleta de estatísticas do stream
        return streamStatsFlow
    }
    
    override fun setBufferSize(bufferMs: Long) {
        // TODO: Implementar configuração de buffer
    }
    
    override fun setAudioEnabled(enabled: Boolean) {
        exoPlayer?.volume = if (enabled) 1.0f else 0.0f
    }
    
    override fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0.0f, 1.0f)
    }
}
