package com.eyedock.app.data.qr

import com.eyedock.app.domain.interfaces.QrParser
import com.eyedock.app.domain.model.QrPayload
import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.model.OnvifInfo
import com.eyedock.app.domain.model.CameraMeta
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrParserImpl @Inject constructor() : QrParser {
    
    private val gson = Gson()
    
    override suspend fun parseQrCode(qrData: String): QrPayload? {
        return detectPayloadType(qrData)
    }
    
    override suspend fun normalizePayload(payload: QrPayload): CameraConnection? {
        return when (payload) {
            is QrPayload.Rtsp -> normalizeRtspPayload(payload)
            is QrPayload.Json -> normalizeJsonPayload(payload)
            is QrPayload.Uid -> normalizeUidPayload(payload)
            is QrPayload.Text -> normalizeTextPayload(payload)
        }
    }
    
    override fun detectPayloadType(qrData: String): QrPayload? {
        return when {
            isRtspUrl(qrData) -> QrPayload.Rtsp(qrData)
            isJsonPayload(qrData) -> QrPayload.Json(qrData)
            isUidPayload(qrData) -> QrPayload.Uid(qrData)
            else -> QrPayload.Text(qrData)
        }
    }
    
    override fun isValidPayload(payload: QrPayload): Boolean {
        return when (payload) {
            is QrPayload.Rtsp -> isValidRtspUrl(payload.url)
            is QrPayload.Json -> isValidJsonPayload(payload.json)
            is QrPayload.Uid -> isValidUid(payload.uid)
            is QrPayload.Text -> payload.text.isNotBlank()
        }
    }
    
    private fun isRtspUrl(data: String): Boolean {
        return data.startsWith("rtsp://", ignoreCase = true)
    }
    
    private fun isJsonPayload(data: String): Boolean {
        return data.trim().startsWith("{") && data.trim().endsWith("}")
    }
    
    private fun isUidPayload(data: String): Boolean {
        // Padrão comum para UIDs de câmeras (ex: G123-456-789-ABC)
        return data.matches(Regex("^[A-Z0-9]+(-[A-Z0-9]+)*$"))
    }
    
    private fun isValidRtspUrl(url: String): Boolean {
        return try {
            URL(url)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isValidJsonPayload(json: String): Boolean {
        return try {
            gson.fromJson(json, Map::class.java)
            true
        } catch (e: JsonSyntaxException) {
            false
        }
    }
    
    private fun isValidUid(uid: String): Boolean {
        return uid.isNotBlank() && uid.length >= 8
    }
    
    private fun normalizeRtspPayload(payload: QrPayload.Rtsp): CameraConnection? {
        return try {
            val url = URL(payload.url)
            val userInfo = url.userInfo?.split(":")
            val username = userInfo?.getOrNull(0)
            val password = userInfo?.getOrNull(1)
            
            CameraConnection(
                proto = "rtsp",
                ip = url.host,
                port = if (url.port != -1) url.port else 554,
                user = username,
                pass = password,
                path = url.path.ifEmpty { "/" },
                meta = CameraMeta(source = "qr-rtsp")
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun normalizeJsonPayload(payload: QrPayload.Json): CameraConnection? {
        return try {
            val jsonMap = gson.fromJson(payload.json, Map::class.java)
            
            CameraConnection(
                proto = jsonMap["proto"] as? String,
                ip = jsonMap["ip"] as? String,
                port = (jsonMap["port"] as? Number)?.toInt(),
                user = jsonMap["user"] as? String,
                pass = jsonMap["pass"] as? String,
                path = jsonMap["path"] as? String,
                ptz = jsonMap["ptz"] as? Boolean ?: false,
                onvif = OnvifInfo(
                    deviceService = jsonMap["onvif_service"] as? String
                ),
                uid = jsonMap["uid"] as? String,
                meta = CameraMeta(
                    brand = jsonMap["brand"] as? String,
                    model = jsonMap["model"] as? String,
                    source = "qr-json"
                )
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun normalizeUidPayload(payload: QrPayload.Uid): CameraConnection? {
        return CameraConnection(
            uid = payload.uid,
            meta = CameraMeta(source = "qr-uid")
        )
    }
    
    private fun normalizeTextPayload(payload: QrPayload.Text): CameraConnection? {
        // Tenta detectar se é um IP ou URL
        return when {
            payload.text.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) -> {
                CameraConnection(
                    ip = payload.text,
                    port = 554,
                    meta = CameraMeta(source = "manual")
                )
            }
            payload.text.startsWith("http://") || payload.text.startsWith("https://") -> {
                try {
                    val url = URL(payload.text)
                    CameraConnection(
                        ip = url.host,
                        port = if (url.port != -1) url.port else 80,
                        path = url.path,
                        meta = CameraMeta(source = "manual")
                    )
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }
}
