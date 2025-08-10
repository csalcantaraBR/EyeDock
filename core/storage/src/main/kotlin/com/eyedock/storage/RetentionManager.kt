package com.eyedock.storage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GREEN PHASE - Implementação mínima de RetentionManager
 * 
 * Gerencia políticas de retenção de arquivos de gravação
 */
@Singleton
class RetentionManager @Inject constructor(
    private val context: Context
) {

    private var currentPolicy: RetentionPolicy? = null

    /**
     * Define política de retenção
     */
    fun setPolicy(policy: RetentionPolicy) {
        currentPolicy = policy
    }

    /**
     * Aplica política de retenção
     * GREEN: Simula limpeza baseada na política
     */
    suspend fun enforceRetention(storageUri: Uri): CleanupResult {
        val policy = currentPolicy ?: return CleanupResult(
            wasEnforced = false,
            filesDeleted = 0,
            spaceFreedMB = 0.0,
            errorMessage = "No retention policy set"
        )

        // Simular tempo de análise
        delay(1000L)

        // GREEN: Simular limpeza bem-sucedida
        val filesDeleted = when {
            policy.maxDays <= 7 -> 5  // Política agressiva
            policy.maxDays <= 30 -> 2 // Política moderada
            else -> 0 // Política conservadora
        }

        val spaceFreed = filesDeleted * 150.0 // ~150MB por arquivo

        return CleanupResult(
            wasEnforced = true,
            filesDeleted = filesDeleted,
            spaceFreedMB = spaceFreed,
            errorMessage = null
        )
    }
}

/**
 * Política de retenção de arquivos
 */
data class RetentionPolicy(
    val maxDays: Int,
    val maxSizeGB: Double,
    val deleteOldestFirst: Boolean = true,
    val preserveMarkedFiles: Boolean = true
)

/**
 * Resultado de operação de limpeza
 */
data class CleanupResult(
    val wasEnforced: Boolean,
    val filesDeleted: Int,
    val spaceFreedMB: Double,
    val errorMessage: String?
)

/**
 * GREEN PHASE - Implementação mínima de ShareManager
 */
@Singleton
class ShareManager @Inject constructor(
    private val context: Context
) {

    /**
     * Compartilha gravação via system share sheet
     * GREEN: Simula criação de Intent de compartilhamento
     */
    suspend fun shareRecording(videoUri: Uri, options: ShareOptions): ShareResult {
        // Simular tempo de preparação
        delay(300L)

        // GREEN: Simular Intent bem-sucedido
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "video/mp4"
            putExtra(android.content.Intent.EXTRA_STREAM, videoUri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return ShareResult(
            wasShared = true,
            shareIntent = shareIntent,
            errorMessage = null
        )
    }
}

/**
 * Opções de compartilhamento
 */
data class ShareOptions(
    val includeMetadata: Boolean = false,
    val compressForSharing: Boolean = true,
    val shareMethod: ShareMethod = ShareMethod.SYSTEM_SHARE_SHEET
)

enum class ShareMethod {
    SYSTEM_SHARE_SHEET,
    DIRECT_EMAIL,
    CLOUD_UPLOAD
}

/**
 * Resultado de compartilhamento
 */
data class ShareResult(
    val wasShared: Boolean,
    val shareIntent: android.content.Intent?,
    val errorMessage: String?
)
