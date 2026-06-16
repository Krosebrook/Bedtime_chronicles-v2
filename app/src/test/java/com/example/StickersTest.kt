package com.example

import com.example.data.availableStickers
import org.junit.Assert.*
import org.junit.Test

class StickersTest {

    @Test
    fun testStickerListNotEmpty() {
        assertFalse(availableStickers.isEmpty())
        assertTrue(availableStickers.size >= 10)
    }

    @Test
    fun testStickerUniqueIds() {
        val ids = availableStickers.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun testDefaultStickersHaveNoKeywords() {
        val defaultStickers = availableStickers.filter { it.unlockKeywords.isEmpty() }
        // Aura Star and Gliding Comet and Ringed Saturn should be unlocked by default
        assertTrue(defaultStickers.any { it.id == "star" })
        assertTrue(defaultStickers.any { it.id == "comet" })
        assertTrue(defaultStickers.any { it.id == "planet" })
    }

    @Test
    fun testUnlockKeywordValidity() {
        availableStickers.forEach { sticker ->
            assertFalse("Sticker name ${sticker.name} is blank", sticker.name.isBlank())
            assertFalse("Sticker emoji ${sticker.name} is blank", sticker.emoji.isBlank())
            assertFalse("Sticker hint ${sticker.name} is blank", sticker.unlockHint.isBlank())
            
            if (sticker.id != "star" && sticker.id != "comet" && sticker.id != "planet") {
                assertFalse("Locked sticker ${sticker.name} must have unlock keywords", sticker.unlockKeywords.isEmpty())
            }
        }
    }

    @Test
    fun testKeywordMatchingSimulation() {
        val whaleSticker = availableStickers.find { it.id == "whale" }
        assertNotNull(whaleSticker)

        // Simulate reading a story paragraph
        val storyContent = "Once upon a time in deep water there lived a small baby whale."
        val hasMatch = whaleSticker!!.unlockKeywords.any { keyword ->
            storyContent.lowercase().contains(keyword)
        }
        assertTrue("Keyword match failed for whale sticker", hasMatch)
    }
}
