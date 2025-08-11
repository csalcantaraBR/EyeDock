package com.eyedock.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cameras")
data class CameraEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ip: String,
    val port: Int,
    val username: String?,
    val password: String?,
    val protocol: String,
    val path: String,
    val manufacturer: String?,
    val model: String?,
    val isOnline: Boolean = false,
    val lastSeen: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
