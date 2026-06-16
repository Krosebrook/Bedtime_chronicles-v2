package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

sealed class AdventureState {
    object CharacterCreation : AdventureState()
    object Loading : AdventureState()
    data class ActivePlay(
        val characterName: String,
        val hairColor: String,
        val clothingStyle: String,
        val backstory: String,
        val currentPrompt: String,
        val choices: List<String>,
        val history: List<Pair<String, String>>, // list of (choiceTaken, narrativeStorySegment)
        val imageUrl: String = ""
    ) : AdventureState()
    data class Error(val message: String) : AdventureState()
}

class AdventureViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val saveDao = database.adventureSaveStateDao()

    private val _state = MutableStateFlow<AdventureState>(AdventureState.CharacterCreation)
    val state: StateFlow<AdventureState> = _state.asStateFlow()

    // All available saves flow
    val allSaves: StateFlow<List<AdventureSaveState>> = saveDao.getAllSaves()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoadingSave = MutableStateFlow(false)
    val isLoadingSave: StateFlow<Boolean> = _isLoadingSave.asStateFlow()

    // Character template quick picks
    val templates = listOf(
        Triple("Orion", "Silver", "Midnight Blue cape with twinkling stars"),
        Triple("Nova", "Neon Pink", "Glowing stardust jumpsuit and golden boots"),
        Triple("Luna", "Pale Gold", "Dreamy feather robe and silver tiara")
    )

    fun startNewGame() {
        _state.value = AdventureState.CharacterCreation
    }

    fun initializeAdventure(name: String, hairColor: String, clothingStyle: String, backstory: String) {
        if (name.isBlank()) return
        _state.value = AdventureState.Loading
        
        viewModelScope.launch {
            try {
                // Build a dreamy introductory prompt
                val introPrompt = """
                    Start a comforting, beautiful, sleep-inducing interactive bedtime text adventure game for a child.
                    The main character's details are:
                    Name: $name
                    Hair Color: $hairColor
                    Clothing Style: $clothingStyle
                    Backstory: $backstory
                    
                    Write the beautiful opening scene of the adventure (around 1 or 2 small soothing paragraphs). Introduce the character and place them in a safe, magical starting location relevant to their backstory, such as a cloud castle, space harbor, or golden treehouse.
                    Then, generate 3 magical, engaging, completely safe choices for the next step.
                    
                    Format your output EXACTLY like this:
                    [Opening story text]
                    ---
                    Choice 1: [First choice, max 10 words]
                    Choice 2: [Second choice, max 10 words]
                    Choice 3: [Third choice, max 10 words]
                """.trimIndent()

                val apiResponse = generateWithGemini(introPrompt)
                val parsed = parseGeminiResponse(apiResponse)
                
                // Generate a dreamlike cover illustration prompt based on the opening segment
                val sceneBrief = parsed.first.take(200)
                val imagePrompt = "A beautiful minimalist vector illustration of a children's storybook scene. " +
                                  "A character named $name with $hairColor hair wearing $clothingStyle in a dreamlike setting: $sceneBrief. " +
                                  "Calm indigo, purple, and pastel gold color scheme, starry starry night sky, cozy and sleep-inducing style, no words."
                val imageUrl = generateImagesWithImagen(imagePrompt)

                _state.value = AdventureState.ActivePlay(
                    characterName = name,
                    hairColor = hairColor,
                    clothingStyle = clothingStyle,
                    backstory = backstory,
                    currentPrompt = parsed.first,
                    choices = parsed.second,
                    history = listOf("" to parsed.first),
                    imageUrl = imageUrl
                )
                
                // Auto save
                autoSaveGame()
            } catch (e: Exception) {
                _state.value = AdventureState.Error(e.message ?: "Failed to start adventure. Is Gemini API Key set?")
            }
        }
    }

    fun makeChoice(choiceText: String) {
        val currentState = _state.value as? AdventureState.ActivePlay ?: return
        _state.value = AdventureState.Loading

        viewModelScope.launch {
            try {
                val fullHistoryString = currentState.history.joinToString("\n\n") { (choice, story) ->
                    (if (choice.isNotEmpty()) "Action: $choice\n" else "") + story
                }

                val prompt = """
                    You are the narrator of an interactive bedtime text adventure for a child.
                    The main character's details are:
                    Name: ${currentState.characterName}
                    Hair Color: ${currentState.hairColor}
                    Clothing Style: ${currentState.clothingStyle}
                    Backstory: ${currentState.backstory}
                    
                    The story context so far is:
                    $fullHistoryString
                    
                    The user chose: $choiceText
                    
                    Write the next comforting, peaceful segment of the story (1 to 2 small paragraphs). 
                    Incorporate elements of their appearance (their ${currentState.hairColor} hair or ${currentState.clothingStyle} clothing) or backstory elements gracefully if appropriate.
                    Keep the tone cozy, encouraging, safe, and winding down for bedtime.
                    At the end, generate exactly 3 new magical choices for the next step.
                    
                    Format your output EXACTLY like this:
                    [Next story segment]
                    ---
                    Choice 1: [First choice, max 10 words]
                    Choice 2: [Second choice, max 10 words]
                    Choice 3: [Third choice, max 10 words]
                """.trimIndent()

                val apiResponse = generateWithGemini(prompt)
                val parsed = parseGeminiResponse(apiResponse)
                
                val updatedHistory = currentState.history + (choiceText to parsed.first)
                
                // Generate a fresh dynamic scene illustration
                val sceneBrief = parsed.first.take(200)
                val imagePrompt = "A beautiful minimalist vector illustration of a children's storybook scene. " +
                                  "A character named ${currentState.characterName} with ${currentState.hairColor} hair in: $sceneBrief. " +
                                  "Calm indigo, soft neon glows, sleep-inducing celestial bedtime vector design, no text."
                val imageUrl = generateImagesWithImagen(imagePrompt)

                _state.value = currentState.copy(
                    currentPrompt = parsed.first,
                    choices = parsed.second,
                    history = updatedHistory,
                    imageUrl = imageUrl
                )
                
                // Auto save
                autoSaveGame()
            } catch (e: Exception) {
                // Return to active play with error message or display error screen
                _state.value = AdventureState.Error(e.message ?: "Failed to progress story.")
            }
        }
    }

    /**
     * Parse the standard Gemini response lines.
     * Splitting by '---' or extracting Option 1, Option 2, Option 3 labels.
     */
    private fun parseGeminiResponse(response: String): Pair<String, List<String>> {
        val parts = response.split("---")
        var narrative = parts.firstOrNull()?.trim() ?: "The story continues beautifully as stardust guides your steps..."
        
        val choices = mutableListOf<String>()
        if (parts.size > 1) {
            val lines = parts[1].split("\n")
            for (line in lines) {
                val cleaned = line.trim()
                if (cleaned.startsWith("Choice ") || cleaned.startsWith("Option ")) {
                    val choiceContent = cleaned.substringAfter(":").trim().removePrefix("-").trim()
                    if (choiceContent.isNotBlank()) {
                        choices.add(choiceContent)
                    }
                } else if (cleaned.startsWith("-") || cleaned.startsWith("*")) {
                    val choiceContent = cleaned.removePrefix("-").removePrefix("*").trim()
                    if (choiceContent.isNotBlank()) {
                        choices.add(choiceContent)
                    }
                }
            }
        }
        
        // If parsing failed or gave less than 3 choices, add default cute options
        if (choices.isEmpty()) {
            val lines = response.split("\n")
            for (line in lines) {
                val cleaned = line.trim()
                if (cleaned.startsWith("Choice 1:") || cleaned.startsWith("Choice 2:") || cleaned.startsWith("Choice 3:")) {
                    choices.add(cleaned.substringAfter(":").trim())
                }
            }
        }
        
        if (choices.isEmpty()) {
            choices.addAll(listOf(
                "Fly higher onto a shining star cloud 🌟",
                "Follow a whispering stardust trail 🌌",
                "Sit comfortably and listen to the cosmic wind lullaby 🎈"
            ))
        }
        
        // Ensure narrator text is neat
        narrative = narrative.removePrefix("**Narrator:**").trim()
        
        return narrative to choices.take(3)
    }

    private suspend fun generateWithGemini(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw Exception("Please configure your Gemini API Key in the AI Studio Secrets panel.")
        }
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text 
            ?: throw Exception("No response text from Gemini API")
    }

    // --- SAVING & LOADING FUNCTIONALITY ---

    fun saveGame(slotId: String) {
        val currentState = _state.value as? AdventureState.ActivePlay ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            val choicesStr = currentState.choices.joinToString("|||")
            // Serialize history as a single string split by ||| and ,,,
            // Pair format: choice,story
            val historyStr = currentState.history.joinToString("|||") { (choice, story) ->
                "${choice.replace(",,,", "")},,,${story.replace(",,,", "")}"
            }

            val saveName = "${currentState.characterName}'s Story - ${currentState.history.size} Steps"
            
            val saveState = AdventureSaveState(
                slotId = slotId,
                saveName = saveName,
                characterName = currentState.characterName,
                hairColor = currentState.hairColor,
                clothingStyle = currentState.clothingStyle,
                backstory = currentState.backstory,
                currentPrompt = currentState.currentPrompt,
                choicesJson = choicesStr,
                historyJson = historyStr,
                sceneImageUrl = currentState.imageUrl,
                timestamp = System.currentTimeMillis()
            )
            
            saveDao.insertSave(saveState)
        }
    }

    private fun autoSaveGame() {
        saveGame("autosave")
    }

    fun loadGame(slotId: String) {
        _isLoadingSave.value = true
        viewModelScope.launch {
            try {
                val saved = withContext(Dispatchers.IO) {
                    saveDao.getSaveBySlot(slotId)
                }
                
                if (saved != null) {
                    // Reconstruct choices list
                    val choicesList = saved.choicesJson.split("|||").filter { it.isNotBlank() }
                    
                    // Reconstruct history pairs
                    val historyList = if (saved.historyJson.isNotBlank()) {
                        saved.historyJson.split("|||").map { item ->
                            val parts = item.split(",,,")
                            val choice = parts.getOrNull(0) ?: ""
                            val story = parts.getOrNull(1) ?: ""
                            choice to story
                        }
                    } else {
                        listOf("" to saved.currentPrompt)
                    }

                    _state.value = AdventureState.ActivePlay(
                        characterName = saved.characterName,
                        hairColor = saved.hairColor,
                        clothingStyle = saved.clothingStyle,
                        backstory = saved.backstory,
                        currentPrompt = saved.currentPrompt,
                        choices = if (choicesList.isNotEmpty()) choicesList else listOf(
                            "Continue onto a stardust sail 🪐",
                            "Examine a sparkling glowing crystal 💎",
                            "Gently float back towards basecamp 🏕️"
                        ),
                        history = historyList,
                        imageUrl = saved.sceneImageUrl
                    )
                }
            } catch (e: Exception) {
                // If failed, fall back
                e.printStackTrace()
            } finally {
                _isLoadingSave.value = false
            }
        }
    }

    fun deleteSave(slotId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            saveDao.deleteSaveBySlot(slotId)
        }
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
        
        // Fallback
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
}
