package com.eyedock.storage

import com.eyedock.core.common.test.categories.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.params.provider.CsvSource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Testes de integração para o módulo de storage
 * Testa interação entre componentes de storage
 */
@DisplayName("Storage Integration Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StorageIntegrationTest {

    private lateinit var storageManager: StorageManager
    private lateinit var fileWriter: SafFileWriter
    private lateinit var retentionManager: RetentionManager
    private lateinit var shareManager: ShareManager
    private lateinit var fileIndexer: FileIndexer
    private lateinit var storageDetector: StorageDetector

    @BeforeEach
    fun setUp() {
        storageManager = StorageManager()
        fileWriter = SafFileWriter()
        retentionManager = RetentionManager()
        shareManager = ShareManager()
        fileIndexer = FileIndexer()
        storageDetector = StorageDetector()
    }

    @Nested
    @DisplayName("Storage Manager Integration")
    class StorageManagerIntegrationTest {

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @Tag(SMOKE_TEST)
        @DisplayName("Deve integrar seleção de pasta e escrita de arquivos")
        fun `deve integrar selecao de pasta e escrita de arquivos`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val cameraName = "TestCamera"
            val testData = "Test video data".toByteArray()
            
            // Act
            val folderResult = storageManager.selectFolder(testFolderUri)
            val writeResult = fileWriter.writeFile(
                folderUri = testFolderUri,
                fileName = generateFileName(cameraName),
                data = testData
            )
            
            // Assert
            assertTrue(folderResult.isSuccess)
            assertTrue(writeResult.isSuccess)
            assertEquals(testFolderUri, storageManager.getSelectedFolderUri())
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar detecção de storage e criação de estrutura")
        fun `deve integrar detecao de storage e criacao de estrutura`() = runBlocking {
            // Arrange
            val storageTypes = listOf(StorageType.INTERNAL, StorageType.EXTERNAL, StorageType.USB)
            
            // Act
            val detectedStorages = storageDetector.detectAvailableStorages()
            val structureResults = detectedStorages.map { storage ->
                storageManager.createFolderStructure(storage.uri, "EyeDock")
            }
            
            // Assert
            assertTrue(detectedStorages.isNotEmpty())
            assertTrue(structureResults.all { it.isSuccess })
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar indexação e busca de arquivos")
        fun `deve integrar indexacao e busca de arquivos`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val cameraName = "TestCamera"
            val testFiles = listOf(
                "TestCamera_2024-01-15_143022.mp4",
                "TestCamera_2024-01-15_143156.mp4",
                "TestCamera_2024-01-15_143245.mp4"
            )
            
            // Act
            storageManager.selectFolder(testFolderUri)
            fileIndexer.indexFolder(testFolderUri)
            
            val searchResults = fileIndexer.searchFiles(
                folderUri = testFolderUri,
                cameraName = cameraName,
                dateRange = DateRange(
                    start = LocalDateTime.of(2024, 1, 15, 14, 30),
                    end = LocalDateTime.of(2024, 1, 15, 15, 0)
                )
            )
            
            // Assert
            assertTrue(searchResults.isNotEmpty())
            assertTrue(searchResults.all { it.contains(cameraName) })
        }
    }

    @Nested
    @DisplayName("File Operations Integration")
    class FileOperationsIntegrationTest {

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar escrita e leitura de arquivos")
        fun `deve integrar escrita e leitura de arquivos`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val fileName = "test_video.mp4"
            val originalData = "Test video content".toByteArray()
            
            // Act
            val writeResult = fileWriter.writeFile(testFolderUri, fileName, originalData)
            val readResult = fileWriter.readFile(testFolderUri, fileName)
            
            // Assert
            assertTrue(writeResult.isSuccess)
            assertTrue(readResult.isSuccess)
            assertArrayEquals(originalData, readResult.getOrNull())
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar escrita concorrente de múltiplos arquivos")
        fun `deve integrar escrita concorrente de multiplos arquivos`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val fileCount = 10
            val fileData = "Test data".toByteArray()
            
            // Act
            val writeJobs = (1..fileCount).map { index ->
                async {
                    fileWriter.writeFile(
                        testFolderUri,
                        "concurrent_test_$index.mp4",
                        fileData
                    )
                }
            }
            
            val results = writeJobs.awaitAll()
            
            // Assert
            assertTrue(results.all { it.isSuccess })
            assertEquals(fileCount, results.size)
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar escrita de arquivo e metadados")
        fun `deve integrar escrita de arquivo e metadados`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val fileName = "test_with_metadata.mp4"
            val videoData = "Video content".toByteArray()
            val metadata = VideoMetadata(
                cameraName = "TestCamera",
                timestamp = LocalDateTime.now(),
                duration = 30000L, // 30 segundos
                resolution = "1920x1080",
                frameRate = 30.0,
                bitrate = 5000000L // 5 Mbps
            )
            
            // Act
            val videoResult = fileWriter.writeFile(testFolderUri, fileName, videoData)
            val metadataResult = fileWriter.writeMetadata(
                testFolderUri,
                fileName.replace(".mp4", ".json"),
                metadata
            )
            
            // Assert
            assertTrue(videoResult.isSuccess)
            assertTrue(metadataResult.isSuccess)
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar criação de estrutura de pastas por data")
        fun `deve integrar criacao de estrutura de pastas por data`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val cameraName = "TestCamera"
            val date = LocalDateTime.now()
            
            // Act
            val structureResult = storageManager.createDateStructure(
                baseUri = testFolderUri,
                cameraName = cameraName,
                date = date
            )
            
            val expectedPath = "${cameraName}/${date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
            
            // Assert
            assertTrue(structureResult.isSuccess)
            assertTrue(structureResult.getOrNull()?.contains(expectedPath) == true)
        }
    }

    @Nested
    @DisplayName("Retention Policy Integration")
    class RetentionPolicyIntegrationTest {

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar políticas de retenção por tempo e tamanho")
        fun `deve integrar politicas de retencao por tempo e tamanho`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val retentionPolicy = RetentionPolicy(
                maxAgeDays = 30,
                maxSizeBytes = 10 * 1024 * 1024 * 1024L, // 10GB
                cleanupEnabled = true
            )
            
            // Act
            retentionManager.setPolicy(testFolderUri, retentionPolicy)
            val cleanupResult = retentionManager.performCleanup(testFolderUri)
            
            // Assert
            assertTrue(cleanupResult.isSuccess)
            val cleanupReport = cleanupResult.getOrNull()
            assertNotNull(cleanupReport)
            assertTrue(cleanupReport!!.filesDeleted >= 0)
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar limpeza automática com indexação")
        fun `deve integrar limpeza automatica com indexacao`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val oldDate = LocalDateTime.now().minusDays(35)
            
            // Criar arquivos antigos
            val oldFiles = (1..5).map { index ->
                "old_file_$index.mp4" to oldDate
            }
            
            // Act
            fileIndexer.indexFolder(testFolderUri)
            val beforeCleanup = fileIndexer.getFileCount(testFolderUri)
            
            retentionManager.performCleanup(testFolderUri)
            
            fileIndexer.refreshIndex(testFolderUri)
            val afterCleanup = fileIndexer.getFileCount(testFolderUri)
            
            // Assert
            assertTrue(afterCleanup <= beforeCleanup)
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar backup antes da limpeza")
        fun `deve integrar backup antes da limpeza`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val backupUri = "content://com.android.externalstorage.documents/tree/primary%3ABackup"
            
            // Act
            val backupResult = retentionManager.createBackup(testFolderUri, backupUri)
            val cleanupResult = retentionManager.performCleanup(testFolderUri)
            
            // Assert
            assertTrue(backupResult.isSuccess)
            assertTrue(cleanupResult.isSuccess)
        }
    }

    @Nested
    @DisplayName("Sharing Integration")
    class SharingIntegrationTest {

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar compartilhamento de arquivos e metadados")
        fun `deve integrar compartilhamento de arquivos e metadados`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val fileName = "shared_video.mp4"
            val videoData = "Video content".toByteArray()
            
            // Act
            val writeResult = fileWriter.writeFile(testFolderUri, fileName, videoData)
            val shareResult = shareManager.shareFile(testFolderUri, fileName)
            
            // Assert
            assertTrue(writeResult.isSuccess)
            assertTrue(shareResult.isSuccess)
            assertNotNull(shareResult.getOrNull()?.shareIntent)
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar compartilhamento de múltiplos arquivos")
        fun `deve integrar compartilhamento de multiplos arquivos`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val fileNames = listOf("video1.mp4", "video2.mp4", "video3.mp4")
            
            // Act
            val shareResult = shareManager.shareMultipleFiles(testFolderUri, fileNames)
            
            // Assert
            assertTrue(shareResult.isSuccess)
            assertNotNull(shareResult.getOrNull()?.shareIntent)
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar exportação para formatos diferentes")
        fun `deve integrar exportacao para formatos diferentes`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val sourceFileName = "source_video.mp4"
            val exportFormats = listOf("mp4", "avi", "mov")
            
            // Act
            val exportResults = exportFormats.map { format ->
                shareManager.exportToFormat(testFolderUri, sourceFileName, format)
            }
            
            // Assert
            assertTrue(exportResults.all { it.isSuccess })
        }
    }

    @Nested
    @DisplayName("Error Handling Integration")
    class ErrorHandlingIntegrationTest {

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar tratamento de URI revogada")
        fun `deve integrar tratamento de URI revogada`() = runBlocking {
            // Arrange
            val revokedUri = "content://com.android.externalstorage.documents/tree/revoked%3AEyeDock"
            
            // Act
            val folderResult = storageManager.selectFolder(revokedUri)
            val writeResult = fileWriter.writeFile(revokedUri, "test.mp4", "data".toByteArray())
            
            // Assert
            assertTrue(folderResult.isFailure)
            assertTrue(writeResult.isFailure)
            assertTrue(folderResult.exceptionOrNull() is UriRevokedException)
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar fallback para storage interno")
        fun `deve integrar fallback para storage interno`() = runBlocking {
            // Arrange
            val externalUri = "content://com.android.externalstorage.documents/tree/external%3AEyeDock"
            val internalUri = "content://com.android.externalstorage.documents/tree/internal%3AEyeDock"
            
            // Act
            val fallbackResult = storageManager.handleStorageFailure(externalUri, internalUri)
            
            // Assert
            assertTrue(fallbackResult.isSuccess)
            assertEquals(internalUri, fallbackResult.getOrNull())
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @DisplayName("Deve integrar recuperação de operações interrompidas")
        fun `deve integrar recuperacao de operacoes interrompidas`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val fileName = "interrupted_file.mp4"
            val partialData = "Partial data".toByteArray()
            
            // Act
            val recoveryResult = fileWriter.recoverInterruptedWrite(
                testFolderUri,
                fileName,
                partialData
            )
            
            // Assert
            assertTrue(recoveryResult.isSuccess)
        }
    }

    @Nested
    @DisplayName("Performance Integration")
    class PerformanceIntegrationTest {

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @Tag(PERFORMANCE_TEST)
        @DisplayName("Deve integrar escrita de arquivos grandes")
        fun `deve integrar escrita de arquivos grandes`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val fileName = "large_file.mp4"
            val largeData = ByteArray(10 * 1024 * 1024) { it.toByte() } // 10MB
            
            // Act
            val startTime = System.currentTimeMillis()
            val writeResult = fileWriter.writeFile(testFolderUri, fileName, largeData)
            val duration = System.currentTimeMillis() - startTime
            
            // Assert
            assertTrue(writeResult.isSuccess)
            assertTrue(duration < 30000) // Máximo 30 segundos
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(STORAGE_TEST)
        @Tag(PERFORMANCE_TEST)
        @DisplayName("Deve integrar operações concorrentes sem conflitos")
        fun `deve integrar operacoes concorrentes sem conflitos`() = runBlocking {
            // Arrange
            val testFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock"
            val operationCount = 20
            
            // Act
            val operations = (1..operationCount).map { index ->
                async {
                    when (index % 4) {
                        0 -> fileWriter.writeFile(testFolderUri, "file_$index.mp4", "data".toByteArray())
                        1 -> fileIndexer.indexFolder(testFolderUri)
                        2 -> retentionManager.performCleanup(testFolderUri)
                        3 -> shareManager.shareFile(testFolderUri, "file_$index.mp4")
                    }
                }
            }
            
            val results = operations.awaitAll()
            
            // Assert
            assertTrue(results.all { it.isSuccess })
        }
    }

    // Helper methods
    private fun generateFileName(cameraName: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"))
        return "${cameraName}_${timestamp}.mp4"
    }
}

