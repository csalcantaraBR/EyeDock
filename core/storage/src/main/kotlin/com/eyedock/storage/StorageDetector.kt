package com.eyedock.storage

import android.content.Context
import kotlinx.coroutines.delay

class StorageDetector(private val context: Context) {
    
    suspend fun detectAvailableStorages(): List<StorageInfo> {
        delay(100)
        return listOf(
            StorageInfo(
                uri = "content://com.android.externalstorage.documents/tree/primary%3AEyeDock",
                type = StorageType.INTERNAL,
                name = "Internal Storage",
                isWritable = true,
                freeSpaceBytes = 1024L * 1024L * 1024L * 10L // 10GB
            ),
            StorageInfo(
                uri = "content://com.android.externalstorage.documents/tree/0000-0000%3AEyeDock",
                type = StorageType.EXTERNAL_SD,
                name = "External SD",
                isWritable = true,
                freeSpaceBytes = 1024L * 1024L * 1024L * 32L // 32GB
            )
        )
    }
    
    fun getAvailableStorageOptions(): List<StorageType> {
        return listOf(StorageType.INTERNAL, StorageType.EXTERNAL_SD, StorageType.USB, StorageType.USB_OTG, StorageType.NETWORK)
    }
}

data class StorageInfo(
    val uri: String,
    val type: StorageType,
    val name: String,
    val isWritable: Boolean,
    val freeSpaceBytes: Long
)

enum class StorageType {
    INTERNAL,
    EXTERNAL,
    EXTERNAL_SD,
    USB,
    USB_OTG,
    NETWORK
}
