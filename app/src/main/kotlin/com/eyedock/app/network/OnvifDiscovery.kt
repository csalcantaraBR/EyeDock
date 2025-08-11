package com.eyedock.app.network

import android.content.Context
import com.eyedock.app.domain.model.OnvifDevice
import com.eyedock.app.utils.Logger
import com.eyedock.app.utils.NetworkUtils
import com.eyedock.app.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Implementação melhorada de descoberta ONVIF
 * Usa WS-Discovery protocol e validação real de dispositivos
 */
class OnvifDiscovery(private val context: Context) {
    
    companion object {
        private const val ONVIF_MULTICAST_ADDRESS = "239.255.255.250"
        private const val ONVIF_MULTICAST_PORT = 3702
        private const val DISCOVERY_TIMEOUT_MS = 5000L
        private const val SOCKET_TIMEOUT_MS = 1000
        
        private val logger = Logger.withTag("OnvifDiscovery")
    }
    
    /**
     * Descobre dispositivos ONVIF na rede usando WS-Discovery
     */
    suspend fun discoverDevices(): List<OnvifDevice> = withContext(Dispatchers.IO) {
        try {
            val subnet = NetworkUtils.getLocalSubnet(context)
            logger.d("Iniciando descoberta ONVIF na subnet: $subnet")
            
            val discoveredDevices = mutableListOf<OnvifDevice>()
            
            withTimeout(DISCOVERY_TIMEOUT_MS) {
                // Enviar probe WS-Discovery
                val probeMessage = createWsDiscoveryProbe()
                val multicastSocket = MulticastSocket(ONVIF_MULTICAST_PORT)
                
                try {
                    multicastSocket.soTimeout = SOCKET_TIMEOUT_MS
                    multicastSocket.joinGroup(InetAddress.getByName(ONVIF_MULTICAST_ADDRESS))
                    
                    // Enviar probe
                    val probePacket = DatagramPacket(
                        probeMessage.toByteArray(),
                        probeMessage.length,
                        InetAddress.getByName(ONVIF_MULTICAST_ADDRESS),
                        ONVIF_MULTICAST_PORT
                    )
                    multicastSocket.send(probePacket)
                    logger.d("Probe WS-Discovery enviado")
                    
                    // Escutar respostas
                    val buffer = ByteArray(4096)
                    var attempts = 0
                    val maxAttempts = 10
                    
                    while (attempts < maxAttempts) {
                        try {
                            val responsePacket = DatagramPacket(buffer, buffer.size)
                            multicastSocket.receive(responsePacket)
                            
                            val response = String(responsePacket.data, 0, responsePacket.length)
                            val sourceIp = responsePacket.address.hostAddress ?: "unknown"
                            logger.d("Resposta recebida de: $sourceIp")
                            
                            val device = parseWsDiscoveryResponse(response, sourceIp)
                            if (device != null) {
                                // Validar se o dispositivo é realmente uma câmera
                                if (NetworkUtils.validateCameraDevice(sourceIp)) {
                                    if (!discoveredDevices.any { it.ip == device.ip }) {
                                        discoveredDevices.add(device)
                                        logger.i("Dispositivo ONVIF válido encontrado: ${device.name} (${device.ip})")
                                    }
                                } else {
                                    logger.d("Dispositivo $sourceIp não é uma câmera válida, ignorando")
                                }
                            }
                            
                        } catch (e: SocketTimeoutException) {
                            attempts++
                            logger.d("Timeout na tentativa $attempts/$maxAttempts")
                        }
                    }
                    
                } finally {
                    multicastSocket.close()
                }
            }
            
            logger.i("Descoberta WS-Discovery concluída. ${discoveredDevices.size} dispositivos válidos encontrados")
            discoveredDevices
            
        } catch (e: Exception) {
            logger.e("Erro durante descoberta ONVIF", e)
            emptyList()
        }
    }
    
