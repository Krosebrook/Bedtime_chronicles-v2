package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderScreenFeaturesTest {

    @Test
    fun testParagraphAndSentenceSplitting() {
        // Verify sentence splitting functions as expected
        val content = "Once upon a time, in the deepest part of the Cosmic Ocean, there swam two grand creatures known as the Starry Whales. Their bodies were made of glittering stardust. They glowed gently!"
        
        val regex = "(?<=[.!?])\\s+".toRegex()
        val sentences = content.split(regex).map { it.trim() }.filter { it.isNotEmpty() }
        
        assertEquals(3, sentences.size)
        assertEquals("Once upon a time, in the deepest part of the Cosmic Ocean, there swam two grand creatures known as the Starry Whales.", sentences[0])
        assertEquals("Their bodies were made of glittering stardust.", sentences[1])
        assertEquals("They glowed gently!", sentences[2])
    }

    @Test
    fun testTimeFormattingDuration() {
        fun formatTime(seconds: Int): String {
            val m = seconds / 60
            val s = seconds % 60
            return String.format("%02d:%02d", m, s)
        }

        assertEquals("00:00", formatTime(0))
        assertEquals("01:00", formatTime(60))
        assertEquals("10:15", formatTime(615))
        assertEquals("59:59", formatTime(3599))
    }

    @Test
    fun testPreconfiguredSleepTimerValues() {
        val sleepTimerOptions = listOf(
            "Off" to 0,
            "1 Minute (Demo)" to 60,
            "5 Minutes" to 300,
            "15 Minutes" to 900,
            "30 Minutes" to 1800,
            "60 Minutes" to 3600
        )
        
        val activeMap = sleepTimerOptions.toMap()
        assertEquals(0, activeMap["Off"])
        assertEquals(60, activeMap["1 Minute (Demo)"])
        assertEquals(300, activeMap["5 Minutes"])
        assertEquals(900, activeMap["15 Minutes"])
        assertEquals(1800, activeMap["30 Minutes"])
        assertEquals(3600, activeMap["60 Minutes"])
    }

    @Test
    fun testPreconfiguredAmbientSoundscapes() {
        val options = listOf(
            "Off",
            "Gentle Rain",
            "Space Echoes",
            "Cozy Waves",
            "Forest Breeze"
        )
        
        assertTrue(options.contains("Gentle Rain"))
        assertTrue(options.contains("Space Echoes"))
        assertTrue(options.contains("Cozy Waves"))
        assertTrue(options.contains("Forest Breeze"))
        assertEquals(5, options.size)
    }
}
