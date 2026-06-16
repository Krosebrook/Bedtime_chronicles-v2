package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("KEY_DARK_MODE", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isMidnightMode = MutableStateFlow(prefs.getBoolean("KEY_MIDNIGHT_MODE", false))
    val isMidnightMode: StateFlow<Boolean> = _isMidnightMode.asStateFlow()

    private val _ttsPitch = MutableStateFlow(prefs.getFloat("KEY_TTS_PITCH", 1.0f))
    val ttsPitch: StateFlow<Float> = _ttsPitch.asStateFlow()

    private val _ttsRate = MutableStateFlow(prefs.getFloat("KEY_TTS_RATE", 1.0f))
    val ttsRate: StateFlow<Float> = _ttsRate.asStateFlow()

    private val _ttsNarrator = MutableStateFlow(prefs.getString("KEY_TTS_NARRATOR", "default") ?: "default")
    val ttsNarrator: StateFlow<String> = _ttsNarrator.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("KEY_DARK_MODE", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setMidnightMode(enabled: Boolean) {
        prefs.edit().putBoolean("KEY_MIDNIGHT_MODE", enabled).apply()
        _isMidnightMode.value = enabled
    }

    fun setTtsPitch(pitch: Float) {
        prefs.edit().putFloat("KEY_TTS_PITCH", pitch).apply()
        _ttsPitch.value = pitch
    }

    fun setTtsRate(rate: Float) {
        prefs.edit().putFloat("KEY_TTS_RATE", rate).apply()
        _ttsRate.value = rate
    }

    fun setTtsNarrator(narrator: String) {
        prefs.edit().putString("KEY_TTS_NARRATOR", narrator).apply()
        _ttsNarrator.value = narrator
    }

    fun getStoryProgress(storyId: String): Int {
        return prefs.getInt("STORY_PROGRESS_$storyId", 0)
    }

    fun setStoryProgress(storyId: String, sentenceIndex: Int) {
        prefs.edit().putInt("STORY_PROGRESS_$storyId", sentenceIndex).apply()
    }

    fun getAmbientSoundscape(): String {
        return prefs.getString("AMBIENT_SOUNDSCAPE", "Off") ?: "Off"
    }

    fun setAmbientSoundscape(soundscape: String) {
        prefs.edit().putString("AMBIENT_SOUNDSCAPE", soundscape).apply()
    }

    fun getAmbientVolume(): Float {
        return prefs.getFloat("AMBIENT_VOLUME", 0.5f)
    }

    fun getSavedStory(storyId: String): Boolean {
        return prefs.getBoolean("SAVED_STORY_$storyId", false)
    }

    fun setSavedStory(storyId: String, saved: Boolean) {
        prefs.edit().putBoolean("SAVED_STORY_$storyId", saved).apply()
    }

    fun setAmbientVolume(volume: Float) {
        prefs.edit().putFloat("AMBIENT_VOLUME", volume).apply()
    }

    fun getStickerBackground(): String {
        return prefs.getString("KEY_STICKER_BACKGROUND", "nebula") ?: "nebula"
    }

    fun setStickerBackground(bg: String) {
        prefs.edit().putString("KEY_STICKER_BACKGROUND", bg).apply()
    }

    // --- BEDTIME CHRONICLES GAME SYSTEM STATS ---

    fun getCompletedStoryCount(): Int {
        return prefs.getInt("STAT_COMPLETED_STORIES", 0)
    }

    fun incrementCompletedStoryCount(): Int {
        val current = getCompletedStoryCount()
        val next = current + 1
        prefs.edit().putInt("STAT_COMPLETED_STORIES", next).apply()
        return next
    }

    fun getVocabularyWordsLearnedCount(): Int {
        return prefs.getInt("STAT_VOCAB_LEARNED", 0)
    }

    fun incrementVocabularyWordsLearnedCount(): Int {
        val current = getVocabularyWordsLearnedCount()
        val next = current + 1
        prefs.edit().putInt("STAT_VOCAB_LEARNED", next).apply()
        return next
    }

    fun getReadingStreak(): Int {
        return prefs.getInt("STAT_READING_STREAK", 0)
    }

    fun setReadingStreak(streak: Int) {
        prefs.edit().putInt("STAT_READING_STREAK", streak).apply()
    }

    fun incrementReadingStreak() {
        val current = getReadingStreak()
        prefs.edit().putInt("STAT_READING_STREAK", current + 1).apply()
    }

    fun getHeroUsedStatus(heroName: String): Boolean {
        return prefs.getBoolean("HERO_USED_${heroName.lowercase().trim()}", false)
    }

    fun setHeroUsed(heroName: String) {
        prefs.edit().putBoolean("HERO_USED_${heroName.lowercase().trim()}", true).apply()
    }

    fun getUsedHeroesCount(): Int {
        val heroNames = listOf("nova", "coral", "orion", "luna", "nimbus", "bloom", "whistle", "shade")
        return heroNames.count { prefs.getBoolean("HERO_USED_$it", false) }
    }

    fun getModeStoryCount(mode: String): Int {
        return prefs.getInt("STAT_MODE_${mode.lowercase()}", 0)
    }

    fun incrementModeStoryCount(mode: String): Int {
        val current = getModeStoryCount(mode)
        val next = current + 1
        prefs.edit().putInt("STAT_MODE_${mode.lowercase()}", next).apply()
        return next
    }

    fun isBadgeUnlocked(badgeId: String): Boolean {
        // Special case: make "first-adventure" unlocked by default as preloaded story experience
        if (badgeId == "first-adventure" && getCompletedStoryCount() == 0) {
            return true
        }
        return prefs.getBoolean("BADGE_UNLOCKED_$badgeId", false)
    }

    fun unlockBadge(badgeId: String): Boolean {
        if (!isBadgeUnlocked(badgeId)) {
            prefs.edit().putBoolean("BADGE_UNLOCKED_$badgeId", true).apply()
            return true
        }
        return false
    }

    fun getUnlockedBadges(): Set<String> {
        val badges = listOf(
            "first-adventure", "night-owl", "early-bird", "all-heroes",
            "mad-libs-master", "dream-weaver", "classic-champion",
            "story-streak-3", "story-streak-7", "bookworm", "legend", "vocabulary-star"
        )
        return badges.filter { isBadgeUnlocked(it) }.toSet()
    }

    companion object {
        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferences(context).also { INSTANCE = it }
            }
        }
    }
}
