package com.eyedock.app.data.onvif

import com.eyedock.app.domain.interfaces.OnvifClient
import com.eyedock.app.domain.interfaces.DeviceInfo
import com.eyedock.app.domain.interfaces.DeviceCapabilities
import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.model.StreamProfile
import com.eyedock.app.domain.model.OnvifInfo
import com.eyedock.app.domain.model.CameraMeta
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.StringWriter
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class OnvifClientImpl @Inject constructor(
    private val httpClient: OkHttpClient
) : OnvifClient {
    
    private val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    private val transformer = TransformerFactory.newInstance().newTransformer()
    
    override suspend fun discoverDevices(): List<CameraConnection> = withContext(Dispatchers.IO) {
        // Implementação básica - retorna lista vazia por enquanto
        // TODO: Implementar WS-Discovery
        emptyList()
    }
    
    override suspend fun getDeviceInformation(
        deviceUrl: String, 
        username: String?, 
        password: String?
    ): DeviceInfo? = withContext(Dispatchers.IO) {
        try {
            val soapRequest = createGetDeviceInformationRequest()
            val response = sendSoapRequest(deviceUrl, soapRequest, username, password)
            
            if (response != null) {
                parseDeviceInformationResponse(response)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getCapabilities(
        deviceUrl: String, 
        username: String?, 
        password: String?
    ): DeviceCapabilities? = withContext(Dispatchers.IO) {
        try {
            val soapRequest = createGetCapabilitiesRequest()
            val response = sendSoapRequest(deviceUrl, soapRequest, username, password)
            
            if (response != null) {
                parseCapabilitiesResponse(response, deviceUrl)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getProfiles(
        mediaUrl: String, 
        username: String?, 
        password: String?
    ): List<StreamProfile> = withContext(Dispatchers.IO) {
        try {
            val soapRequest = createGetProfilesRequest()
            val response = sendSoapRequest(mediaUrl, soapRequest, username, password)
            
            if (response != null) {
                parseProfilesResponse(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getStreamUri(
        mediaUrl: String, 
        profileToken: String, 
        username: String?, 
        password: String?
    ): String? = withContext(Dispatchers.IO) {
        try {
            val soapRequest = createGetStreamUriRequest(profileToken)
            val response = sendSoapRequest(mediaUrl, soapRequest, username, password)
            
            if (response != null) {
                parseStreamUriResponse(response)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun testConnection(
        deviceUrl: String, 
        username: String?, 
        password: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val capabilities = getCapabilities(deviceUrl, username, password)
            capabilities != null
        } catch (e: Exception) {
            false
        }
    }
    
    private fun createGetDeviceInformationRequest(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
                <soap:Body>
                    <tds:GetDeviceInformation xmlns:tds="http://www.onvif.org/ver10/device/wsdl"/>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private fun createGetCapabilitiesRequest(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
                <soap:Body>
                    <tds:GetCapabilities xmlns:tds="http://www.onvif.org/ver10/device/wsdl">
                        <tds:Category>All</tds:Category>
                    </tds:GetCapabilities>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private fun createGetProfilesRequest(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
                <soap:Body>
                    <trt:GetProfiles xmlns:trt="http://www.onvif.org/ver10/media/wsdl"/>
                </trt:GetProfiles>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private fun createGetStreamUriRequest(profileToken: String): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
                <soap:Body>
                    <trt:GetStreamUri xmlns:trt="http://www.onvif.org/ver10/media/wsdl">
                        <trt:StreamSetup>
                            <tt:Stream xmlns:tt="http://www.onvif.org/ver10/schema">RTP-Unicast</tt:Stream>
                            <tt:Transport xmlns:tt="http://www.onvif.org/ver10/schema">
                                <tt:Protocol>RTSP</tt:Protocol>
                            </tt:Transport>
                        </trt:StreamSetup>
                        <trt:ProfileToken>$profileToken</trt:ProfileToken>
                    </trt:GetStreamUri>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
    
    private suspend fun sendSoapRequest(
        url: String, 
        soapBody: String, 
        username: String?, 
        password: String?
    ): String? {
        return try {
            val requestBuilder = Request.Builder()
                .url(url)
                .post(RequestBody.create("application/soap+xml".toMediaType(), soapBody))
                .header("Content-Type", "application/soap+xml; charset=utf-8")
            
            // TODO: Implementar autenticação HTTP Basic/Digest
            
            val request = requestBuilder.build()
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseDeviceInformationResponse(response: String): DeviceInfo? {
        return try {
            val doc = documentBuilder.parse(response.byteInputStream())
            val manufacturer = getElementText(doc, "Manufacturer")
            val model = getElementText(doc, "Model")
            val firmwareVersion = getElementText(doc, "FirmwareVersion")
            val serialNumber = getElementText(doc, "SerialNumber")
            val hardwareId = getElementText(doc, "HardwareId")
            
            DeviceInfo(
                manufacturer = manufacturer,
                model = model,
                firmwareVersion = firmwareVersion,
                serialNumber = serialNumber,
                hardwareId = hardwareId
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseCapabilitiesResponse(response: String, deviceUrl: String): DeviceCapabilities? {
        return try {
            val doc = documentBuilder.parse(response.byteInputStream())
            val deviceService = getElementText(doc, "Device")
            val mediaService = getElementText(doc, "Media")
            val ptzService = getElementText(doc, "PTZ")
            
            DeviceCapabilities(
                deviceService = deviceService ?: deviceUrl,
                mediaService = mediaService,
                ptzService = ptzService,
                hasPtz = ptzService != null,
                hasImaging = true, // Assumindo que sempre tem imaging
                hasAudio = true    // Assumindo que sempre tem audio
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseProfilesResponse(response: String): List<StreamProfile> {
        return try {
            val doc = documentBuilder.parse(response.byteInputStream())
            val profiles = doc.getElementsByTagName("Profiles")
            val result = mutableListOf<StreamProfile>()
            
            for (i in 0 until profiles.length) {
                val profile = profiles.item(i) as Element
                val token = profile.getAttribute("token")
                val name = getElementText(profile, "Name") ?: "Profile $i"
                
                // TODO: Extrair mais informações do perfil (codec, resolution, etc.)
                
                result.add(
                    StreamProfile(
                        deviceId = "", // Será preenchido pelo repositório
                        token = token,
                        name = name,
                        streamUri = "", // Será obtido via getStreamUri
                        isMain = i == 0, // Primeiro perfil é o principal
                        isSub = i > 0    // Outros são sub-streams
                    )
                )
            }
            
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseStreamUriResponse(response: String): String? {
        return try {
            val doc = documentBuilder.parse(response.byteInputStream())
            getElementText(doc, "Uri")
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getElementText(doc: Document, tagName: String): String? {
        val elements = doc.getElementsByTagName(tagName)
        return if (elements.length > 0) {
            elements.item(0).textContent
        } else {
            null
        }
    }
    
    private fun getElementText(element: Element, tagName: String): String? {
        val elements = element.getElementsByTagName(tagName)
        return if (elements.length > 0) {
            elements.item(0).textContent
        } else {
            null
        }
    }
}
