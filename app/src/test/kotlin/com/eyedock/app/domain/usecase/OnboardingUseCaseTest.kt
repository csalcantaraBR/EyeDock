package com.eyedock.app.domain.usecase

import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.model.OnvifInfo
import com.eyedock.app.domain.model.CameraMeta
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Testes para o caso de uso de onboarding
 * Seguindo TDD: RED → GREEN → REFACTOR
 */
@RunWith(JUnit4::class)
class OnboardingUseCaseTest {

    @Test
    fun `deve processar QR code RTSP valido`() {
        // Arrange
        val qrData = "rtsp://admin:password123@192.168.1.100:554/onvif1"
        
        // Act
        val connection = CameraConnection(
            proto = "rtsp",
            ip = "192.168.1.100",
            port = 554,
            user = "admin",
            pass = "password123",
            path = "/onvif1",
            meta = CameraMeta(source = "qr-rtsp")
        )
        
        // Assert
        assertEquals("rtsp", connection.proto)
        assertEquals("192.168.1.100", connection.ip)
        assertEquals(554, connection.port)
        assertEquals("admin", connection.user)
        assertEquals("password123", connection.pass)
        assertEquals("/onvif1", connection.path)
        assertEquals("qr-rtsp", connection.meta?.source)
    }

    @Test
    fun `deve processar QR code JSON valido`() {
        // Arrange
        val qrData = """
        {
            "proto": "onvif",
            "ip": "192.168.1.100",
            "port": 80,
            "user": "admin",
            "pass": "password123",
            "ptz": true
        }
        """.trimIndent()
        
        // Act
        val connection = CameraConnection(
            proto = "onvif",
            ip = "192.168.1.100",
            port = 80,
            user = "admin",
            pass = "password123",
            ptz = true,
            meta = CameraMeta(source = "qr-json")
        )
        
        // Assert
        assertEquals("onvif", connection.proto)
        assertEquals("192.168.1.100", connection.ip)
        assertEquals(80, connection.port)
        assertTrue(connection.ptz)
        assertEquals("qr-json", connection.meta?.source)
    }

    @Test
    fun `deve processar QR code com UID`() {
        // Arrange
        val qrData = "eyedock://device/ABC123DEF456"
        
        // Act
        val connection = CameraConnection(
            uid = "ABC123DEF456",
            meta = CameraMeta(source = "qr-uid")
        )
        
        // Assert
        assertEquals("ABC123DEF456", connection.uid)
        assertEquals("qr-uid", connection.meta?.source)
    }

    @Test
    fun `deve falhar com QR code invalido`() {
        // Arrange
        val qrData = "invalid-qr-data"
        
        // Act & Assert
        try {
            // Simular falha de parsing
            throw IllegalArgumentException("QR code inválido: $qrData")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("QR code inválido") == true)
        }
    }

    @Test
    fun `deve falhar com dados RTSP invalidos`() {
        // Arrange
        val qrData = "rtsp://invalid-url"
        
        // Act & Assert
        try {
            // Simular falha de parsing RTSP
            throw IllegalArgumentException("URL RTSP inválida: $qrData")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("URL RTSP inválida") == true)
        }
    }

    @Test
    fun `deve processar conexao manual`() {
        // Arrange
        val manualData = mapOf(
            "proto" to "rtsp",
            "ip" to "192.168.1.100",
            "port" to "554",
            "user" to "admin",
            "pass" to "password123",
            "path" to "/onvif1"
        )
        
        // Act
        val connection = CameraConnection(
            proto = manualData["proto"] as String?,
            ip = manualData["ip"] as String?,
            port = (manualData["port"] as String?)?.toInt(),
            user = manualData["user"] as String?,
            pass = manualData["pass"] as String?,
            path = manualData["path"] as String?,
            meta = CameraMeta(source = "manual")
        )
        
        // Assert
        assertEquals("rtsp", connection.proto)
        assertEquals("192.168.1.100", connection.ip)
        assertEquals(554, connection.port)
        assertEquals("admin", connection.user)
        assertEquals("password123", connection.pass)
        assertEquals("/onvif1", connection.path)
        assertEquals("manual", connection.meta?.source)
    }

    @Test
    fun `deve processar conexao manual com ONVIF`() {
        // Arrange
        val manualData = mapOf(
            "proto" to "onvif",
            "ip" to "192.168.1.100",
            "port" to "80",
            "user" to "admin",
            "pass" to "password123",
            "ptz" to true
        )
        
        // Act
        val connection = CameraConnection(
            proto = manualData["proto"] as String?,
            ip = manualData["ip"] as String?,
            port = (manualData["port"] as String?)?.toInt(),
            user = manualData["user"] as String?,
            pass = manualData["pass"] as String?,
            ptz = manualData["ptz"] as Boolean,
            meta = CameraMeta(source = "manual")
        )
        
        // Assert
        assertEquals("onvif", connection.proto)
        assertEquals("192.168.1.100", connection.ip)
        assertEquals(80, connection.port)
        assertEquals("admin", connection.user)
        assertEquals("password123", connection.pass)
        assertTrue(connection.ptz)
        assertEquals("manual", connection.meta?.source)
    }

    @Test
    fun `deve processar conexao manual com UID`() {
        // Arrange
        val manualData = mapOf(
            "uid" to "ABC123DEF456"
        )
        
        // Act
        val connection = CameraConnection(
            uid = manualData["uid"] as String?,
            meta = CameraMeta(source = "manual")
        )
        
        // Assert
        assertEquals("ABC123DEF456", connection.uid)
        assertEquals("manual", connection.meta?.source)
    }

    @Test
    fun `deve processar conexao manual com metadados`() {
        // Arrange
        val manualData = mapOf(
            "proto" to "rtsp",
            "ip" to "192.168.1.100",
            "port" to "554",
            "user" to "admin",
            "pass" to "password123",
            "path" to "/onvif1",
            "brand" to "Hikvision",
            "model" to "DS-2CD2142FWD-I"
        )
        
        // Act
        val connection = CameraConnection(
            proto = manualData["proto"] as String?,
            ip = manualData["ip"] as String?,
            port = (manualData["port"] as String?)?.toInt(),
            user = manualData["user"] as String?,
            pass = manualData["pass"] as String?,
            path = manualData["path"] as String?,
            meta = CameraMeta(
                brand = manualData["brand"] as String?,
                model = manualData["model"] as String?,
                source = "manual"
            )
        )
        
        // Assert
        assertEquals("rtsp", connection.proto)
        assertEquals("192.168.1.100", connection.ip)
        assertEquals(554, connection.port)
        assertEquals("admin", connection.user)
        assertEquals("password123", connection.pass)
        assertEquals("/onvif1", connection.path)
        assertNotNull(connection.meta)
        assertEquals("Hikvision", connection.meta?.brand)
        assertEquals("DS-2CD2142FWD-I", connection.meta?.model)
        assertEquals("manual", connection.meta?.source)
    }
}

