package com.eyedock.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun PtzJoystick(
    onPanTilt: (Float, Float) -> Unit,
    onZoom: (Float) -> Unit,
    enabled: Boolean,
    testTag: String = "ptz_joystick"
) {
    Surface(
        modifier = Modifier
            .size(100.dp)
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surface
    ) {
        // Implementação do joystick seria aqui
        // Por enquanto, apenas um placeholder
    }
}
