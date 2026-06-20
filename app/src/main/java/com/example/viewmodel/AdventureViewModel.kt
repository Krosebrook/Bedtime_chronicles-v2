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
        val imageUrl: String = "",
        val inventory: List<String> = listOf("Stardust Compass"), // Start with Stardust Compass
        val currentRegion: String = "Whispering Canopy Forest"
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        }
        return false
    }

    private fun getOfflineAdventureFallback(step: Int, choiceText: String): Pair<String, List<String>> {
        val narrative = when (step) {
            0 -> "You step onto a sparkling stardust cloud. In the distance, a friendly space whale guides you safely through the deep celestial sky. The air is warm and comforting, and you feel incredibly safe.\n\nWhich path will you explore next?"
            1 -> "Choosing to '$choiceText', you find a gentle golden path that whispers sweet melodies. A glowing starlight lantern lights your way, making everything feel snug and cozy."
            2 -> "Following the path, you arrive at the Sleepy Nebula Garden, where dreamflowers bloom under a soft silver moon. Their gentle fragrance invites you to close your eyes and rest."
            else -> "A sweet bedtime chorus sings as your adventure winds down. You wrap yourself in a warm blanket of stars, feeling ready for deep, lovely dreams."
        }
        val choices = when (step) {
            0 -> listOf("Follow the space whale", "Visit the starlight castle", "Float gently on the cloud")
            1 -> listOf("Explore the Sleepy Garden", "Listen to the star song", "Rest near the stream")
            else -> listOf("Curl up and sleep", "Whisper sweet dreams", "Close your eyes")
        }
        return narrative to choices
    }

    fun startNewGame() {
        _state.value = AdventureState.CharacterCreation
    }

    fun initializeAdventure(name: String, hairColor: String, clothingStyle: String, backstory: String) {
        if (name.isBlank()) return
        _state.value = AdventureState.Loading
        
        viewModelScope.launch {
            if (!isNetworkAvailable()) {
                val (narrative, choices) = getOfflineAdventureFallback(0, "")
                _state.value = AdventureState.ActivePlay(
                    characterName = name,
                    hairColor = hairColor,
                    clothingStyle = clothingStyle,
                    backstory = backstory,
                    currentPrompt = narrative,
                    choices = choices,
                    history = listOf("" to narrative),
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB1jLdKFdhpel1Kh5sjxf5LPi-lM2_IJkVTr3vU_S39m-NjISPDYMTdLYYVPsp3_scPtcgN0ZEfMckmFjCYwIN6gu3CKClELppPt-PwJlFfJwv6QTe7fUBtMmn5GNGgep8WrL7OKBM-Bri0iHWfxxCH5qKhVrUw7fwZ9PSzQ_s3xuWk1E-Fd7cU3yjwk8i37ZCOJwk712gYoQjBwx5VN3FKP0kA2UZXYWo7f_Eij0xQzUuxYc-zGK516RKEqOs5QZo6jRO7v2QOT7M"
                )
                autoSaveGame()
                return@launch
            }

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
                // Return fallback instead of hard error screen
                val (narrative, choices) = getOfflineAdventureFallback(0, "")
                _state.value = AdventureState.ActivePlay(
                    characterName = name,
                    hairColor = hairColor,
                    clothingStyle = clothingStyle,
                    backstory = backstory,
                    currentPrompt = narrative,
                    choices = choices,
                    history = listOf("" to narrative),
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB1jLdKFdhpel1Kh5sjxf5LPi-lM2_IJkVTr3vU_S39m-NjISPDYMTdLYYVPsp3_scPtcgN0ZEfMckmFjCYwIN6gu3CKClELppPt-PwJlFfJwv6QTe7fUBtMmn5GNGgep8WrL7OKBM-Bri0iHWfxxCH5qKhVrUw7fwZ9PSzQ_s3xuWk1E-Fd7cU3yjwk8i37ZCOJwk712gYoQjBwx5VN3FKP0kA2UZXYWo7f_Eij0xQzUuxYc-zGK516RKEqOs5QZo6jRO7v2QOT7M"
                )
                autoSaveGame()
            }
        }
    }

    fun makeChoice(choiceText: String) {
        val currentState = _state.value as? AdventureState.ActivePlay ?: return
        _state.value = AdventureState.Loading
        
        viewModelScope.launch {
            if (!isNetworkAvailable()) {
                val nextStep = currentState.history.size
                val (narrative, choices) = getOfflineAdventureFallback(nextStep, choiceText)
                val updatedHistory = currentState.history + (choiceText to narrative)
                _state.value = currentState.copy(
                    currentPrompt = narrative,
                    choices = choices,
                    history = updatedHistory
                )
                autoSaveGame()
                return@launch
            }

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
                // Fallback to offline choice generation rather than showing full error screen
                val nextStep = currentState.history.size
                val (narrative, choices) = getOfflineAdventureFallback(nextStep, choiceText)
                val updatedHistory = currentState.history + (choiceText to narrative)
                _state.value = currentState.copy(
                    currentPrompt = narrative,
                    choices = choices,
                    history = updatedHistory
                )
                autoSaveGame()
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
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        
        val response = RetrofitClient.service.generateContent("Bearer dev-cozy-storytime-token-2026", request)
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

            val saveName = "${currentState.characterName}'s Story - ${currentState.history.size} Steps - Region: ${currentState.currentRegion}"
            
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
                timestamp = System.currentTimeMillis(),
                inventoryJson = currentState.inventory.joinToString(",")
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

                    val loadedInv = if (saved.inventoryJson.isBlank()) {
                        listOf("Stardust Compass")
                    } else {
                        saved.inventoryJson.split(",").filter { it.isNotBlank() }
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
                        imageUrl = saved.sceneImageUrl,
                        inventory = loadedInv,
                        currentRegion = if (saved.saveName.contains(" - Region: ")) saved.saveName.substringAfter(" - Region: ").trim() else "Whispering Canopy Forest"
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

    fun generateDetailedBackstory(name: String, origin: String, motivation: String, pastEvent: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            if (!isNetworkAvailable()) {
                onResult("$name originates from $origin. They are deeply motivated $motivation. Historically, one of the significant events that shaped them is when they $pastEvent.")
                return@launch
            }
            try {
                val prompt = """
                    Write a highly imaginative, deeply peaceful, and inspiring 2-paragraph bedtime legend backstory for a children's hero character.
                    Character Details:
                    - Name: $name
                    - Origin/Birthplace: $origin
                    - Purpose/Motivation: $motivation
                    - Key Milestone: $pastEvent
                    
                    Write in the third-person. The style should be incredibly magical, starry, and soothing, safe for comforting a child's mind before sleep. Do not add scary elements, threats, or combat. Maximum 100 words.
                """.trimIndent()
                val backstoryText = generateWithGemini(prompt)
                onResult(backstoryText.trim())
            } catch (e: Exception) {
                onResult("$name originates from $origin. They are deeply motivated $motivation. Historically, one of the significant events that shaped them is when they $pastEvent.")
            }
        }
    }

    fun addItemToInventory(item: String) {
        val currentState = _state.value as? AdventureState.ActivePlay ?: return
        if (currentState.inventory.size >= 5) return // cap at 5 items
        val updatedInv = currentState.inventory + item
        _state.value = currentState.copy(inventory = updatedInv)
        autoSaveGame()
    }

    fun removeItemFromInventory(item: String) {
        val currentState = _state.value as? AdventureState.ActivePlay ?: return
        val updatedInv = currentState.inventory.filter { it != item }
        _state.value = currentState.copy(inventory = updatedInv)
        autoSaveGame()
    }

    fun useItemFromInventory(item: String) {
        val currentState = _state.value as? AdventureState.ActivePlay ?: return
        val updatedInv = currentState.inventory.filter { it != item }
        _state.value = currentState.copy(inventory = updatedInv)
        makeChoice("I use my magical $item to gently solve this bedtime quest!")
    }

    fun findTreasures(onFound: (String) -> Unit) {
        val possibleItems = listOf(
            "Stardust Compass",
            "Sleeping Moonstone",
            "Lullaby Shell",
            "Glowing Star Key",
            "Nebula Cloud Blanket",
            "Cosmic Dream-Lantern",
            "Mechanical Skybeetle"
        )
        val statePlay = _state.value as? AdventureState.ActivePlay ?: return
        val currentInv = statePlay.inventory
        val remaining = possibleItems.filter { !currentInv.contains(it) }
        
        if (remaining.isEmpty()) {
            onFound("") // satchel is already filled with every unique starry item!
            return
        }
        val foundItem = remaining.random()
        onFound(foundItem)
    }

    fun travelToRegion(regionName: String, regionDescription: String, chosenEncounter: String) {
        val currentState = _state.value as? AdventureState.ActivePlay ?: return
        _state.value = AdventureState.Loading
        
        viewModelScope.launch {
            val travelPromptStr = "I travel to $regionName on my dynamic space map... where I experience: $chosenEncounter"
            
            if (!isNetworkAvailable()) {
                val updatedHistory = currentState.history + (travelPromptStr to "You arrive safely in the beautiful $regionName. $regionDescription\n\nSuddenly, you encounter: $chosenEncounter. It fills you with a gentle, cozy sense of security and peaceful wind-down thoughts.")
                _state.value = currentState.copy(
                    currentRegion = regionName,
                    currentPrompt = "You arrive safely in the beautiful $regionName.\n\nDescription: $regionDescription\n\nEncounter: $chosenEncounter\n\nThe cozy magic of the starry landscape cradles you softly as you prepare for sweet bedtime dreams.",
                    choices = listOf(
                        "Nestle down and listen to the ambient breeze 🍃",
                        "Explore the quiet glowing pathways 🌟",
                        "Float on a sleepy cloud of stars 🪐"
                    ),
                    history = updatedHistory
                )
                autoSaveGame()
                return@launch
            }

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
                    ${fullHistoryString.takeLast(1500)}
                    
                    The hero opens their dynamic cosmic star-map and travels to the region: $regionName.
                    Region Description: $regionDescription
                    
                    During their travel, they experience this peaceful bedside random encounter:
                    $chosenEncounter
                    
                    Write the next comforting, peaceful segment of the story (1 to 2 small paragraphs). 
                    Incorporate elements of their journey to $regionName and the encounter beautifully.
                    Keep the tone cozy, encouraging, safe, and winding down for bedtime.
                    At the end, generate exactly 3 new magical choices for the next step within this beautiful region.
                    
                    Format your output EXACTLY like this:
                    [Next story segment]
                    ---
                    Choice 1: [First choice, max 10 words]
                    Choice 2: [Second choice, max 10 words]
                    Choice 3: [Third choice, max 10 words]
                """.trimIndent()

                val apiResponse = generateWithGemini(prompt)
                val parsed = parseGeminiResponse(apiResponse)
                
                val updatedHistory = currentState.history + (travelPromptStr to parsed.first)
                
                // Generate a fresh dynamic scene illustration
                val sceneBrief = parsed.first.take(200)
                val imagePrompt = "A beautiful minimalist vector illustration of a children's storybook scene. " +
                                  "A character named ${currentState.characterName} visiting the $regionName region with: $sceneBrief. " +
                                  "Calm indigo, starry skies, sleep-inducing celestial bedtime vector design, no text."
                val imageUrl = generateImagesWithImagen(imagePrompt)

                _state.value = currentState.copy(
                    currentRegion = regionName,
                    currentPrompt = parsed.first,
                    choices = parsed.second,
                    history = updatedHistory,
                    imageUrl = imageUrl
                )
                
                autoSaveGame()
            } catch (e: Exception) {
                // Return offline fallback instead of error screen
                val updatedHistory = currentState.history + (travelPromptStr to "You arrive safely in the beautiful $regionName. $regionDescription\n\nSuddenly, you encounter: $chosenEncounter. It fills you with a gentle, cozy sense of security and peaceful wind-down thoughts.")
                _state.value = currentState.copy(
                    currentRegion = regionName,
                    currentPrompt = "You arrive safely in the beautiful $regionName.\n\nDescription: $regionDescription\n\nEncounter: $chosenEncounter\n\nThe cozy magic of the starry landscape cradles you softly as you prepare for sweet bedtime dreams.",
                    choices = listOf(
                        "Nestle down and listen to the ambient breeze 🍃",
                        "Explore the quiet glowing pathways 🌟",
                        "Float on a sleepy cloud of stars 🪐"
                    ),
                    history = updatedHistory
                )
                autoSaveGame()
            }
        }
    }

    private suspend fun generateImagesWithImagen(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val request = GenerateImagesRequest(prompt = prompt)
            val response = RetrofitClient.service.generateImages("Bearer dev-cozy-storytime-token-2026", request)
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
