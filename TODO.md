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

- **Cosmic Sticker Book (Phase 2)**: Full interactive sticker book with drag-and-drop, rotatable and scalable canvas elements, dynamic category filtering, background customization, and dynamic unlocks based on story content read.
- **Magical Page Transitions (Phase 2)**: Added subtle, delightful scale-in, slide, and fade page transitions matching modern Material 3 standard aesthetics inside `ReaderScreen.kt`.
- **Home, Reader, Creator, and Library Screens (Phase 1)**: Ported all primary presenter layouts to 100% native Kotlin and Jetpack Compose.
- **Wave-Shaping Ambient Audio (Phase 1)**: Interactive DSP soundscapes dynamically synthesized on-device (`AmbientSoundHelper`) for gentle sleep therapy.
