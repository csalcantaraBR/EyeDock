package com.eyedock.app.domain.model

data class OnvifDevice(
    val ip: String,
    val name: String,
    val manufacturer: String,
    val model: String,
    val firmwareVersion: String,
    val serialNumber: String,
    val hardwareId: String,
    val location: String,
    val port: Int = 554,
    val capabilities: List<String> = emptyList()
)
