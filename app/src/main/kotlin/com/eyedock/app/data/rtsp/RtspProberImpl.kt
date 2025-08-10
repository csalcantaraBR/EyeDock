package com.eyedock.app.data.rtsp

import com.eyedock.app.domain.interfaces.RtspProber
import com.eyedock.app.domain.model.Auth
import com.eyedock.app.domain.model.RtspOptions
import com.eyedock.app.domain.model.Sdp
import com.eyedock.app.domain.model.SdpMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.net.URL

class RtspProberImpl(
    private val httpClient: OkHttpClient
) : RtspProber {
    
    override suspend fun options(uri: String, auth: Auth?): RtspOptions = withContext(Dispatchers.IO) {
        val rtspUrl = URL(uri)
        val requestBuilder = Request.Builder()
            .url(uri)
            .method("OPTIONS", null)
        
        if (auth != null) {
            val credentials = okhttp3.Credentials.basic(auth.username, auth.password)
            requestBuilder.addHeader("Authorization", credentials)
        }
        
        val request = requestBuilder.build()
        
        return@withContext try {
            val response = httpClient.newCall(request).execute()
            val methods = response.header("Public")?.split(",")?.map { it.trim() } ?: emptyList()
            val allow = response.header("Allow")?.split(",")?.map { it.trim() } ?: emptyList()
            
            RtspOptions(
                methods = allow,
                public = methods
            )
        } catch (e: IOException) {
            RtspOptions()
        }
    }
    
    override suspend fun describe(uri: String, auth: Auth?): Sdp = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder()
            .url(uri)
            .method("DESCRIBE", null)
            .addHeader("Accept", "application/sdp")
        
        if (auth != null) {
            val credentials = okhttp3.Credentials.basic(auth.username, auth.password)
            requestBuilder.addHeader("Authorization", credentials)
        }
        
        val request = requestBuilder.build()
        
        return@withContext try {
            val response = httpClient.newCall(request).execute()
            val sdpContent = response.body?.string() ?: ""
            parseSdp(sdpContent)
        } catch (e: IOException) {
            Sdp()
        }
    }
    
    private fun parseSdp(sdpContent: String): Sdp {
        val media = mutableListOf<SdpMedia>()
        val lines = sdpContent.split("\n")
        
        var currentMedia: SdpMedia? = null
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine.startsWith("m=") -> {
                    // Media line: m=<media> <port> <proto> <fmt> ...
                    val parts = trimmedLine.substring(2).split(" ")
                    if (parts.size >= 3) {
                        val mediaType = parts[0]
                        val port = parts[1].toIntOrNull() ?: 0
                        val protocol = parts[2]
                        val payloads = parts.drop(3).mapNotNull { it.toIntOrNull() }
                        
                        currentMedia = SdpMedia(
                            type = mediaType,
                            port = port,
                            protocol = protocol,
                            payloads = payloads
                        )
                        media.add(currentMedia)
                    }
                }
                trimmedLine.startsWith("a=rtpmap:") && currentMedia != null -> {
                    // RTP mapping line
                    // a=rtpmap:<payload_type> <encoding_name>/<clock_rate>[/<encoding_parameters>]
                    val parts = trimmedLine.substring(9).split(" ")
                    if (parts.size >= 2) {
                        val payloadType = parts[0].toIntOrNull()
                        val encodingInfo = parts[1].split("/")
                        if (encodingInfo.size >= 2) {
                            val encodingName = encodingInfo[0]
                            // Atualizar o media atual com informações de codec
                            val index = media.indexOf(currentMedia)
                            if (index >= 0) {
                                media[index] = currentMedia.copy(
                                    payloads = currentMedia.payloads + (payloadType ?: 0)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        return Sdp(media = media)
    }
}
