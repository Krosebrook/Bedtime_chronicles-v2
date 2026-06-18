# CLAUDE.md — Infinity Heroes: Bedtime Chronicles

## ⚠️ Read this first — repository is mid-migration

This repo is **transitioning from a hybrid Expo (React Native) + Express.js app to a
native Android app (Kotlin + Jetpack Compose)**. Two stacks coexist right now:

| Stack | Status | Where the real source lives |
|-------|--------|------------------------------|
| **Native Android (Kotlin/Compose)** | **PRIMARY / go-forward** (see `docs/adr/0006-migrate-to-native-android-kotlin.md`, *accepted* 2026-06-11) | `app/src/main/java/com/example/` + Gradle files |
| **Expo + Express (TypeScript)** | **LEGACY** (still present, being phased out) | `legacy-prototype/react-native/` (organized copy) |

**Important:** the repo **root is a flattened export dump** — hundreds of stale,
partly-duplicated `.ts`/`.tsx` files plus screenshots, HTML, `.zip`s, and timestamped
`.md` duplicates live there. Those root copies are **not** the buildable source and
their imports don't resolve (there is no `server/`, `lib/`, `components/`, `scripts/`
directory at root). When working on the legacy stack, use `legacy-prototype/react-native/`.
See **Repository State / Known Clutter** below.

---

## Project Overview

AI-powered interactive bedtime story app for children ages 3–9. Kids create custom
superheroes and experience personalized, AI-generated adventures with illustrations,
narration, sticker books, and gamification.

**App identity:** `applicationId = com.aistudio.bedtime.hfto` · display name "Infinity Heroes"

---

# PART 1 — PRIMARY: Native Android (Kotlin + Jetpack Compose)

## Tech Stack

- **Language:** Kotlin `2.2.10` (code style `official`)
- **UI:** Jetpack Compose (BOM `2024.09.00`), Material 3, edge-to-edge
- **Navigation:** Navigation-Compose `2.8.9` (single-Activity, string routes)
- **State:** Kotlin `StateFlow` + `collectAsStateWithLifecycle()` (lifecycle-aware), ViewModels
- **Local DB:** Room `2.7.0` with **KSP** `2.3.5` code-gen
- **Lightweight prefs:** MMKV `1.3.9` (`AppPreferences`)
- **Networking / AI:** Retrofit `2.12.0` + OkHttp `4.10.0` + kotlinx.serialization, calling Google Generative Language REST directly
- **Images:** Coil `2.7.0`
- **Audio:** native `AudioTrack` DSP synthesis + Android `TextToSpeech`
- **Build:** Android Gradle Plugin `9.1.1`, single `:app` module, Java 11
- **Secrets:** Secrets Gradle Plugin `2.0.1` (`.env` / `.env.example` → `BuildConfig`)
- **Testing:** Robolectric `4.16.1` (JVM unit), **Roborazzi** `1.59.0` (screenshot), Espresso (instrumented)

SDK levels: `compileSdk 36`, `targetSdk 36`, `minSdk 24`. `namespace = com.example`.

## Project Structure (Android)

```
app/                                    # the :app Gradle module
  build.gradle.kts                      # module build config, deps, signing, secrets{}
  src/main/AndroidManifest.xml          # single MainActivity launcher; INTERNET permission only
  src/main/java/com/example/
    MainActivity.kt                     # ComponentActivity → enableEdgeToEdge → AppNavigation
    MainApplication.kt                  # Application subclass
    ui/
      navigation/Navigation.kt          # NavHost; startDestination = "tutorial"
      theme/                            # Color.kt, Type.kt, Theme.kt (Material 3 + DeepSpaceStarryBackground)
      screens/                          # Tutorial, Home, Library, Reader, Create,
                                        #   StoryDetails, Profile, Adventure, StickerBook
    data/
      AppDatabase.kt                    # Room DB (v4) + DatabaseProvider singleton
      UserProfile.kt / UserProfileDao.kt
      GeneratedStoryContent.kt / GeneratedStoryDao.kt
      AdventureSaveState.kt / (AdventureSaveStateDao)
      PlacedSticker.kt / PlacedStickerDao.kt / Stickers.kt
      GeminiService.kt                  # Retrofit client → Gemini + Imagen
      AppPreferences.kt                 # MMKV-backed prefs exposing StateFlow
      RecentlyViewedManager.kt, SampleStories.kt, BedtimeAssets.kt
    util/
      AmbientSoundHelper.kt             # AudioTrack DSP ambient sound (rain/ocean/wind/breeze)
      TextToSpeechHelper.kt             # Android TTS narration
  src/test/                             # Robolectric unit + Roborazzi screenshot tests (+ /screenshots)
  src/androidTest/                      # Espresso instrumented tests
build.gradle.kts                        # root build (plugins declared apply=false)
settings.gradle.kts                     # includes :app; rootProject.name = "My Application"
gradle.properties                       # JVM/parallel/cache/config-cache settings
gradle/libs.versions.toml               # version catalog (single source of dependency versions)
```

