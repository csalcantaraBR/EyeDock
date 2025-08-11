package com.eyedock.app.domain.interfaces

import kotlinx.coroutines.flow.Flow

/**
 * Interface for cloud storage services (Google Drive, OneDrive, etc.)
 */
interface CloudStorage {
    
    /**
     * Cloud storage service types
     */
    enum class ServiceType {
        GOOGLE_DRIVE,
        ONEDRIVE,
        SHAREPOINT,
        DROPBOX
    }
    
    /**
     * Upload status
     */
    sealed class UploadStatus {
        object Idle : UploadStatus()
        object Uploading : UploadStatus()
        data class Progress(val percentage: Int) : UploadStatus()
        data class Success(val fileId: String, val downloadUrl: String?) : UploadStatus()
        data class Error(val message: String) : UploadStatus()
    }
    
    /**
     * File metadata for cloud storage
     */
    data class CloudFile(
        val id: String,
        val name: String,
        val size: Long,
        val mimeType: String,
        val createdTime: Long,
        val modifiedTime: Long,
        val downloadUrl: String? = null
    )
    
    /**
     * Initialize the cloud storage service
     */
    suspend fun initialize(): Boolean
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Authenticate user
     */
    suspend fun authenticate(): Boolean
    
    /**
     * Sign out user
     */
    suspend fun signOut()
    
    /**
     * Upload a file to cloud storage
     */
    suspend fun uploadFile(
        localFilePath: String,
        fileName: String,
        mimeType: String = "video/mp4"
    ): Flow<UploadStatus>
    
    /**
     * List files in cloud storage
     */
    suspend fun listFiles(folderId: String? = null): List<CloudFile>
    
    /**
     * Create a folder in cloud storage
     */
    suspend fun createFolder(folderName: String, parentFolderId: String? = null): String?
    
    /**
     * Delete a file from cloud storage
     */
    suspend fun deleteFile(fileId: String): Boolean
    
    /**
     * Get download URL for a file
     */
    suspend fun getDownloadUrl(fileId: String): String?
    
    /**
     * Get available storage space
     */
    suspend fun getStorageInfo(): StorageInfo?
    
    /**
     * Storage information
     */
    data class StorageInfo(
        val totalSpace: Long,
        val usedSpace: Long,
        val availableSpace: Long
    )
}
