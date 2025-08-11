package com.eyedock.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyedock.app.data.repository.CameraRepository
import com.eyedock.app.di.AppModule
import com.eyedock.app.domain.interfaces.Player
import com.eyedock.app.domain.interfaces.PlayerEventListener
import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.data.local.entity.CameraEntity
import com.eyedock.app.domain.interfaces.PlayerState
import com.eyedock.app.utils.Constants
import com.eyedock.app.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for managing live view functionality.
 * Handles camera streaming, player controls, and recording.
 */
class LiveViewViewModel : ViewModel() {

    private val cameraRepository: CameraRepository = AppModule.getCameraRepository()
    private val player: Player = AppModule.getPlayer()
    
    private val _uiState = MutableStateFlow<LiveViewUiState>(LiveViewUiState.Idle)
    val uiState: StateFlow<LiveViewUiState> = _uiState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _connectedState = MutableStateFlow<ConnectedState>(ConnectedState())
    val connectedState: StateFlow<ConnectedState> = _connectedState.asStateFlow()

    // Alternative paths for RTSP streams
    private val rtspPaths = listOf(
        "/onvif1",
        "/live/ch00_0",
        "/live/ch0", 
        "/live",
        "/cam/realmonitor",
        "/video1",
        "/video",
        "/stream1",
        "/stream"
    )
    private var currentPathIndex = 0
    private var currentConnection: CameraConnection? = null

    init {
        setupPlayer()
    }

    private fun setupPlayer() {
        player.initialize()
        player.setEventListener(object : PlayerEventListener {
            override fun onPlayerStateChanged(state: PlayerState) {
                viewModelScope.launch {
                    // Logger.d("LiveViewViewModel", "Player state changed to: $state")
                    when (state) {
                        PlayerState.PLAYING -> {
                            _isPlaying.value = true
                            updateConnectedState { it.copy(isPlaying = true) }
                        }
                        PlayerState.PAUSED -> {
                            _isPlaying.value = false
                            updateConnectedState { it.copy(isPlaying = false) }
                        }
                        PlayerState.BUFFERING -> {
                            updateConnectedState { it.copy(connectionStatus = "Buffering...") }
                        }
                        PlayerState.READY -> {
                            updateConnectedState { it.copy(connectionStatus = "Ready") }
                        }
                        PlayerState.ERROR -> {
                            // Logger.e("LiveViewViewModel", "Player entered ERROR state")
                            _uiState.value = LiveViewUiState.Error("Player error occurred")
                        }
                        else -> {
                            // Logger.d("LiveViewViewModel", "Player in state: $state")
                        }
                    }
                }
            }

            override fun onError(error: String) {
                viewModelScope.launch {
                    // Logger.e("LiveViewViewModel", "Player error: $error")
                    
                    // Se for erro de source, tentar path alternativo
                    if (error.contains("Source error") || 
                        error.contains("source") ||
                        error.contains("404") ||
                        error.contains("not found") ||
                        error.contains("connection") ||
                        error.contains("timeout") ||
                        error.contains("failed")) {
                        // Logger.d("LiveViewViewModel", "Source error detected, trying alternative path...")
                        // Logger.d("LiveViewViewModel", "Current path index: $currentPathIndex")
                        // Logger.d("LiveViewViewModel", "Total paths available: ${rtspPaths.size}")
                        tryAlternativePath()
                    } else {
                        // Logger.d("LiveViewViewModel", "Non-source error, not trying alternative paths")
                        _uiState.value = LiveViewUiState.Error("Stream error: $error")
                    }
                }
            }

            override fun onBuffering(percent: Int) {
                viewModelScope.launch {
                    // Logger.d("LiveViewViewModel", "Buffering: $percent%")
                    updateConnectedState { it.copy(connectionStatus = "Buffering: $percent%") }
                }
            }

            override fun onFirstFrame() {
                viewModelScope.launch {
                    // Logger.i("LiveViewViewModel", "First frame received!")
                    updateConnectedState { it.copy(
                        connectionStatus = "Live",
                        lastFrameTime = System.currentTimeMillis()
                    ) }
                }
            }
        })
    }

