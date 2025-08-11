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
        private const val TAG = "OnvifDiscovery"
        private const val ONVIF_MULTICAST_ADDRESS = "239.255.255.250"
        private const val ONVIF_MULTICAST_PORT = 3702
        private const val DISCOVERY_TIMEOUT_MS = 5000L
        private const val SOCKET_TIMEOUT_MS = 1000
        
        private val logger = Logger
    }
    
    /**
     * Descobre dispositivos ONVIF na rede usando WS-Discovery
     */
    suspend fun discoverDevices(): List<OnvifDevice> {
        return withContext(Dispatchers.IO) {
            try {
                logger.d(TAG, "Starting ONVIF device discovery")
                val devices = mutableListOf<OnvifDevice>()
                
                // Try WS-Discovery first
                val wsDevices = performWsDiscovery()
                devices.addAll(wsDevices)
                
                // Then scan network for additional devices
                val networkDevices = scanNetworkForDevices()
                devices.addAll(networkDevices)
                
                logger.d(TAG, "Discovery completed. Found ${devices.size} devices")
                devices
            } catch (e: Exception) {
                logger.e(TAG, "Discovery failed: ${e.message}")
                emptyList()
            }
        }
    }
    
    /**
     * Perform WS-Discovery using multicast
     */
    private suspend fun performWsDiscovery(): List<OnvifDevice> {
        return withContext(Dispatchers.IO) {
            val devices = mutableListOf<OnvifDevice>()
            
            try {
                val multicastSocket = MulticastSocket(ONVIF_MULTICAST_PORT)
                multicastSocket.soTimeout = SOCKET_TIMEOUT_MS
                multicastSocket.joinGroup(InetAddress.getByName(ONVIF_MULTICAST_ADDRESS))
                
                val probeMessage = createWsDiscoveryProbe()
                val probeData = probeMessage.toByteArray()
                val probePacket = DatagramPacket(
                    probeData, 
                    probeData.size,
                    InetAddress.getByName(ONVIF_MULTICAST_ADDRESS),
                    ONVIF_MULTICAST_PORT
                )
                
                multicastSocket.send(probePacket)
                logger.d(TAG, "WS-Discovery probe sent")
                
                // Listen for responses
                val buffer = ByteArray(4096)
                val startTime = System.currentTimeMillis()
                
                while (System.currentTimeMillis() - startTime < DISCOVERY_TIMEOUT_MS) {
                    try {
                        val responsePacket = DatagramPacket(buffer, buffer.size)
                        multicastSocket.receive(responsePacket)
                        
                        val response = String(responsePacket.data, 0, responsePacket.length)
                        val sourceIp = responsePacket.address.hostAddress
                        
                        logger.d(TAG, "Received response from $sourceIp")
                        
                        val device = parseWsDiscoveryResponse(response, sourceIp)
                        if (device != null) {
                            devices.add(device)
                            logger.i(TAG, "Found ONVIF device: ${device.name} at ${device.ip}")
                        }
                        
                    } catch (e: SocketTimeoutException) {
                        // Continue listening
                        continue
                    } catch (e: Exception) {
                        logger.w(TAG, "Error processing WS-Discovery response: ${e.message}")
                    }
                }
                
                multicastSocket.close()
                
            } catch (e: Exception) {
                logger.e(TAG, "WS-Discovery failed: ${e.message}")
            }
            
            devices
        }
    }
    
    /**
     * Scan network for devices using direct IP scanning with improved validation
     */
    suspend fun scanNetworkForDevices(): List<OnvifDevice> {
        return withContext(Dispatchers.IO) {
            val devices = mutableListOf<OnvifDevice>()
            val subnet = NetworkUtils.getLocalSubnet(context)
            
            Logger.d(TAG, "Scanning subnet: $subnet")
            
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
                        Logger.d(TAG, "Host reachable: $ip")
                        
                        // Test if it's a valid camera with more rigorous validation
                        if (NetworkUtils.testCameraConnectivity(ip)) {
                            cameraCount++
                            Logger.i(TAG, "Valid camera found: $ip")
                            
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
                            Logger.d(TAG, "IP $ip is reachable but not a valid camera")
                        }
                    }
                    
                    // Add small delay to avoid overwhelming the network
                    delay(25) // Reduced delay for faster scanning
                    
                } catch (e: Exception) {
                    Logger.d(TAG, "Error scanning IP $ip: ${e.message}")
                }
            }
            
            Logger.i(TAG, "Network scan completed: $testedCount tested, $reachableCount reachable, $cameraCount cameras found")
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
            Logger.e(TAG, "Erro ao fazer parse da resposta WS-Discovery: ${e.message}")
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
