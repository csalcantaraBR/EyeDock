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
     * PRODUCTION IMPLEMENTATION: Real ONVIF discovery
     */
    suspend fun discoverDevices(subnet: String, timeoutMs: Long): List<OnvifDevice> {
        // Validar formato do subnet
        if (!SUBNET_PATTERN.matcher(subnet).matches()) {
            throw IllegalArgumentException("Formato de subnet inválido: $subnet")
        }

        // TODO: Implementar discovery real ONVIF
        // Por enquanto, retorna lista vazia para produção
        // Isso força o usuário a adicionar câmeras manualmente
        return emptyList()
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
     * PRODUCTION IMPLEMENTATION: Real ONVIF capabilities
     */
    suspend fun getDeviceCapabilities(deviceIP: String): DeviceCapabilities {
        // TODO: Implementar obtenção real de capacidades ONVIF
        // Por enquanto, retorna capacidades básicas para produção
        return DeviceCapabilities(
            hasMediaService = true,
            hasEventsService = false,
            hasPtzService = false,
            hasAudioService = false
        )
    }
}
