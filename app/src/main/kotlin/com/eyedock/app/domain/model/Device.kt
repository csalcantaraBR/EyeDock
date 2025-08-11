package com.eyedock.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidade de persistência para dispositivos
 */
@Entity(tableName = "devices")
data class Device(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,                    // Nome amigável da câmera
    val brand: String? = null,           // Marca da câmera
    val model: String? = null,           // Modelo da câmera
    val uid: String? = null,             // UID/Serial proprietário
    val lastKnownIp: String? = null,     // Último IP conhecido
    val rtspHost: String? = null,        // Host RTSP
    val rtspPort: Int = 554,             // Porta RTSP
    val rtspPath: String? = null,        // Caminho RTSP
    val hasPtz: Boolean = false,         // Suporta PTZ
    val onvifDeviceService: String? = null, // URL do device service ONVIF
    val onvifMediaService: String? = null,  // URL do media service ONVIF
    val onvifPtzService: String? = null,    // URL do PTZ service ONVIF
    val isOnline: Boolean = false,       // Status online/offline
    val lastSeen: Long? = null,          // Última vez que foi vista
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entidade para credenciais criptografadas
 */
@Entity(tableName = "credentials")
data class Credential(
    val deviceId: String,                // ID do dispositivo
    val username: String,                // Usuário
    val passwordEncrypted: String,       // Senha criptografada
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entidade para perfis de stream
 */
@Entity(tableName = "stream_profiles")
data class StreamProfile(
    val deviceId: String,                // ID do dispositivo
    val token: String,                   // Token do perfil ONVIF
    val name: String,                    // Nome do perfil
    val streamUri: String,               // URI do stream
    val codec: String? = null,           // Codec (H.264, H.265)
    val resolution: String? = null,      // Resolução (1920x1080)
    val bitrate: Int? = null,            // Bitrate
    val frameRate: Int? = null,          // Frame rate
    val isMain: Boolean = false,         // É o stream principal
    val isSub: Boolean = false,          // É o sub-stream
    val isAudio: Boolean = false,        // É stream de áudio
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Cache de descoberta para dispositivos
 */
@Entity(tableName = "discovery_cache")
data class DiscoveryCache(
    val uid: String? = null,             // UID do dispositivo
    val mac: String? = null,             // MAC address
    val candidateIps: List<String> = emptyList(), // IPs candidatos
    val lastDiscovery: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 horas
)
