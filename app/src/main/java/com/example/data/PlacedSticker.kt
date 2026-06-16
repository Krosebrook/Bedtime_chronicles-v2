package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placed_stickers")
data class PlacedSticker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stickerId: String,
    val x: Float, // relative location X (0.0 to 1.0)
    val y: Float, // relative location Y (0.0 to 1.0)
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f
)
