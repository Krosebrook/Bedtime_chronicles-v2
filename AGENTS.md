# AI Agent Developer Guide (AGENTS.md)

> This document defines structural rules, design patterns, and quality constraints for AI Coding Agents editing the **Infinity Heroes: Bedtime Chronicles** Android Application (Kotlin / Jetpack Compose).

---

## 🤖 1. Core Mandates for AI Assistants

1.  **State Observation Consistency**: Always use `collectAsStateWithLifecycle()` when observing room data or preference state inside UI screens to respect Lifecycle boundaries. Do not bypass this with simple `collectAsState()`.
2.  **Edge-to-Edge Compliance**: Always declare system visual insets dynamically using Material 3 Scaffold parameters. Never hardcode absolute pixel sizing.
3.  **UI Component Testing IDs**: Any interactive element, card choice, input box, or title section created **MUST** utilize `.testTag(...)` using a unique `snake_case` identifier (e.g. `testTag("submit_button")`).
4.  **No Sudden Audio Cuts**: When creating or modifying playbacks, timers, or sleep settings, preserve the **10-second linear volume fade decay** so kids do not wake up from sharp audible switches.
5.  **Platform Sync Rule**: If you update the app name listed in `/app/src/main/res/values/strings.xml` (`app_name`), you **must** also update the matching `name` property inside `/metadata.json`.

---

## 🛠️ 2. File and Package Conventions

Keep files structured under the primary package layout (`com.example`):
*   `com.example.ui.screens.*` — Stateless Composable Screens (e.g. `ReaderScreen`, `ProfileScreen`).
*   `com.example.viewmodel.*` — Exposes structured state containers. Do not hold strong Context references inside ViewModels.
*   `com.example.data.*` — Key-value layers (`AppPreferences`), centralized lists (`BedtimeAssets`), and DB bindings.
*   `com.example.util.*` — Synchronous utilities (`TextToSpeechHelper`, `AmbientSoundHelper`).

---

## 📦 3. Local State & Database Rules

*   **Database Schema**: SQLite persistence is managed through Android Room. Any entity changes require explicit migration notes. Avoid altering active DAO structures unless requested by the user.
*   **Preferences**: App settings or gamification values must reside inside `AppPreferences.kt` via thread-safe singletons `AppPreferences.getInstance(context)`.

---

## 🧪 4. Build & Compile Checks

If the Gradle compilation fails:
1.  **Missing Imports**: Check for Compose UI platforms imports (`androidx.compose.ui.platform.LocalContext`, `androidx.compose.ui.text.style.TextAlign`).
2.  **No Wrapper Calls**: Always use the direct `compile_applet` tool or run `gradle` tasks directly inside execution calls. Do not invoke `gradlew` or `./gradlew` as the environment does not host standard wrapper binaries.
3.  **No TODO blocks**: Never commit placeholder blocks or loose comment blocks. Implement functional operations cleanly.
