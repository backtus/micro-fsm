# Decisions

Newest first. One entry per consequential decision. Evidence: `docs/research/`.

## 2026-10-29 — Web prototype dependencies

- **Decision:** `web/` uses react, react-dom, react-router-dom (HashRouter routing for the static demo), vite and @vitejs/plugin-react (build/dev), typescript (strict), vitest and jsdom (test runner and DOM), @testing-library/react, @testing-library/user-event and @testing-library/jest-dom (behaviour tests), eslint, @eslint/js, typescript-eslint, eslint-plugin-react-hooks and globals (lint). No UI framework; plain CSS + CSS Modules.
- **Context:** Design-partner prototype (B1–B3); consistent with the Vite + React front-end decision. All data is invented; no personnummer.
- **Alternatives:** UI framework (rejected: more weight, little value for a small demo).
- **Consequences:** RUT rounding (down to whole kronor) is provisional until checked against Begaran.xsd.

## 2026-10-29 — Backend skeleton: toolchain and dependencies

- **Decision:** Java 25 (`java25` Lambda runtime, Amazon Linux 2023, deprecation 2029-06-30; newest LTS Lambda offers, SnapStart supported in supported Regions — eu-north-1 still to verify in the spike). Quarkus 3.40.1 (Hibernate ORM 7.4, so `@UuidGenerator(style = VERSION_7)` is used and no in-house UUIDv7 generator), Kotlin 2.4.21, Gradle 9.8.2 (Kotlin DSL, version catalog, foojay toolchain resolver 1.0.0 to auto-provision the JDK).
- **Dependencies (reasons):**
  - `quarkus-amazon-lambda-http` — run JAX-RS app behind API Gateway HTTP API; produces `function.zip`.
  - `quarkus-rest`, `quarkus-rest-jackson`, `jackson-module-kotlin` — REST + JSON with Kotlin data classes.
  - `quarkus-kotlin` — Kotlin support in Quarkus.
  - `quarkus-hibernate-orm-panache-kotlin` — persistence (repository style).
  - `quarkus-jdbc-postgresql` — PostgreSQL driver / Dev Services (DSQL is PG-compatible).
  - `quarkus-flyway`, `quarkus-flyway-postgresql` — migrations (Flyway 12; PostgreSQL module needed for Flyway's PG support).
  - Test only: `quarkus-junit`, `rest-assured`, `assertj-core`.
- **Context:** A1 spike; DSQL decision depends on this skeleton working.
- **Alternatives:** Java 21 (older LTS); in-house UUIDv7 generator (unneeded); AWS Advanced JDBC Wrapper DSQL plugin (heavier).
- **Consequences:** Flyway's PostgreSQL transactional lock is turned off and `${index_async}` is supplied by a `FlywayConfigurationCustomizer` (a Quarkus property cannot hold an empty value).

## 2026-10-29 — Personal data inventory

- **Decision:** Personal data is limited to these categories and purposes: customer identity, contact and address (service delivery, invoicing); personnummer (RUT claim only, encrypted); property identifiers for RUT where needed; access notes and door codes (job access, encrypted); worker identity and contact (scheduling); assignments and time reports (scheduling, invoicing, RUT actual hours); free-text job notes (operational instructions only; UI guidance tells users not to enter sensitive data such as health information). Retention is TBD, with the 7-year bookkeeping requirement as a consideration. The MVP stores no personal data offline on devices.
- **Context:** GDPR; offline storage rules are undecided, so MVP caches static assets only.
- **Alternatives:** none considered; collecting less is preferred.
- **Consequences:** Retention and deletion rules must be decided before launch; each new data field must map to a purpose here.

## 2026-10-29 — Product scope and ambition

- **Decision:** Side-income micro-SaaS; keep ambition low and achievable. Segment order: home cleaning → move-out cleaning (flyttstädning) → possibly moving. RUT support at XML-file level (Skatteverket upload, schema v6) in MVP; Fortnox integration is the first post-MVP item; Spiris later.
- **Alternatives:** venture-scale ambition; Fortnox in MVP (rejected).
- **Context:** Kent's direction. One potential design partner (the person who asked for this) — needs something concrete to see before any promises.
- **Consequences:** Prioritise a showable demo early. Avoid investments that only pay off at scale (partner programmes, sales tooling, BankID).

## 2026-10-29 — Proprietary code

- **Decision:** Removed the MIT license; the code is proprietary (all rights reserved).
- **Context:** Commercial SaaS.
- **Alternatives:** keep MIT (open source).
- **Consequences:** none.

## 2026-10-29 — IaC language: Kotlin

- **Decision:** AWS CDK written in Kotlin.
- **Context:** One language across the stack.
- **Alternatives:** TypeScript (best CDK ergonomics and examples).
- **Consequences:** One language across the stack; fewer copy-paste examples, some verbosity.

## 2026-10-29 — Database: Aurora DSQL, gated by a spike

- **Decision:** Aurora DSQL (Postgres-compatible, scale-to-zero, no VPC) with a shared schema, `tenant_id` on every table, UUIDv7 keys. Adoption conditional on a 1-day spike proving Quarkus + Hibernate/Panache + Flyway + DSQL connector + optimistic-concurrency retries work in eu-north-1.
- **Context:** Relational domain; idle cost must be ~$0; Lambda outside VPC avoids NAT cost.
- **Alternatives:** DynamoDB single-table (true $0, but awkward for scheduling/reporting queries and our relational model); Aurora Serverless v2 (full Postgres, but ~15 s resume and VPC, ~$40+/month realistic); Neon (not in Stockholm). Aurora/Neon are not fallbacks.
- **Consequences:** No pgcrypto/extensions → app-level encryption. One DDL per transaction affects migrations. If the spike fails, fall back to DynamoDB.

## 2026-10-29 — Lambda runtime: JVM + SnapStart (arm64), native-ready

- **Decision:** Single Quarkus "lambdalith" behind API Gateway HTTP API, JVM runtime with SnapStart on arm64. Keep code native-compatible to switch to Mandrel if cold starts disappoint.
- **Context:** Native builds are slow and fragile in CI; SnapStart gives sub-second cold starts with normal JVM builds. eu-north-1 arm64 SnapStart availability to be verified in the spike.
- **Alternatives:** Mandrel native (fastest cold start, heavy builds); many small functions (more infra, no benefit at this scale).
- **Consequences:** Measure cold start in the walking skeleton; revisit if p95 > 1 s.

## 2026-10-29 — Auth: Cognito Essentials; BankID later via broker

- **Decision:** Cognito user pool with tenant id in token claims; JWT authorizer on API Gateway. BankID added post-MVP via a broker (e.g. Criipto/Idura).
- **Context:** Cheap, AWS-native, tenant claims in token.
- **Alternatives:** Direct BankID (SEK 80–250k setup); third-party IdPs (Auth0 etc., cost).
- **Consequences:** Free up to 10k MAU.

## 2026-10-29 — Personal data encryption

- **Decision:** Personnummer and access notes encrypted in the application with one KMS customer-managed key (envelope encryption); keyed hash (HMAC) for lookup.
- **Context:** Personnummer is sensitive; DSQL lacks pgcrypto.
- **Alternatives:** DSQL-managed encryption at rest only (rejected: personnummer and access notes also require application-level encryption).
- **Consequences:** ~$1/month fixed cost per key.

## 2026-10-29 — Front end, IaC, CI

- **Context:** Cheap static hosting, keyless CI.
- **Decision:** Vite + React PWA on S3 + CloudFront. AWS CDK for IaC, written in Kotlin. GitHub Actions with OIDC to AWS — no long-lived keys.
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
