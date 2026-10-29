# Decisions

Newest first. One entry per consequential decision. Entries marked **PROPOSED** await Kent's approval. Evidence: `docs/research/architecture-options.md`.

## 2026-10-29 — PROPOSED — Database: Aurora DSQL, gated by a spike

- **Decision:** Aurora DSQL (Postgres-compatible, scale-to-zero, no VPC) with a shared schema, `tenant_id` on every table, UUIDv7 keys. Adoption conditional on a 1-day spike proving Quarkus + Hibernate/Panache + Flyway + DSQL connector + optimistic-concurrency retries work in eu-north-1.
- **Context:** Relational domain; idle cost must be ~$0; Lambda outside VPC avoids NAT cost.
- **Alternatives:** DynamoDB single-table (true $0, but awkward for scheduling/reporting queries and our relational model); Aurora Serverless v2 (full Postgres, but ~15 s resume and VPC, ~$40+/month realistic); Neon (not in Stockholm).
- **Consequences:** No pgcrypto/extensions → app-level encryption. One DDL per transaction affects migrations. If the spike fails, fall back to DynamoDB.

## 2026-10-29 — PROPOSED — Lambda runtime: JVM + SnapStart (arm64), native-ready

- **Decision:** Single Quarkus "lambdalith" behind API Gateway HTTP API, JVM runtime with SnapStart on arm64. Keep code native-compatible to switch to Mandrel if cold starts disappoint.
- **Context:** Native builds are slow and fragile in CI; SnapStart gives sub-second cold starts with normal JVM builds. eu-north-1 arm64 SnapStart availability to be verified in the spike.
- **Alternatives:** Mandrel native (fastest cold start, heavy builds); many small functions (more infra, no benefit at this scale).
- **Consequences:** Measure cold start in the walking skeleton; revisit if p95 > 1 s.

## 2026-10-29 — PROPOSED — Auth: Cognito Essentials; BankID later via broker

- **Decision:** Cognito user pool with tenant id in token claims; JWT authorizer on API Gateway. BankID added post-MVP via a broker (e.g. Criipto/Idura).
- **Alternatives:** Direct BankID (SEK 80–250k setup); third-party IdPs (Auth0 etc., cost).
- **Consequences:** Free up to 10k MAU.

## 2026-10-29 — PROPOSED — Personal data encryption

- **Decision:** Personnummer and access notes encrypted in the application with one KMS customer-managed key (envelope encryption); keyed hash (HMAC) for lookup.
- **Consequences:** ~$1/month fixed cost per key.

## 2026-10-29 — PROPOSED — Front end, IaC, CI

- **Decision:** Vite + React PWA on S3 + CloudFront. AWS CDK for IaC (language: see open question). GitHub Actions with OIDC to AWS — no long-lived keys.
- **Alternatives:** SAM (less flexible), Terraform/OpenTofu (state backend to manage, separate language).
- **Consequences:** Estimated idle bill ~$1.60–1.80/month per environment.

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
