package com.eyedock.app.domain.model

data class CameraConnection(
    var proto: String? = null,
    var ip: String? = null,
    var port: Int? = null,
    var user: String? = null,
    var pass: String? = null,
    var path: String? = null,
    var ptz: Boolean? = null,
    var onvifDeviceService: String? = null,
    var uid: String? = null,
    var meta: Map<String, String> = emptyMap()
) {
    fun toRtspUri(): String {
        val protocol = proto ?: "rtsp"
        val host = ip ?: ""
        val portStr = port?.let { ":$it" } ?: ""
        val auth = if (user != null && pass != null) "$user:$pass@" else ""
        val pathStr = path?.let { if (it.startsWith("/")) it else "/$it" } ?: ""
        
        return "$protocol://$auth$host$portStr$pathStr"
    }
    
    fun isValid(): Boolean {
        return !ip.isNullOrBlank() && port != null && port!! > 0
    }
}

sealed class QrPayload {
    data class Rtsp(val url: String) : QrPayload()
    data class Json(val json: String) : QrPayload()
    data class Uid(val uid: String) : QrPayload()
}

data class Auth(
    val username: String,
    val password: String
)

data class OnvifEndpoint(
    val ip: String,
    val port: Int = 80,
    val deviceService: String,
    val types: List<String> = emptyList()
)

data class Capabilities(
    val device: String? = null,
    val media: String? = null,
    val ptz: String? = null
)

data class DeviceInfo(
    val manufacturer: String? = null,
    val model: String? = null,
    val serialNumber: String? = null,
    val hardwareId: String? = null
)

data class MediaProfile(
    val token: String,
    val name: String,
    val codec: String? = null,
    val resolution: String? = null,
    val bitrate: Int? = null
)

data class RtspOptions(
    val methods: List<String> = emptyList(),
    val public: List<String> = emptyList()
)

data class Sdp(
    val media: List<SdpMedia> = emptyList()
)

data class SdpMedia(
    val type: String,
    val port: Int,
    val protocol: String,
    val payloads: List<Int> = emptyList()
)
