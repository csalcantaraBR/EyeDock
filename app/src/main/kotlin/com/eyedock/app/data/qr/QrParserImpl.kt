package com.eyedock.app.data.qr

import com.eyedock.app.domain.interfaces.QrParser
import com.eyedock.app.domain.model.QrPayload
import org.json.JSONObject
import java.net.URI

class QrParserImpl : QrParser {
    
    override fun parse(text: String): QrPayload {
        val trimmedText = text.trim()
        
        // 1. Verificar se é URL RTSP
        if (isRtspUrl(trimmedText)) {
            return QrPayload.Rtsp(trimmedText)
        }
        
        // 2. Verificar se é JSON
        if (isJson(trimmedText)) {
            return QrPayload.Json(trimmedText)
        }
        
        // 3. Verificar se é UID/Serial (texto livre)
        if (isUid(trimmedText)) {
            return QrPayload.Uid(trimmedText)
        }
        
        // 4. Se não for nenhum dos formatos conhecidos, tratar como UID
        return QrPayload.Uid(trimmedText)
    }
    
    private fun isRtspUrl(text: String): Boolean {
        return try {
            val uri = URI(text)
            uri.scheme?.lowercase() in listOf("rtsp", "rtsps")
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isJson(text: String): Boolean {
        return try {
            JSONObject(text)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isUid(text: String): Boolean {
        // Verificar se parece ser um UID/serial (padrões comuns)
        val uidPatterns = listOf(
            Regex("^[A-Z0-9]{3,}-[A-Z0-9]{3,}-[A-Z0-9]{3,}"), // G123-456-789
            Regex("^[A-Z0-9]{8,}"), // Serial numbers longos
            Regex("^[A-Z0-9]{4,}"), // IDs curtos
            Regex("^[A-Z0-9]+$") // Apenas letras e números
        )
        
        return uidPatterns.any { it.matches(text) }
    }
}
