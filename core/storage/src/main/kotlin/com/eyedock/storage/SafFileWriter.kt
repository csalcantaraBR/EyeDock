package com.eyedock.storage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GREEN PHASE - Implementação mínima de SafFileWriter
 * 
 * Escreve arquivos de vídeo e metadata usando Storage Access Framework
 */
@Singleton
class SafFileWriter @Inject constructor(
    private val context: Context
) {

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private val TIME_FORMAT = SimpleDateFormat("HHmmss", Locale.US)
    }

    /**
     * Escreve segmento de vídeo com metadata
     * GREEN: Simula escrita e retorna paths estruturados
     */
    suspend fun writeSegment(
        storageUri: Uri,
        videoData: ByteArray,
        metadata: RecordingMetadata
    ): WriteResult {
        // Simular tempo de escrita
        delay(500L)

        // GREEN: Gerar path estruturado baseado no metadata
        val date = Date(metadata.timestamp)
        val dateStr = DATE_FORMAT.format(date)
        val timeStr = TIME_FORMAT.format(date)
        
        val videoPath = "${metadata.cameraName}/$dateStr/$timeStr.mp4"
        val metadataPath = "${metadata.cameraName}/$dateStr/$timeStr.json"

        // GREEN: Simular escrita bem-sucedida
        return WriteResult(
            success = true,
            filePath = videoPath,
            metadataPath = metadataPath,
            bytesWritten = videoData.size.toLong(),
            errorMessage = null
        )
    }
}

/**
 * Metadata de uma gravação
 */
data class RecordingMetadata(
    val cameraName: String,
    val timestamp: Long,
    val duration: Long, // milliseconds
    val resolution: String,
    val codec: String,
    val fileSize: Long = 0L,
    val hasAudio: Boolean = false
)

/**
 * Resultado de operação de escrita
 */
data class WriteResult(
    val success: Boolean,
    val filePath: String,
    val metadataPath: String?,
    val bytesWritten: Long,
    val errorMessage: String?
)
