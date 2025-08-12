package com.eyedock.storage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StorageManager(private val context: Context) {
    
    private var currentStorageUri: Uri? = null
    private var hasPermission = false
    private var selectedFolderUri: String? = null
    
    fun selectStorageLocation(uri: Uri) {
        currentStorageUri = uri
        hasPermission = true
    }
    
    fun hasValidPermission(): Boolean {
        return hasPermission && currentStorageUri != null
    }
    
    fun getCurrentStorageUri(): Uri? {
        return currentStorageUri
    }
    
    fun simulateAppRestart() {
        // Simular persistência através de restart
        // Em implementação real, isso seria salvo em SharedPreferences
    }
    
    fun handleRevokedPermission() {
        hasPermission = false
        currentStorageUri = null
    }
    
    fun handleRevokedPermission(revokedUri: Uri): PermissionResult {
        hasPermission = false
        currentStorageUri = null
        return PermissionResult(
            canContinue = false,
            userAction = PermissionAction.RE_GRANT
        )
    }
    
    suspend fun selectFolder(folderUri: String): FolderSelectionResult {
        delay(100)
        selectedFolderUri = folderUri
        return FolderSelectionResult.Success(folderUri)
    }
    
    fun getSelectedFolderUri(): String? {
        return selectedFolderUri
    }
    
    suspend fun createFolderStructure(baseUri: String, folderName: String): FolderCreationResult {
        delay(200)
        return FolderCreationResult.Success("$baseUri/$folderName")
    }
    
    suspend fun createDateStructure(baseUri: String, cameraName: String, date: LocalDateTime): Result<String> {
        delay(100)
        val datePath = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return Result.success("$baseUri/$cameraName/$datePath")
    }
    
    suspend fun handleStorageFailure(failedUri: String, fallbackUri: String): Result<String> {
        delay(100)
        return Result.success(fallbackUri)
    }
}

sealed class FolderSelectionResult {
    data class Success(val folderUri: String) : FolderSelectionResult()
    data class Failure(val error: String) : FolderSelectionResult()
    
    val isSuccess: Boolean
        get() = this is Success
        
    val isFailure: Boolean
        get() = this is Failure
        
    fun exceptionOrNull(): Exception? {
        return if (this is Failure) Exception(error) else null
    }
    
    fun getOrNull(): String? {
        return if (this is Success) folderUri else null
    }
}

sealed class FolderCreationResult {
    data class Success(val folderUri: String) : FolderCreationResult()
    data class Failure(val error: String) : FolderCreationResult()
    
    val isSuccess: Boolean
        get() = this is Success
        
    val isFailure: Boolean
        get() = this is Failure
}

data class PermissionResult(
    val canContinue: Boolean,
    val userAction: PermissionAction
)

enum class PermissionAction {
    RE_GRANT,
    SELECT_NEW_LOCATION,
    CANCEL
}

data class VideoMetadata(
    val cameraName: String,
    val timestamp: LocalDateTime,
    val duration: Long,
    val resolution: String,
    val frameRate: Double,
    val bitrate: Long
)

class UriRevokedException(message: String) : Exception(message)
