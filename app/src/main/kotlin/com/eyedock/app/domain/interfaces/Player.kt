package com.eyedock.app.domain.interfaces

import com.eyedock.app.domain.model.Auth

interface Player {
    fun play(uri: String, auth: Auth?)
    fun stop()
    fun snapshot(): ByteArray?
}