    /**
     * Connect to camera and start streaming
     */
    fun connectToCamera(cameraId: String) {
        viewModelScope.launch {
            // Logger.d("LiveViewViewModel", "Connecting to camera with ID: $cameraId")
            _uiState.value = LiveViewUiState.Connecting
            
            try {
                // Get camera from repository
                // Logger.d("LiveViewViewModel", "Fetching camera from repository...")
                val cameraEntity = cameraRepository.getCameraById(cameraId.toLong())
                if (cameraEntity == null) {
                    // Logger.e("LiveViewViewModel", "Camera not found with ID: $cameraId")
                    _uiState.value = LiveViewUiState.Error(Constants.ErrorMessages.CAMERA_NOT_FOUND)
                    return@launch
                }
                
                // Logger.d("LiveViewViewModel", "Camera found: ${cameraEntity.name} at ${cameraEntity.ip}:${cameraEntity.port}")
                
                // Convert to CameraConnection
                val cameraConnection = CameraConnection(
                    proto = cameraEntity.protocol,
                    ip = cameraEntity.ip,
                    port = cameraEntity.port,
                    user = cameraEntity.username,
                    pass = cameraEntity.password,
                    path = cameraEntity.path,
                    meta = null // CameraEntity doesn't have meta property
                )
                
                currentConnection = cameraConnection
                currentPathIndex = 0
                
                // Build RTSP URL
                val streamUrl = buildRtspUrl(cameraConnection)
                // Logger.d("LiveViewViewModel", "Built RTSP URL: $streamUrl")
                
                // Start playback
                if (player.getPlayerState() != PlayerState.IDLE) {
                    // Logger.d("LiveViewViewModel", "Player already initialized, stopping current playback...")
                    player.stop()
                }
                
                // Logger.d("LiveViewViewModel", "Setting up player with stream URL...")
                player.setStreamUrl(streamUrl, cameraConnection.user, cameraConnection.pass)
                
                // Logger.d("LiveViewViewModel", "Starting playback...")
                player.play()
                
                _uiState.value = LiveViewUiState.Connected(cameraEntity)
                
            } catch (e: Exception) {
                // Logger.e("LiveViewViewModel", "Failed to connect to camera", e)
                _uiState.value = LiveViewUiState.Error("Connection failed: ${e.message}")
            }
        }
    }

    /**
     * Build RTSP URL with alternative paths
     */
    private fun buildRtspUrl(connection: CameraConnection): String {
        val path = if (currentPathIndex < rtspPaths.size) {
            // Logger.d("LiveViewViewModel", "Using first path: $path")
            rtspPaths[currentPathIndex]
        } else {
            connection.path ?: ""
        }
        
        val finalUrl = if (connection.user != null && connection.pass != null) {
            "rtsp://${connection.user}:${connection.pass}@${connection.ip}:${connection.port}$path"
        } else {
            "rtsp://${connection.ip}:${connection.port}$path"
        }
        
        // Logger.d("LiveViewViewModel", "Final RTSP URL: $finalUrl")
        return finalUrl
    }

