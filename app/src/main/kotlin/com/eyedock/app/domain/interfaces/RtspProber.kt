package com.eyedock.app.domain.interfaces

import com.eyedock.app.domain.model.Auth
import com.eyedock.app.domain.model.RtspOptions
import com.eyedock.app.domain.model.Sdp

interface RtspProber {
    suspend fun options(uri: String, auth: Auth?): RtspOptions
    suspend fun describe(uri: String, auth: Auth?): Sdp
}
