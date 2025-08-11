package com.eyedock.app.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eyedock.app.data.repository.CloudBackupRepository
import com.eyedock.app.domain.interfaces.CloudStorage
import com.eyedock.app.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CloudBackupViewModel(
    private val cloudBackupRepository: CloudBackupRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "CloudBackupViewModel"
    }
    
    private val _uiState = MutableStateFlow<CloudBackupUiState>(CloudBackupUiState.Idle)
    val uiState: StateFlow<CloudBackupUiState> = _uiState.asStateFlow()
    
    private val _cloudFiles = MutableStateFlow<List<CloudStorage.CloudFile>>(emptyList())
    val cloudFiles: StateFlow<List<CloudStorage.CloudFile>> = _cloudFiles.asStateFlow()
    
    private val _availableServices = MutableStateFlow<List<CloudStorage.ServiceType>>(emptyList())
    val availableServices: StateFlow<List<CloudStorage.ServiceType>> = _availableServices.asStateFlow()
    
    private val _storageInfo = MutableStateFlow<CloudStorage.StorageInfo?>(null)
    val storageInfo: StateFlow<CloudStorage.StorageInfo?> = _storageInfo.asStateFlow()
    
    init {
        Logger.d(TAG, "Initializing CloudBackupViewModel")
        loadAvailableServices()
        checkAuthenticationStatus()
        
        // Initialize Google Drive automatically
        viewModelScope.launch {
            cloudBackupRepository.initializeCloudStorage(CloudStorage.ServiceType.GOOGLE_DRIVE)
        }
    }
    
    sealed class CloudBackupUiState {
        object Idle : CloudBackupUiState()
        object Loading : CloudBackupUiState()
        object Authenticating : CloudBackupUiState()
        data class Authenticated(val serviceType: CloudStorage.ServiceType) : CloudBackupUiState()
        data class Uploading(val fileName: String, val progress: Int = 0) : CloudBackupUiState()
        data class UploadSuccess(val fileId: String, val downloadUrl: String?) : CloudBackupUiState()
        data class Error(val message: String) : CloudBackupUiState()
    }
    
    /**
     * Initialize cloud storage service
     */
    fun initializeCloudStorage(serviceType: CloudStorage.ServiceType) {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Initializing cloud storage: $serviceType")
                _uiState.value = CloudBackupUiState.Loading
                
                val initialized = cloudBackupRepository.initializeCloudStorage(serviceType)
                if (initialized) {
                    _uiState.value = CloudBackupUiState.Authenticated(serviceType)
                    loadCloudFiles()
                    loadStorageInfo()
                } else {
                    _uiState.value = CloudBackupUiState.Error("Failed to initialize cloud storage")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Initialization error: ${e.message}")
                _uiState.value = CloudBackupUiState.Error(e.message ?: "Initialization failed")
            }
        }
    }
    
    /**
     * Authenticate with cloud storage
     */
    fun authenticate() {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Authenticating with cloud storage")
                _uiState.value = CloudBackupUiState.Authenticating
                
                // First, ensure Google Drive is initialized
                val initialized = cloudBackupRepository.initializeCloudStorage(CloudStorage.ServiceType.GOOGLE_DRIVE)
                if (!initialized) {
                    _uiState.value = CloudBackupUiState.Error("Failed to initialize Google Drive")
                    return@launch
                }
                
                // Now try to authenticate
                val authenticated = cloudBackupRepository.authenticate()
                if (authenticated) {
                    _uiState.value = CloudBackupUiState.Authenticated(CloudStorage.ServiceType.GOOGLE_DRIVE)
                    loadCloudFiles()
                    loadStorageInfo()
                } else {
                    _uiState.value = CloudBackupUiState.Error("Authentication failed - please sign in")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Authentication error: ${e.message}")
                _uiState.value = CloudBackupUiState.Error(e.message ?: "Authentication failed")
            }
        }
    }
    
    /**
     * Handle Google Sign-In result
     */
    fun handleGoogleSignInResult(data: Intent?): Boolean {
        return viewModelScope.launch {
            try {
                Logger.d(TAG, "Handling Google Sign-In result")
                val success = cloudBackupRepository.handleGoogleSignInResult(data)
                if (success) {
                    _uiState.value = CloudBackupUiState.Authenticated(CloudStorage.ServiceType.GOOGLE_DRIVE)
                    loadCloudFiles()
                    loadStorageInfo()
                } else {
                    _uiState.value = CloudBackupUiState.Error("Google Sign-In failed")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Google Sign-In result handling error: ${e.message}")
                _uiState.value = CloudBackupUiState.Error(e.message ?: "Sign-In failed")
            }
        }.let { true } // Return true to indicate we handled the result
    }
    
    /**
     * Get Google Sign-In intent
     */
    fun getGoogleSignInIntent(): Intent? {
        return cloudBackupRepository.getGoogleSignInIntent()
    }
    
    /**
     * Sign out from cloud storage
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Signing out from cloud storage")
                cloudBackupRepository.signOut()
                _uiState.value = CloudBackupUiState.Idle
                _cloudFiles.value = emptyList()
                _storageInfo.value = null
            } catch (e: Exception) {
                Logger.e(TAG, "Sign out error: ${e.message}")
                _uiState.value = CloudBackupUiState.Error(e.message ?: "Sign out failed")
            }
        }
    }
    
    /**
     * Upload file to cloud storage
     */
    fun uploadFile(localFilePath: String, fileName: String, mimeType: String) {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Uploading file: $fileName")
                cloudBackupRepository.uploadFile(localFilePath, fileName, mimeType).collect { status ->
                    when (status) {
                        is CloudStorage.UploadStatus.Idle -> {
                            // Do nothing for idle state
                        }
                        is CloudStorage.UploadStatus.Uploading -> {
                            _uiState.value = CloudBackupUiState.Uploading(fileName, 0)
                        }
                        is CloudStorage.UploadStatus.Progress -> {
                            _uiState.value = CloudBackupUiState.Uploading(fileName, status.percentage)
                        }
                        is CloudStorage.UploadStatus.Success -> {
                            _uiState.value = CloudBackupUiState.UploadSuccess(status.fileId, status.downloadUrl)
                            loadCloudFiles() // Refresh the file list
                        }
                        is CloudStorage.UploadStatus.Error -> {
                            _uiState.value = CloudBackupUiState.Error(status.message)
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Upload error: ${e.message}")
                _uiState.value = CloudBackupUiState.Error(e.message ?: "Upload failed")
            }
        }
    }
    
    /**
     * Delete file from cloud storage
     */
    fun deleteCloudFile(fileId: String) {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Deleting cloud file: $fileId")
                val success = cloudBackupRepository.deleteCloudFile(fileId)
                if (success) {
                    loadCloudFiles() // Refresh the file list
                } else {
                    _uiState.value = CloudBackupUiState.Error("Failed to delete file")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Delete error: ${e.message}")
                _uiState.value = CloudBackupUiState.Error(e.message ?: "Delete failed")
            }
        }
    }
    
    /**
     * Get download URL for a file
     */
    fun getDownloadUrl(fileId: String, onUrlReceived: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Getting download URL for file: $fileId")
                val url = cloudBackupRepository.getDownloadUrl(fileId)
                onUrlReceived(url)
            } catch (e: Exception) {
                Logger.e(TAG, "Get download URL error: ${e.message}")
                onUrlReceived(null)
            }
        }
    }
    
    /**
     * Refresh cloud files and storage info
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Refreshing cloud data")
                loadCloudFiles()
                loadStorageInfo()
            } catch (e: Exception) {
                Logger.e(TAG, "Refresh error: ${e.message}")
                _uiState.value = CloudBackupUiState.Error(e.message ?: "Refresh failed")
            }
        }
    }
    
    private fun loadAvailableServices() {
        _availableServices.value = listOf(
            CloudStorage.ServiceType.GOOGLE_DRIVE,
            CloudStorage.ServiceType.ONEDRIVE,
            CloudStorage.ServiceType.SHAREPOINT,
            CloudStorage.ServiceType.DROPBOX
        )
    }
    
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            try {
                val isAuthenticated = cloudBackupRepository.isAuthenticated()
                if (isAuthenticated) {
                    _uiState.value = CloudBackupUiState.Authenticated(CloudStorage.ServiceType.GOOGLE_DRIVE)
                    loadCloudFiles()
                    loadStorageInfo()
                } else {
                    _uiState.value = CloudBackupUiState.Idle
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Authentication status check error: ${e.message}")
                _uiState.value = CloudBackupUiState.Error(e.message ?: "Status check failed")
            }
        }
    }
    
    private suspend fun loadCloudFiles() {
        try {
            val files = cloudBackupRepository.listCloudFiles()
            _cloudFiles.value = files
        } catch (e: Exception) {
            Logger.e(TAG, "Load cloud files error: ${e.message}")
            _cloudFiles.value = emptyList()
        }
    }
    
    private suspend fun loadStorageInfo() {
        try {
            val info = cloudBackupRepository.getStorageInfo()
            _storageInfo.value = info
        } catch (e: Exception) {
            Logger.e(TAG, "Load storage info error: ${e.message}")
            _storageInfo.value = null
        }
    }
}

class CloudBackupViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CloudBackupViewModel::class.java)) {
            val repository = CloudBackupRepository(context)
            @Suppress("UNCHECKED_CAST")
            return CloudBackupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


