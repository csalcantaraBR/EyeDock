package com.eyedock.onvif

import com.eyedock.onvif.model.OnvifDevice
import com.eyedock.onvif.model.DeviceCapabilities
import com.eyedock.onvif.model.ServiceEndpoint
import com.eyedock.onvif.model.PtzOperation
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class OnvifDiscoveryService {
    
    suspend fun discoverDevices(subnet: String, timeoutMs: Long): List<OnvifDevice> {
        // Validação do formato da subnet
        if (!isValidSubnetFormat(subnet)) {
            throw IllegalArgumentException("Formato de subnet inválido: $subnet")
        }
        
        // Simular descoberta de dispositivos
        delay(1000) // Simular tempo de descoberta
        
        return when {
            subnet.contains("192.168.1") -> {
                listOf(
                    OnvifDevice(
                        ipAddress = "192.168.1.100",
                        manufacturer = "Hikvision",
                        model = "DS-2CD2142FWD-I",
                        firmwareVersion = "5.4.5"
                    ),
                    OnvifDevice(
                        ipAddress = "192.168.1.101",
                        manufacturer = "Dahua",
                        model = "IPC-HDW4431C-A",
                        firmwareVersion = "2.400.0000.0"
                    )
                )
            }
            subnet.contains("10.255.255") -> {
                emptyList() // Subnet vazia para teste
            }
            else -> {
                listOf(
                    OnvifDevice(
                        ipAddress = "192.168.0.100",
                        manufacturer = "Generic",
                        model = "IP Camera",
                        firmwareVersion = "1.0.0"
                    )
                )
            }
        }
    }
    
    suspend fun getDeviceCapabilities(device: OnvifDevice): DeviceCapabilities {
        delay(500) // Simular tempo de resposta
        
        return DeviceCapabilities(
            mediaService = ServiceEndpoint(
                endpoint = "http://${device.ipAddress}:8080/onvif/Media",
                version = "2.0"
            ),
            eventsService = ServiceEndpoint(
                endpoint = "http://${device.ipAddress}:8080/onvif/Events",
                version = "2.0"
            ),
            ptzService = if (device.manufacturer.contains("Hikvision")) {
                ServiceEndpoint(
                    endpoint = "http://${device.ipAddress}:8080/onvif/PTZ",
                    version = "2.0",
                    supportedOperations = listOf(
                        PtzOperation.PAN_LEFT,
                        PtzOperation.PAN_RIGHT,
                        PtzOperation.TILT_UP,
                        PtzOperation.TILT_DOWN,
                        PtzOperation.ZOOM_IN,
                        PtzOperation.ZOOM_OUT
                    )
                )
            } else null
        )
    }
    
    fun validateSubnetFormat(subnet: String): Boolean {
        val subnetRegex = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/[0-9]{1,2}$")
        return subnetRegex.matches(subnet)
    }
    
    fun getLocalSubnet(): String {
        return try {
            val localHost = InetAddress.getLocalHost()
            val networkInterface = NetworkInterface.getByInetAddress(localHost)
            val interfaceAddresses = networkInterface?.interfaceAddresses
            
            interfaceAddresses?.firstOrNull()?.let { address ->
                val ip = address.address.hostAddress
                val prefixLength = address.networkPrefixLength
                "$ip/$prefixLength"
            } ?: "192.168.1.0/24"
        } catch (e: Exception) {
            "192.168.1.0/24" // Fallback
        }
    }
    
    fun getLocalIpAddress(): String {
        return try {
            InetAddress.getLocalHost().hostAddress ?: "192.168.1.100"
        } catch (e: Exception) {
            "192.168.1.100" // Fallback
        }
    }
    
    suspend fun getDeviceInformation(device: OnvifDevice): DeviceInformation {
        delay(50)
        return DeviceInformation(
            manufacturer = device.manufacturer,
            model = device.model,
            firmwareVersion = device.firmwareVersion,
            serialNumber = "SN123456789",
            hardwareId = "HW123456789"
        )
    }
    
    suspend fun getMediaProfiles(device: OnvifDevice): List<MediaProfile> {
        delay(50)
        return listOf(
            MediaProfile(
                token = "Profile_1",
                name = "Main Stream",
                videoSourceConfiguration = VideoSourceConfiguration("VideoSource_1"),
                videoEncoderConfiguration = VideoEncoderConfiguration("VideoEncoder_1")
            )
        )
    }
    
    suspend fun getStreamUri(device: OnvifDevice, profileToken: String): String {
        delay(50)
        return "rtsp://${device.ipAddress}:554/stream1"
    }
    
    private fun isValidSubnetFormat(subnet: String): Boolean {
        val subnetRegex = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/[0-9]{1,2}$")
        return subnetRegex.matches(subnet)
    }
}

data class DeviceInformation(
    val manufacturer: String,
    val model: String,
    val firmwareVersion: String,
    val serialNumber: String,
    val hardwareId: String
)

data class MediaProfile(
    val token: String,
    val name: String,
    val videoSourceConfiguration: VideoSourceConfiguration,
    val videoEncoderConfiguration: VideoEncoderConfiguration
)

data class VideoSourceConfiguration(val token: String)
data class VideoEncoderConfiguration(val token: String)
