# COPPA Compliance Analysis — Bedtime Chronicles

**Date:** 2026-06-15  
**Target Platform:** Native Android Application (Kotlin, Jetpack Compose, Room Database)  
**Target Audience:** Children ages 3–9 and parents.  
**Regulation:** Children's Online Privacy Protection Act (COPPA), 15 U.S.C. § 6501 et seq.  

---

## 🛡️ 1. Overview & Core Compliance Directive

Because **Infinity Heroes: Bedtime Chronicles** specifically targets children ages 3–9, the app architecture has been designed on an **offline-first local-by-default runtime principle**. This ensures complete, robust compliance with the Children's Online Privacy Protection Act (COPPA) without requiring complex backend servers or remote user tracking.

No cloud registration, email sign-ups, or social credential sharing is required to run the application, shielding children from public data tracking.

---

## 💾 2. Local Storage Sandbox & Room SQL Schema

All personal metadata (such as custom child profile nicknames, read streak histories, high scores, and generated story books) are persisted exclusively on-device within the sandbox of the native Android operating system (`/data/data/com.example/databases/`).

### Local Data Scope Matrix
| Data Unit | Storage Engine | Contains Personal Info? | Operational Purpose |
|---|---|---|---|
| `UserProfile` (table) | Room SQLite DB | **Nickname & favorite hero** | Locally tracks streaks, developmental read counts, and unlocked achievement trophies. Nicknames do not require real names; parents can supply pseudonyms. |
| `GeneratedStory` (table) | Room SQLite DB | **Reference to child name** | Houses the paragraphs and coverings of generated bedtime books. |
| Shared Preferences | Private Prefs | **No** | Stores basic configurations like audio synthesizers volume levels, tutorial states, and current profile ID. |

---

## 🛰️ 3. Stateless GenAI Communication Boundaries

To perform dynamic, magical bedtime story modifications, the client makes direct type-safe Retrofit calls to the Google Gemini and Imagen REST API endpoints:
- **No Tracking Logs**: These requests contains zero persistent tracking flags, cookies, advertising identifiers, or user telemetry headers.
- **Strict Anonymity**: Requests are fully anonymous and stateless. The Gemini engine processes prompt text templates (e.g. *"Generate a classic bedtime story about superhero Leo and a talking owl resolving a mystery"*), returns content models, and deletes execution contexts immediately.
- **Children Safety Rules**: System instructions supplied with every request enforce strict kid-appropriate boundaries to prevent the model from generating themes unsuitable for sleep environments.

---

## 👪 4. Parent Co-Presence & Gates

To keep settings, credentials, and configuration boundaries secure and supervised:
- Any access to custom settings, text-size toggles, system volume controls, or custom profile generation panels can are protected via simple **Parental Inset Gates** (e.g., math solver popup challenges, PIN code guards, or simple age-verification panels).
- This ensures human-in-the-loop validation, providing parents full authority and observation over the learning environment.
