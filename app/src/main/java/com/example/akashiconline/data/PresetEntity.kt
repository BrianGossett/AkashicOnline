package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val workSeconds: Int,
    val restSeconds: Int,
    val rounds: Int,
    val createdAt: Long,
)
