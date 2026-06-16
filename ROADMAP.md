# Master Development & Technical Roadmap: Infinity Heroes Native Port

**Last Updated:** 2026-06-15  
**Project:** Infinity Heroes: Bedtime Chronicles (Android Jetpack Compose Native App)  
**Target Platform:** Native Android (Min SDK: 26, Target SDK: 35)  
**Underlying Architecture:** Kotlin MVVM + Jetpack Compose M3 + Room SQLite Database + Retrofit REST Client  

---

## 🌌 Executive Summary
This document serves as the absolute source of truth for the native Android transition of **Infinity Heroes: Bedtime Chronicles**. It merges the legacy launch readiness phases (`2026-06-11-launch-readiness-phases.md`), the native features backlog (`ROADMAP.md`), the launch-readiness outline (`2026-04-08-launch-readiness.md`), and the maturity pipeline (`2026-04-08-maturity.md`). 

All major presentation modules (Home, Story Reader, Library, and Character Creation) are successfully running natively on Android with responsive styling and advanced fade-in page transitions. Core child safety protocols (COPPA compliance) and high-value interactive features are queued next.

---

## 🎯 Master Porting & Feature Status Checklist

| Legacy Feature Area | Native File Location (Kotlin Equivalent) | Target Phase | Port Status | Details / Gap Analysis |
|:---|:---|:---:|:---:|:---|
| **Home Screen Redesign** | `ui/screens/HomeScreen.kt` | Phase 1 | ✅ Completed | Displays ongoing adventures, custom constellation backgrounds, and feature launches. |
| **Character Creation** | `ui/screens/CreateStoryScreen.kt` | Phase 1 | ✅ Completed | Interactive multi-genre carousel selector, dynamic hero profiles, setting parameters. |
| **Chronicles Library** | `ui/screens/LibraryScreen.kt` | Phase 1 | ✅ Completed | Catalog of generated chronicles, filtered with stylish genre-themed filter chips. |
| **Bedtime Story Reader** | `ui/screens/ReaderScreen.kt` | Phase 1 | ✅ Completed | Interactive multi-page layout. Features custom line font scaling, paragraph narration. |
| **Interactive Transitions** | `ui/screens/ReaderScreen.kt` (page animated bounds) | Phase 2 | ✅ Completed | **Subtle magical fade-in, scale, and soft slide** page transitions added. |
| **Cosmic Sticker Book** | `ui/screens/StickerBookScreen.kt` | Phase 2 | ✅ Completed | Canvas workspace allowing drag and drop, scale, rotation, and dynamic keyword unlocks. |
| **COPPA Parental Gate** | `ui/screens/components/ParentGateDialog.kt` | Phase 3 | ⏳ Pending | Arithmetic authentication challenges ($x+y$ gating) to safeguard settings and API routes. |
| **Native Privacy Screen** | `ui/screens/PrivacyScreen.kt` | Phase 3 | ⏳ Pending | Native UI rendering layout of COPPA-compliant privacy policy clauses. |
| **"Mad Libs" Silly Mode** | `ui/screens/MadLibsScreen.kt` | Phase 4 | ⏳ Pending | custom inputs (silly noun, superhero food, places) injected directly into Gemini prompts. |
| **Calming Pulsing Orb** | `ui/screens/components/PulsingOrb.kt` | Phase 4 | ⏳ Pending | Custom Canvas-rendered pulsing orb running an 8-sec breathing loop (4s inhale / 4s exhale). |
| **Hero Voice Chat Screen** | `ui/screens/VoiceChatScreen.kt` | Phase 5 | ⏳ Pending | Multimodal voice interaction screen coordinating audio recorder, Base64 JSON streams. |

---

## 📋 WSJF Prioritization & Backlog Scoring

To maximize delivery momentum and value, items are prioritized using **Weighted Shortest Job First (WSJF)**.

$$\text{WSJF} = \frac{\text{Business Value} + \text{Time Criticality} + \text{Risk Reduction / Opp Enablement}}{\text{Job Size}}$$

*   **Scale:** Fibonacci-like integers (1, 2, 3, 5, 8, 13)

### WSJF Backlog Prioritization Matrix

| Rank | Item Name | BV | TC | RR | Size | Score | Proposed Category |
|:---:|:---|:---:|:---:|:---:|:---:|:---:|:---|
| **1** | First-Launch / Settings Native Privacy Policy Screen | 13 | 13 | 5 | 1 | **31.0** | Compliance |
| **2** | COPPA Parental Gate & Consent Modal overlays | 13 | 13 | 8 | 2 | **17.0** | Security / Compliance |
| **3** | AI retry patterns with Exponential Jitter & Offlines fallback | 5 | 3 | 8 | 2 | **8.0** | Core Stability |
| **4** | "Mad Libs" Silliness Custom prompt injector | 8 | 3 | 5 | 3 | **5.3** | Interactive Feature |
| **5** | Google Play / EAS native target configs & Play console setup | 13 | 5 | 5 | 5 | **4.6** | Deployment |
| **6** | Dynamic Calming Ambient space starfields & Pulsing Orb loops | 5 | 3 | 3 | 3 | **3.6** | Polish / Sleep UX |
| **7** | Offline Client storage Room DB schema migrations | 5 | 5 | 5 | 5 | **3.0** | Architecture Gaps |
| **8** | Multimodal Spoken voice chat Base64 JSON pipelines | 8 | 2 | 5 | 8 | **1.8** | Highly Complex Feature |

---

## 🛠️ Step-by-Step Phase Implementation

