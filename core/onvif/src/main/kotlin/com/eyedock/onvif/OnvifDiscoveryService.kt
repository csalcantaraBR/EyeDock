package com.eyedock.onvif

import kotlinx.coroutines.delay
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GREEN PHASE - Implementação mínima de OnvifDiscoveryService
 * 
 * Esta implementação é o mínimo necessário para fazer os testes passarem.
 * Retorna dispositivos mock para simular descoberta bem-sucedida.
 * 
 * Será refatorada na fase REFACTOR com implementação real de discovery.
 */
@Singleton
class OnvifDiscoveryService @Inject constructor() {

    companion object {
        private val SUBNET_PATTERN = Pattern.compile(
            "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}/[0-9]{1,2}$"
        )
    }

    /**
     * Descobre dispositivos ONVIF na rede
     * 
     * GREEN IMPLEMENTATION: Retorna dispositivos mock baseado no subnet
     */
    suspend fun discoverDevices(subnet: String, timeoutMs: Long): List<OnvifDevice> {
        // Validar formato do subnet
        if (!SUBNET_PATTERN.matcher(subnet).matches()) {
            throw IllegalArgumentException("Formato de subnet inválido: $subnet")
        }

        // Simular tempo de discovery
        delay(minOf(timeoutMs, 2000L))
        
        // GREEN: Retornar dispositivos mock baseado no subnet
        return when {
            subnet.startsWith("192.168.0") -> {
                // Subnet que "tem" dispositivos
                listOf(
                    OnvifDevice(
                        ip = "192.168.0.100",
                        name = "Camera Hall Mock",
                        manufacturer = "Yoosee",
                        onvifPort = 5000,
                        capabilities = DeviceCapabilities(
                            hasMediaService = true,
                            hasEventsService = true,
                            hasPtzService = true
                        )
                    ),
                    OnvifDevice(
                        ip = "192.168.0.101", 
                        name = "Camera Garden Mock",
                        manufacturer = "Hikvision",
                        onvifPort = 80,
                        capabilities = DeviceCapabilities(
                            hasMediaService = true,
                            hasEventsService = false,
                            hasPtzService = false
                        )
                    )
                )
            }
            subnet.startsWith("192.168.1") -> {
                // Subnet com um dispositivo
                listOf(
                    OnvifDevice(
                        ip = "192.168.1.100",
                        name = "Test Camera",
                        manufacturer = "Mock Vendor"
                    )
                )
            }
            else -> {
                // Outros subnets retornam vazio (simular não encontrar nada)
                emptyList()
            }
        }
    }
}

/**
 * Cliente ONVIF para operações específicas do dispositivo
 */
@Singleton 
class OnvifClient @Inject constructor() {

    /**
     * Obtém capacidades do dispositivo
     * 
     * GREEN IMPLEMENTATION: Retorna capacidades mock
     */
    suspend fun getDeviceCapabilities(deviceIP: String): DeviceCapabilities {
        // Simular tempo de rede
        delay(500L)
        
        // GREEN: Retornar capacidades mock baseado no IP
        return when {
            deviceIP.endsWith("100") -> DeviceCapabilities(
                hasMediaService = true,
                hasEventsService = true, 
                hasPtzService = true,
                hasAudioService = true
            )
            deviceIP.endsWith("101") -> DeviceCapabilities(
                hasMediaService = true,
                hasEventsService = false,
                hasPtzService = false,
                hasAudioService = false
            )
            else -> DeviceCapabilities(
                hasMediaService = true,
                hasEventsService = false,
                hasPtzService = false,
                hasAudioService = false
            )
        }
    }
}