    /**
     * Scan network for devices using direct IP scanning with improved validation
     */
    suspend fun scanNetworkForDevices(): List<OnvifDevice> {
        return withContext(Dispatchers.IO) {
            val devices = mutableListOf<OnvifDevice>()
            val subnet = NetworkUtils.getLocalSubnet(context)
            
            Logger.d("Scanning subnet: $subnet")
            
            val baseIp = subnet.substring(0, subnet.lastIndexOf("."))
            var testedCount = 0
            var reachableCount = 0
            var cameraCount = 0
            
            // Scan IP range 1-254 with improved efficiency
            for (i in 1..254) {
                val ip = "$baseIp.$i"
                testedCount++
                
                try {
                    // First check if host is reachable with shorter timeout
                    if (NetworkUtils.isHostReachable(ip, 500)) { // 500ms timeout for ping
                        reachableCount++
                        Logger.d("Host reachable: $ip")
                        
                        // Test if it's a valid camera with more rigorous validation
                        if (NetworkUtils.testCameraConnectivity(ip)) {
                            cameraCount++
                            Logger.i("Valid camera found: $ip")
                            
                            val device = OnvifDevice(
                                name = "Camera at $ip",
                                ip = ip,
                                port = 554, // Default RTSP port
                                manufacturer = "Unknown",
                                model = "IP Camera",
                                firmwareVersion = "Unknown",
                                serialNumber = "Unknown",
                                hardwareId = "Unknown",
                                location = "Unknown",
                                capabilities = emptyList()
                            )
                            devices.add(device)
                        } else {
                            Logger.d("IP $ip is reachable but not a valid camera")
                        }
                    }
                    
                    // Add small delay to avoid overwhelming the network
                    delay(25) // Reduced delay for faster scanning
                    
                } catch (e: Exception) {
                    Logger.d("Error scanning IP $ip: ${e.message}")
                }
            }
            
            Logger.i("Network scan completed: $testedCount tested, $reachableCount reachable, $cameraCount cameras found")
            devices
        }
    }
    
    /**
     * Cria mensagem WS-Discovery Probe
     */
    private fun createWsDiscoveryProbe(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope 
                xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
                xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
                xmlns:wsd="http://schemas.xmlsoap.org/ws/2005/04/discovery">
                <soap:Header>
                    <wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>
                    <wsa:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>
                    <wsa:MessageID>urn:uuid:${java.util.UUID.randomUUID()}</wsa:MessageID>
                </soap:Header>
                <soap:Body>
                    <wsd:Probe>
                        <wsd:Types>dn:NetworkVideoTransmitter</wsd:Types>
                    </wsd:Probe>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    /**
     * Parse da resposta WS-Discovery
     */
    private fun parseWsDiscoveryResponse(response: String, sourceIp: String): OnvifDevice? {
        return try {
            // Extrair informações básicas do XML
            val deviceName = extractXmlValue(response, "DeviceName") ?: "ONVIF Device"
            val manufacturer = extractXmlValue(response, "Manufacturer") ?: "Unknown"
            val model = extractXmlValue(response, "Model") ?: "Unknown"
            
            OnvifDevice(
                ip = sourceIp,
                name = deviceName,
                manufacturer = manufacturer,
                model = model,
                firmwareVersion = "Unknown",
                serialNumber = "Unknown",
                hardwareId = "Unknown",
                location = "Unknown",
                port = 554, // Default RTSP port
                capabilities = emptyList()
            )
            
        } catch (e: Exception) {
            Logger.e("Erro ao fazer parse da resposta WS-Discovery", e)
            null
        }
    }
    
    /**
     * Extrai valor de tag XML
     */
    private fun extractXmlValue(xml: String, tagName: String): String? {
        val startTag = "<$tagName>"
        val endTag = "</$tagName>"
        val startIndex = xml.indexOf(startTag)
        val endIndex = xml.indexOf(endTag)
        
        return if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            xml.substring(startIndex + startTag.length, endIndex)
        } else null
    }
    
    /**
     * Converte IP inteiro para string
     */
    private fun Int.toIpString(): String {
        return "${(this shr 24) and 0xFF}.${(this shr 16) and 0xFF}.${(this shr 8) and 0xFF}.${this and 0xFF}"
    }
}
