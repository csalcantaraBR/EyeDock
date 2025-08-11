package com.eyedock.app.data.repository

import android.content.Context
import android.content.Intent
import com.eyedock.app.data.cloud.GoogleDriveStorage
import com.eyedock.app.data.cloud.MockCloudStorage
import com.eyedock.app.domain.interfaces.CloudStorage
import com.eyedock.app.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CloudBackupRepository(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "CloudBackupRepository"
        private const val USE_MOCK_STORAGE = false // Set to false to use real Google Drive
    }
    
    private val googleDriveStorage = GoogleDriveStorage(context)
    private val mockCloudStorage = MockCloudStorage(context)
    private var currentStorage: CloudStorage? = null
    
    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()
    
    private val _cloudFiles = MutableStateFlow<List<CloudStorage.CloudFile>>(emptyList())
    val cloudFiles: StateFlow<List<CloudStorage.CloudFile>> = _cloudFiles.asStateFlow()
    
    sealed class BackupStatus {
        object Idle : BackupStatus()
        object Initializing : BackupStatus()
        object Authenticating : BackupStatus()
        data class Uploading(val fileName: String, val progress: Int = 0) : BackupStatus()
        data class Success(val fileId: String, val downloadUrl: String?) : BackupStatus()
        data class Error(val message: String) : BackupStatus()
    }
    
    /**
     * Initialize cloud storage service
     */
    suspend fun initializeCloudStorage(serviceType: CloudStorage.ServiceType): Boolean {
        return try {
            Logger.d(TAG, "Initializing cloud storage: $serviceType")
            _backupStatus.value = BackupStatus.Initializing
            
            currentStorage = when (serviceType) {
                CloudStorage.ServiceType.GOOGLE_DRIVE -> {
                    if (USE_MOCK_STORAGE) {
                        Logger.d(TAG, "Using Mock Cloud Storage for development")
                        mockCloudStorage
                    } else {
                        Logger.d(TAG, "Using Google Drive Storage")
                        googleDriveStorage
                    }
                }
                CloudStorage.ServiceType.ONEDRIVE -> {
                    Logger.d(TAG, "OneDrive not implemented - using Mock Storage")
                    mockCloudStorage
                }
                CloudStorage.ServiceType.SHAREPOINT -> {
                    Logger.d(TAG, "SharePoint not implemented - using Mock Storage")
                    mockCloudStorage
                }
                CloudStorage.ServiceType.DROPBOX -> {
                    Logger.d(TAG, "Dropbox not implemented - using Mock Storage")
                    mockCloudStorage
                }
            }
            
            Logger.d(TAG, "Current storage set to: ${currentStorage?.javaClass?.simpleName}")
            
            val initialized = currentStorage?.initialize() ?: false
            if (initialized) {
                Logger.d(TAG, "Cloud storage initialized successfully")
                loadCloudFiles()
            } else {
                Logger.d(TAG, "Cloud storage initialization failed")
                _backupStatus.value = BackupStatus.Error("Failed to initialize cloud storage")
            }
            
            initialized
        } catch (e: Exception) {
            Logger.e(TAG, "Cloud storage initialization error: ${e.message}")
            _backupStatus.value = BackupStatus.Error(e.message ?: "Initialization failed")
            false
        }
    }
    
    /**
     * Authenticate with cloud storage
     */
    suspend fun authenticate(): Boolean {
        return try {
            Logger.d(TAG, "Authenticating with cloud storage")
            _backupStatus.value = BackupStatus.Authenticating
            
            val authenticated = currentStorage?.authenticate() ?: false
            if (authenticated) {
                Logger.d(TAG, "Authentication successful")
                loadCloudFiles()
            } else {
                Logger.d(TAG, "Authentication failed")
                _backupStatus.value = BackupStatus.Error("Authentication failed")
            }
            
            authenticated
        } catch (e: Exception) {
            Logger.e(TAG, "Authentication error: ${e.message}")
            _backupStatus.value = BackupStatus.Error(e.message ?: "Authentication failed")
            false
        }
    }
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return try {
            val authenticated = currentStorage?.isAuthenticated() ?: false
            Logger.d(TAG, "Authentication check: $authenticated")
            authenticated
        } catch (e: Exception) {
            Logger.e(TAG, "Authentication check error: ${e.message}")
            false
        }
    }
    
    /**
     * Get Google Sign-In intent (only for Google Drive)
     */
    fun getGoogleSignInIntent(): Intent? {
        return if (currentStorage is GoogleDriveStorage) {
            (currentStorage as GoogleDriveStorage).getSignInIntent()
        } else {
            Logger.d(TAG, "Not using Google Drive - no sign-in intent available")
            null
        }
    }
    
    /**
     * Handle Google Sign-In result (only for Google Drive)
     */
    suspend fun handleGoogleSignInResult(data: Intent?): Boolean {
        return try {
            Logger.d(TAG, "Handling Google Sign-In result")
            if (currentStorage is GoogleDriveStorage) {
                val success = (currentStorage as GoogleDriveStorage).handleSignInResult(data)
                if (success) {
                    loadCloudFiles()
                }
                success
            } else {
                Logger.d(TAG, "Not using Google Drive - cannot handle sign-in result")
                false
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Google Sign-In result handling error: ${e.message}")
            false
        }
    }
    
    /**
     * Sign out from cloud storage
     */
    suspend fun signOut() {
        try {
            Logger.d(TAG, "Signing out from cloud storage")
            currentStorage?.signOut()
            _cloudFiles.value = emptyList()
            _backupStatus.value = BackupStatus.Idle
        } catch (e: Exception) {
            Logger.e(TAG, "Sign out error: ${e.message}")
        }
    }
    
    /**
     * Upload file to cloud storage
     */
    suspend fun uploadFile(
        localFilePath: String,
        fileName: String,
        mimeType: String
    ): Flow<CloudStorage.UploadStatus> {
        return try {
            Logger.d(TAG, "Uploading file: $fileName")
            currentStorage?.uploadFile(localFilePath, fileName, mimeType)
                ?: kotlinx.coroutines.flow.flow {
                    emit(CloudStorage.UploadStatus.Error("No cloud storage initialized"))
                }
        } catch (e: Exception) {
            Logger.e(TAG, "Upload error: ${e.message}")
            kotlinx.coroutines.flow.flow {
                emit(CloudStorage.UploadStatus.Error(e.message ?: "Upload failed"))
            }
        }
    }
    
    /**
     * List cloud files
     */
    suspend fun listCloudFiles(): List<CloudStorage.CloudFile> {
        return try {
            Logger.d(TAG, "Listing cloud files")
            currentStorage?.listFiles() ?: emptyList()
        } catch (e: Exception) {
            Logger.e(TAG, "List cloud files error: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Delete cloud file
     */
    suspend fun deleteCloudFile(fileId: String): Boolean {
        return try {
            Logger.d(TAG, "Deleting cloud file: $fileId")
            currentStorage?.deleteFile(fileId) ?: false
        } catch (e: Exception) {
            Logger.e(TAG, "Delete cloud file error: ${e.message}")
            false
        }
    }
    
    /**
     * Get download URL for a file
     */
    suspend fun getDownloadUrl(fileId: String): String? {
        return try {
            Logger.d(TAG, "Getting download URL for file: $fileId")
            currentStorage?.getDownloadUrl(fileId)
        } catch (e: Exception) {
            Logger.e(TAG, "Get download URL error: ${e.message}")
            null
        }
    }
    
    /**
     * Get storage information
     */
    suspend fun getStorageInfo(): CloudStorage.StorageInfo? {
        return try {
            Logger.d(TAG, "Getting storage info")
            currentStorage?.getStorageInfo()
        } catch (e: Exception) {
            Logger.e(TAG, "Get storage info error: ${e.message}")
            null
        }
    }
    
    private suspend fun loadCloudFiles() {
        try {
            val files = listCloudFiles()
            _cloudFiles.value = files
            Logger.d(TAG, "Loaded ${files.size} cloud files")
        } catch (e: Exception) {
            Logger.e(TAG, "Load cloud files error: ${e.message}")
            _cloudFiles.value = emptyList()
        }
    }
}
