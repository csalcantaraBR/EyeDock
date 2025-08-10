package com.eyedock.app.domain.interfaces

import com.eyedock.app.domain.model.CameraConnection
import com.eyedock.app.domain.model.QrPayload

interface Normalizer {
    suspend fun normalize(payload: QrPayload): CameraConnection
}