> **Note:** there is currently **no Gradle wrapper** (`gradlew` / `gradle/wrapper/`) checked
> in. Build/test from **Android Studio**, or generate a wrapper (`gradle wrapper`) against a
> locally installed Gradle before running the commands below.

## Navigation graph (`ui/navigation/Navigation.kt`)

Single `NavHost`, horizontal slide + fade transitions. `startDestination = "tutorial"`.
Routes: `tutorial` → `home`, then `home` fans out to `library`, `create`, `profile`,
`adventure`, `sticker_book`, and `story_details/{storyId}` (→ reader flow).

## Data & AI layer

- **Room** (`AppDatabase`, version 4, `exportSchema = false`): entities `UserProfile`,
  `GeneratedStoryContent`, `AdventureSaveState`, `PlacedSticker`; one DAO each.
  `DatabaseProvider.getDatabase()` is a `@Volatile` double-checked singleton that
  preloads sample stories on `onCreate` and uses `fallbackToDestructiveMigration()`.
- **GeminiService** (`data/GeminiService.kt`): Retrofit interface against
  `https://generativelanguage.googleapis.com/`:
  - Stories: `POST v1beta/models/gemini-3.5-flash:generateContent`
  - Cover art: `POST v1beta/models/imagen-3.0-generate-002:generateImages`
  - API key passed as `?key=` query param (from `BuildConfig`, injected by Secrets plugin).
  - `RetrofitClient` uses kotlinx.serialization (`ignoreUnknownKeys`) and 60s OkHttp timeouts.

## Environment / Secrets

`.env.example` documents `GEMINI_API_KEY`. The Secrets Gradle Plugin reads `.env`
(falling back to `.env.example`) at build time and exposes values via `BuildConfig`.
**Never hardcode keys in Kotlin source.** In AI Studio, the key is injected from user secrets.

## Common Commands (Android)

```bash
# Generate the wrapper first if ./gradlew is absent:
gradle wrapper

./gradlew :app:assembleDebug          # build debug APK
./gradlew :app:installDebug           # install on a connected device/emulator
./gradlew :app:testDebugUnitTest      # JVM/Robolectric unit tests
./gradlew :app:verifyRoborazzi        # screenshot tests (recordRoborazzi to update goldens)
./gradlew :app:connectedAndroidTest   # instrumented (Espresso) tests
./gradlew :app:lint                   # Android lint
```

Signing: `debugConfig` uses the bundled `debug.keystore`; `release` reads
`KEYSTORE_PATH` / `STORE_PASSWORD` / `KEY_PASSWORD` from the environment.

## Android Conventions

- Kotlin `official` code style; immutable state via `StateFlow`, collected with
  `collectAsStateWithLifecycle()` so background narration/sound is lifecycle-safe.
- Compose-first, Material 3 design tokens (theme in `ui/theme/`); support light / dark / midnight modes (`AppPreferences.isDarkMode` / `isMidnightMode`).
- Room access through DAOs + KSP-generated code only; bump DB `version` and add a
  migration (or accept destructive fallback) on schema change.
- All persistence stays on-device (COPPA — see ADR-0006 consequences).
- Add/upgrade dependencies via the **version catalog** (`gradle/libs.versions.toml`), not inline.

---

# PART 2 — LEGACY: Expo (React Native) + Express

> Being phased out per ADR-0006. The **organized, buildable** copy is under
> `legacy-prototype/react-native/` (`components/`, `config/`, `hooks/`, `services/`,
> `screens/`, `tests/`). The flattened copies at repo root are stale — their imports
> (`server/index.ts`, `./routes/health`, `lib/…`) do **not** resolve, so the legacy npm
> scripts (`server:dev`, `expo:dev`, `scripts/build.js`) will **not** run from root as-is.

### Stack (versions verified in `package.json`)
- **Frontend:** Expo SDK `~55`, React Native `0.85.3` (New Architecture), Expo Router `~56`, React `19.2.7`, React Compiler
- **State:** TanStack React Query v5 + React Context; AsyncStorage for local data
- **Validation:** Zod v4 · **Backend:** Express v5, Node `>=20.19 <21 || >=22.13 <23`, npm `>=10`
- **DB:** PostgreSQL + Drizzle ORM v0.45 (voice-chat only) · **Auth:** Firebase Admin (optional)
- **TTS:** ElevenLabs · **Video:** OpenAI Sora 2 (optional)

### Legacy npm scripts (`package.json`)
`expo:dev`, `server:dev` (`tsx server/index.ts`), `server:build` (esbuild), `server:prod`,
`expo:static:build`, `db:push`, `lint`, `lint:fix`, `typecheck`, `test` (vitest),
`test:watch`, `test:coverage`, plus newer: `audit`, `audit:fix`, `preflight`,
and `build` (now just `echo 'Build OK'`).

### Legacy AI router (multi-provider fallback)
Text chain (circuit-broken + retried, `server/ai/router.ts`):
Anthropic `claude-sonnet-4-6` → Gemini `gemini-2.5-flash` → OpenAI `gpt-4o-mini` →
OpenRouter Meta-Llama `llama-4-scout-17b-16e-instruct` → xAI `grok-3-mini` →
Mistral `mistral-small-3.1-24b-instruct` → Cohere `command-a-03-2025`.
Image chain: Gemini `gemini-2.5-flash-image` → OpenAI `gpt-image-1`.

