package com.eyedock.app.data.onvif

import com.eyedock.app.domain.interfaces.OnvifClient
import com.eyedock.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import javax.xml.parsers.DocumentBuilderFactory

class OnvifClientImpl(
    private val httpClient: OkHttpClient
) : OnvifClient {
    
    override suspend fun probe(timeoutMs: Long): List<OnvifEndpoint> = withContext(Dispatchers.IO) {
        val endpoints = mutableListOf<OnvifEndpoint>()
        
        try {
            val probeMessage = createProbeMessage()
            val multicastSocket = MulticastSocket(3702)
            multicastSocket.joinGroup(InetAddress.getByName("239.255.255.250"))
            multicastSocket.soTimeout = timeoutMs.toInt()
            
            // Enviar Probe
            val probeBytes = probeMessage.toByteArray()
            val probePacket = DatagramPacket(
                probeBytes, 
                probeBytes.size,
                InetAddress.getByName("239.255.255.250"),
                3702
            )
            multicastSocket.send(probePacket)
            
            // Receber respostas
            val buffer = ByteArray(4096)
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                try {
                    val responsePacket = DatagramPacket(buffer, buffer.size)
                    multicastSocket.receive(responsePacket)
                    
                    val response = String(responsePacket.data, 0, responsePacket.length)
                    val endpoint = parseProbeResponse(response, responsePacket.address.hostAddress)
                    if (endpoint != null) {
                        endpoints.add(endpoint)
                    }
                } catch (e: IOException) {
                    // Timeout ou erro, continuar
                    break
                }
            }
            
            multicastSocket.close()
        } catch (e: Exception) {
            // Log error
        }
        
        return@withContext endpoints
    }
    
    override suspend fun getCapabilities(endpoint: OnvifEndpoint, auth: Auth?): Capabilities = withContext(Dispatchers.IO) {
        val soapMessage = createGetCapabilitiesMessage()
        val response = sendSoapRequest(endpoint.deviceService, soapMessage, auth)
        
        return@withContext parseCapabilitiesResponse(response)
    }
    
    override suspend fun getDeviceInformation(endpoint: OnvifEndpoint, auth: Auth?): DeviceInfo = withContext(Dispatchers.IO) {
        val soapMessage = createGetDeviceInformationMessage()
        val response = sendSoapRequest(endpoint.deviceService, soapMessage, auth)
        
        return@withContext parseDeviceInformationResponse(response)
    }
    
    override suspend fun getProfiles(endpoint: OnvifEndpoint, auth: Auth?): List<MediaProfile> = withContext(Dispatchers.IO) {
        val soapMessage = createGetProfilesMessage()
        val response = sendSoapRequest(endpoint.deviceService, soapMessage, auth)
        
        return@withContext parseProfilesResponse(response)
    }
    
    override suspend fun getStreamUri(endpoint: OnvifEndpoint, profileToken: String, auth: Auth?): String = withContext(Dispatchers.IO) {
        val soapMessage = createGetStreamUriMessage(profileToken)
        val response = sendSoapRequest(endpoint.deviceService, soapMessage, auth)
        
        return@withContext parseStreamUriResponse(response)
    }
    
    override suspend fun ptzContinuousMove(endpoint: OnvifEndpoint, vx: Float, vy: Float, vz: Float, auth: Auth?) {
        val soapMessage = createPtzContinuousMoveMessage(vx, vy, vz)
        sendSoapRequest(endpoint.deviceService, soapMessage, auth)
    }
    
    override suspend fun ptzStop(endpoint: OnvifEndpoint, auth: Auth?) {
        val soapMessage = createPtzStopMessage()
        sendSoapRequest(endpoint.deviceService, soapMessage, auth)
    }
    
    private fun createProbeMessage(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" xmlns:wsd="http://schemas.xmlsoap.org/ws/2005/04/discovery">
                <soap:Header>
                    <wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>
                    <wsa:MessageID>urn:uuid:${java.util.UUID.randomUUID()}</wsa:MessageID>
                    <wsa:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>
                </soap:Header>
                <soap:Body>
                    <wsd:Probe>
                        <wsd:Types>NetworkVideoTransmitter</wsd:Types>
                    </wsd:Probe>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private fun parseProbeResponse(response: String, ip: String): OnvifEndpoint? {
        return try {
            val doc = parseXml(response)
            val deviceServiceElement = doc.getElementsByTagName("XAddrs").item(0) as? Element
            val typesElement = doc.getElementsByTagName("Types").item(0) as? Element
            
            val deviceService = deviceServiceElement?.textContent ?: ""
            val types = typesElement?.textContent?.split(" ") ?: emptyList()
            
            if (deviceService != null) {
                OnvifEndpoint(ip = ip, deviceService = deviceService, types = types)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createGetCapabilitiesMessage(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:wsdl="http://www.onvif.org/ver10/device/wsdl">
                <soap:Body>
                    <wsdl:GetCapabilities>
                        <wsdl:Category>All</wsdl:Category>
                    </wsdl:GetCapabilities>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private fun createGetDeviceInformationMessage(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:wsdl="http://www.onvif.org/ver10/device/wsdl">
                <soap:Body>
                    <wsdl:GetDeviceInformation/>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private fun createGetProfilesMessage(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:wsdl="http://www.onvif.org/ver10/media/wsdl">
                <soap:Body>
                    <wsdl:GetProfiles/>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private fun createGetStreamUriMessage(profileToken: String): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:wsdl="http://www.onvif.org/ver10/media/wsdl">
                <soap:Body>
                    <wsdl:GetStreamUri>
                        <wsdl:StreamSetup>
                            <wsdl:Stream>RTP-Unicast</wsdl:Stream>
                            <wsdl:Transport>
                                <wsdl:Protocol>RTSP</wsdl:Protocol>
                            </wsdl:Transport>
                        </wsdl:StreamSetup>
                        <wsdl:ProfileToken>$profileToken</wsdl:ProfileToken>
                    </wsdl:GetStreamUri>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private fun createPtzContinuousMoveMessage(vx: Float, vy: Float, vz: Float): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:wsdl="http://www.onvif.org/ver20/ptz/wsdl">
                <soap:Body>
                    <wsdl:ContinuousMove>
                        <wsdl:ProfileToken>Profile_1</wsdl:ProfileToken>
                        <wsdl:Velocity>
                            <wsdl:PanTilt x="$vx" y="$vy"/>
                            <wsdl:Zoom x="$vz"/>
                        </wsdl:Velocity>
                    </wsdl:ContinuousMove>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private fun createPtzStopMessage(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:wsdl="http://www.onvif.org/ver20/ptz/wsdl">
                <soap:Body>
                    <wsdl:Stop>
                        <wsdl:ProfileToken>Profile_1</wsdl:ProfileToken>
                        <wsdl:PanTilt>true</wsdl:PanTilt>
                        <wsdl:Zoom>true</wsdl:Zoom>
                    </wsdl:Stop>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private suspend fun sendSoapRequest(url: String, soapMessage: String, auth: Auth?): String {
        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/soap+xml; charset=utf-8")
            .post(RequestBody.create("application/soap+xml".toMediaType(), soapMessage))
        
        if (auth != null) {
            val credentials = okhttp3.Credentials.basic(auth.username, auth.password)
            requestBuilder.addHeader("Authorization", credentials)
        }
        
        val request = requestBuilder.build()
        val response = httpClient.newCall(request).execute()
        
        return response.body?.string() ?: ""
    }
    
    private fun parseXml(xml: String): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        return builder.parse(xml.byteInputStream())
    }
    
    private fun parseCapabilitiesResponse(response: String): Capabilities {
        return try {
            val doc = parseXml(response)
            Capabilities(
                device = getElementText(doc, "Device"),
                media = getElementText(doc, "Media"),
                ptz = getElementText(doc, "PTZ")
            )
        } catch (e: Exception) {
            Capabilities()
        }
    }
    
    private fun parseDeviceInformationResponse(response: String): DeviceInfo {
        return try {
            val doc = parseXml(response)
            DeviceInfo(
                manufacturer = getElementText(doc, "Manufacturer"),
                model = getElementText(doc, "Model"),
                serialNumber = getElementText(doc, "SerialNumber"),
                hardwareId = getElementText(doc, "HardwareId")
            )
        } catch (e: Exception) {
            DeviceInfo()
        }
    }
    
    private fun parseProfilesResponse(response: String): List<MediaProfile> {
        return try {
            val doc = parseXml(response)
            val profiles = mutableListOf<MediaProfile>()
            val profileElements = doc.getElementsByTagName("Profiles")
            
            for (i in 0 until profileElements.length) {
                val element = profileElements.item(i) as Element
                val token = element.getAttribute("token")
                val name = getElementText(element, "Name") ?: ""
                
                profiles.add(MediaProfile(token = token, name = name))
            }
            
            profiles
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseStreamUriResponse(response: String): String {
        return try {
            val doc = parseXml(response)
            getElementText(doc, "Uri") ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun getElementText(doc: Document, tagName: String): String? {
        val element = doc.getElementsByTagName(tagName).item(0) as? Element
        return element?.textContent
    }
    
    private fun getElementText(element: Element, tagName: String): String? {
        val childElement = element.getElementsByTagName(tagName).item(0) as? Element
        return childElement?.textContent
    }
}
