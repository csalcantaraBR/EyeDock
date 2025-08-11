package com.eyedock.app.domain.interfaces

import com.eyedock.app.domain.model.QrPayload
import com.eyedock.app.domain.model.CameraConnection

/**
 * Interface para parser de QR Codes
 */
interface QrParser {
    
    /**
     * Parse um QR Code e retorna o payload normalizado
     */
    suspend fun parseQrCode(qrData: String): QrPayload?
    
    /**
     * Normaliza um payload em CameraConnection
     */
    suspend fun normalizePayload(payload: QrPayload): CameraConnection?
    
    /**
     * Detecta o tipo de payload baseado no conteúdo
     */
    fun detectPayloadType(qrData: String): QrPayload?
    
    /**
     * Valida se o payload é válido
     */
    fun isValidPayload(payload: QrPayload): Boolean
}
