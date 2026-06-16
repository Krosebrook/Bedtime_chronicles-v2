package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.net.URLEncoder

sealed class GenerationState {
    object Idle : GenerationState()
    object Generating : GenerationState()
    data class Success(val story: GeneratedStoryContent) : GenerationState()
    data class Error(val message: String) : GenerationState()
}

class StoryGenerationViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val state: StateFlow<GenerationState> = _state
    
    private val database = DatabaseProvider.getDatabase(application)
    private val storyDao = database.generatedStoryDao()

    fun generateStory(heroName: String, theme: String, setting: String, keywords: String, narratorPersona: String = "Gentle Grandma") {
        if (_state.value is GenerationState.Generating) return
        _state.value = GenerationState.Generating
        
        viewModelScope.launch {
            try {
                // Determine persona styling prompt
                val personaPrompt = when (narratorPersona) {
                    "Gentle Grandma" -> "Narrated in the warm, slow-paced, and loving style of a Gentle Grandma telling a cozy bedtime tale. She uses nurturing language, comforting reminders that the hero is safe and sound, and gentle, loving expressions."
                    "Exciting Explorer" -> "Narrated in the enthusiastic, curious, and starry-eyed style of an Exciting Explorer. He is enthusiastic about discoveries but narrates in a soft, whispers-of-the-wild tone, inviting the reader to uncover gentle bedtime secrets."
                    "Classic Bard" -> "Narrated in the poetic, elegant, and rhythmic style of a Classic Bard. Use poetic cadence, gentle imagery, rich metaphors, and beautiful flowing prose to lull the listener to sleep."
                    "Cosmic Wizard" -> "Narrated in the serene, mysterious, and awe-inspiring style of a Cosmic Wizard who speaks with deep interstellar wisdom of stardust, quiet nebulae, and glowing magic, setting a peaceful galactic atmosphere."
                    else -> "Narrated in a standard, soothing bedtime storyteller voice."
                }

                // 1. Generate Story Text
                val prompt = "Write a short, soothing bedtime story (around 3 paragraphs) about a hero named $heroName in a $setting. " +
                             "The theme/genre is $theme. " +
                             "$personaPrompt " +
                             "Incorporate these elements or keywords into the story to make it unique and engaging: $keywords. " +
                             "Respond with exactly the title on the first line, then a blank line, then the story."
                val responseText = generateWithGemini(prompt)
                
                val lines = responseText.trim().split("\n")
                val title = lines.firstOrNull()?.removePrefix("**")?.removeSuffix("**") ?: "A Magical Journey"
                val content = lines.drop(2).joinToString("\n").trim()
                
                // 2. Generate Custom AI Illustration Prompt using Gemini based on Title and Story Plot Summary
                val illustrationPromptConfig = "Based on the bedtime story titled \"$title\" and its plot summary: \"${content.take(300)}...\", " +
                                               "write a detailed, beautiful, child-friendly 1-sentence prompt for an AI image generator. " +
                                               "The prompt should describe a single captivating, dreamlike scene featuring the hero or key elements from the story. " +
                                               "Specify a high-quality illustration style: minimalist bedtime vector illustration, magical feel, soft glow, and a calming children's storybook color scheme of deep indigo and stardust. " +
                                               "Do not include any text, letters, or words in the illustration. Keep it strictly focused on visual scenery."
                
                val customImagePrompt = try {
                    val aiPrompt = generateWithGemini(illustrationPromptConfig).trim()
                    if (aiPrompt.isNotBlank()) {
                        aiPrompt.removePrefix("\"").removeSuffix("\"").trim()
                    } else {
                        "A beautiful minimalist vector illustration of a $theme story titled $title, stardust, deep indigo, magical children's storybook"
                    }
                } catch (e: Exception) {
                    // Fallback if custom prompt generation fails
                    "A beautiful minimalist vector illustration of a $theme story set in a $setting, magical, highly detailed, deep indigo."
                }

                val coverImageUrl = generateImagesWithImagen(customImagePrompt)
                
                val story = GeneratedStoryContent(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    coverImageUrl = coverImageUrl,
                    category = theme
                )
                
                // 3. Save to Local DB
                saveToLocalDb(story)
                
                _state.value = GenerationState.Success(story)
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Failed to generate story. Have you provided a valid GEMINI_API_KEY?")
            }
        }
    }
    
    fun reset() {
        _state.value = GenerationState.Idle
    }

    private suspend fun generateImagesWithImagen(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "https://image.pollinations.ai/prompt/${URLEncoder.encode(prompt, "UTF-8")}"
        }

        try {
            val request = GenerateImagesRequest(prompt = prompt)
            val response = RetrofitClient.service.generateImages(apiKey, request)
            val base64Bytes = response.generatedImages?.firstOrNull()?.image?.imageBytes
            if (!base64Bytes.isNullOrBlank()) {
                val localPath = saveBase64ImageLocally(base64Bytes)
                if (localPath != null) {
                    return@withContext localPath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Dynamic fallback
        "https://image.pollinations.ai/prompt/${URLEncoder.encode(prompt, "UTF-8")}"
    }

    private suspend fun saveBase64ImageLocally(base64Str: String): String? = withContext(Dispatchers.IO) {
        try {
            val cleanBase64 = base64Str.replace("\n", "").replace("\r", "").trim()
            val decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
            val file = java.io.File(getApplication<Application>().filesDir, "cover_${UUID.randomUUID()}.jpg")
            java.io.FileOutputStream(file).use { fos ->
                fos.write(decodedBytes)
            }
            "file://${file.absolutePath}"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun generateWithGemini(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw Exception("Please add your Gemini API Key in the AI Studio Secrets panel.")
        }
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text 
            ?: throw Exception("No response text from Gemini API")
    }
    
    private suspend fun saveToLocalDb(story: GeneratedStoryContent) = withContext(Dispatchers.IO) {
        try {
            storyDao.insertStory(story)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
