# Plan: DSQL spike (A) and design-partner demo (B)

Planner output 2026-10-29, accepted by Director. Technical decisions 1–12 below accepted; 13 needs Kent.

## Tracks

- **A1.1** Backend skeleton + Job slice (Gradle Kotlin DSL, Quarkus 3.x, Kotlin, Panache, Flyway, `X-Tenant-Id` header, UUIDv7, migration lint test). Tests on Dev Services Postgres.
- **A1.2** OCC retry on SQLSTATE 40001, `POST /jobs/{id}/complete`, `%dsql` profile (DSQL JDBC connector, IAM token, Agroal min 0/max 2/lifetime 50 min, no connection at SnapStart checkpoint).
- **A2.1** CDK-in-Kotlin spike stack (DSQL, Lambda arm64 + SnapStart + alias, HTTP API, budget 10 USD, tags, log retention 7 d). `cdk synth` without credentials; assertion tests.
- **A2.2** Deploy, measure, decide — **blocked on AWS account**. Results in `docs/research/dsql-spike-results.md`. Time-box 1 day.
- **B1** `web/` scaffold (Vite + React + TS strict, Vitest, HashRouter), seed data, store with localStorage + reset, "Prototyp" banner, `calculateInvoiceBasis` in öre with config-driven rates.
- **B2** Field worker: Min dag, job detail, mark done with hours.
- **B3** Manager: week view with reassign, invoice basis with RUT split and notes.

Order: A1.1 → A1.2 → A2.1 → (account) → A2.2; B1 → B2 → B3.

## Canonical commands

- `./gradlew build` — all JVM modules
- `./gradlew :backend:test` — needs Docker/Podman
- `./gradlew :backend:quarkusDev`
- `cd infra && npx cdk synth`
- `npm --prefix web ci && npm --prefix web test && npm --prefix web run build`
- `npm --prefix web run dev` — runs the demo

## Go/no-go — DSQL ADR (all must pass; workarounds must be config, placeholder or <100 LOC)

| # | Criterion | Pass |
|---|---|---|
| D1 | Flyway | V1+V2 apply on empty cluster; rerun is no-op |
| D2 | Hibernate/Panache | A1 flows work unchanged via deployed API |
| D3 | IAM auth | No DB password anywhere; request succeeds after 20 min idle |
| D4 | OCC | 20-way concurrent complete: zero 5xx, consistent state, retries logged |
| D5 | Idle cost | DSQL+Lambda+API < $0.05/day after ≥24 h idle |

## Go/no-go — runtime ADR

| # | Criterion | Pass |
|---|---|---|
| R1 | SnapStart arm64 in eu-north-1 | deploy succeeds |
| R2 | Cold start | client p95 ≤ 1.5 s incl. DB read; Restore p95 ≤ 1.0 s (1.5–3 s: try priming, then Mandrel) |
| R3 | Warm | p95 ≤ 150 ms DB read |

## Accepted technical decisions

1. Root Gradle build (`backend/`, `infra/`); `web/` separate npm project. 2. Spike tenant via header; `@TenantId` with Cognito later. 3. UUIDv7: Hibernate `@UuidGenerator(VERSION_7)` if available, else ~30-line in-house generator. 4. Flyway placeholder `${index_async}`, transactional lock off, migrations via separate invoke. 5. Agroal lazy min 0/max 2. 6. Plain CSS + CSS Modules. 7. Vitest + RTL + jsdom. 8. react-router HashRouter for demo. 9. npm, Node LTS pinned. 10. Integer öre; RUT rounding rule documented and tested, to be confirmed against XSD. 11. Google Maps URL. 12. Budget 10 USD, alerts 50/80/100%.

## Needs Kent

13. Pre-approve `cdk destroy` of the `env=spike` stack after results are written.
