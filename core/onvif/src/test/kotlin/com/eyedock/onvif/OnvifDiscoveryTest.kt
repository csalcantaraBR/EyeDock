package com.eyedock.onvif

import com.eyedock.core.common.test.categories.*
import com.eyedock.onvif.model.OnvifDevice
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.params.provider.CsvSource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

/**
 * Testes funcionais para descoberta ONVIF
 * Seguindo TDD: RED → GREEN → REFACTOR
 */
@DisplayName("ONVIF Discovery Functional Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OnvifDiscoveryTest {

    private lateinit var onvifDiscoveryService: OnvifDiscoveryService
    private lateinit var networkScanner: NetworkScanner
    private lateinit var wsDiscoveryClient: WsDiscoveryClient

    @BeforeEach
    fun setUp() {
        onvifDiscoveryService = OnvifDiscoveryService()
        networkScanner = NetworkScanner()
        wsDiscoveryClient = WsDiscoveryClient()
    }

    @Nested
    @DisplayName("Network Discovery")
    class NetworkDiscoveryTest {
        
        private lateinit var onvifDiscoveryService: OnvifDiscoveryService
        private lateinit var networkScanner: NetworkScanner
        private lateinit var wsDiscoveryClient: WsDiscoveryClient

        @BeforeEach
        fun setUp() {
            onvifDiscoveryService = OnvifDiscoveryService()
            networkScanner = NetworkScanner()
            wsDiscoveryClient = WsDiscoveryClient()
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(SMOKE_TEST)
        @Tag(ONVIF_TEST)
        @Tag(DISCOVERY_TEST)
        @DisplayName("Deve descobrir dispositivos ONVIF na rede local")
        fun `deve descobrir dispositivos ONVIF na rede local`() = runBlocking {
            // Arrange
            val subnet = onvifDiscoveryService.getLocalSubnet()
            val timeoutMs = 10_000L
            
            // Act
            val devices = withTimeoutOrNull(timeoutMs) {
                onvifDiscoveryService.discoverDevices(subnet, timeoutMs)
            }
            
            // Assert
            assertNotNull(devices)
            println("Dispositivos encontrados: ${devices?.size ?: 0}")
            
            // Em ambiente de teste, pode não haver dispositivos reais
            // mas o serviço deve funcionar sem exceções
            devices?.forEach { device ->
                assertNotNull(device.ipAddress)
                assertNotNull(device.manufacturer)
                assertTrue(device.ipAddress.matches(Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")))
            }
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(ONVIF_TEST)
        @Tag(DISCOVERY_TEST)
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        @DisplayName("Deve enumerar serviços Media Events PTZ quando disponível")
        fun `deve enumerar servicos Media Events PTZ quando disponivel`() = runBlocking {
            // Arrange
            val testIp = "192.168.1.100" // IP de teste
            val device = OnvifDevice(
                ipAddress = testIp,
                manufacturer = "Test Manufacturer",
                model = "Test Model",
                firmwareVersion = "1.0.0"
            )
            
            // Act
            val capabilities = onvifDiscoveryService.getDeviceCapabilities(device)
            
            // Assert
            assertNotNull(capabilities)
            assertNotNull(capabilities.mediaService)
            assertNotNull(capabilities.eventsService)
            
            // Verificar se PTZ está disponível (pode não estar em todos os dispositivos)
            capabilities.ptzService?.let { ptzService ->
                assertNotNull(ptzService.endpoint)
                assertTrue(ptzService.supportedOperations.isNotEmpty())
            }
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve falhar graciosamente quando nenhum dispositivo encontrado")
        fun `deve falhar graciosamente quando nenhum dispositivo encontrado`() = runBlocking {
            // Arrange
            val invalidSubnet = "10.255.255.0/24" // Subnet que não deve ter dispositivos
            val timeoutMs = 5_000L
            
            // Act
            val devices = withTimeoutOrNull(timeoutMs) {
                onvifDiscoveryService.discoverDevices(invalidSubnet, timeoutMs)
            }
            
            // Assert
            assertNotNull(devices)
            assertTrue(devices?.isEmpty() ?: true)
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve timeout discovery após 10 segundos")
        fun `deve timeout discovery apos 10 segundos`() = runBlocking {
            // Arrange
            val slowSubnet = "192.168.1.0/24"
            val timeoutMs = 10_000L
            
            // Act & Assert
            val startTime = System.currentTimeMillis()
            
            val devices = withTimeoutOrNull(timeoutMs) {
                onvifDiscoveryService.discoverDevices(slowSubnet, timeoutMs)
            }
            
            val duration = System.currentTimeMillis() - startTime
            
            // Deve completar dentro do timeout
            assertTrue(duration <= timeoutMs + 1000) // Margem de 1s
            assertNotNull(devices)
        }

        @ParameterizedTest
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @ValueSource(strings = [
            "192.168.1.0/24",
            "10.0.0.0/24", 
            "172.16.0.0/24"
        ])
        @DisplayName("Deve validar formato de subnet")
        fun `deve validar formato de subnet`(subnet: String) = runBlocking {
            // Act
            val isValid = onvifDiscoveryService.validateSubnetFormat(subnet)
            
            // Assert
            assertTrue(isValid)
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve rejeitar formato de subnet inválido")
        fun `deve rejeitar formato de subnet invalido`() {
            // Arrange
            val invalidSubnets = listOf(
                "192.168.1.0",
                "192.168.1.0/33",
                "192.168.1.0/-1",
                "invalid-subnet",
                ""
            )
            
            // Act & Assert
            invalidSubnets.forEach { subnet ->
                assertFalse(onvifDiscoveryService.validateSubnetFormat(subnet))
            }
        }
        
        // Helper methods
        private fun getLocalSubnet(): String {
            return onvifDiscoveryService.getLocalSubnet()
        }

        private fun getLocalIpAddress(): String {
            return onvifDiscoveryService.getLocalIpAddress()
        }
    }

    @Nested
    @DisplayName("WS-Discovery Protocol")
    class WsDiscoveryTest {
        
        private lateinit var onvifDiscoveryService: OnvifDiscoveryService
        private lateinit var networkScanner: NetworkScanner
        private lateinit var wsDiscoveryClient: WsDiscoveryClient

        @BeforeEach
        fun setUp() {
            onvifDiscoveryService = OnvifDiscoveryService()
            networkScanner = NetworkScanner()
            wsDiscoveryClient = WsDiscoveryClient()
        }

        @Test
        @Tag(INTEGRATION_TEST)
        @Tag(ONVIF_TEST)
        @Tag(DISCOVERY_TEST)
        @DisplayName("Deve enviar probe WS-Discovery")
        fun `deve enviar probe WS-Discovery`() = runBlocking {
            // Arrange
            val probeMessage = createWsDiscoveryProbe()
            
            // Act
            val response = withTimeoutOrNull(5000) {
                wsDiscoveryClient.sendProbe(probeMessage)
            }
            
            // Assert
            // Em ambiente de teste, pode não haver resposta
            // mas o cliente deve funcionar sem exceções
            assertNotNull(response)
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve criar probe message válida")
        fun `deve criar probe message valida`() {
            // Act
            val probeMessage = createWsDiscoveryProbe()
            
            // Assert
            assertNotNull(probeMessage)
            assertTrue(probeMessage.contains("Probe"))
            assertTrue(probeMessage.contains("http://schemas.xmlsoap.org/ws/2005/04/discovery"))
            assertTrue(probeMessage.contains("Types"))
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve parsear resposta WS-Discovery")
        fun `deve parsear resposta WS-Discovery`() {
            // Arrange
            val mockResponse = createMockWsDiscoveryResponse()
            
            // Act
            val devices = wsDiscoveryClient.parseProbeResponse(mockResponse)
            
            // Assert
            assertNotNull(devices)
            assertTrue(devices.isNotEmpty())
            
            val device = devices.first()
            assertEquals("192.168.1.100", device.ipAddress)
            assertEquals("Test Manufacturer", device.manufacturer)
            assertEquals("Test Model", device.model)
        }
        
        // Helper methods
        private fun getLocalSubnet(): String {
            return onvifDiscoveryService.getLocalSubnet()
        }

        private fun getLocalIpAddress(): String {
            return onvifDiscoveryService.getLocalIpAddress()
        }
        
        private fun createWsDiscoveryProbe(): String {
            return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" 
                              xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" 
                              xmlns:wsd="http://schemas.xmlsoap.org/ws/2005/04/discovery">
                    <soap:Header>
                        <wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>
                        <wsa:MessageID>urn:uuid:12345678-1234-1234-1234-123456789012</wsa:MessageID>
                        <wsa:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>
                    </soap:Header>
                    <soap:Body>
                        <wsd:Probe>
                            <wsd:Types>dn:NetworkVideoTransmitter</wsd:Types>
                        </wsd:Probe>
                    </soap:Body>
                </soap:Envelope>
            """.trimIndent()
        }
        
        private fun createMockWsDiscoveryResponse(): String {
            return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" 
                              xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" 
                              xmlns:wsd="http://schemas.xmlsoap.org/ws/2005/04/discovery">
                    <soap:Header>
                        <wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/ProbeMatches</wsa:Action>
                        <wsa:MessageID>urn:uuid:87654321-4321-4321-4321-210987654321</wsa:MessageID>
                        <wsa:RelatesTo>urn:uuid:12345678-1234-1234-1234-123456789012</wsa:RelatesTo>
                        <wsa:To>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:To>
                    </soap:Header>
                    <soap:Body>
                        <wsd:ProbeMatches>
                            <wsd:ProbeMatch>
                                <wsa:EndpointReference>
                                    <wsa:Address>urn:uuid:device-1234-5678-9abc-def012345678</wsa:Address>
                                </wsa:EndpointReference>
                                <wsd:Types>dn:NetworkVideoTransmitter</wsd:Types>
                                <wsd:Scopes>onvif://www.onvif.org/name/Test Camera</wsd:Scopes>
                                <wsd:XAddrs>http://192.168.1.100:8080/onvif/device_service</wsd:XAddrs>
                                <wsd:MetadataVersion>1</wsd:MetadataVersion>
                            </wsd:ProbeMatch>
                        </wsd:ProbeMatches>
                    </soap:Body>
                </soap:Envelope>
            """.trimIndent()
        }
    }

    @Nested
    @DisplayName("Network Scanner")
    class NetworkScannerTest {
        
        private lateinit var onvifDiscoveryService: OnvifDiscoveryService
        private lateinit var networkScanner: NetworkScanner
        private lateinit var wsDiscoveryClient: WsDiscoveryClient

        @BeforeEach
        fun setUp() {
            onvifDiscoveryService = OnvifDiscoveryService()
            networkScanner = NetworkScanner()
            wsDiscoveryClient = WsDiscoveryClient()
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve escanear subnet para IPs ativos")
        fun `deve escanear subnet para IPs ativos`() = runBlocking {
            // Arrange
            val subnet = getLocalSubnet()
            
            // Act
            val activeIps = withTimeoutOrNull(10000) {
                networkScanner.scanSubnet(subnet)
            }
            
            // Assert
            assertNotNull(activeIps)
            assertTrue(activeIps?.isNotEmpty() ?: false)
            
            // Deve conter pelo menos o IP local
            val localIp = getLocalIpAddress()
            assertTrue(activeIps?.contains(localIp) ?: false)
        }
        
        // Helper methods
        private fun getLocalSubnet(): String {
            return onvifDiscoveryService.getLocalSubnet()
        }

        private fun getLocalIpAddress(): String {
            return onvifDiscoveryService.getLocalIpAddress()
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve detectar portas ONVIF abertas")
        fun `deve detectar portas ONVIF abertas`() = runBlocking {
            // Arrange
            val testIp = "192.168.1.100"
            val onvifPorts = listOf(80, 8080, 554, 8000)
            
            // Act
            val openPorts = withTimeoutOrNull(5000) {
                networkScanner.scanPorts(testIp, onvifPorts)
            }
            
            // Assert
            assertNotNull(openPorts)
            // Em ambiente de teste, pode não haver portas abertas
            // mas o scanner deve funcionar sem exceções
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve validar formato de IP")
        fun `deve validar formato de IP`() {
            // Arrange
            val validIps = listOf(
                "192.168.1.1",
                "10.0.0.1", 
                "172.16.0.1",
                "127.0.0.1"
            )
            
            val invalidIps = listOf(
                "192.168.1.256",
                "192.168.1",
                "192.168.1.1.1",
                "invalid-ip",
                ""
            )
            
            // Act & Assert
            validIps.forEach { ip ->
                assertTrue(networkScanner.isValidIpAddress(ip))
            }
            
            invalidIps.forEach { ip ->
                assertFalse(networkScanner.isValidIpAddress(ip))
            }
        }
    }

    @Nested
    @DisplayName("Device Information")
    class DeviceInformationTest {
        
        private lateinit var onvifDiscoveryService: OnvifDiscoveryService
        private lateinit var networkScanner: NetworkScanner
        private lateinit var wsDiscoveryClient: WsDiscoveryClient

        @BeforeEach
        fun setUp() {
            onvifDiscoveryService = OnvifDiscoveryService()
            networkScanner = NetworkScanner()
            wsDiscoveryClient = WsDiscoveryClient()
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve obter informações básicas do dispositivo")
        fun `deve obter informacoes basicas do dispositivo`() = runBlocking {
            // Arrange
            val device = OnvifDevice(
                ipAddress = "192.168.1.100",
                manufacturer = "Test Manufacturer",
                model = "Test Model",
                firmwareVersion = "1.0.0"
            )
            
            // Act
            val info = onvifDiscoveryService.getDeviceInformation(device)
            
            // Assert
            assertNotNull(info)
            assertEquals(device.manufacturer, info.manufacturer)
            assertEquals(device.model, info.model)
            assertEquals(device.firmwareVersion, info.firmwareVersion)
            assertNotNull(info.serialNumber)
            assertNotNull(info.hardwareId)
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve obter perfis de mídia")
        fun `deve obter perfis de midia`() = runBlocking {
            // Arrange
            val device = OnvifDevice(
                ipAddress = "192.168.1.100",
                manufacturer = "Test Manufacturer",
                model = "Test Model",
                firmwareVersion = "1.0.0"
            )
            
            // Act
            val profiles = onvifDiscoveryService.getMediaProfiles(device)
            
            // Assert
            assertNotNull(profiles)
            assertTrue(profiles.isNotEmpty())
            
            val profile = profiles.first()
            assertNotNull(profile.token)
            assertNotNull(profile.name)
            assertNotNull(profile.videoSourceConfiguration)
            assertNotNull(profile.videoEncoderConfiguration)
        }

        @Test
        @Tag(FAST_TEST)
        @Tag(ONVIF_TEST)
        @DisplayName("Deve obter URL de stream RTSP")
        fun `deve obter URL de stream RTSP`() = runBlocking {
            // Arrange
            val device = OnvifDevice(
                ipAddress = "192.168.1.100",
                manufacturer = "Test Manufacturer",
                model = "Test Model",
                firmwareVersion = "1.0.0"
            )
            val profileToken = "Profile_1"
            
            // Act
            val streamUrl = onvifDiscoveryService.getStreamUri(device, profileToken)
            
            // Assert
            assertNotNull(streamUrl)
            assertTrue(streamUrl.startsWith("rtsp://"))
            assertTrue(streamUrl.contains(device.ipAddress))
        }
        
        // Helper methods
        private fun getLocalSubnet(): String {
            return onvifDiscoveryService.getLocalSubnet()
        }

        private fun getLocalIpAddress(): String {
            return onvifDiscoveryService.getLocalIpAddress()
        }
    }

    // Helper methods
    private fun getLocalSubnet(): String {
        return onvifDiscoveryService.getLocalSubnet()
    }

    private fun getLocalIpAddress(): String {
        return onvifDiscoveryService.getLocalIpAddress()
    }

    private fun createWsDiscoveryProbe(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" 
                          xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" 
                          xmlns:wsd="http://schemas.xmlsoap.org/ws/2005/04/discovery">
                <soap:Header>
                    <wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>
                    <wsa:MessageID>urn:uuid:12345678-1234-1234-1234-123456789012</wsa:MessageID>
                    <wsa:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>
                </soap:Header>
                <soap:Body>
                    <wsd:Probe>
                        <wsd:Types>dn:NetworkVideoTransmitter</wsd:Types>
                    </wsd:Probe>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }

    private fun createMockWsDiscoveryResponse(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" 
                          xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" 
                          xmlns:wsd="http://schemas.xmlsoap.org/ws/2005/04/discovery">
                <soap:Header>
                    <wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/ProbeMatches</wsa:Action>
                    <wsa:MessageID>urn:uuid:87654321-4321-4321-4321-210987654321</wsa:MessageID>
                    <wsa:RelatesTo>urn:uuid:12345678-1234-1234-1234-123456789012</wsa:RelatesTo>
                    <wsa:To>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:To>
                </soap:Header>
                <soap:Body>
                    <wsd:ProbeMatches>
                        <wsd:ProbeMatch>
                            <wsa:EndpointReference>
                                <wsa:Address>urn:uuid:device-1234-5678-9abc-def012345678</wsa:Address>
                            </wsa:EndpointReference>
                            <wsd:Types>dn:NetworkVideoTransmitter</wsd:Types>
                            <wsd:Scopes>onvif://www.onvif.org/name/Test Camera</wsd:Scopes>
                            <wsd:XAddrs>http://192.168.1.100:8080/onvif/device_service</wsd:XAddrs>
                            <wsd:MetadataVersion>1</wsd:MetadataVersion>
                        </wsd:ProbeMatch>
                    </wsd:ProbeMatches>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
}


