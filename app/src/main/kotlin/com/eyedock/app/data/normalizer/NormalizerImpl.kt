package com.eyedock.app.data.normalizer

import com.eyedock.app.domain.interfaces.Normalizer
import com.eyedock.app.domain.interfaces.OnvifClient
import com.eyedock.app.domain.model.*
import org.json.JSONObject
import java.net.URI

class NormalizerImpl(
    private val onvifClient: OnvifClient
) : Normalizer {
    
    override suspend fun normalize(payload: QrPayload): CameraConnection {
        return when (payload) {
            is QrPayload.Rtsp -> normalizeRtspUrl(payload.url)
            is QrPayload.Json -> normalizeJson(payload.json)
            is QrPayload.Uid -> normalizeUid(payload.uid)
        }
    }
    
    private fun normalizeRtspUrl(url: String): CameraConnection {
        val uri = URI(url)
        val userInfo = uri.userInfo?.split(":") ?: emptyList()
        
        return CameraConnection(
            proto = uri.scheme,
            ip = uri.host,
            port = if (uri.port != -1) uri.port else getDefaultPort(uri.scheme),
            user = userInfo.getOrNull(0),
            pass = userInfo.getOrNull(1),
            path = uri.path.ifEmpty { null },
            meta = mapOf("source" to "qr-rtsp")
        )
    }
    
    private fun normalizeJson(json: String): CameraConnection {
        val jsonObj = JSONObject(json)
        
        return CameraConnection(
            proto = jsonObj.optString("proto").takeIf { it.isNotEmpty() },
            ip = jsonObj.optString("ip").takeIf { it.isNotEmpty() },
            port = jsonObj.optInt("port").takeIf { it > 0 },
            user = jsonObj.optString("user").takeIf { it.isNotEmpty() },
            pass = jsonObj.optString("pass").takeIf { it.isNotEmpty() },
            path = jsonObj.optString("path").takeIf { it.isNotEmpty() },
            ptz = jsonObj.optBoolean("ptz"),
            onvifDeviceService = jsonObj.optString("onvif_service").takeIf { it.isNotEmpty() },
            uid = jsonObj.optString("uid").takeIf { it.isNotEmpty() },
            meta = mapOf("source" to "qr-json")
        )
    }
    
    private suspend fun normalizeUid(uid: String): CameraConnection {
        // Executar WS-Discovery para encontrar dispositivos
        val endpoints = onvifClient.probe(2500)
        
        // Procurar por dispositivo com UID correspondente
        for (endpoint in endpoints) {
            try {
                val deviceInfo = onvifClient.getDeviceInformation(endpoint, null)
                if (deviceInfo.serialNumber == uid || deviceInfo.hardwareId == uid) {
                    return CameraConnection(
                        proto = "rtsp",
                        ip = endpoint.ip,
                        port = endpoint.port,
                        onvifDeviceService = endpoint.deviceService,
                        uid = uid,
                        meta = mapOf("source" to "qr-uid")
                    )
                }
            } catch (e: Exception) {
                // Continuar para o próximo endpoint
            }
        }
        
        // Se não encontrou, retornar conexão básica com UID
        return CameraConnection(
            uid = uid,
            meta = mapOf("source" to "qr-uid", "discovery_failed" to "true")
        )
    }
    
    private fun getDefaultPort(scheme: String?): Int {
        return when (scheme?.lowercase()) {
            "rtsp" -> 554
            "rtsps" -> 322
            else -> 554
        }
    }
}
