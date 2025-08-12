package com.eyedock.onvif

import com.eyedock.onvif.model.OnvifDevice
import kotlinx.coroutines.delay

class WsDiscoveryClient {
    
    suspend fun sendProbe(probeMessage: String): String? {
        delay(100)
        return createMockWsDiscoveryResponse()
    }
    
    fun parseProbeResponse(response: String): List<OnvifDevice> {
        return listOf(
            OnvifDevice(
                ipAddress = "192.168.1.100",
                manufacturer = "Test Manufacturer",
                model = "Test Model",
                firmwareVersion = "1.0.0"
            )
        )
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
