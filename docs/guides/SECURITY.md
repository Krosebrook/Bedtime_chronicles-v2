# Security Policy — Bedtime Chronicles

Security and kid-safety are top priorities for **Infinity Heroes: Bedtime Chronicles**. This native Android application is engineered to protect children's raw inputs, secure user credentials, sandbox database objects, and prevent prompt manipulation.

---

## 🔒 1. Secrets Management (Gradle Secrets Plugin)

To protect production API keys (e.g., `GEMINI_API_KEY`) from exposure inside repository check-ins:
- We utilize the **Secrets Gradle Plugin for Android** to inject environment-level values into `BuildConfig` at compile-time.
- All secrets are declared inside local developmental `.env` files (e.g. `GEMINI_API_KEY=xyz`) and ignored by git boundaries.
- **Reference Example**:
  ```kotlin
  val apiKey = BuildConfig.GEMINI_API_KEY
  ```

---

## 🍼 2. Safe Bedtime Content & Child Filter Constraints

To prevent children from encountering non-compliance themes or being exposed to prompt injection bypasses:
- ViewModels enforce structured kid-friendly instructions (located under `com.example.viewmodel.StoryGenerationViewModel`) before forwarding prompt payloads to Gemini's LLM routing.
- The AI story generator is locked strictly into a pre-defined JSON schema returned from the models. Any text failing schemas or attempting command overrides is caught, logged, and gracefully swapped with optimized localized fallback bedtime templates (`BedtimeAssets.kt`).

---

## 💾 3. Sandbox Data Isolation & Offline Scoping

The application operates in a decoupled, offline-first sandbox:
- **Room SQLite databases** are local to the client's Android sandbox (`/data/data/com.example/databases/`), ensuring profile statistics, unlocked achievements, unique names, and bedtime narratives are completely private and never uploaded to public clouds.
- **Shared Preferences** saving basic configurations (e.g., sound synth volume levels) utilize private mode (`Context.MODE_PRIVATE`).

---

## 🤖 4. SDK Dependencies Audit

The Android build ecosystem dependencies (Jetpack Compose, Room, Retrofit, Navigation Compose, Kotlin Coroutines, etc.) are declared centrally in:
- `/gradle/libs.versions.toml` (Version Catalog)
- `/app/build.gradle.kts` (App plugin block)

We perform clean security checks by ensuring dependencies reference proven, verified libraries maintained directly by Google, Jetpack, and Square, avoiding experimental, unverified third-party libraries.

---

## 🐛 5. Reporting a Vulnerability

**Do not open a public GitHub issue for security or child compliance vulnerabilities.**

To report a vulnerability, contact the repository developers or owner confidentially via email or the repository's direct security channels.
