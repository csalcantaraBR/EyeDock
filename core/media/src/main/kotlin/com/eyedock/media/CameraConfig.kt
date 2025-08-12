package com.eyedock.media

data class CameraConfig(
    val ip: String,
    val rtspPort: Int = 554,
    val user: String? = null,
    val password: String? = null,
    val onvifPort: Int = 8080,
    val streamPath: String = "/onvif1"
)
