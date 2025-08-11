package com.eyedock.app.data.cloud

import android.content.Context
import com.eyedock.app.domain.interfaces.CloudStorage
import com.eyedock.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Mock implementation of CloudStorage for development and testing
 * This simulates cloud storage functionality without requiring real authentication
 */
class MockCloudStorage(
    private val context: Context
) : CloudStorage {
    
    companion object {
        private const val TAG = "MockCloudStorage"
    }
    
    private var isAuthenticated = false
    private var mockFiles = mutableListOf<CloudStorage.CloudFile>()
    
    init {
        // Initialize with some mock files
        val currentTime = System.currentTimeMillis()
        mockFiles.addAll(listOf(
            CloudStorage.CloudFile(
                id = "mock_file_1",
                name = "Sample Recording 1.mp4",
                size = 1024 * 1024 * 10, // 10MB
                mimeType = "video/mp4",
                createdTime = currentTime - 86400000, // 1 day ago
                modifiedTime = currentTime - 86400000 // 1 day ago
            ),
            CloudStorage.CloudFile(
                id = "mock_file_2",
                name = "Sample Recording 2.mp4",
                size = 1024 * 1024 * 15, // 15MB
                mimeType = "video/mp4",
                createdTime = currentTime - 172800000, // 2 days ago
                modifiedTime = currentTime - 172800000 // 2 days ago
            ),
            CloudStorage.CloudFile(
                id = "mock_file_3",
                name = "Motion Detection Event.mp4",
                size = 1024 * 1024 * 8, // 8MB
                mimeType = "video/mp4",
                createdTime = currentTime - 43200000, // 12 hours ago
                modifiedTime = currentTime - 43200000 // 12 hours ago
            )
        ))
    }
    
    override suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Initializing Mock Cloud Storage")
            // Simulate initialization delay
            kotlinx.coroutines.delay(500)
            Logger.d(TAG, "Mock Cloud Storage initialized successfully")
            return@withContext true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize Mock Cloud Storage: ${e.message}")
            return@withContext false
        }
    }
    
    override suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.IO) {
        Logger.d(TAG, "Mock authentication check: $isAuthenticated")
        isAuthenticated
    }
    
    override suspend fun authenticate(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Starting mock authentication")
            // Simulate authentication delay
            kotlinx.coroutines.delay(1000)
            isAuthenticated = true
            Logger.d(TAG, "Mock authentication successful")
            return@withContext true
        } catch (e: Exception) {
            Logger.e(TAG, "Mock authentication failed: ${e.message}")
            return@withContext false
        }
    }
    
    override suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Signing out from Mock Cloud Storage")
            isAuthenticated = false
            Logger.d(TAG, "Mock sign out successful")
        } catch (e: Exception) {
            Logger.e(TAG, "Mock sign out failed: ${e.message}")
        }
    }
    
    override suspend fun uploadFile(
        localFilePath: String,
        fileName: String,
        mimeType: String
    ): Flow<CloudStorage.UploadStatus> = flow {
        try {
            emit(CloudStorage.UploadStatus.Uploading)
            
            if (!isAuthenticated) {
                emit(CloudStorage.UploadStatus.Error("Not authenticated"))
                return@flow
            }
            
            Logger.d(TAG, "Starting mock upload: $fileName")
            
            // Simulate upload progress
            for (progress in 0..100 step 10) {
                kotlinx.coroutines.delay(200) // Simulate upload time
                emit(CloudStorage.UploadStatus.Progress(progress))
            }
            
            // Add the file to mock storage
            val newFile = CloudStorage.CloudFile(
                id = "mock_file_${System.currentTimeMillis()}",
                name = fileName,
                size = 1024 * 1024 * 5, // 5MB mock size
                mimeType = mimeType,
                createdTime = System.currentTimeMillis(),
                modifiedTime = System.currentTimeMillis()
            )
            mockFiles.add(newFile)
            
            Logger.d(TAG, "Mock upload successful: ${newFile.id}")
            emit(CloudStorage.UploadStatus.Success(newFile.id, "https://mock-storage.com/file/${newFile.id}"))
        } catch (e: Exception) {
            Logger.e(TAG, "Mock upload failed: ${e.message}")
            emit(CloudStorage.UploadStatus.Error(e.message ?: "Upload failed"))
        }
    }
    
    override suspend fun listFiles(folderId: String?): List<CloudStorage.CloudFile> = withContext(Dispatchers.IO) {
        try {
            if (!isAuthenticated) {
                Logger.d(TAG, "Not authenticated - cannot list files")
                return@withContext emptyList()
            }
            
            Logger.d(TAG, "Listing mock files: ${mockFiles.size} files found")
            return@withContext mockFiles.toList()
        } catch (e: Exception) {
            Logger.e(TAG, "Mock list files failed: ${e.message}")
            return@withContext emptyList()
        }
    }
    
    override suspend fun createFolder(folderName: String, parentFolderId: String?): String? = withContext(Dispatchers.IO) {
        try {
            if (!isAuthenticated) {
                Logger.d(TAG, "Not authenticated - cannot create folder")
                return@withContext null
            }
            
            val folderId = "mock_folder_${System.currentTimeMillis()}"
            Logger.d(TAG, "Mock folder created: $folderId")
            return@withContext folderId
        } catch (e: Exception) {
            Logger.e(TAG, "Mock create folder failed: ${e.message}")
            return@withContext null
        }
    }
    
    override suspend fun deleteFile(fileId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isAuthenticated) {
                Logger.d(TAG, "Not authenticated - cannot delete file")
                return@withContext false
            }
            
            val removed = mockFiles.removeAll { it.id == fileId }
            Logger.d(TAG, "Mock file deletion: $removed files removed")
            return@withContext removed
        } catch (e: Exception) {
            Logger.e(TAG, "Mock delete file failed: ${e.message}")
            return@withContext false
        }
    }
    
    override suspend fun getDownloadUrl(fileId: String): String? = withContext(Dispatchers.IO) {
        try {
            if (!isAuthenticated) {
                Logger.d(TAG, "Not authenticated - cannot get download URL")
                return@withContext null
            }
            
            val fileExists = mockFiles.any { it.id == fileId }
            if (fileExists) {
                val url = "https://mock-storage.com/download/$fileId"
                Logger.d(TAG, "Mock download URL generated: $url")
                return@withContext url
            } else {
                Logger.d(TAG, "Mock file not found: $fileId")
                return@withContext null
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Mock get download URL failed: ${e.message}")
            return@withContext null
        }
    }
    
    override suspend fun getStorageInfo(): CloudStorage.StorageInfo? = withContext(Dispatchers.IO) {
        try {
            if (!isAuthenticated) {
                Logger.d(TAG, "Not authenticated - cannot get storage info")
                return@withContext null
            }
            
            val totalSpace = 100L * 1024 * 1024 * 1024 // 100GB
            val usedSpace = mockFiles.sumOf { it.size }
            val availableSpace = totalSpace - usedSpace
            
            val info = CloudStorage.StorageInfo(
                totalSpace = totalSpace,
                usedSpace = usedSpace,
                availableSpace = availableSpace
            )
            
            Logger.d(TAG, "Mock storage info: ${info.usedSpace}/${info.totalSpace} bytes used")
            return@withContext info
        } catch (e: Exception) {
            Logger.e(TAG, "Mock get storage info failed: ${e.message}")
            return@withContext null
        }
    }
}