### Legacy server route modules
`routes.ts` composes `routes/{health,story,images,tts,music,suggest,video}.ts`
(+ `context.ts` singletons, `helpers.ts` rate-limit/error plumbing) behind a
per-route auth gate, rate limiter, load-shedding, and idempotency cache.

### Legacy client hooks (story screen)
`useStoryAudio`, `useAutoAdvance`, `useBackgroundMusic`, `useLoadingMessages`,
`useSceneGeneration`, `useSleepTimer`, `useVideoGeneration`.

### Story modes & durations (shared product spec)
Modes: **Classic** (choices), **Mad Libs** (silly, user words), **Sleep** (calming).
Durations: short(3/200–300w), medium-short(4/350–450), medium(5/500–650),
long(6/750–950), epic(7/1000–1300).

### AsyncStorage keys (legacy client)
`@infinity_heroes_<descriptor>`: `app_settings`, `profiles`, `active_profile`, `stories`,
`read`, `badges`, `streaks`, `parent_controls`, `favorites`, `onboarding_complete`,
`parent_consent`, `preferences` (legacy), `settings_migrated`, `storage_version`.

### Child-safety rules (still apply to ALL AI prompts, both stacks)
- No violence, weapons, scary/horror, death, injury, illness, abandonment, or loss
- No real brands, celebrities, or copyrighted characters
- No bullying, exclusion, or anxiety-inducing language; all choices lead to positive outcomes
- Focus on courage, kindness, friendship, wonder, imagination, comfort
- (Legacy) all user string inputs sanitized via `sanitizeString()` before prompt inclusion;
  AI keys stay server-side only.

### Shared product content
- **8 hero templates:** Nova, Coral, Orion, Luna, Nimbus, Bloom, Whistle, Shade
- **6 content themes:** courage · kindness · friendship · wonder · imagination · comfort
- **9 narrator voices** (legacy ElevenLabs) across Sleep / Classic / Fun modes
- **12 badges** (First Adventure, Night Owl, Early Bird, Hero Collector, Silly Storyteller,
  Dream Weaver, Classic Champion, On Fire!, Diamond Reader, Bookworm, Story Legend, Word Wizard)

---

# Repository State / Known Clutter

The repo root is a **flattened export/dump**, not a clean source tree. To avoid being misled:

- **Canonical Android source** → `app/src/`.
- **Canonical legacy RN source** → `legacy-prototype/react-native/`.
- **Root-level `.ts`/`.tsx`** (e.g. `index.ts`, `router.ts`, `story.ts`, `storage.ts`,
  `*.tsx` screens/components) → **stale flattened copies**; imports don't resolve. Don't edit
  these expecting the app to build.
- **`docs/`** is reorganized: `docs/guides/` (+`legacy/`, `skills/`), `docs/audits/`,
  `docs/agents/` (12 specs), `docs/adr/` (6 ADRs incl. `0006`), plus an unrelated
  `docs/nextjs-best-practices/` (not used by this project).
- **Junk/duplicate patterns at root:** `-1`-suffixed copies (`index-1.ts`, `_layout-1.tsx`),
  timestamped files (`*_1773*.{ts,md,html,png}`, `ANIMATIONS_1771*.md`, …), `code*.html`,
  `screen*.png`, `*.zip`, `Pasted-*.txt`, a 0-byte `untitled.tsx`, and a malformed
  `storage,comprehensive,test-1.ts`.

## Proposed Cleanup (recommendation — not yet done)

A follow-up cleanup PR should:
1. Delete root timestamped/`-1` duplicate `.md`/`.html`/`.png`/`.ts`, `*.zip`,
   `Pasted-*.txt`, and the empty `untitled.tsx`.
2. Pick a canonical home for the legacy TypeScript app (consolidate into
   `legacy-prototype/` or remove the broken root copies) so imports resolve again.
3. Move stray root docs (`ROADMAP.md`, `CHANGELOG.md`, `TEST-COVERAGE-ANALYSIS.md`,
   `database-migrations.md`, `DESIGN.md`, `SECURITY-FIXES-*.md`) into `docs/`.
4. Remove or relocate the unrelated `docs/nextjs-best-practices/`.
5. Add `.gitignore` / `.gitattributes` rules to stop re-importing screenshot/binary dumps.

---

# Files/Directories — Do Not Modify Without Explicit Approval

- `legacy-prototype/` — frozen reference copy of the old RN/Express app.
- `gradle/libs.versions.toml` — dependency version catalog; coordinate any bump.
- `.replit` — Replit workspace config shared by all contributors.
- The generated/duplicate root dumps (screenshots, `*.zip`, timestamped/`-1` files) —
  leave them for the dedicated cleanup PR rather than touching ad hoc.

# Root-level meta files (reference)
`AGENTS.md`, `CONTRIBUTING.md`, `CONVENTIONS.md`, `GEMINI.md`, `GLOSSARY.md`, `MEMORY.md`,
`TODO.md`, `agent-registry.json`, `skills-lock.json`, and this `CLAUDE.md`.
