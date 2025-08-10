package com.eyedock.app.domain.interfaces

import com.eyedock.app.domain.model.*

interface OnvifClient {
    suspend fun probe(timeoutMs: Long = 2500): List<OnvifEndpoint>
    suspend fun getCapabilities(endpoint: OnvifEndpoint, auth: Auth?): Capabilities
    suspend fun getDeviceInformation(endpoint: OnvifEndpoint, auth: Auth?): DeviceInfo
    suspend fun getProfiles(endpoint: OnvifEndpoint, auth: Auth?): List<MediaProfile>
    suspend fun getStreamUri(endpoint: OnvifEndpoint, profileToken: String, auth: Auth?): String
    suspend fun ptzContinuousMove(endpoint: OnvifEndpoint, vx: Float, vy: Float, vz: Float, auth: Auth?)
    suspend fun ptzStop(endpoint: OnvifEndpoint, auth: Auth?)
}
