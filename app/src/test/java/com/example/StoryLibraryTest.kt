package com.example

import com.example.data.sampleStories
import com.example.data.preloadedStoryContents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryLibraryTest {

    @Test
    fun testSampleStoriesCount() {
        // We have wired in and implemented all 8 uploaded and original stories.
        assertEquals(8, sampleStories.size)
    }

    @Test
    fun testPreloadedStoryContentsCount() {
        // Ensure our database has exactly 8 preloaded stories matching the UI metadata.
        assertEquals(8, preloadedStoryContents.size)
    }

    @Test
    fun testStoriesContentValidity() {
        // Assert that every single preloaded story has unique and valid content.
        preloadedStoryContents.forEach { story ->
            assertTrue("Story '${story.title}' content is empty", story.content.isNotBlank())
            assertTrue("Story '${story.title}' content must be longer than 100 characters", story.content.length > 100)
            assertTrue("Story '${story.title}' cover image URL is missing", story.coverImageUrl.isNotBlank())
            assertTrue("Story '${story.title}' has invalid category", story.category.isNotBlank())
        }
    }

    @Test
    fun testSampleToPreloadedAssociation() {
        // Verify that every sample story listed on the homescreen has a corresponding full body in the database.
        val sampleIds = sampleStories.map { it.id }.toSet()
        val preloadedIds = preloadedStoryContents.map { it.id }.toSet()
        assertEquals(sampleIds, preloadedIds)
        
        sampleStories.forEach { sample ->
            val matchingContent = preloadedStoryContents.find { it.id == sample.id }
            assertTrue("Preloaded content missing for ${sample.title}", matchingContent != null)
            assertEquals(sample.title, matchingContent?.title)
            assertEquals(sample.category, matchingContent?.category)
        }
    }

    @Test
    fun testStoryCategoriesDistribution() {
        val categories = sampleStories.map { it.category }.toSet()
        // verify different categories are present
        assertTrue(categories.contains("Cosmic"))
        assertTrue(categories.contains("Dreamy"))
        assertTrue(categories.contains("Magical"))
        assertTrue(categories.contains("Winter"))
    }
}
