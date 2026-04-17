# Development Roadmap

**Last Updated:** 2026-04-17

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
| Add testing framework (Vitest) | Testing | 2026-04-07 |
| Add KeyboardAwareScrollView to forms | UX | 2026-04-07 |
| Implement read/unread story indicators | Feature | 2026-04-07 |
| Add `npm audit` to CI | Security | 2026-04-17 |

## Backlog (Prioritized)

### High Priority

| # | Item | Value | Criticality | Risk | Size | WSJF | Notes |
|---|------|-------|-------------|------|------|------|-------|
| 1 | Upgrade to Expo SDK 55 | 5 | 8 | 5 | 3 | 6.0 | Removes need for expo-asset patch; unblocks high-severity `@xmldom/xmldom` fix |
| 2 | Use HeroCard.tsx in quick-create | 3 | 2 | 1 | 2 | 3.0 | Reuse orphaned component |
| 3 | Build voice chat UI screen | 8 | 3 | 2 | 5 | 2.6 | Backend routes ready; needs Expo screen + audio recording |
| 4 | Build story feedback/rating UI | 3 | 2 | 1 | 3 | 2.0 | Storage function exists (updateFeedback); no call sites yet |

### Low Priority (WSJF < 4)

| # | Item | Value | Criticality | Risk | Size | WSJF | Notes |
|---|------|-------|-------------|------|------|------|-------|
| 5 | Add persistent rate limiting (Redis) | 3 | 1 | 3 | 3 | 2.3 | Current in-memory is sufficient for single-instance |
| 6 | Add authentication | 5 | 1 | 3 | 8 | 1.1 | Only needed if costs become a concern |
| 7 | Encrypt client-side storage | 2 | 1 | 2 | 5 | 1.0 | Data is non-sensitive (stories, badges) |

## Dependencies

- Item 1 (Expo 55) blocks removal of `patches/expo-asset+12.0.12.patch` and fixes transitive `@xmldom/xmldom` high CVE
- Item 3 (Voice chat UI) depends on audio recording permissions and expo-av
- Item 6 (Auth) would require significant architecture changes

## Known Audit Issues

7 high-severity CVEs remain in transitive dependencies — all require breaking major-version bumps:
- `@xmldom/xmldom` — blocked by Expo SDK 55 upgrade (Item 1)
- `drizzle-orm` — requires 0.39 → 0.45 breaking upgrade
- `@tootallnate/once` (via `firebase-admin`) — requires firebase-admin downgrade

CI currently uses `--audit-level=critical`. Tighten to `--audit-level=high` after Item 1 and drizzle-orm upgrade.

## Tracked TODOs in Code

| Location | TODO | Blocked By |
|----------|------|------------|
| `patches/expo-asset+12.0.12.patch:9` | Remove HTTPS dev server patch | Expo SDK 55 upgrade |
