package com.eyedock.storage

import android.content.Context
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.mock

class SimpleConstructorTest {
    
    @Test
    fun `test StorageManager constructor`() {
        val context = mock<Context>()
        val storageManager = StorageManager(context)
        assertNotNull(storageManager)
    }
    
    @Test
    fun `test SafFileWriter constructor`() {
        val context = mock<Context>()
        val fileWriter = SafFileWriter(context)
        assertNotNull(fileWriter)
    }
    
    @Test
    fun `test RetentionManager constructor`() {
        val context = mock<Context>()
        val retentionManager = RetentionManager(context)
        assertNotNull(retentionManager)
    }
    
    @Test
    fun `test ShareManager constructor`() {
        val context = mock<Context>()
        val shareManager = ShareManager(context)
        assertNotNull(shareManager)
    }
    
    @Test
    fun `test FileIndexer constructor`() {
        val context = mock<Context>()
        val fileIndexer = FileIndexer(context)
        assertNotNull(fileIndexer)
    }
    
    @Test
    fun `test StorageDetector constructor`() {
        val context = mock<Context>()
        val storageDetector = StorageDetector(context)
        assertNotNull(storageDetector)
    }
}
