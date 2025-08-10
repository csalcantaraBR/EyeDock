package com.eyedock.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eyedock.app.viewmodels.LiveViewViewModel
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveViewScreen(
    cameraId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: LiveViewViewModel = hiltViewModel()
) {
    var showPtzControls by remember { mutableStateOf(false) }
    var showAudioControls by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isNightVision by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()

    // Verificação de segurança para cameraId
    LaunchedEffect(cameraId) {
        if (cameraId.isNullOrBlank()) {
            // Se não há cameraId, mostrar erro e voltar
            viewModel.clearError()
        } else {
            viewModel.loadCamera(cameraId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        uiState.cameraName ?: "Live View",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPtzControls = !showPtzControls }
                    ) {
                        Icon(
                            Icons.Default.ControlCamera,
                            contentDescription = "PTZ Controls"
                        )
                    }
                    IconButton(
                        onClick = { showAudioControls = !showAudioControls }
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Audio Controls"
                        )
                    }
                    IconButton(
                        onClick = { isNightVision = !isNightVision }
                    ) {
                        Icon(
                            if (isNightVision) Icons.Default.Nightlight else Icons.Default.LightMode,
                            contentDescription = "Night Vision"
                        )
                    }
                    IconButton(
                        onClick = { 
                            isRecording = !isRecording
                            if (isRecording) viewModel.startRecording() else viewModel.stopRecording()
                        }
                    ) {
                        Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Error handling
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Main content
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Video player area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // TODO: Implementar player RTSP real
                        // Por enquanto, placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Player RTSP não implementado",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (cameraId != null) {
                                Text(
                                    text = "Câmera ID: $cameraId",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Controls area
                    if (showPtzControls) {
                        PtzControls(
                            onMove = { x, y -> viewModel.movePtz(x, y) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(16.dp)
                        )
                    }

                    if (showAudioControls) {
                        AudioControls(
                            isMicrophoneEnabled = uiState.isMicrophoneEnabled,
                            microphoneVolume = uiState.microphoneVolume,
                            isSpeakerEnabled = uiState.isSpeakerEnabled,
                            speakerVolume = uiState.speakerVolume,
                            onToggleMicrophone = { viewModel.toggleMicrophone() },
                            onSetMicrophoneVolume = { viewModel.setMicrophoneVolume(it) },
                            onToggleSpeaker = { viewModel.toggleSpeaker() },
                            onSetSpeakerVolume = { viewModel.setSpeakerVolume(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PtzControls(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    
    // Calcular valores fora do Canvas
    val radius20dp = with(density) { 20.dp.toPx() }
    val radius8dp = with(density) { 8.dp.toPx() }
    val strokeWidth = with(density) { 2.dp.toPx() }
    
    // Obter cores fora do Canvas
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Controles PTZ",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { 
                                isDragging = false
                                onMove(0f, 0f) // Stop movement
                            },
                            onDrag = { _, dragAmount ->
                                val x = dragAmount.x / size.width
                                val y = dragAmount.y / size.height
                                onMove(x, y)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw joystick
                    drawCircle(
                        color = primaryColor,
                        radius = radius20dp,
                        style = Stroke(width = strokeWidth)
                    )
                    
                    if (isDragging) {
                        drawCircle(
                            color = primaryColor,
                            radius = radius8dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioControls(
    isMicrophoneEnabled: Boolean,
    microphoneVolume: Float,
    isSpeakerEnabled: Boolean,
    speakerVolume: Float,
    onToggleMicrophone: () -> Unit,
    onSetMicrophoneVolume: (Float) -> Unit,
    onToggleSpeaker: () -> Unit,
    onSetSpeakerVolume: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Controles de Áudio",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Microphone controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleMicrophone) {
                    Icon(
                        if (isMicrophoneEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Toggle Microphone"
                    )
                }
                Text(
                    text = "Microfone",
                    modifier = Modifier.weight(1f)
                )
                Slider(
                    value = microphoneVolume,
                    onValueChange = onSetMicrophoneVolume,
                    enabled = isMicrophoneEnabled,
                    modifier = Modifier.width(100.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Speaker controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleSpeaker) {
                    Icon(
                        if (isSpeakerEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Speaker"
                    )
                }
                Text(
                    text = "Alto-falante",
                    modifier = Modifier.weight(1f)
                )
                Slider(
                    value = speakerVolume,
                    onValueChange = onSetSpeakerVolume,
                    enabled = isSpeakerEnabled,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}
