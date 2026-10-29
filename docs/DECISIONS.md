# Decisions

Newest first. One entry per consequential decision.

## 2026-10-29 — Backend language: Kotlin + Quarkus

- **Decision:** Business logic and backend services in Kotlin on Quarkus, deployed to AWS Lambda.
- **Context:** Kent's preferred long-term business language; Quarkus has first-class Lambda support and native compilation.
- **Alternatives:** Go (faster cold starts with zero effort, simpler builds) — rejected for business code; still allowed for tooling/front-end.
- **Consequences:** Cold-start strategy (native vs SnapStart) must be decided; build pipeline will be heavier than Go.

## 2026-10-29 — Region and data protection baseline

- **Decision:** All data and compute in `eu-north-1`. Personnummer treated as sensitive: encrypted, never logged, minimal access.
- **Context:** Swedish customers, RUT requires personnummer, GDPR.
- **Alternatives:** none considered seriously.
- **Consequences:** Some AWS services/features not available in eu-north-1 must be checked per decision.
