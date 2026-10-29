# STATE

**Goal:** A showable demo for the design partner, and a proven architecture (DSQL spike) leading to a deployed walking skeleton.
**Phase:** building
**Updated:** 2026-10-29

## Done
- Discovery: `docs/product/brief.md`, `docs/research/*.md`.
- Architecture ADRs accepted; scope, ambition, Kotlin CDK, proprietary licence decided (see DECISIONS.md).
- Demo prototype built in `web/` (B1–B3: Min dag, job detail, week grid with reassign, invoice basis with RUT). Run instructions and click-through script in `web/README.md`.

## In progress
- Review of the demo on `feat/web-demo` (round 1 findings addressed; awaiting re-review).
- DSQL spike backend skeleton on branch `feat/backend-skeleton` (A1.1/A1.2).

## Next step
- Re-review and merge the demo; show it to the design partner. Continue the spike; CDK stack (A2.1) after the backend slice.

## Waiting on Kent
- AWS account (needed for the spike's cloud part and the walking skeleton): dedicated account, budget alarm, way for the factory to authenticate (SSO profile or OIDC role).
