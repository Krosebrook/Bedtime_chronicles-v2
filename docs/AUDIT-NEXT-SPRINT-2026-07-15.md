# Audit & Next-Sprint Proposal — 2026-07-15

This is a companion note to the full audit done in the canonical repo:
[`chaosclubco/infinite-heros-bedtime-chronicles-v5` PR #353](https://github.com/ChaosClubCo/Infinite-Heros-Bedtime-Chronicles-v5/pull/353)
(`docs/AUDIT-NEXT-SPRINT-2026-07-15.md` there). Most of that audit's findings
are about the canonical repo's own code and don't apply here. This note covers
the one finding that's specific to **this** repo.

## Finding: this repo's docs converged, its code didn't

PR #3 ("Merge planning: adopt canonical v5 super-version tree", 2026-07-13)
copied the canonical repo's `CLAUDE.md` into this repo. That file now
describes, as shipped features:

- An AI-output content-safety guard (`server/safety/*`,
  `shared/safety-terms.ts`) that re-checks every generated story and
  regenerates or falls back to a known-safe story if it fails moderation.
- An on-device narration cache (`lib/tts-cache.ts`, `lib/narration.ts`).
- The M3 durable cloud-sync engine (`lib/sync/*`, `components/SyncManager.tsx`,
  `lib/SyncOfflineContext.tsx`, `shared/sync-types.ts`,
  `supabase/migrations/0001_sync_tables.sql`).

**None of those 19 files exist in this repo.** Diffing this branch's tree
against the canonical repo's `main` (`git ls-tree -r --name-only`, both at
their current HEADs) confirms it — this repo only got the doc, not the
source. Concretely, that means **this repo's own `CLAUDE.md` currently claims
a child-safety moderation control is protecting every generated story, when
in this tree it isn't running at all.** For a children's app, that's the
finding worth flagging loudly rather than letting it sit.

This repo's `main` and this audit branch are otherwise current (dependabot
commits through 2026-07-13; typecheck/lint/test status not independently
re-verified here — see the canonical repo's audit for those numbers, which
apply once the code above is ported).

## Recommendation

Next sprint should either:
1. Port the 19 files from the canonical repo (they're already built, tested,
   and running there — this is a copy + wire-up + test-run, not new
   development), or
2. If this repo is intentionally being left behind pending a fuller
   convergence, correct `CLAUDE.md` here to stop describing unshipped
   features as shipped until that happens.

Full WSJF scoring and the rest of the cross-repo picture (including a
similarly-stalled convergence in `krosebrook/infinite-heros-bedtime-chronicles-v5-main`)
is in the canonical repo's audit doc linked above.