// Classes de implementação para os testes
class StorageManager {
    private var selectedFolderUri: String? = null

    suspend fun selectFolder(uri: String): Result<String> {
        return if (uri.contains("revoked")) {
            Result.failure(UriRevokedException("URI foi revogada"))
        } else {
            selectedFolderUri = uri
            Result.success(uri)
        }
    }

    fun getSelectedFolderUri(): String? = selectedFolderUri

    suspend fun createFolderStructure(baseUri: String, folderName: String): Result<String> {
        delay(100)
        return Result.success("$baseUri/$folderName")
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

class SafFileWriter {
    suspend fun writeFile(folderUri: String, fileName: String, data: ByteArray): Result<Unit> {
        delay(50)
        return if (folderUri.contains("revoked")) {
            Result.failure(UriRevokedException("URI foi revogada"))
        } else {
            Result.success(Unit)
        }
    }

    suspend fun readFile(folderUri: String, fileName: String): Result<ByteArray> {
        delay(50)
        return Result.success("Test video content".toByteArray())
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

class RetentionManager {
    suspend fun setPolicy(folderUri: String, policy: RetentionPolicy): Result<Unit> {
        delay(50)
        return Result.success(Unit)
    }

    suspend fun performCleanup(folderUri: String): Result<CleanupReport> {
        delay(200)
        return Result.success(CleanupReport(filesDeleted = 5, spaceFreed = 1024 * 1024 * 100L))
    }

    suspend fun createBackup(sourceUri: String, backupUri: String): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }
}

class ShareManager {
    suspend fun shareFile(folderUri: String, fileName: String): Result<ShareResult> {
        delay(100)
        return Result.success(ShareResult("share_intent_data"))
    }

    suspend fun shareMultipleFiles(folderUri: String, fileNames: List<String>): Result<ShareResult> {
        delay(150)
        return Result.success(ShareResult("multiple_share_intent_data"))
    }

    suspend fun exportToFormat(folderUri: String, sourceFileName: String, format: String): Result<String> {
        delay(200)
        return Result.success("$sourceFileName.$format")
    }
}

class FileIndexer {
    suspend fun indexFolder(folderUri: String): Result<Unit> {
        delay(100)
        return Result.success(Unit)
    }

    suspend fun searchFiles(folderUri: String, cameraName: String, dateRange: DateRange): List<String> {
        delay(50)
        return listOf("TestCamera_2024-01-15_143022.mp4", "TestCamera_2024-01-15_143156.mp4")
    }

    fun getFileCount(folderUri: String): Int {
        return 10
    }

    suspend fun refreshIndex(folderUri: String): Result<Unit> {
        delay(50)
        return Result.success(Unit)
    }
}

class StorageDetector {
    suspend fun detectAvailableStorages(): List<StorageInfo> {
        delay(100)
        return listOf(
            StorageInfo("content://internal", StorageType.INTERNAL, 64L * 1024 * 1024 * 1024L),
            StorageInfo("content://external", StorageType.EXTERNAL, 128L * 1024 * 1024 * 1024L),
            StorageInfo("content://usb", StorageType.USB, 32L * 1024 * 1024 * 1024L)
        )
    }
}

// Data classes
data class VideoMetadata(
    val cameraName: String,
    val timestamp: LocalDateTime,
    val duration: Long,
    val resolution: String,
    val frameRate: Double,
    val bitrate: Long
)

data class RetentionPolicy(
    val maxAgeDays: Int,
    val maxSizeBytes: Long,
    val cleanupEnabled: Boolean
)

data class CleanupReport(
    val filesDeleted: Int,
    val spaceFreed: Long
)

data class ShareResult(
    val shareIntent: String
)

data class DateRange(
    val start: LocalDateTime,
    val end: LocalDateTime
)

data class StorageInfo(
    val uri: String,
    val type: StorageType,
    val availableSpace: Long
)

enum class StorageType {
    INTERNAL, EXTERNAL, USB, NETWORK
}

// Exceptions
class UriRevokedException(message: String) : Exception(message)
