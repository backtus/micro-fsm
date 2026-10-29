# STATE

**Goal:** Define the FSM product and its architecture well enough to build a first thin, deployed slice.
**Phase:** discovery
**Updated:** 2026-10-29

## Done
- Repo bootstrapped with factory docs structure.
- Backend stack decided: Kotlin + Quarkus on Lambda (see DECISIONS.md).

## In progress
- Discovery: product brief, architecture research (database, Quarkus on Lambda runtime, IaC), RUT/Skatteverket and accounting integration research.

## Next step
- Kent reviews `docs/product/brief.md` and the proposed ADRs.

## Waiting on Kent
- Review of discovery output.
- License: repo has MIT; confirm that is intended for a commercial SaaS.
- `gh auth login` (needed for PRs).
