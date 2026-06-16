<!-- Last verified: 2026-06-15 -->
# GEMINI.md — Gemini CLI Agent Context

Custom instructions for Gemini CLI and Gemini-based coding agents working on this native Android repository.

---

## Project Context

**Project:** Infinity Heroes: Bedtime Chronicles
**Type:** Children's bedtime story mobile app (ages 3–9)
**Architecture:** Modern Android Development (MAD) Native App (Kotlin / Jetpack Compose)
**Repo structure:** Native Android project with `/app` module containing screens, viewmodels, data access, and utilities.

The app lets children select cosmic superhero characters and participate in interactive bedtime stories (Cosmic Adventures) generated via the Google Gemini API (Imagen 3 for Cover and Scene generation under `v1beta/models/imagen-3.0-generate-002:generateImages`). Stories include programmatic high-fidelity soundscapes (DSP synthesized waves), Text-to-Speech narration, and gentle fading sleep timers to protect baby sleep states.

---

## Tech Stack

| Concern | Technology |
|---------|-----------|
| Mobile OS | Android (minSdk: 26, targetSdk: 35) |
| Language | Kotlin exclusively (1.9+) |
| View Layer | Jetpack Compose (Material 3 components and color tokens) |
| Navigation | Navigation Compose (with type-safe serializable route structures) |
| Dependency Injection | Clean constructor injection (ViewModelProvider / local instantiation) |
| Local Persistence | Jetpack Room SQLite databases with DAOs (`AppDatabase`) |
| Preferences | Singleton AppPreferences (`AppPreferences.kt`) |
| Async/Concurrency | Kotlin Coroutines, StateFlow, collection with Lifecycle awareness |
| Audio/Sound | Android MediaPlayer (TTS) & background thread AudioTrack synthesized wave DSP (`AmbientSoundHelper`) |
| LLM & Art APIs | Retrofit direct integration with Google Gemini & Imagen REST API services |

---

## Code Conventions

### Naming & Package Rules
- Composable Screens: `XxxScreen.kt` under `com.example.ui.screens.*`
- Composable Components: Located in reusable UI utility blocks or distinct custom layout components
- ViewModels: `XxxViewModel.kt` under `com.example.viewmodel.*` (no strong context leaks)
- DAO & Database: `XxxDao.kt` and Entity mappings under `com.example.data.*`
- Synchronous Utilities: `XxxHelper.kt` under `com.example.util.*`

### Import Order
1. Android framework & Kotlin core imports
2. Compose UI, Material 3 foundation components, and state extensions
3. Room & database flow imports
4. Relative package imports (`com.example.*`)

### Kotlin Standards
- Prefer strict immutable state flows (`StateFlow` exposed from ViewModel via read-only property).
- Avoid wildcards or raw unchecked types (`Any` / raw type casting).
- Always observe state using `collectAsStateWithLifecycle()` to maintain lifecycle safety.

---

## Security & Child Compliance

These rules are non-negotiable:

1. **Local Key Protection**: Do not hardcode API Keys or client credentials in source code. Use the Gradle Secrets Plugin (`GEMINI_API_KEY` injected via `BuildConfig`).
2. **Cozy Safety Context**: Ensure AI prompt generators contain clear rules safe for children, filtered words, and kid-appropriate storytelling modes.
3. **Failsafe Content Fallback**: When network calls fail or keys are absent, fallback smoothly to optimized pre-packaged localized bedtime stories (`BedtimeAssets.kt`).

---

## Architecture Constraints

- **Single Screen Constraint**: For simple feature scopes, keep layouts single-screen to avoid navigation sprawl.
- **Edge-to-Edge compliance**: Always design composables with a Material 3 `Scaffold` container that automatically accounts for window insets (`Modifier.statusBarsPadding()` or `contentPadding`).
- **Touch Target Density**: Ensure touchable widgets are at least `48dp` x `48dp` for accessibility standard compliance.
- **Test Tags**: Utilize `.testTag("unique_snake_case")` modifiers on any interactive elements (buttons, inputs, sliders) for robust automated test instrumentation.

---

## Compilation, Verification & Build Commands

Always run verification before claiming task completion:

```bash
# Verify build / compile the native APK
compile_applet
```

Do not invoke wrapper binaries like `./gradlew` or `gradlew` directly because the execution container operates on a raw Gradle installation.

---

## Common Workflows

### Add a Composable Screen
1. Create `XxxScreen.kt` in `com.example.ui.screens/`.
2. Add a serializable routing marker key object.
3. Wire up the Screen route composition in `com.example.ui.navigation.Navigation.kt`.
4. Ensure all layout items contain clean `.testTag("<screen_name>_<item_name>")` attributes.

### Add a Database DAO
1. Write the target SQL Room `@Entity` class in `com.example.data/`.
2. Map operations under an interface annotated with `@Dao`.
3. Declare DAO getters in `AppDatabase.kt`.
4. Run `compile_applet` to trigger annotation processors and KSP compilation.
