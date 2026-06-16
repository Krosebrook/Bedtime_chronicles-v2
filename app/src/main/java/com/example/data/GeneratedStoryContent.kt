package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "generated_stories")
@Serializable
data class GeneratedStoryContent(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val coverImageUrl: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)
