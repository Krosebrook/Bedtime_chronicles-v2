package com.example.data

import com.tencent.mmkv.MMKV
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

object RecentlyViewedManager {
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val json = Json { ignoreUnknownKeys = true }
    private const val RECENT_STORIES_KEY = "recently_viewed_stories"
    private const val MAX_RECENT_STORIES = 10

    /**
     * Adds a story to the list of recently viewed stories.
     * Keeps up to MAX_RECENT_STORIES. If already present, moves it to the top.
     */
    fun addRecentlyViewed(story: GeneratedStoryContent) {
        try {
            val currentStories = getRecentlyViewed().toMutableList()
            // Remove existing duplicate if present
            currentStories.removeAll { it.id == story.id }
            // Add to the front of the list
            currentStories.add(0, story)
            // Limit to max count
            val limitedStories = if (currentStories.size > MAX_RECENT_STORIES) {
                currentStories.subList(0, MAX_RECENT_STORIES)
            } else {
                currentStories
            }
            // Serialize and save to MMKV
            val serialized = json.encodeToString(ListSerializer(GeneratedStoryContent.serializer()), limitedStories)
            mmkv.encode(RECENT_STORIES_KEY, serialized)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves the list of recently viewed stories ordered from most recent to oldest.
     */
    fun getRecentlyViewed(): List<GeneratedStoryContent> {
        val serialized = mmkv.decodeString(RECENT_STORIES_KEY) ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(GeneratedStoryContent.serializer()), serialized)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Clears all recently viewed stories.
     */
    fun clearRecentlyViewed() {
        try {
            mmkv.removeValueForKey(RECENT_STORIES_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
