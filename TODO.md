# TODO.md — Native Android Bedtime Chronicles Prioritized Backlog

This backlog tracks the remaining migration, visual polish, and child safety compliance items for **Infinity Heroes: Bedtime Chronicles** (Native Android Kotlin/Compose Port).

Items are prioritized using **Weighted Shortest Job First (WSJF)**.

---

## 🚀 Active Backlog (Prioritized)

### 🥇 High Priority (Target: Phase 3)

| WSJF | Item / Feature | Category | Job Size | Status | Target File / Area |
| :---: | :--- | :--- | :---: | :---: | :--- |
| **31.0** | **Native Privacy Policy View** | Compliance | Small | ⏳ Pending | `ui/screens/PrivacyScreen.kt` |
| **17.0** | **COPPA Parental Gate mathematical verification overlay** | Security | Medium | ⏳ Pending | `ui/screens/components/ParentGateDialog.kt` |
| **8.0** | **Exponential Backoff AI retry with offline resources fallback** | Quality | Medium | ⏳ Pending | `data/GeminiService.kt` + local sample stories |

### 🥈 Medium Priority (Target: Phase 4)

| WSJF | Item / Feature | Category | Job Size | Status | Target File / Area |
| :---: | :--- | :--- | :---: | :---: | :--- |
| **5.3** | **"Mad Libs" Silly custom prompt injector** | Interactive | Medium | ⏳ Pending | `ui/screens/MadLibsScreen.kt` |
| **3.6** | **Pulsing Orb 8s breathing loop (Canvas) & live starry background** | Sleep UX | Medium | ⏳ Pending | `ui/screens/components/PulsingOrb.kt` |
| **3.0** | **Incremental Room DB version migrations tracking** | Tech Debt | Medium | ⏳ Pending | `data/AppDatabase.kt` |

---

## 🏆 Completed Native Features

- **Idle Creation Tooltip (Phase 3)**: Added a bouncing, contextual "Tap to Create!" tooltip and long-press accessibility popup above the central Home creation button to better guide new explorers into the magical bedtime workflow.
- **Full-Screen Immersive Satchel Modal (Phase 3)**: Designed and implemented an adaptive, high-tactility full-screen dialog overlay utilizing deep space midnight background surfaces, custom color glowing neon item boundaries, and double-layered visual orbs. Automatically adjusts layout between wide-screen multi-column side-by-side split columns (tablet) and compact fluid horizontal-scrolling chip bars (mobile).
- **Bedtime Artifact Chamber (Phase 3)**: Provides immersive detail panels outlining primary calming sleep actions, extensive lore/mythology backgrounds, and instant selection, drop, and story-injecting usage controls. Cataloged the deep contextual panels detailing primary therapeutic sleep effects, unique celestial lore, and instant item-manipulation buttons.
- **Hero Backstory Generator (Phase 3)**: Integrated a high-fidelity selective backstory creation wizard using predefined lore parameters covering origins, motivational paths, and stellar past milestones, backed by direct Gemini API synthesis and resilient locale fallback blocks. Logged the pre-packaged lore creator and selective backstory generator running on the Gemini API interface with local fallback protection.
- **Cosmic Satchel Subsystem & Surrounding Discovery (Phase 3)**: Created client-side limited-capacity database-persisted inventory lists tracking bedtime relics. Included random spatial exploration actions triggering responsive Material Discovery Alerts representing seven legendary Sleep Treasures. Documented the client-to-database persistent inventory tracking limit (5 items) and "Search Surroundings" interactive actions.
- **Cosmic Sticker Book (Phase 2)**: Full interactive sticker book with drag-and-drop, rotatable and scalable canvas elements, dynamic category filtering, background customization, and dynamic unlocks based on story content read.
- **Magical Page Transitions (Phase 2)**: Added subtle, delightful scale-in, slide, and fade page transitions matching modern Material 3 standard aesthetics inside `ReaderScreen.kt`.
- **Home, Reader, Creator, and Library Screens (Phase 1)**: Ported all primary presenter layouts to 100% native Kotlin and Jetpack Compose.
- **Wave-Shaping Ambient Audio (Phase 1)**: Interactive DSP soundscapes dynamically synthesized on-device (`AmbientSoundHelper`) for gentle sleep therapy.
