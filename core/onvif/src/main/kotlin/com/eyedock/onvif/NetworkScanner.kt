package com.eyedock.onvif

import kotlinx.coroutines.delay

class NetworkScanner {
    
    suspend fun scanSubnet(subnet: String): List<String> {
        delay(100)
        return listOf("192.168.1.1", "192.168.1.100", "192.168.1.254")
    }
    
    suspend fun scanPorts(ip: String, ports: List<Int>): List<Int> {
        delay(50)
        return listOf(80, 8080) // Portas que "respondem"
    }
    
    fun isValidIpAddress(ip: String): Boolean {
        return ip.matches(Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"))
    }
}
