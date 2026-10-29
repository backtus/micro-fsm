# STATE

**Goal:** —
**Phase:** shelved (2026-10-29)
**Updated:** 2026-10-29

## Why shelved
Kent chose not to pursue the cleaning business. Sweden: Wisegent and Upflow already cover the MVP. Norway: window-cleaning rounds looked like a real gap (see `docs/research/norway-*.md`) but are not a business Kent wants to run. See DECISIONS.md 2026-10-29.

## What exists
- Product brief and research: Sweden (RUT, architecture), Norway (regulation, competitors).
- Clickable Swedish demo in `web/` (`npm --prefix web ci && npm --prefix web run dev`).
- Backend skeleton (Quarkus/Kotlin, DSQL-safe migrator, OCC retry) and CI — green.
- Unmerged: CDK spike stack (PR #5, closed, branch kept).

## If revived
Start with interviews (window cleaners in Trondheim), then decide AWS security boundary before resuming PR #5.