    /**
     * Try alternative RTSP paths when current path fails
     */
    private suspend fun tryAlternativePath() {
        val connection = currentConnection ?: return
        
        // Logger.d("LiveViewViewModel", "tryAlternativePath() called")
        
        if (connection.ip?.isEmpty() != false) {
            // Logger.d("LiveViewViewModel", "Camera connection available: ${connection.ip}")
            return
        }
        
        currentPathIndex++
        if (currentPathIndex >= rtspPaths.size) {
            // Logger.d("LiveViewViewModel", "Trying path $currentPathIndex: $nextPath")
            _uiState.value = LiveViewUiState.Error("All RTSP paths failed")
            return
        }
        
        val nextPath = rtspPaths[currentPathIndex]
        val failedPath = if (currentPathIndex > 0) rtspPaths[currentPathIndex - 1] else "default"
        
        // Logger.d("LiveViewViewModel", "Trying alternative path: $nextPath (failed: $failedPath)")
        
        val newUrl = if (connection.user != null && connection.pass != null) {
            "rtsp://${connection.user}:${connection.pass}@${connection.ip}:${connection.port}$nextPath"
        } else {
            "rtsp://${connection.ip}:${connection.port}$nextPath"
        }
        
        // Logger.d("LiveViewViewModel", "Alternative RTSP URL: $finalUrl")
        
        try {
            // Logger.d("LiveViewViewModel", "Trying new RTSP URL: $newUrl")
            player.stop()
            player.setStreamUrl(newUrl, connection.user, connection.pass)
            
            // Logger.d("LiveViewViewModel", "Setting new stream URL on player...")
            player.play()
            // Logger.d("LiveViewViewModel", "Starting playback with new URL...")
            
        } catch (e: Exception) {
            // Logger.e("LiveViewViewModel", "Failed to try alternative path", e)
            tryAlternativePath() // Try next path
        }
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        viewModelScope.launch {
            // Logger.d("LiveViewViewModel", "Toggle play/pause called. Current player state: ${player.getPlayerState()}")
            
            when (player.getPlayerState()) {
                PlayerState.PLAYING -> {
                    // Logger.d("LiveViewViewModel", "Player is playing, pausing...")
                    player.pause()
                    _isPlaying.value = false
                }
                PlayerState.PAUSED, PlayerState.READY -> {
                    // Logger.d("LiveViewViewModel", "Player is paused/ready, playing...")
                    player.play()
                    _isPlaying.value = true
                }
                PlayerState.ERROR -> {
                    // Logger.d("LiveViewViewModel", "Player is in error state, attempting to reconnect with alternative paths...")
                    currentConnection?.let { connection ->
                        val alternativeUrl = if (connection.user != null && connection.pass != null) {
                            "rtsp://${connection.user}:${connection.pass}@${connection.ip}:${connection.port}${rtspPaths[currentPathIndex]}"
                        } else {
                            "rtsp://${connection.ip}:${connection.port}${rtspPaths[currentPathIndex]}"
                        }
                        
                                                 // Logger.d("LiveViewViewModel", "Reconnecting with alternative URL: $alternativeUrl")
                         player.stop()
                         player.setStreamUrl(alternativeUrl, connection.user, connection.pass)
                        player.play()
                        _isPlaying.value = true
                    }
                }
                PlayerState.IDLE -> {
                    // Logger.d("LiveViewViewModel", "Player is idle, attempting to start playback...")
                    currentConnection?.let { connection ->
                        val streamUrl = buildRtspUrl(connection)
                                                 // Logger.d("LiveViewViewModel", "Starting playback with URL: $streamUrl")
                         player.setStreamUrl(streamUrl, connection.user, connection.pass)
                        player.play()
                        _isPlaying.value = true
                    }
                }
                else -> {
                    // Logger.d("LiveViewViewModel", "Player in unknown state, attempting to play...")
                    player.play()
                    _isPlaying.value = true
                }
            }
        }
    }

    /**
     * Toggle audio mute
     */
    fun toggleMute() {
        viewModelScope.launch {
            val newMuted = !_isMuted.value
            _isMuted.value = newMuted
            player.setAudioEnabled(!newMuted)
            // Logger.d("LiveViewViewModel", "Audio ${if (newMuted) "muted" else "unmuted"}")
        }
    }

    /**
     * Toggle recording (placeholder - not implemented in Player interface)
     */
    fun toggleRecording() {
        viewModelScope.launch {
            val newRecording = !_isRecording.value
            _isRecording.value = newRecording
            // TODO: Implement recording functionality
            // Logger.d("LiveViewViewModel", "Recording ${if (newRecording) "started" else "stopped"}")
        }
    }

    /**
     * Take a snapshot (placeholder - not implemented in Player interface)
     */
    fun takeSnapshot(): File? {
        // TODO: Implement snapshot functionality
        // Logger.d("LiveViewViewModel", "Snapshot taken")
        return null
    }

    /**
     * Update connected state
     */
    private fun updateConnectedState(update: (ConnectedState) -> ConnectedState) {
        _connectedState.value = update(_connectedState.value)
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}

/**
 * UI states for live view
 */
sealed class LiveViewUiState {
    object Idle : LiveViewUiState()
    object Connecting : LiveViewUiState()
    data class Connected(val camera: CameraEntity) : LiveViewUiState()
    data class Error(val message: String) : LiveViewUiState()
}

/**
 * Connected state data
 */
data class ConnectedState(
    val isPlaying: Boolean = false,
    val connectionStatus: String = "Disconnected",
    val lastFrameTime: Long = 0L
)

