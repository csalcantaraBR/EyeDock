package com.eyedock.app.utils

/**
 * Application-wide constants
 */
object Constants {
    
    // ===== NETWORK =====
    const val DEFAULT_RTSP_PORT = 554
    const val DEFAULT_ONVIF_PORT = 80
    const val DEFAULT_HTTP_PORT = 80
    const val DEFAULT_HTTPS_PORT = 443
    
    const val CONNECTION_TIMEOUT_MS = 5000
    const val DISCOVERY_TIMEOUT_MS = 3000
    const val PING_TIMEOUT_MS = 1000
    const val ONVIF_TEST_TIMEOUT_MS = 2000
    
    // ===== DATABASE =====
    const val DATABASE_NAME = "camera_database"
    const val DATABASE_VERSION = 1
    
    // ===== UI =====
    const val DEFAULT_ANIMATION_DURATION = 300L
    const val SNAPSHOT_FEEDBACK_DURATION = 2000L
    
    // ===== CAMERA =====
    const val DEFAULT_RTSP_PATH = "/"
    const val DEFAULT_STREAM_PATH = "/stream1"
    
    // ===== VALIDATION =====
    const val MIN_CAMERA_NAME_LENGTH = 1
    const val MAX_CAMERA_NAME_LENGTH = 50
    const val MIN_USERNAME_LENGTH = 1
    const val MAX_USERNAME_LENGTH = 32
    const val MIN_PASSWORD_LENGTH = 1
    const val MAX_PASSWORD_LENGTH = 64
    
    // ===== ERROR MESSAGES =====
    object ErrorMessages {
        const val CAMERA_NOT_FOUND = "Camera not found"
        const val CONNECTION_FAILED = "Connection failed"
        const val INVALID_IP_ADDRESS = "Invalid IP address"
        const val INVALID_PORT = "Invalid port number"
        const val CAMERA_NOT_REACHABLE = "Camera not reachable"
        const val PORT_CLOSED = "Port is closed"
        const val NAME_REQUIRED = "Camera name is required"
        const val IP_REQUIRED = "IP address is required"
        const val SAVE_FAILED = "Failed to save camera"
        const val LOAD_FAILED = "Failed to load cameras"
        const val DISCOVERY_FAILED = "Discovery failed"
        const val INVALID_QR_CODE = "Invalid QR code format"
        const val QR_PROCESSING_ERROR = "Error processing QR code"
    }
    
    // ===== SUCCESS MESSAGES =====
    object SuccessMessages {
        const val CAMERA_SAVED = "Camera saved successfully"
        const val CAMERA_DELETED = "Camera deleted successfully"
        const val CONNECTION_SUCCESS = "Camera is reachable"
        const val SNAPSHOT_TAKEN = "Snapshot taken"
    }
    
    // ===== COMMON SUBNETS =====
    object CommonSubnets {
        const val HOME_NETWORK = "192.168.1.0/24"
        const val OFFICE_NETWORK = "10.0.0.0/24"
        const val GUEST_NETWORK = "192.168.2.0/24"
    }
}
