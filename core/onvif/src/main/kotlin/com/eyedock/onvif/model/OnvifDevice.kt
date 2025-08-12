package com.eyedock.onvif.model

data class OnvifDevice(
    val ipAddress: String,
    val manufacturer: String,
    val model: String,
    val firmwareVersion: String,
    val onvifPort: Int = 8080,
    val username: String? = null,
    val password: String? = null,
    val capabilities: DeviceCapabilities? = null
)
