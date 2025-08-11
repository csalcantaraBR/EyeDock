package com.eyedock.app.domain.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

/**
 * Modelo normalizado para conexão de câmera
 * Suporta diferentes formatos de entrada (QR codes, manual, etc.)
 */
@Parcelize
data class CameraConnection(
    val proto: String? = null,           // "rtsp", "onvif"
    val ip: String? = null,              // IP da câmera
    val port: Int? = null,               // Porta (554 para RTSP, 80 para ONVIF)
    val user: String? = null,            // Usuário
    val pass: String? = null,            // Senha (não persistir)
    val path: String? = null,            // Caminho RTSP (/onvif1, etc.)
    val ptz: Boolean = false,            // Suporta PTZ
    val onvif: OnvifInfo? = null,        // Informações ONVIF
    val uid: String? = null,             // UID/Serial proprietário
    val meta: CameraMeta? = null         // Metadados da câmera
) : Parcelable

@Parcelize
data class OnvifInfo(
    val deviceService: String? = null,   // URL do device service
    val mediaService: String? = null,    // URL do media service
    val ptzService: String? = null       // URL do PTZ service
) : Parcelable

@Parcelize
data class CameraMeta(
    val brand: String? = null,           // Marca da câmera
    val model: String? = null,           // Modelo da câmera
    val source: String? = null,          // Origem: "qr-rtsp", "qr-json", "qr-uid", "manual"
    val manufacturer: String? = null,    // Fabricante
    val firmwareVersion: String? = null, // Versão do firmware
    val serialNumber: String? = null     // Número de série
) : Parcelable

/**
 * Tipos de payload de QR Code suportados
 */
sealed class QrPayload {
    data class Rtsp(val url: String) : QrPayload()
    data class Json(val json: String) : QrPayload()
    data class Uid(val uid: String) : QrPayload()
    data class Text(val text: String) : QrPayload()
}

/**
 * Estados de conexão da câmera
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR,
    AUTHENTICATING
}

/**
 * Tipos de erro de conexão
 */
enum class ConnectionError {
    NETWORK_ERROR,
    AUTHENTICATION_FAILED,
    INVALID_URL,
    TIMEOUT,
    UNSUPPORTED_PROTOCOL,
    DEVICE_NOT_FOUND,
    UNKNOWN
}
