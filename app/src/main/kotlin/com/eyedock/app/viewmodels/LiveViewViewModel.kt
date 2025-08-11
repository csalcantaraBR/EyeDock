package com.eyedock.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyedock.app.data.repository.CameraRepository
import com.eyedock.app.di.AppModule
import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.interfaces.Player
import com.eyedock.app.domain.interfaces.PlayerState
import com.eyedock.app.domain.interfaces.PlayerEventListener
import com.eyedock.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.eyedock.app.domain.model.CameraMeta
import com.eyedock.app.utils.Logger
import kotlinx.coroutines.delay

/**
 * ViewModel for managing live view functionality.
 * Handles camera connection, streaming state, and media controls.
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

    private var currentCameraConnection: CameraConnection? = null
    private var currentPathIndex = 0
    private val rtspPaths = listOf(
        "/onvif1",
        "/live/ch00_0",
        "/live/ch0", 
        "/live",
        "/cam/realmonitor",
        "/video1",
        "/video",
        "/stream1",
        "/stream",
        "/h264",
        "/mpeg4",
        "/mjpeg"
    )

    init {
        // Initialize player and set up event listener
        player.initialize()
        player.setEventListener(object : PlayerEventListener {
            override fun onPlayerStateChanged(state: PlayerState) {
                viewModelScope.launch {
                    Logger.d("Player state changed to: $state")
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
                            Logger.e("Player entered ERROR state")
                            _uiState.value = LiveViewUiState.Error("Player error occurred")
                        }
                        else -> {
                            Logger.d("Player in state: $state")
                        }
                    }
                }
            }

            override fun onError(error: String) {
                viewModelScope.launch {
                    Logger.e("Player error: $error")
                    
                    // Se for erro de source, tentar path alternativo
                    if (error.contains("Source error") || 
                        error.contains("source") ||
                        error.contains("404") ||
                        error.contains("not found") ||
                        error.contains("connection") ||
                        error.contains("timeout") ||
                        error.contains("failed")) {
                        Logger.d("Source error detected, trying alternative path...")
                        Logger.d("Current path index: $currentPathIndex")
                        Logger.d("Total paths available: ${rtspPaths.size}")
                        tryAlternativePath()
                    } else {
                        Logger.d("Non-source error, not trying alternative paths")
                        _uiState.value = LiveViewUiState.Error("Stream error: $error")
                    }
                }
            }

            override fun onBuffering(percent: Int) {
                viewModelScope.launch {
                    Logger.d("Buffering: $percent%")
                    updateConnectedState { it.copy(connectionStatus = "Buffering: $percent%") }
                }
            }

            override fun onFirstFrame() {
                viewModelScope.launch {
                    Logger.i("First frame received!")
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
            Logger.d("Connecting to camera with ID: $cameraId")
            _uiState.value = LiveViewUiState.Connecting
            
            try {
                // Get camera from repository
                Logger.d("Fetching camera from repository...")
                val cameraEntity = cameraRepository.getCameraById(cameraId.toLong())
                if (cameraEntity == null) {
                    Logger.e("Camera not found with ID: $cameraId")
                    _uiState.value = LiveViewUiState.Error(Constants.ErrorMessages.CAMERA_NOT_FOUND)
                    return@launch
                }
                
                Logger.d("Camera found: ${cameraEntity.name} at ${cameraEntity.ip}:${cameraEntity.port}")
                
                // Convert to CameraConnection
                val cameraConnection = CameraConnection(
                    proto = cameraEntity.protocol,
                    ip = cameraEntity.ip,
                    port = cameraEntity.port,
                    user = cameraEntity.username,
                    pass = cameraEntity.password,
                    path = cameraEntity.path,
                    meta = CameraMeta(source = "manual")
                )
                
                currentCameraConnection = cameraConnection
                
                // Build RTSP URL
                val streamUrl = buildRtspUrl(cameraConnection)
                Logger.d("Built RTSP URL: $streamUrl")
                
                // Initialize player if needed
                if (!player.getPlayerState().equals(PlayerState.IDLE)) {
                    Logger.d("Player already initialized, stopping current playback...")
                    player.stop()
                }
                
                // Set up player with stream
                Logger.d("Setting up player with stream URL...")
                player.setStreamUrl(streamUrl, cameraConnection.user, cameraConnection.pass)
                
                // Update UI state to connected
                _uiState.value = LiveViewUiState.Connected(
                    cameraConnection = cameraConnection,
                    isPlaying = false,
                    isMuted = false,
                    isRecording = false,
                    streamUrl = streamUrl,
                    connectionStatus = "Connecting...",
                    lastFrameTime = System.currentTimeMillis()
                )
                
                // Start playback
                Logger.d("Starting playback...")
                player.play()
                
                Logger.i("Successfully connected to camera: ${cameraConnection.ip}")
                
            } catch (e: Exception) {
                Logger.e("Failed to connect to camera", e)
                _uiState.value = LiveViewUiState.Error("${Constants.ErrorMessages.CONNECTION_FAILED}: ${e.message}")
            }
        }
    }

    /**
     * Build RTSP URL from camera connection
     */
    private fun buildRtspUrl(connection: CameraConnection): String {
        val protocol = connection.proto ?: "rtsp"
        val port = connection.port ?: 554
        var path = connection.path ?: ""
        
        // If path is empty, use the first path from our list
        if (path.isEmpty() || path == "/") {
            path = rtspPaths[0] // Start with first path
            currentPathIndex = 0
            Logger.d("Using first path: $path")
        }
        
        val finalUrl = if (connection.user != null && connection.pass != null) {
            "$protocol://${connection.user}:${connection.pass}@${connection.ip}:$port$path"
        } else {
            "$protocol://${connection.ip}:$port$path"
        }
        
        Logger.d("Final RTSP URL: $finalUrl")
        return finalUrl
    }
    
    /**
     * Try alternative RTSP paths if the current one fails
     */
    private fun tryAlternativePaths(connection: CameraConnection, failedPath: String): String? {
        val protocol = connection.proto ?: "rtsp"
        val port = connection.port ?: 554
        
        // Common RTSP paths for IP cameras (excluding the failed one)
        val commonPaths = listOf(
            "/onvif1",
            "/live/ch00_0", 
            "/live/ch0",
            "/live",
            "/cam/realmonitor",
            "/video1",
            "/video",
            "/stream1",
            "/stream",
            "/h264",
            "/mpeg4",
            "/mjpeg"
        )
        
        // Find the next path to try
        val currentIndex = commonPaths.indexOf(failedPath)
        val nextIndex = if (currentIndex >= 0 && currentIndex < commonPaths.size - 1) currentIndex + 1 else 0
        
        val nextPath = commonPaths[nextIndex]
        Logger.d("Trying alternative path: $nextPath (failed: $failedPath)")
        
        val finalUrl = if (connection.user != null && connection.pass != null) {
            "$protocol://${connection.user}:${connection.pass}@${connection.ip}:$port$nextPath"
        } else {
            "$protocol://${connection.ip}:$port$nextPath"
        }
        
        Logger.d("Alternative RTSP URL: $finalUrl")
        return finalUrl
    }
    
    /**
     * Try next alternative path automatically
     */
    private suspend fun tryAlternativePath() {
        Logger.d("tryAlternativePath() called")
        currentCameraConnection?.let { connection ->
            Logger.d("Camera connection available: ${connection.ip}")
            if (currentPathIndex < rtspPaths.size - 1) {
                currentPathIndex++
                val nextPath = rtspPaths[currentPathIndex]
                
                Logger.d("Trying path $currentPathIndex: $nextPath")
                
                val protocol = connection.proto ?: "rtsp"
                val port = connection.port ?: 554
                
                val newUrl = if (connection.user != null && connection.pass != null) {
                    "$protocol://${connection.user}:${connection.pass}@${connection.ip}:$port$nextPath"
                } else {
                    "$protocol://${connection.ip}:$port$nextPath"
                }
                
                Logger.d("Trying new RTSP URL: $newUrl")
                
                // Update UI state
                _uiState.value = LiveViewUiState.Connected(
                    cameraConnection = connection,
                    isPlaying = false,
                    isMuted = _isMuted.value,
                    isRecording = _isRecording.value,
                    streamUrl = newUrl,
                    connectionStatus = "Trying path: $nextPath",
                    lastFrameTime = System.currentTimeMillis()
                )
                
                // Try new URL
                Logger.d("Setting new stream URL on player...")
                player.setStreamUrl(newUrl, connection.user, connection.pass)
                Logger.d("Starting playback with new URL...")
                player.play()
                
            } else {
                Logger.e("All RTSP paths failed - tried $currentPathIndex paths")
                _uiState.value = LiveViewUiState.Error("All streaming paths failed. Please check camera settings.")
            }
        } ?: run {
            Logger.e("No camera connection available")
            _uiState.value = LiveViewUiState.Error("No camera connection available")
        }
    }

    /**
     * Update connected state if currently connected
     */
    private fun updateConnectedState(update: (LiveViewUiState.Connected) -> LiveViewUiState.Connected) {
        val currentState = _uiState.value
        if (currentState is LiveViewUiState.Connected) {
            _uiState.value = update(currentState)
        }
    }

    /**
     * Disconnect from the current camera
     */
    fun disconnect() {
        player.stop()
        _uiState.value = LiveViewUiState.Idle
        _isPlaying.value = false
        _isMuted.value = false
        _isRecording.value = false
        currentCameraConnection = null
    }

    /**
     * Toggle play/pause state
     */
    fun togglePlayPause() {
        try {
            Logger.d("Toggle play/pause called. Current player state: ${player.getPlayerState()}")
            
            when (player.getPlayerState()) {
                PlayerState.PLAYING -> {
                    Logger.d("Player is playing, pausing...")
                    player.pause()
                }
                PlayerState.PAUSED, PlayerState.READY -> {
                    Logger.d("Player is paused/ready, playing...")
                    player.play()
                }
                PlayerState.ERROR -> {
                    Logger.d("Player is in error state, attempting to reconnect with alternative paths...")
                    // If player is in error state, try alternative paths
                    currentCameraConnection?.let { connection ->
                        val currentUrl = buildRtspUrl(connection)
                        val alternativeUrl = tryAlternativePaths(connection, "/onvif1") // Start with first alternative
                        
                        if (alternativeUrl != null) {
                            Logger.d("Reconnecting with alternative URL: $alternativeUrl")
                            player.setStreamUrl(alternativeUrl, connection.user, connection.pass)
                            player.play()
                        } else {
                            Logger.e("No alternative paths available")
                            _uiState.value = LiveViewUiState.Error("No alternative streaming paths available")
                        }
                    } ?: run {
                        Logger.e("No current camera connection available for reconnection")
                        _uiState.value = LiveViewUiState.Error("No camera connection available")
                    }
                }
                PlayerState.IDLE -> {
                    Logger.d("Player is idle, attempting to start playback...")
                    currentCameraConnection?.let { connection ->
                        val streamUrl = buildRtspUrl(connection)
                        Logger.d("Starting playback with URL: $streamUrl")
                        player.setStreamUrl(streamUrl, connection.user, connection.pass)
                        player.play()
                    } ?: run {
                        Logger.e("No current camera connection available")
                        _uiState.value = LiveViewUiState.Error("No camera connection available")
                    }
                }
                else -> {
                    Logger.d("Player in unknown state, attempting to play...")
                    player.play()
                }
            }
        } catch (e: Exception) {
            Logger.e("Error in togglePlayPause", e)
            _uiState.value = LiveViewUiState.Error("Playback error: ${e.message}")
        }
    }

    /**
     * Toggle mute state
     */
    fun toggleMute() {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        player.setAudioEnabled(!newMuted)
        
        updateConnectedState { it.copy(isMuted = newMuted) }
        Logger.d("Audio ${if (newMuted) "muted" else "unmuted"}")
    }

    /**
     * Toggle recording state
     */
    fun toggleRecording() {
        val newRecording = !_isRecording.value
        _isRecording.value = newRecording
        updateConnectedState { it.copy(isRecording = newRecording) }
        Logger.d("Recording ${if (newRecording) "started" else "stopped"}")
    }

    /**
     * Take a snapshot
     */
    fun takeSnapshot() {
        if (_uiState.value is LiveViewUiState.Connected) {
            _uiState.value = LiveViewUiState.SnapshotTaken
            
            // Reset to connected state after a delay
            viewModelScope.launch {
                delay(Constants.SNAPSHOT_FEEDBACK_DURATION)
                val currentState = _uiState.value
                if (currentState is LiveViewUiState.SnapshotTaken) {
                    // Restore the previous connected state
                    currentCameraConnection?.let { connection ->
                        val streamUrl = buildRtspUrl(connection)
                        _uiState.value = LiveViewUiState.Connected(
                            cameraConnection = connection,
                            isPlaying = _isPlaying.value,
                            isMuted = _isMuted.value,
                            isRecording = _isRecording.value,
                            streamUrl = streamUrl,
                            connectionStatus = "Live",
                            lastFrameTime = System.currentTimeMillis()
                        )
                    }
                }
            }
            
            Logger.d("Snapshot taken")
        }
    }

    /**
     * Get the player instance for UI integration
     */
    fun getPlayer(): Player {
        return player
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}

/**
 * UI states for the live view screen
 */
sealed class LiveViewUiState {
    object Idle : LiveViewUiState()
    object Connecting : LiveViewUiState()
    data class Connected(
        val cameraConnection: CameraConnection,
        val isPlaying: Boolean,
        val isMuted: Boolean,
        val isRecording: Boolean,
        val streamUrl: String,
        val connectionStatus: String,
        val lastFrameTime: Long
    ) : LiveViewUiState()
    data class Error(val message: String) : LiveViewUiState()
    object SnapshotTaken : LiveViewUiState()
}

