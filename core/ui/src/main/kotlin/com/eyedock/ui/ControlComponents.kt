package com.eyedock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * GREEN PHASE - Implementação mínima de PTZ Joystick
 * 
 * Controle circular para Pan-Tilt-Zoom
 */
@Composable
fun PtzJoystick(
    onPanTilt: (deltaX: Float, deltaY: Float) -> Unit,
    onZoom: (delta: Float) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    testTag: String = "ptz_joystick"
) {
    var centerPosition by remember { mutableStateOf(Offset.Zero) }
    var knobPosition by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = modifier
            .size(120.dp) // Maior que 48dp para acessibilidade
            .testTag(testTag)
            .semantics { 
                contentDescription = "PTZ control joystick for camera movement" 
            }
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = if (enabled) 1f else 0.5f
                )
            )
            .pointerInput(enabled) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            centerPosition = Offset(size.width / 2f, size.height / 2f)
                            knobPosition = offset
                        },
                        onDragEnd = {
                            knobPosition = centerPosition
                            onPanTilt(0f, 0f) // Reset to center
                        }
                    ) { _, dragAmount ->
                        val newPosition = knobPosition + dragAmount
                        val deltaFromCenter = newPosition - centerPosition
                        val distance = sqrt(deltaFromCenter.x.pow(2) + deltaFromCenter.y.pow(2))
                        val maxRadius = size.width / 2f - 20.dp.toPx()
                        
                        knobPosition = if (distance <= maxRadius) {
                            newPosition
                        } else {
                            val angle = atan2(deltaFromCenter.y, deltaFromCenter.x)
                            centerPosition + Offset(
                                cos(angle) * maxRadius,
                                sin(angle) * maxRadius
                            )
                        }
                        
                        // Normalize values between -1 and 1
                        val normalizedX = (knobPosition.x - centerPosition.x) / maxRadius
                        val normalizedY = (knobPosition.y - centerPosition.y) / maxRadius
                        
                        onPanTilt(normalizedX, normalizedY)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            // Inner knob
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        
        // Zoom controls
        Column(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            IconButton(
                onClick = { onZoom(0.1f) },
                enabled = enabled,
                modifier = Modifier.semantics { 
                    contentDescription = "Zoom in" 
                }
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_input_add),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            IconButton(
                onClick = { onZoom(-0.1f) },
                enabled = enabled,
                modifier = Modifier.semantics { 
                    contentDescription = "Zoom out" 
                }
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_input_delete),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * GREEN PHASE - Implementação mínima de HoldToTalkButton
 */
@Composable
fun HoldToTalkButton(
    onTalkStart: () -> Unit,
    onTalkEnd: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    testTag: String = "talk_button"
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Button(
        onClick = { /* Handle click if needed */ },
        enabled = enabled,
        modifier = modifier
            .size(64.dp) // Maior que 48dp
            .testTag(testTag)
            .semantics { 
                contentDescription = "Hold to talk button for two-way audio" 
            }
            .pointerInput(enabled) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = {
                            isPressed = true
                            onTalkStart()
                        },
                        onDragEnd = {
                            isPressed = false
                            onTalkEnd()
                        }
                    ) { _, _ ->
                        // Continue holding
                    }
                }
            },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            else 
                MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            painter = painterResource(android.R.drawable.ic_btn_speak_now),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * GREEN PHASE - Implementação mínima de RecordButton
 */
@Composable
fun RecordButton(
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "record_button"
) {
    FloatingActionButton(
        onClick = onToggleRecording,
        modifier = modifier
            .size(56.dp) // Maior que 48dp
            .testTag(testTag)
            .semantics { 
                contentDescription = if (isRecording) "Stop recording" else "Start recording"
            },
        containerColor = if (isRecording) 
            MaterialTheme.colorScheme.error 
        else 
            MaterialTheme.colorScheme.primary
    ) {
        Icon(
            painter = painterResource(
                if (isRecording) 
                    android.R.drawable.ic_media_pause
                else 
                    android.R.drawable.ic_media_play
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * GREEN PHASE - Controles completos da live view
 */
@Composable
fun LiveViewControls(
    onPtzMove: (deltaX: Float, deltaY: Float) -> Unit,
    onZoom: (delta: Float) -> Unit,
    onToggleNightVision: () -> Unit,
    onToggleSpotlight: () -> Unit,
    onToggleAutoTracking: () -> Unit,
    onTalkStart: () -> Unit,
    onTalkEnd: () -> Unit,
    onRecord: () -> Unit,
    onSnapshot: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "live_controls"
) {
    var isRecording by remember { mutableStateOf(false) }
    var nightVisionEnabled by remember { mutableStateOf(false) }
    var spotlightEnabled by remember { mutableStateOf(false) }
    var autoTrackingEnabled by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(testTag)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Night vision toggle
            IconButton(
                onClick = {
                    nightVisionEnabled = !nightVisionEnabled
                    onToggleNightVision()
                },
                modifier = Modifier.semantics { 
                    contentDescription = "Night vision toggle" 
                }
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_dialog_info),
                    contentDescription = null,
                    tint = if (nightVisionEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Spotlight toggle
            IconButton(
                onClick = {
                    spotlightEnabled = !spotlightEnabled
                    onToggleSpotlight()
                },
                modifier = Modifier.semantics { 
                    contentDescription = "Spotlight toggle" 
                }
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_dialog_alert),
                    contentDescription = null,
                    tint = if (spotlightEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Auto tracking toggle
            IconButton(
                onClick = {
                    autoTrackingEnabled = !autoTrackingEnabled
                    onToggleAutoTracking()
                },
                modifier = Modifier.semantics { 
                    contentDescription = "Auto tracking toggle" 
                }
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_mylocation),
                    contentDescription = null,
                    tint = if (autoTrackingEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Center area with PTZ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PTZ Joystick
            PtzJoystick(
                onPanTilt = onPtzMove,
                onZoom = onZoom,
                testTag = "ptz_control"
            )
            
            // Talk button
            HoldToTalkButton(
                onTalkStart = onTalkStart,
                onTalkEnd = onTalkEnd,
                testTag = "talk_control"
            )
        }
        
        // Bottom controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Record button
            RecordButton(
                isRecording = isRecording,
                onToggleRecording = {
                    isRecording = !isRecording
                    onRecord()
                },
                testTag = "record_control"
            )
            
            // Snapshot button
            IconButton(
                onClick = onSnapshot,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { 
                        contentDescription = "Take snapshot" 
                    }
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_camera),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PtzJoystickPreview() {
    MaterialTheme {
        PtzJoystick(
            onPanTilt = { _, _ -> },
            onZoom = { }
        )
    }
}

@Preview
@Composable
private fun LiveViewControlsPreview() {
    MaterialTheme {
        LiveViewControls(
            onPtzMove = { _, _ -> },
            onZoom = { },
            onToggleNightVision = { },
            onToggleSpotlight = { },
            onToggleAutoTracking = { },
            onTalkStart = { },
            onTalkEnd = { },
            onRecord = { },
            onSnapshot = { }
        )
    }
}
