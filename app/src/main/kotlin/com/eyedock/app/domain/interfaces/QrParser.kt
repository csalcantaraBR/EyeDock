package com.eyedock.app.domain.interfaces

import com.eyedock.app.domain.model.QrPayload

interface QrParser {
    fun parse(text: String): QrPayload
}