### Tier 1: Compliance & Regulatory Guardrails (Immediate Next Steps)

#### 1. Native Privacy Policy View (WSJF: 31.0)
*   **Target**: Ensure compliant access metadata is easily accessible from any core layout. Required for App Store approval.
*   **Implementation Steps**:
    1. Create `com.example.ui.screens.PrivacyScreen.kt` showcasing custom scrollable text blocks.
    2. Populate it with standardized COPPA data collection notices (e.g., "Privacy policy: No persistent tracking cookie telemetry, audio streams are transient").
    3. Link this view from both the `ProfileScreen` settings panel and the first-run installation prompt.
*   **Testing criteria**: Use `testTag("privacy_view_content")`, scroll down to verify readable text rendering.

#### 2. COPPA Parental Gate & Consent Dialog (WSJF: 17.0)
*   **Target**: Impose a strict interactive check preventing young children from editing profile metrics, triggering custom AI configurations, or accessing API parameters alone.
*   **Implementation Steps**:
    1. Create dynamic challenge helper `ParentGateDialog.kt` rendering a random mathematical equation (e.g. $x + y$ where $x \in [5..12]$, $y \in [4..11]$).
    2. Render modern Material 3 options with dynamic grid bounds.
    3. Persist Consent confirmation flag securely inside `AppPreferences.kt` on positive submit (`is_parent_consented`).
    4. Inject this Gate overlay interceptor block as a requirement before clicking into administrative areas.
*   **Testing criteria**: Add explicit `.testTag("parent_gate_challenge")` and `.testTag("parent_gate_option_correct")` to assert math calculation verification.

---

### Tier 2: Stability & Offline Resilience (Value Scaling)

#### 3. Graceful AI Outage Fallbacks & Local Bedtime Assets (WSJF: 8.0)
*   **Target**: Guarantee a delightful bedtime story companion even on long flights or during internet outages.
*   **Implementation Steps**:
    1. Implement local, preloaded backup assets within `BedtimeAssets.kt` & `SampleStories.kt`.
    2. Configure a retry-handling architecture with an **exponential backoff and random jitter** scheduler around LLM REST requests in `GeminiService.kt`.
    3. If the network continues to fail after 3 attempts, fallback cleanly to a randomized, pre-packaged bedtime local resource adventure story.
*   **Testing criteria**: Create a unit test mock that disables internet connectivity and verify the viewmodel loads local backups without crashing.

#### 4. "Mad Libs" Bedtime Silliness Prompt Injection (WSJF: 5.3)
*   **Target**: Interactive "fill in the blanks" mode. Allows kids to type unique adjectives, names, locations, and toys which are injected into Gemini's story prompt generator.
*   **Implementation Steps**:
    1. Build stateless `MadLibsScreen.kt` using standard M3 `OutlinedTextField` inputs.
    2. Validate completion factors (requires at least 3 filled entries prior to generation).
    3. Feed structured variables (e.g. "setting: candy star, main_character: Bob") into `StoryGenerationViewModel.kt`'s dynamic Prompt Creator wrapper.
*   **Testing criteria**: Verify input tags `"madlibs_input_setting"` and `"madlibs_submit_btn"` are fully accessible and correct text inputs are routed.

---

### Tier 3: Visual Polish & Sleep Hygiene (Sensory Comfort)

#### 5. Ambient Calming Pulsing Orb & Starfield Canvas (WSJF: 3.6)
*   **Target**: Cozy, hypnotic background visual state helping kids fall asleep gently during Text-To-Speech audio streams.
*   **Implementation Steps**:
    1. Create `com.example.ui.screens.components.StarField.kt` with a Compose `Canvas` rendering simple twinkling starry coordinates.
    2. Construct `PulsingOrb.kt` running an infinite, smooth scale transition set to an 8-second breathing loop (4 seconds slow scale-up for inhalation, 4 seconds scale-down for exhalation).
    3. Coordinate gradient colors (soft indigo, twilight amethyst) running behind character narrations.

---

### Tier 4: Future Multimodal Tech (Long-Term Gaps)

#### 6. Spoken Hero Chat Gateway (WSJF: 1.8)
*   **Target**: Immersive verbal chat sandbox where children chat natively with their favorite custom sleep heroes.
*   **Implementation Steps**:
    1. Build `com.example.ui.screens.VoiceChatScreen.kt` with high-contrast conversational elements.
    2. Implement transient recording buffers via Android's `MediaRecorder` or `AudioRecord` APIs.
    3. Encode raw audio samples to Base64 and exchange chat records with the active production backend.
    4. Gracefully play returning synthesized WAV streams with soft 10-second volume fadeout transitions.

---

## 🔒 Mandatory Quality & Engineering Constraints

Consistent with our `AGENTS.md` and `GEMINI.md` mandates, every roadmap action must comply with these technical guardrails:

1.  **State Observation Boundaries**: Always observe flow variables using `collectAsStateWithLifecycle()` to prevent memory leaks and respect standard Android UI bounds.
2.  **Edge-To-Edge Compliance**: Rely strictly on standard Material 3 Scaffolds which automatically calculate adaptive safe window insets. Avoid hardcoded visual paddings.
3.  **No Auditory Cuts**: All playbacks, background soundtracks, and TTS streams must fade smoothly with a **10-second decay curve** to prevent children from waking up during sudden changes.
4.  **Automatic Test Tags**: Ensure every card, button, slider, and selector features a distinct, snake_case test tag modifier (`Modifier.testTag("...")`).
