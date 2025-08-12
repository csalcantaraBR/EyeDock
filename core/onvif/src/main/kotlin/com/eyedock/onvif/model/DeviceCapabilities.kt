package com.eyedock.onvif.model

data class DeviceCapabilities(
    val mediaService: ServiceEndpoint,
    val eventsService: ServiceEndpoint,
    val ptzService: ServiceEndpoint? = null,
    val audioService: ServiceEndpoint? = null
)

data class ServiceEndpoint(
    val endpoint: String,
    val version: String,
    val supportedOperations: List<PtzOperation> = emptyList()
)

enum class PtzOperation {
    PAN_LEFT,
    PAN_RIGHT,
    TILT_UP,
    TILT_DOWN,
    ZOOM_IN,
    ZOOM_OUT,
    PAN_TILT_ABSOLUTE,
    PAN_TILT_RELATIVE,
    ZOOM_ABSOLUTE,
    ZOOM_RELATIVE
}
