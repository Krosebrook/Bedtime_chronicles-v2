# API Reference — Client-Side Integrations

Instead of hosting standard, heavyweight backend API servers, the **Infinity Heroes: Bedtime Chronicles** Android application resides on an elegant, decoupled, **offline-first local architecture**. It interacts directly with Google's cloud AI infrastructure via high-performance, type-safe Retrofit interfaces.

All remote API keys (such as `GEMINI_API_KEY`) are managed securely inside Google's secret console and injected compile-time via the Secrets Gradle Plugin.

---

## 🛰️ 1. Retrofit API Definitions

The client networking interfaces reside inside `com.example.data.GeminiService`.

### Interface Definition (`GeminiService.kt`)
```kotlin
interface GeminiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateStory(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    @POST("v1beta/models/imagen-3.0-generate-002:generateImages")
    suspend fun generateImages(
        @Query("key") apiKey: String,
        @Body request: ImagenRequest
    ): ImagenResponse
}
```

---

## 🤖 2. Gemini GenAI Integration (`gemini-2.5-flash`)

### 2.1 Request Structure (`GeminiRequest`)
We construct structured JSON schemas detailing output preferences (System Instruction forcing child safety rules, adventure structures, and JSON output response structures).

```json
{
  "contents": [
    {
      "parts": [
        {
          "text": "Generate a cozy bedtime adventure in JSON format about standard superhero Luna the Guardian of Stars trying to find a lost star."
        }
      ]
    }
  ],
  "generationConfig": {
    "responseMimeType": "application/json",
    "responseSchema": {
      "type": "OBJECT",
      "properties": {
        "title": { "type": "STRING" },
        "author": { "type": "STRING" },
        "paragraphs": {
          "type": "ARRAY",
          "items": { "type": "STRING" }
        }
      },
      "required": ["title", "author", "paragraphs"]
    }
  }
}
```

### 2.2 Response Structure (`GeminiResponse`)
The direct response contains candidates containing the JSON content segments parsed inside the client.

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "{\n  \"title\": \"Luna and the Wandering Comet\",\n  \"author\": \"Cosmic Chronometer\",\n  \"paragraphs\": [\n    \"Once upon a starlit tonight, Luna took flight...\",\n    \"The stellar winds carried her soft laughter...\"\n  ]\n}"
          }
        ]
      }
    }
  ]
}
```

---

## 🎨 3. Imagen 3 Art Generation (`imagen-3.0-generate-002`)

### 3.1 Request Structure (`ImagenRequest`)
Instructs Google's visual diffusion model to return beautiful whimsical vector art matching children's safe dreaming criteria.

```json
{
  "prompt": "Whimsical beautiful kid-friendly illustration of a child astronaut looking for a lost star in an purple dust constellation, warm soft studio lighting, bedtime dreamscape style, vector pastel painting",
  "numberOfImages": 1,
  "aspectRatio": "16:9"
}
```

### 3.2 Response Structure (`ImagenResponse`)
Returns base64 binary strings or remote image reference URLs to render directly on-screen inside our Coil loaders.

```json
{
  "generatedImages": [
    {
      "image": {
        "imageBytes": "iVBORw0KGgoAAAANSUhEUgA..."
      }
    }
  ]
}
```

---

## 🔒 4. Local Database Persistence & DAOs

When stories and image covers are generated, they are immediately persisted within the client's localized SQLite Room DB.

- **`GeneratedStoryDao`**:
  - `getAllStories()`: Returns reactive `Flow<List<GeneratedStory>>`
  - `insertStory(story: GeneratedStory)`: Inserts newly synthesized books.
  - `deleteStory(story: GeneratedStory)`: Erases a narrative book.
- **`UserProfileDao`**:
  - `getAllProfiles()`: Emits list arrays of child accounts.
  - `insertProfile(profile: UserProfile)`: Unlocks fresh accounts.
  - `updateProfile(profile: UserProfile)`: Records read session completions, tracking total vocabulary gains, active streaks, and unlocks developmental trophies.
