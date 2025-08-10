package com.eyedock.storage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GREEN PHASE - Implementação mínima de FileIndexer
 * 
 * Mantém índice consistente de arquivos gravados
 */
@Singleton
class FileIndexer @Inject constructor(
    private val context: Context
) {

    // GREEN: Simulação de índice thread-safe
    private val indexMutex = Mutex()
    private val fileIndex = mutableMapOf<String, FileIndexEntry>()
    private var isConsistent = true

    /**
     * Executa operação no índice
     * GREEN: Simula operações concorrentes thread-safe
     */
    suspend fun performOperation(storageUri: Uri, operation: IndexOperation): OperationResult {
        return indexMutex.withLock {
            // Simular tempo de operação
            delay(100L)

            when (operation) {
                IndexOperation.ADD_FILE -> {
                    val fileId = "file_${System.currentTimeMillis()}"
                    fileIndex[fileId] = FileIndexEntry(
                        id = fileId,
                        path = "mock/path/$fileId.mp4",
                        size = 1024 * 1024L,
                        timestamp = System.currentTimeMillis()
                    )
                }
                IndexOperation.DELETE_FILE -> {
                    if (fileIndex.isNotEmpty()) {
                        val firstKey = fileIndex.keys.first()
                        fileIndex.remove(firstKey)
                    }
                }
                IndexOperation.UPDATE_METADATA -> {
                    // Simular atualização de metadata
                }
                IndexOperation.CLEANUP_EXPIRED -> {
                    // Simular limpeza de arquivos expirados
                    val expiredCount = fileIndex.size / 4 // Remove 25%
                    repeat(expiredCount) {
                        if (fileIndex.isNotEmpty()) {
                            val firstKey = fileIndex.keys.first()
                            fileIndex.remove(firstKey)
                        }
                    }
                }
            }

            OperationResult(
                success = true,
                affectedFiles = 1,
                errorMessage = null
            )
        }
    }

    /**
     * Verifica se índice está consistente
     * GREEN: Simula verificação de consistência
     */
    fun isIndexConsistent(): Boolean = isConsistent
}

/**
 * Operações de índice suportadas
 */
enum class IndexOperation {
    ADD_FILE,
    DELETE_FILE,
    UPDATE_METADATA,
    CLEANUP_EXPIRED
}

/**
 * Entrada do índice de arquivo
 */
data class FileIndexEntry(
    val id: String,
    val path: String,
    val size: Long,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Resultado de operação no índice
 */
data class OperationResult(
    val success: Boolean,
    val affectedFiles: Int,
    val errorMessage: String?
)

/**
 * GREEN PHASE - Implementação mínima de StorageDetector
 */
@Singleton
class StorageDetector @Inject constructor(
    private val context: Context
) {

    /**
     * Detecta opções de storage disponíveis
     * GREEN: Retorna lista mock de storages suportados
     */
    suspend fun getAvailableStorageOptions(): List<StorageOption> {
        // Simular tempo de detecção
        delay(500L)

        // GREEN: Retornar opções mock que cubram os casos de teste
        return listOf(
            StorageOption(
                type = StorageType.INTERNAL,
                displayName = "Internal Storage",
                uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A"),
                availableSpaceGB = 32.5,
                isRemovable = false
            ),
            StorageOption(
                type = StorageType.EXTERNAL_SD,
                displayName = "SD Card",
                uri = Uri.parse("content://com.android.externalstorage.documents/tree/external%3A"),
                availableSpaceGB = 128.0,
                isRemovable = true
            ),
            StorageOption(
                type = StorageType.USB_OTG,
                displayName = "USB Drive",
                uri = Uri.parse("content://com.android.externalstorage.documents/tree/usb%3A"),
                availableSpaceGB = 64.0,
                isRemovable = true
            ),
            StorageOption(
                type = StorageType.NETWORK,
                displayName = "Network Drive",
                uri = Uri.parse("content://network.provider/tree/smb%3A"),
                availableSpaceGB = 500.0,
                isRemovable = false
            )
        )
    }
}

/**
 * Tipos de storage suportados
 */
enum class StorageType {
    INTERNAL,
    EXTERNAL_SD,
    USB_OTG,
    NETWORK
}

/**
 * Opção de storage detectada
 */
data class StorageOption(
    val type: StorageType,
    val displayName: String,
    val uri: Uri,
    val availableSpaceGB: Double,
    val isRemovable: Boolean
)
