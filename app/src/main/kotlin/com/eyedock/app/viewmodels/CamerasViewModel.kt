package com.eyedock.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyedock.app.data.repository.CameraRepository
import com.eyedock.app.di.AppModule
import com.eyedock.app.data.local.entity.CameraEntity
import com.eyedock.app.utils.Constants
import com.eyedock.app.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the cameras list screen.
 * Handles loading, displaying, and managing camera entities.
 */
class CamerasViewModel : ViewModel() {

    private val cameraRepository: CameraRepository = AppModule.getCameraRepository()
    private val logger = Logger.withTag("CamerasViewModel")

    private val _uiState = MutableStateFlow<CamerasUiState>(CamerasUiState.Loading)
    val uiState: StateFlow<CamerasUiState> = _uiState.asStateFlow()

    private val _cameras = MutableStateFlow<List<CameraEntity>>(emptyList())
    val cameras: StateFlow<List<CameraEntity>> = _cameras.asStateFlow()

    /**
     * Load all cameras from the database
     */
    fun loadCameras() {
        viewModelScope.launch {
            logger.d("Loading cameras from database")
            _uiState.value = CamerasUiState.Loading
            try {
                cameraRepository.getAllCameras().collect { cameraList ->
                    logger.d("Loaded ${cameraList.size} cameras")
                    _cameras.value = cameraList
                    _uiState.value = CamerasUiState.Success
                }
            } catch (e: Exception) {
                logger.e("Failed to load cameras", e)
                _uiState.value = CamerasUiState.Error("${Constants.ErrorMessages.LOAD_FAILED}: ${e.message}")
            }
        }
    }

    /**
     * Delete a camera by its ID
     */
    fun deleteCamera(cameraId: Long) {
        viewModelScope.launch {
            try {
                logger.d("Deleting camera with ID: $cameraId")
                cameraRepository.deleteCamera(cameraId)
                logger.i("Camera deleted successfully")
            } catch (e: Exception) {
                logger.e("Failed to delete camera with ID: $cameraId", e)
                // Handle error silently for now
                // Could add error state handling here
            }
        }
    }
}

/**
 * UI states for the cameras screen
 */
sealed class CamerasUiState {
    object Loading : CamerasUiState()
    object Success : CamerasUiState()
    data class Error(val message: String) : CamerasUiState()
}
