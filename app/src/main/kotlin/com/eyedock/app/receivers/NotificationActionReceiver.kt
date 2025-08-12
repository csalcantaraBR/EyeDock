package com.eyedock.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eyedock.app.services.RecordingService

class NotificationActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.eyedock.ACTION_STOP_RECORDING" -> {
                // Stop recording
                RecordingService.stopService(context)
            }
            "com.eyedock.ACTION_OPEN_CAMERA" -> {
                // Open camera view
                // TODO: Implement camera opening logic
            }
        }
    }
}
