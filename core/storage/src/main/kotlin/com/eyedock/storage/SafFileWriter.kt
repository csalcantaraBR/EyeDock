package com.eyedock.storage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import com.eyedock.storage.UriRevokedException

class SafFileWriter(private val context: Context) {
    
    suspend fun writeSegment(
        baseUri: Uri,
        videoData: ByteArray,
        metadata: RecordingMetadata
    ): WriteResult {
        delay(1000) // Simular tempo de escrita
        
        val timestamp = Date(metadata.timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeFormat = SimpleDateFormat("HHmmss", Locale.US)
        
        val dateFolder = dateFormat.format(timestamp)
        val timeFile = timeFormat.format(timestamp)
        
        val fileName = "${metadata.cameraName}/${dateFolder}/${timeFile}.mp4"
        val metadataName = "${metadata.cameraName}/${dateFolder}/${timeFile}.json"
        
        return WriteResult(
            success = true,
            filePath = fileName,
            metadataPath = metadataName,
            bytesWritten = videoData.size.toLong()
        )
    }
    
    suspend fun writeFile(folderUri: String, fileName: String, data: ByteArray): Result<Unit> {
        delay(500) // Simular tempo de escrita
        return if (folderUri.contains("revoked")) {
            Result.failure(UriRevokedException("URI foi revogada"))
        } else {
            Result.success(Unit)
        }
    }
    
    suspend fun readFile(folderUri: String, fileName: String): ReadResult {
        delay(300) // Simular tempo de leitura
        return ReadResult.Success("Test video content".toByteArray())
    }
    
    suspend fun writeSegment(folderUri: String, fileName: String, segmentData: ByteArray): WriteResult {
        delay(200) // Simular tempo de escrita de segmento
        return WriteResult(
            success = true,
            filePath = "$folderUri/$fileName",
            metadataPath = null,
            bytesWritten = segmentData.size.toLong()
        )
    }
    
    suspend fun writeMetadata(folderUri: String, fileName: String, metadata: VideoMetadata): Result<Unit> {
        delay(50)
        return Result.success(Unit)
    }
    
    suspend fun recoverInterruptedWrite(folderUri: String, fileName: String, partialData: ByteArray): Result<Unit> {
        delay(100)
        return Result.success(Unit)
    }
}

sealed class ReadResult {
    data class Success(val data: ByteArray) : ReadResult()
    data class Failure(val error: String) : ReadResult()
    
    val isSuccess: Boolean
        get() = this is Success
        
    val isFailure: Boolean
        get() = this is Failure
        
    fun getOrNull(): ByteArray? {
        return if (this is Success) data else null
    }
}

data class RecordingMetadata(
    val cameraName: String,
    val timestamp: Long,
    val duration: Long,
    val resolution: String,
    val codec: String
)

data class WriteResult(
    val success: Boolean,
    val filePath: String,
    val metadataPath: String?,
    val bytesWritten: Long
) {
    val isSuccess: Boolean
        get() = success
}
