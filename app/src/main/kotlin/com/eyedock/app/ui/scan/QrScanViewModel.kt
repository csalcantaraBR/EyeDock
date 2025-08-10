package com.eyedock.app.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrScanViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(QrScanUiState())
    val uiState: StateFlow<QrScanUiState> = _uiState.asStateFlow()
    
    fun onQrDetected(qrText: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                detectedQrText = qrText,
                isQrDetected = true
            )
        }
    }
    
    fun clearQrDetection() {
        _uiState.value = _uiState.value.copy(
            detectedQrText = null,
            isQrDetected = false
        )
    }
}

data class QrScanUiState(
    val detectedQrText: String? = null,
    val isQrDetected: Boolean = false
)
