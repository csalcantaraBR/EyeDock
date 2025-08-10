package com.eyedock.events

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GREEN PHASE - Implementação mínima de NotificationManager
 * 
 * Gerencia notificações push para eventos de câmeras
 */
@Singleton
class NotificationManager @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "eyedock_events"
        private const val CHANNEL_NAME = "Camera Events"
        private const val MOTION_NOTIFICATION_ID = 1001
    }

    /**
     * Envia alerta de movimento
     * GREEN: Simula envio de notificação
     */
    suspend fun sendMotionAlert(motionEvent: MotionEvent): NotificationResult {
        // Simular tempo de processamento
        delay(100L)
        
        // GREEN: Simular notificação bem-sucedida
        val notificationId = "${MOTION_NOTIFICATION_ID}_${motionEvent.timestamp}"
        val title = "Motion detected in ${motionEvent.cameraId}"
        val message = "Confidence: ${(motionEvent.confidence * 100).toInt()}%"
        
        // Em implementação real, criaria notificação Android real
        // createNotificationChannel()
        // val notification = buildMotionNotification(title, message, motionEvent)
        // notificationManager.notify(notificationId.hashCode(), notification)
        
        return NotificationResult(
            wasSent = true,
            notificationId = notificationId,
            title = title,
            message = message
        )
    }

    /**
     * Envia alerta de som
     * GREEN: Simula envio de notificação de som
     */
    suspend fun sendSoundAlert(soundEvent: SoundEvent): NotificationResult {
        delay(100L)
        
        val notificationId = "${MOTION_NOTIFICATION_ID + 1}_${soundEvent.timestamp}"
        val title = "Sound detected in ${soundEvent.cameraId}"
        val message = "Volume: ${(soundEvent.volume * 100).toInt()}%"
        
        return NotificationResult(
            wasSent = true,
            notificationId = notificationId,
            title = title,
            message = message
        )
    }

    /**
     * Cria canal de notificação (implementação real)
     */
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for camera events"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Constrói notificação de movimento (implementação real)
     */
    private fun buildMotionNotification(
        title: String,
        message: String,
        motionEvent: MotionEvent
    ): android.app.Notification {
        val intent = createDeepLinkIntent(motionEvent)
        val pendingIntent = PendingIntent.getActivity(
            context,
            motionEvent.timestamp.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_media_play,
                "View Recording",
                pendingIntent
            )
            .build()
    }

    /**
     * Cria intent para deep link
     */
    private fun createDeepLinkIntent(motionEvent: MotionEvent): Intent {
        return Intent().apply {
            // Em implementação real, seria Intent para MainActivity
            // action = "com.eyedock.OPEN_TIMELINE"
            putExtra("camera_id", motionEvent.cameraId)
            putExtra("timestamp", motionEvent.timestamp)
            putExtra("event_type", "motion")
        }
    }
}

/**
 * GREEN PHASE - Implementação mínima de DeepLinkHandler
 */
@Singleton
class DeepLinkHandler @Inject constructor() {

    /**
     * Trata tap em notificação
     * GREEN: Simula navegação para timeline
     */
    suspend fun handleNotificationTap(intent: NotificationIntent): NavigationResult {
        delay(50L) // Simular processamento
        
        // GREEN: Simular navegação bem-sucedida
        return NavigationResult(
            isSuccess = true,
            destination = "timeline",
            cameraId = intent.cameraId,
            seekToTimestamp = intent.timestamp,
            eventType = intent.eventType
        )
    }
}

/**
 * Resultado de notificação
 */
data class NotificationResult(
    val wasSent: Boolean,
    val notificationId: String,
    val title: String,
    val message: String
)

/**
 * Intent de notificação
 */
data class NotificationIntent(
    val cameraId: String,
    val timestamp: Long,
    val eventType: String
)

/**
 * Resultado de navegação
 */
data class NavigationResult(
    val isSuccess: Boolean,
    val destination: String,
    val cameraId: String,
    val seekToTimestamp: Long,
    val eventType: String
)
