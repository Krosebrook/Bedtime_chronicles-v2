# Audit & Next-Sprint Proposal — 2026-07-15

This is a companion note to the full audit done in the canonical repo:
`chaosclubco/infinite-heros-bedtime-chronicles-v5` PR #353
(`docs/AUDIT-NEXT-SPRINT-2026-07-15.md` there — a private repo, so no live link
here; see that repo directly). Most of that audit's findings are about the
canonical repo's own code and don't apply here. This note covers the one
finding specific to **this** repo, corrected after review (see below).

## Finding: this repo never received the M1–M3 work — code or docs

PR #3 ("Merge planning: adopt canonical v5 super-version tree", 2026-07-13)
copied a snapshot of the canonical repo's `CLAUDE.md` into this repo — but
that snapshot predates the canonical repo's M1–M3 work (the AI-output
content-safety guard, on-device narration cache, and durable cloud-sync
engine, added there over 2026-07-12–13). **An earlier version of this note
claimed this repo's `CLAUDE.md` describes those features as shipped — that
was wrong** (caught in review, thank you). Checked directly: this repo's
`CLAUDE.md` has no mention of `server/safety/*`, `SyncManager.tsx`,
`lib/sync/`, `lib/tts-cache.ts`/`lib/narration.ts`, or `cloudSyncEnabled`
anywhere. The docs and the code are consistent with each other here — both
are just missing the same later work.

What *is* still true, and still worth a line: this repo's story-generation
safety is the **prompt-side-only** version — `CHILD_SAFETY_RULES`
(`server/prompts.ts`) is present and included in every generation prompt, as
it always has been, but the newer **output-side** re-check (moderate the
AI's actual response, regenerate once if it fails, fall back to a known-safe
story as a last resort) that the canonical repo added does not exist here.
That's a real capability gap between the two repos' safety postures, just
not a docs-vs-code mismatch the way the earlier version of this note
described it.

Diffing this branch's tree against the canonical repo's current `main`
(`git ls-tree -r --name-only`, both at their HEADs) confirms 19 files are
absent here: `server/safety/*`, `shared/safety-terms.ts`, `lib/sync/*`,
`components/SyncManager.tsx`, `lib/SyncOfflineContext.tsx`,
`shared/sync-types.ts`, `supabase/migrations/0001_sync_tables.sql`,
`lib/tts-cache.ts`, `lib/narration.ts`, and their test files.

This repo's `main` and this audit branch are otherwise current (Dependabot
commits through 2026-07-13; typecheck/lint/test status not independently
re-verified here — see the canonical repo's audit for those numbers, which
apply once the code above is ported).

## Recommendation

Next sprint should port the 19 files from the canonical repo — they're
already built, tested, and running there, so this is a copy + wire-up +
test-run, not new development. Priority order if split up: the output-side
safety guard first (closes the real capability gap above), then the
on-device TTS cache and cloud-sync engine (parity/feature work, lower
urgency since cloud sync is opt-in and off by default even in canonical).

Full WSJF scoring and the rest of the cross-repo picture (including a
similarly-stalled convergence in `krosebrook/infinite-heros-bedtime-chronicles-v5-main`)
is in the canonical repo's audit doc referenced above.
