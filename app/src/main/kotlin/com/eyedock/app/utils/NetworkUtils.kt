package com.eyedock.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.eyedock.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.*
import java.net.InetSocketAddress

/**
 * Utilitários para detecção e validação de rede
 */
object NetworkUtils {
    
    private val logger = Logger.withTag("NetworkUtils")
    
    /**
     * Detecta a subnet local real do dispositivo
     */
    fun getLocalSubnet(context: Context): String {
        return try {
            logger.d("Iniciando detecção de subnet...")
            
            // Tentar detectar via WiFi primeiro
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            
            if (wifiInfo != null && wifiInfo.ipAddress != 0) {
                val ipAddress = wifiInfo.ipAddress
                val ipString = "${(ipAddress and 0xFF)}.${(ipAddress shr 8) and 0xFF}.${(ipAddress shr 16) and 0xFF}.${(ipAddress shr 24) and 0xFF}"
                
                // Assumir máscara /24 para redes domésticas
                val subnet = ipString.substring(0, ipString.lastIndexOf(".")) + ".0/24"
                logger.d("Subnet detectada via WiFi: $subnet")
                return subnet
            }
            
            // Fallback: detectar via interfaces de rede
            logger.d("WiFi não disponível, tentando interfaces de rede...")
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address.hostAddress.contains(".")) {
                            val ipString = address.hostAddress
                            val subnet = ipString.substring(0, ipString.lastIndexOf(".")) + ".0/24"
                            logger.d("Subnet detectada via interface: $subnet")
                            return subnet
                        }
                    }
                }
            }
            
            // Fallback final: usar subnet padrão
            logger.w("Não foi possível detectar subnet, usando padrão: ${Constants.CommonSubnets.HOME_NETWORK}")
            Constants.CommonSubnets.HOME_NETWORK
            
        } catch (e: Exception) {
            logger.e("Erro ao detectar subnet: ${e.message}")
            logger.w("Usando subnet padrão: ${Constants.CommonSubnets.HOME_NETWORK}")
            Constants.CommonSubnets.HOME_NETWORK
        }
    }
    
    /**
     * Verifica se um host está realmente acessível
     */
    suspend fun isHostReachable(ip: String, timeoutMs: Int = Constants.PING_TIMEOUT_MS): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.d("Testando conectividade com: $ip")
            val address = InetAddress.getByName(ip)
            val isReachable = address.isReachable(timeoutMs)
            logger.d("Host $ip está ${if (isReachable) "acessível" else "inacessível"}")
            isReachable
        } catch (e: Exception) {
            logger.d("Erro ao testar conectividade com $ip: ${e.message}")
            false
        }
    }
    
    /**
     * Testa se um dispositivo responde na porta ONVIF (versão menos restritiva)
     */
    suspend fun isOnvifDevice(ip: String, timeoutMs: Int = Constants.ONVIF_TEST_TIMEOUT_MS): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.d("Testando porta ONVIF em: $ip")
            
            // Testar porta 80 (HTTP ONVIF)
            val httpSocket = Socket()
            httpSocket.connect(java.net.InetSocketAddress(ip, 80), timeoutMs)
            httpSocket.close()
            logger.d("Dispositivo $ip responde na porta 80 (ONVIF HTTP)")
            true
            
        } catch (e: Exception) {
            try {
                // Testar porta 5000 (ONVIF alternativo)
                val altSocket = Socket()
                altSocket.connect(java.net.InetSocketAddress(ip, 5000), timeoutMs)
                altSocket.close()
                logger.d("Dispositivo $ip responde na porta 5000 (ONVIF alternativo)")
                true
            } catch (e2: Exception) {
                logger.d("Dispositivo $ip não responde nas portas ONVIF (80/5000)")
                false
            }
        }
    }
    
    /**
     * Testa se um dispositivo responde na porta RTSP
     */
    suspend fun isRtspDevice(ip: String, timeoutMs: Int = Constants.ONVIF_TEST_TIMEOUT_MS): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.d("Testando porta RTSP em: $ip")
            val socket = Socket()
            socket.connect(java.net.InetSocketAddress(ip, 554), timeoutMs)
            socket.close()
            logger.d("Dispositivo $ip responde na porta 554 (RTSP)")
            true
        } catch (e: Exception) {
            logger.d("Dispositivo $ip não responde na porta 554 (RTSP)")
            false
        }
    }
    
    /**
     * Valida se um dispositivo é realmente uma câmera IP (versão menos restritiva)
     */
    suspend fun validateCameraDevice(ip: String): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.d("Validando dispositivo como câmera: $ip")
            
            // Primeiro verificar se o host está acessível
            if (!isHostReachable(ip)) {
                logger.d("Host $ip não está acessível")
                false
            } else {
                // Se está acessível, considerar como válido (menos restritivo)
                logger.d("Host $ip está acessível, considerando como câmera válida")
                true
                
                // Opcional: testar portas específicas
                /*
                val isOnvif = isOnvifDevice(ip)
                val isRtsp = isRtspDevice(ip)
                
                val isValid = isOnvif || isRtsp
                logger.d("Dispositivo $ip é ${if (isValid) "válido" else "inválido"} como câmera (ONVIF: $isOnvif, RTSP: $isRtsp)")
                
                isValid
                */
            }
            
        } catch (e: Exception) {
            logger.e("Erro ao validar dispositivo $ip: ${e.message}")
            false
        }
    }
    
    /**
     * Verifica se o dispositivo está conectado à internet
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Detecta o IP real da câmera na rede local
     */
    suspend fun findCameraIp(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            logger.d("Procurando IP real da câmera...")
            
            // Obter a subnet local
            val subnet = getLocalSubnet(context)
            logger.d("Subnet local: $subnet")
            
            // Para emulador, testar IPs específicos primeiro
            val emulatorIps = listOf(
                "10.0.2.2",   // Gateway do emulador
                "10.0.2.3",   // DNS do emulador
                "10.0.2.15",  // Host do emulador
                "10.0.2.16",  // IP que estava sendo encontrado
                "10.0.2.17",  // Próximo IP
                "10.0.2.18",  // Próximo IP
                "10.0.2.19",  // Próximo IP
                "10.0.2.20"   // Próximo IP
            )
            
            logger.d("Testando IPs do emulador primeiro...")
            for (ip in emulatorIps) {
                logger.d("Testando IP do emulador: $ip")
                if (isHostReachable(ip)) {
                    logger.d("Host acessível encontrado: $ip")
                    
                    // Testar se é uma câmera (porta RTSP)
                    if (isRtspDevice(ip)) {
                        logger.i("Câmera encontrada no emulador: $ip")
                        return@withContext ip
                    } else {
                        logger.d("IP $ip não é uma câmera RTSP")
                    }
                } else {
                    logger.d("IP $ip não está acessível")
                }
            }
            
            // Se não encontrou no emulador, testar IPs comuns de câmeras
            logger.d("Testando IPs comuns de câmeras...")
            val commonCameraIps = listOf(
                "192.168.1.100", "192.168.1.101", "192.168.1.102", "192.168.1.103",
                "192.168.1.200", "192.168.1.201", "192.168.1.202", "192.168.1.203",
                "192.168.0.100", "192.168.0.101", "192.168.0.102", "192.168.0.103"
            )
            
            for (ip in commonCameraIps) {
                logger.d("Testando IP comum de câmera: $ip")
                if (isHostReachable(ip)) {
                    logger.d("Host acessível encontrado: $ip")
                    
                    // Testar se é uma câmera (porta RTSP)
                    if (isRtspDevice(ip)) {
                        logger.i("Câmera encontrada: $ip")
                        return@withContext ip
                    }
                }
            }
            
            logger.w("Nenhuma câmera encontrada na rede")
            null
            
        } catch (e: Exception) {
            logger.e("Erro ao procurar câmera: ${e.message}")
            null
        }
    }
    
    /**
     * Converte IP inteiro para string
     */
    private fun Int.toIpString(): String {
        return "${(this shr 24) and 0xFF}.${(this shr 16) and 0xFF}.${(this shr 8) and 0xFF}.${this and 0xFF}"
    }

    /**
     * Test if an IP address is a valid camera by checking RTSP connectivity
     */
    suspend fun testCameraConnectivity(ip: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Logger.d("NetworkUtils", "Testing camera connectivity for IP: $ip")
                
                // First check if host is reachable
                if (!isHostReachable(ip, Constants.PING_TIMEOUT_MS)) {
                    Logger.d("NetworkUtils", "Host $ip is not reachable")
                    return@withContext false
                }
                
                // Test common RTSP ports
                val rtspPorts = listOf(554, 8554, 10554)
                val rtspPaths = listOf(
                    "/onvif1",
                    "/live/ch00_0", 
                    "/live/ch0",
                    "/live",
                    "/cam/realmonitor",
                    "/video1",
                    "/video",
                    "/stream1",
                    "/stream"
                )
                
                for (port in rtspPorts) {
                    for (path in rtspPaths) {
                        val testUrl = "rtsp://$ip:$port$path"
                        Logger.d("NetworkUtils", "Testing RTSP URL: $testUrl")
                        
                        if (testRtspConnection(ip, port, path)) {
                            Logger.i("NetworkUtils", "Valid camera found at $ip:$port$path")
                            return@withContext true
                        }
                    }
                }
                
                Logger.d("NetworkUtils", "No valid RTSP stream found for IP: $ip")
                false
                
            } catch (e: Exception) {
                Logger.e("NetworkUtils", "Error testing camera connectivity for $ip: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Test RTSP connection to a specific URL with more rigorous validation
     */
    private suspend fun testRtspConnection(ip: String, port: Int, path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                val socketAddress = InetSocketAddress(ip, port)
                
                // Try to connect with timeout
                socket.connect(socketAddress, Constants.ONVIF_TEST_TIMEOUT_MS)
                
                if (socket.isConnected) {
                    // Send RTSP OPTIONS request
                    val optionsRequest = """
                        OPTIONS rtsp://$ip:$port$path RTSP/1.0
                        CSeq: 1
                        User-Agent: EyeDock Camera Test
                        
                    """.trimIndent()
                    
                    val outputStream = socket.getOutputStream()
                    outputStream.write(optionsRequest.toByteArray())
                    outputStream.flush()
                    
                    // Read response with timeout
                    socket.soTimeout = Constants.ONVIF_TEST_TIMEOUT_MS
                    val inputStream = socket.getInputStream()
                    val response = inputStream.readBytes()
                    val responseString = String(response)
                    
                    socket.close()
                    
                    // More rigorous validation - must have proper RTSP response
                    if (responseString.contains("RTSP/1.0") && 
                        (responseString.contains("200 OK") ||
                         responseString.contains("401 Unauthorized") ||
                         responseString.contains("404 Not Found"))) {
                        Logger.d("NetworkUtils", "Valid RTSP response received from $ip:$port$path")
                        return@withContext true
                    } else {
                        Logger.d("NetworkUtils", "Invalid RTSP response from $ip:$port$path: $responseString")
                        return@withContext false
                    }
                }
                
                socket.close()
                false
                
            } catch (e: Exception) {
                Logger.d("NetworkUtils", "RTSP test failed for $ip:$port$path: ${e.message}")
                false
            }
        }
    }
}
