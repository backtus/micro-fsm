# STATE

**Goal:** —
**Phase:** shelved (2026-10-29)
**Updated:** 2026-11-01

## Why shelved
Kent chose not to pursue the cleaning business. Sweden: Wisegent and Upflow already cover the MVP. Norway: window-cleaning rounds looked like a real gap (see `docs/research/norway-*.md`) but are not a business Kent wants to run. See DECISIONS.md 2026-10-29.

## What exists
- Product brief and research: Sweden (RUT, architecture), Norway (regulation, competitors).
- Clickable Swedish demo in `web/` (`npm --prefix web ci && npm --prefix web run dev`).
- Backend skeleton (Quarkus/Kotlin, DSQL-safe migrator, OCC retry) and CI — green.
- CDK-in-Kotlin spike stack in `infra/` (PR #9): synthesized and tested locally, never deployed.

## Open source
Published under MIT on 2026-11-01 (see DECISIONS.md). No support is offered. The GitHub repository is archived (read-only); unarchive it under Settings to revive the project.

## If revived
Start with interviews (window cleaners in Trondheim), then decide the AWS security boundary (dedicated account, bootstrap policy, budget region) before deploying `infra/`.
