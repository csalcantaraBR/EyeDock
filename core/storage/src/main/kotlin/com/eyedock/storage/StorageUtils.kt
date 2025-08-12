package com.eyedock.storage

import java.text.SimpleDateFormat
import java.util.*

fun generateFileName(cameraName: String): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
    return "${cameraName}_$timestamp.mp4"
}
