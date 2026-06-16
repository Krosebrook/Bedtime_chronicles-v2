# Bedtime Chronicles — System Architecture

An elegant, modern native Android mobile app architecture (Kotlin, Jetpack Compose, Material 3) engineered with local Room SQL databases, direct Google Gemini integrations (including Imagen 3), reactive state flows, programmatic high-precision wave audio synthesis, and graceful state restoration.

---

## 🛠️ Package & Class Architecture Overview

The system is organized into decoupled layers following **Modern Android Development (MAD)** standards and the **MVVM (Model-View-ViewModel)** architectural pattern.

```
                    ┌───────────────────────────┐
                    │     UI/Compose Views      │
                    │   (Screens & Composables) │
                    └─────────────┬─────────────┘
                                  │ Observes UI State
                                  ▼
                    ┌───────────────────────────┐
                    │        ViewModels         │
                    │  (Exposes UI StateFlows)  │
                    └─────────────┬─────────────┘
                                  │ Orchestrates
                                  ▼
                ┌─────────────────┴─────────────────┐
                ▼                                   ▼
   ┌──────────────────────────┐        ┌──────────────────────────┐
   │      Data Repository     │        │     Asynchronous Devs    │
   │   (Room SQLite, DAOs,    │        │  (TTS Engines, DSP audio │
   │   Gemini Retrofit API)   │        │     Ambient Helper)      │
   └──────────────────────────┘        └──────────────────────────┘
```

### 1. Presentation Layer (`com.example.ui`)
- **`screens.*`**: Stateless/stateful composable screens mapping to Material 3 UI design specifications:
  - `HomeScreen.kt`: Personalized hub showing active profiles, unlocked badges, developmental attributes, and an immersive **Cosmic Adventure Launcher Banner**.
  - `CreateStoryScreen.kt`: Interactive customized builder (choices for hero, companion, tone, setting, and custom problems) returning custom JSON scripts from Gemini.
  - `ReaderScreen.kt`: Advanced e-reader with fluid animation transitions, reading progression tracker, word-by-word highlight controls, active sleep synthesizer toggles, and volume/speed settings.
  - `AdventureScreen.kt`: Interactive roleplay game style "Cosmic Adventure" with dynamic player choices, ambient background synthesis, and direct voice synthesis.
  - `LibraryScreen.kt`: Catalog of all custom-generated and pre-saved bedtime stories stored locally.
  - `StoryDetailsScreen.kt`: Launchboard for saved stories detailing characters, date generated, and summary metrics.
  - `ProfileScreen.kt`: Gamification dashboard managing multiple child profiles, custom emoji-avatars, daily read streaks, achievements, and statistics.
  - `TutorialScreen.kt`: Intrepid starry initial onboard introducing children and parents to the cozy reading flow.
- **`navigation.*`**:
  - `Navigation.kt`: Type-safe route navigation engine utilizing `@Serializable` marker classes and animated transitions.
- **`theme.*`**:
  - `Theme.kt`, `Color.kt`, `Type.kt`: System parameters implementing the cozy, dark-mode adaptive **Midnight Velvet Cosmological Design Pattern** built on M3 primitives.

### 2. State & Business Layer (`com.example.viewmodel`)
- **`UserProfileViewModel.kt`**: Manages profile creation, logins, active profile selection state, read statistics, and gamified badge unlocking triggers.
- **`StoryGenerationViewModel.kt`**: Coordinates interactive calls to the remote Gemini service, builds safe storytelling prompts, processes the resulting JSON segments, and stores successful stories locally.
- **`ReaderViewModel.kt`**: Holds the active sentence position during story reading to support **sentence-level progressive memory restoration**.
- **`AdventureViewModel.kt`**: Exposes choices and orchestrates branching roleplay story flow segments from the Gemini LLM.
- **`LibraryViewModel.kt`**: Feeds list streams of generated bedtime adventures directly from the Room local database.

### 3. Data & Persistence Layer (`com.example.data`)
- **`AppDatabase.kt`**: Local SQLite database structured with Android Room.
  - **`GeneratedStoryDao.kt`**: Interface mapping local CRUD queries for books and scene assets.
  - **`UserProfileDao.kt`**: Manages profile entity listings, daily count metrics, and unlocked badges.
- **`AppPreferences.kt`**: Singleton class wrapping Android Shared Preferences for simple configuration properties (active profile id, tutorial completed, system audio volume, voice speed).
- **`GeminiService.kt`**: Retrofit REST wrapper providing secure endpoint pathways to call:
  - `gemini-2.5-flash:generateContent`: For children's storytelling lines.
  - `imagen-3.0-generate-002:generateImages`: Generates coverage and scene images using AI text prompt formulations.
- **`BedtimeAssets.kt` & `SampleStories.kt`**: Local offline fallback stories ensuring seamless enjoyment even during high network outages.

### 4. Hardware & Utility Helpers (`com.example.util`)
- **`AmbientSoundHelper.kt`**: High-performance background audio synthesis thread executing real-time wave equations using `AudioTrack` buffer arrays (bypassing heavy MP3 assets download sizes):
  - **Waveshapes**: Rain (pink noise filter), Ocean Waves (modulated sinusoidal volume curves on pure white noise), Space Wind (filtered sweep signals), and Forest Breeze.
  - **Volume Decay**: A deterministic linear volume fade decaying gracefully over exactly **10 seconds** once the timer runs down to zero to keep child sleep status undisturbed.
- **`TextToSpeechHelper.kt`**: Local native Android TTS wrapper facilitating comfortable narration of sentences with playback progress listeners.

---

## 💾 Core Data Persistence Schemas

### 1. User Profiles Table (`user_profiles`)
```sql
CREATE TABLE user_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    avatar TEXT NOT NULL,
    streakCount INTEGER NOT NULL DEFAULT 0,
    lastReadTimestamp INTEGER,
    completedStoriesCount INTEGER NOT NULL DEFAULT 0,
    unlockedBadgesText TEXT NOT NULL
);
```

### 2. Generated Stories Table (`generated_stories`)
```sql
CREATE TABLE generated_stories (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    heroName TEXT NOT NULL,
    mode TEXT NOT NULL,
    coverUrl TEXT NOT NULL,
    paragraphsJson TEXT NOT NULL,
    dateCreated INTEGER NOT NULL
);
```

---

## ⚡ AI Prompt Security & Fallbacks

- **Sanitization & Child Safety**: The ViewModels pass strict parameters ensuring formatting instructions prevent harmful themes. Text responses are parsed into predictable JSON paragraph structures.
- **ImagenCover Prompts**: Covers and illustrated scenes are described automatically using kid-friendly prompts (e.g. `"Whimsical child-friendly illustration of [scene], warm pastel colors, bedtime dreamscape style, soft studio lighting"`).
- **Graceful Failures**: If the Gemini API returns errors or a token fails, the VM traps the exception and loads an optimized fallback story immediately to maintain an uninterrupted user experience.
