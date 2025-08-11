package com.eyedock.app.domain

import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.model.OnvifInfo
import com.eyedock.app.domain.model.CameraMeta
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Testes unitários para o domínio de conexão de câmeras
 * Seguindo TDD: RED → GREEN → REFACTOR
 */
@RunWith(JUnit4::class)
class CameraConnectionTest {

    @Test
    fun `deve criar conexao valida com dados basicos`() {
        // Arrange
        val proto = "rtsp"
        val ip = "192.168.1.100"
        val port = 554
        val user = "admin"
        val pass = "password123"
        val path = "/onvif1"
        
        // Act
        val connection = CameraConnection(
            proto = proto,
            ip = ip,
            port = port,
            user = user,
            pass = pass,
            path = path,
            ptz = true
        )
        
        // Assert
        assertEquals(proto, connection.proto)
        assertEquals(ip, connection.ip)
        assertEquals(port, connection.port)
        assertEquals(user, connection.user)
        assertEquals(pass, connection.pass)
        assertEquals(path, connection.path)
        assertTrue(connection.ptz)
    }

    @Test
    fun `deve criar conexao com informacoes ONVIF`() {
        // Arrange
        val onvifInfo = OnvifInfo(
            deviceService = "http://192.168.1.100:80/onvif/device_service",
            mediaService = "http://192.168.1.100:80/onvif/media_service",
            ptzService = "http://192.168.1.100:80/onvif/ptz_service"
        )
        
        val meta = CameraMeta(
            brand = "Hikvision",
            model = "DS-2CD2142FWD-I",
            source = "qr-json"
        )
        
        // Act
        val connection = CameraConnection(
            proto = "onvif",
            ip = "192.168.1.100",
            port = 80,
            user = "admin",
            pass = "password123",
            onvif = onvifInfo,
            meta = meta
        )
        
        // Assert
        assertEquals("onvif", connection.proto)
        assertNotNull(connection.onvif)
        assertEquals("http://192.168.1.100:80/onvif/device_service", connection.onvif?.deviceService)
        assertNotNull(connection.meta)
        assertEquals("Hikvision", connection.meta?.brand)
        assertEquals("DS-2CD2142FWD-I", connection.meta?.model)
    }

    @Test
    fun `deve criar conexao com valores padrao`() {
        // Act
        val connection = CameraConnection()
        
        // Assert
        assertNotNull(connection)
        assertNull(connection.proto)
        assertNull(connection.ip)
        assertNull(connection.port)
        assertNull(connection.user)
        assertNull(connection.pass)
        assertNull(connection.path)
        assertFalse(connection.ptz)
    }
}
