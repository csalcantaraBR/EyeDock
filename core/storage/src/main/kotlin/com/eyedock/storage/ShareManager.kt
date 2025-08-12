package com.eyedock.storage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay

class ShareManager(private val context: Context) {
    
    suspend fun shareRecording(
        filePath: String,
        options: ShareOptions
    ): ShareResult {
        delay(500) // Simular tempo de compartilhamento
        return ShareResult.Success("shared_$filePath")
    }
    
    suspend fun shareRecording(
        videoUri: Uri,
        options: ShareOptions
    ): ShareResult {
        delay(500) // Simular tempo de compartilhamento
        return ShareResult.Success("shared_${videoUri.lastPathSegment}")
    }
    
    suspend fun shareFile(folderUri: String, fileName: String): Result<ShareResult> {
        delay(100)
        return Result.success(ShareResult.Success("share_intent_data"))
    }
    
    suspend fun shareMultipleFiles(folderUri: String, fileNames: List<String>): Result<ShareResult> {
        delay(150)
        return Result.success(ShareResult.Success("multiple_share_intent_data"))
    }
    
    suspend fun exportToFormat(folderUri: String, sourceFileName: String, format: String): Result<String> {
        delay(200)
        return Result.success("$sourceFileName.$format")
    }
}

data class ShareOptions(
    val method: ShareMethod,
    val title: String,
    val description: String
) {
    constructor(includeMetadata: Boolean, compressForSharing: Boolean, shareMethod: ShareMethod) : this(
        method = shareMethod,
        title = "Share Recording",
        description = "Shared from EyeDock"
    )
}

enum class ShareMethod {
    EMAIL,
    WHATSAPP,
    TELEGRAM,
    COPY_LINK,
    SYSTEM_SHARE_SHEET
}

sealed class ShareResult {
    data class Success(val shareId: String) : ShareResult()
    data class Failure(val error: String) : ShareResult()
    
    val wasShared: Boolean
        get() = this is Success
        
    val shareIntent: String?
        get() = if (this is Success) "Intent for $shareId" else null
}
