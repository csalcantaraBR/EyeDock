package com.eyedock.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun RecordButton(
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    testTag: String = "record_button"
) {
    Button(
        onClick = onToggleRecording,
        modifier = Modifier
            .size(56.dp)
            .testTag(testTag),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    ) {
        // Conteúdo do botão seria aqui
    }
}
