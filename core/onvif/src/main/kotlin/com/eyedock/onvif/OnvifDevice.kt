package com.eyedock.onvif

/**
 * GREEN PHASE - Implementação mínima de OnvifDevice
 * 
 * Esta é uma implementação simples para fazer os testes passarem.
 * Será refatorada na fase REFACTOR.
 */
data class OnvifDevice(
    val ip: String,
    val name: String,
    val manufacturer: String = "Unknown",
    val onvifPort: Int = 5000,
    val rtspPort: Int = 554,
    val capabilities: DeviceCapabilities = DeviceCapabilities()
)

/**
 * Capacidades do dispositivo ONVIF
 */
data class DeviceCapabilities(
    val hasMediaService: Boolean = true,
    val hasEventsService: Boolean = false,
    val hasPtzService: Boolean = false,
    val hasAudioService: Boolean = false
)
