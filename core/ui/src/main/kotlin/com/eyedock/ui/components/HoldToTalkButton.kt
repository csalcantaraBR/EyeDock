package com.eyedock.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun HoldToTalkButton(
    onTalkStart: () -> Unit,
    onTalkEnd: () -> Unit,
    enabled: Boolean,
    testTag: String = "talk_button"
) {
    Button(
        onClick = { /* Implementação seria aqui */ },
        modifier = Modifier
            .size(56.dp)
            .testTag(testTag),
        enabled = enabled
    ) {
        // Conteúdo do botão seria aqui
    }
}
