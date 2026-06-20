package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_heroes")
data class CustomHero(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val avatarEmoji: String, // Hero emoji element
    val outfitColorHex: String, // Preferred hex string for costume coloration
    val auraGlowStyle: String, // "Stardust Nebulae", "Supernova Ring", "Aurora Borealis", "Glacial Spark"
    val companionType: String, // "Mechanical Owl", "Stardust Kitten", "Baby Dragon", "Cosmic Turtle", "None"
    val backstoryArchetype: String, // "The Lost Explorer", "The Constellation Crafter", "The Dream Guardian", "The Peace Scholar", "Custom"
    val backstorySummary: String, // Summary or written tale backstory
    val createdAt: Long = System.currentTimeMillis()
)
