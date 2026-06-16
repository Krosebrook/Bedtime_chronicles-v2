# 🌌 Infinity Heroes: Bedtime Chronicles (v6)

An elegant, fully-integrated bedtime reading system engineered for native Android (Kotlin, Jetpack Compose, Material 3), high-fidelity programmatic DSP soundscapes, progressive reading memory, and safe sleep transitions.

---

## 🛠️ Project Architecture

This project is fully structured as an offline-first **Modern Android Development (MAD)** application:

*   **View Layer (Compose)**: Uses state-of-the-art UI structures and fully responsive containers utilizing Material 3 tokens.
*   **Audio Synthesis (DSP)**: Programmatic mono-synth executing real-time wave equations (`AmbientSoundHelper`) on local sound buffers to prevent heavy asset downloads.
*   **Storage & Cache**: Anchored by **Jetpack Room SQLite** databases combined with single-instance preferences wrappers (`AppPreferences`).
*   **AI Bedtime Assistant**: Integration structures utilizing the Google Gemini API to structure kid-friendly bedtime reading journeys dynamically.

---

## 📖 Best Practice Guides

To ensure codebase health, security compliance, and reliable designs, please review the appropriate guides:

*   **[Android Best Practices](./docs/guides/ANDROID-BEST-PRACTICES.md)** — Architectural separations, DSP waveshaping formulas, collection of lifecycle-aware flow states, and testing IDs.
*   **[AI Agent Developer Rules](./AGENTS.md)** — Development boundaries, mandate lists, sync checks, and package locations for AI coding helpers working on subsequent turns.
*   **[Cozy Reading Experience](./README-BEDTIME-EXPERIENCE.md)** — Detailed visual guidelines and feature overviews of the bedtime client.

---

## 🚀 Key Bedtime Chronicles Features

### 1. 📖 Progressive Story Memory
*   Saves active reading location down to the precise **active sentence** during narration or manual reading.
*   Intuitive recovery dialogue prompts children to **Resume** or **Start Over** upon returning to a storybook.

### 2. 🎵 Ambient Wave-Shaping Soundscapes
*   *Rain, Ocean Waves, Space Wind, and Forest Breeze* synthesizers generated in background run loops.
*   10-second linear volume decay factor ensures a gentle decay of sound output to safeguard baby sleep states when the timer ticks to zero.

### 3. 🏆 Gamified Bedtime Journeys
*   Select from **8 unique cosmic heroes** using the template configuration carousels.
*   Unlock **12 developmental badges** by hitting streak goals, reading specific categories, or growing vocabulary counts.
*   Design custom emojis avatars instantly using profile picker panels.
