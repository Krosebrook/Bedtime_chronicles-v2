# Development Roadmap

**Last Updated:** 2026-04-07

Items are scored using Weighted Shortest Job First (WSJF): `(Business Value + Time Criticality + Risk Reduction) / Job Size`

## Completed

| Item | Category | Date |
|------|----------|------|
| Unify dual settings systems | Bug Fix | 2026-03-13 |
| Add security headers | Security | 2026-03-13 |
| Wire up voice chat routes | Feature | 2026-03-13 |
| Resolve dead code triage | Maintenance | 2026-03-13 |
| Restore saveStoryScene persistence | Bug Fix | 2026-03-13 |
| Fix storyId mismatch in completion | Bug Fix | 2026-03-13 |
| Create comprehensive documentation | Docs | 2026-03-13 |
| Update .env.example | Docs | 2026-03-13 |
| Add testing framework (Vitest) | Tech Debt | 2026-04-07 |
| Add KeyboardAwareScrollView to forms | Feature | 2026-04-07 |
| Wire read/unread story indicators | Feature | 2026-04-07 |
| Wire story feedback/rating UI | Feature | 2026-04-07 |
| Reuse HeroCard.tsx in hero selection | Feature | 2026-04-07 |
| Add npm audit to CI | Security | 2026-04-07 |
| Fix audio pipeline (model name + ffmpeg) | Bug Fix | 2026-03-25 |
| Model audit (rolling aliases, dead refs) | Maintenance | 2026-03-25 |
| Wire Gemini image integration route | Feature | 2026-03-25 |
| Harden input validation (parseInt, content, audio, voice) | Security | 2026-03-25 |
| Add conversation pagination | Feature | 2026-03-25 |
| Add Permissions-Policy & X-Permitted-Cross-Domain-Policies headers | Security | 2026-03-25 |
| Safe JSON parsing for AI responses | Bug Fix | 2026-03-25 |
| Replace unsafe `catch (error: any)` patterns | Code Quality | 2026-03-25 |

## Backlog (Prioritized)

### High Priority

| # | Item | Value | Criticality | Risk | Size | WSJF | Notes |
|---|------|-------|-------------|------|------|------|-------|
| 1 | Upgrade to Expo SDK 55 | 5 | 8 | 5 | 3 | 6.0 | Removes need for expo-asset patch (TODO in patches/) |
| 2 | Build voice chat UI screen | 8 | 3 | 2 | 5 | 2.6 | Backend routes ready; needs Expo screen + audio recording |

### Low Priority (WSJF < 4)

| # | Item | Value | Criticality | Risk | Size | WSJF | Notes |
|---|------|-------|-------------|------|------|------|-------|
| 9 | Add persistent rate limiting (Redis) | 3 | 1 | 3 | 3 | 2.3 | Current in-memory is sufficient for single-instance |
| 10 | Add authentication | 5 | 1 | 3 | 8 | 1.1 | Only needed if costs become a concern |
| 11 | Encrypt client-side storage | 2 | 1 | 2 | 5 | 1.0 | Data is non-sensitive (stories, badges) |

## Dependencies

- Item 2 (Expo 55) blocks removal of `patches/expo-asset+12.0.12.patch`
- Item 4 (Voice chat UI) depends on audio recording permissions and expo-av
- Item 10 (Auth) would require significant architecture changes

## Tracked TODOs in Code

| Location | TODO | Blocked By |
|----------|------|------------|
| `patches/expo-asset+12.0.12.patch:9` | Remove HTTPS dev server patch | Expo SDK 55 upgrade |
