# Architecture options — micro-fsm

Status: research input, 2026-10-29. Not a decision record; decisions go to `docs/DECISIONS.md`. Prices are USD, us-east-1 list prices unless stated; eu-north-1 is typically equal or within ~10%. Items marked ⚠️ need verification in the AWS console/pricing calculator before committing.

Fixed constraints: Kotlin + Quarkus on Lambda, eu-north-1, fully serverless, idle < ~$2/month, multi-tenant (1–30 users per tenant), relational domain, GDPR (personnummer encrypted).

## 1. Database

### Options

- **DynamoDB single-table** — true pay-per-request, 25 GB free tier, zero idle cost. Relational domain (jobs ↔ schedules ↔ staff ↔ invoices ↔ RUT claims) must be hand-modelled into access patterns; ad-hoc reporting is poor (needs export to S3/Athena). Tenant isolation via partition-key prefix + IAM `dynamodb:LeadingKeys` condition.
- **Aurora DSQL** — serverless, PostgreSQL-compatible, billed per DPU ($8/million DPU) + storage ($0.33/GB-month), free tier 100k DPU + 1 GB/month; no instances, no VPC needed (public IAM-authenticated endpoint). Available in eu-north-1 ([AWS docs](https://docs.aws.amazon.com/aurora-dsql/latest/userguide/what-is-aurora-dsql.html), [aws-services.info lists eu-north-1 since 2026-05-11](https://www.aws-services.info/dsql.html)). Pricing: [usage.ai](https://www.usage.ai/blogs/aws/database-savings-plans/aurora/dsql-pricing/). Gaps have been closing: identity columns/sequences are now supported ([dev.to, V. Kazulkin](https://dev.to/aws-heroes/serverless-applications-with-java-and-aurora-dsql-part-5-support-for-identity-columns-and-4lb2)) and the AWS migration guide now states foreign keys are supported ([AWS migration guide](https://docs.aws.amazon.com/aurora-dsql/latest/userguide/working-with-postgresql-compatibility-migration-guide.html)) ⚠️ verify FK enforcement semantics and that it is enabled in eu-north-1. Remaining known limits (verify current list in the [DSQL dialect blog](https://aws.amazon.com/blogs/database/dsql-sql-dialect-how-amazon-aurora-dsql-differs-from-single-instance-postgresql/)): no triggers, no extensions (no pgcrypto, no PostGIS), no row-level security policies (⚠️), one DDL statement per transaction, transaction row/time limits (~3,000 rows / 5 min ⚠️), optimistic concurrency (serialization errors must be retried), IAM token auth instead of passwords. AWS ships a JDBC connector that wraps pgJDBC with IAM token generation ([awslabs/aurora-dsql-connectors](https://github.com/awslabs/aurora-dsql-connectors/blob/main/java/jdbc/README.md)); Hibernate/Panache works via the PostgreSQL dialect with care (prefer UUID keys, avoid batch DDL, Flyway with one-statement-per-transaction migrations ⚠️ Flyway compatibility to be spiked).
- **Aurora Serverless v2 (PostgreSQL) with auto-pause to 0 ACU** — full PostgreSQL (FKs, RLS, extensions, everything Hibernate expects). Auto-pause after an idle timeout; resume takes ~15 s (longer after >24 h paused) — the first request after idle would time out or feel broken. Storage billed while paused (~$0.10–0.12/GB-month) plus I/O. Requires a VPC: Lambda in VPC is fine (no NAT needed if only talking to DB + VPC endpoints), but anything else the function calls (Cognito, S3, KMS, SES) then needs VPC endpoints (~$7–8/month each per AZ) or a NAT (~$35/month) — this breaks the idle budget. Data API avoids VPC-attached Lambda but adds latency and is non-JDBC.
- **Neon / Supabase / other managed Postgres outside AWS** — Neon scale-to-zero with free tier, Postgres-complete, branching is great for agents. Neon has an AWS eu-central-1 (Frankfurt) region; no Stockholm ⚠️. Data stays in EU (GDPR OK with DPA), but it is a second vendor, a second sub-processor to disclose, egress over public internet, and cold start ~0.5–1 s.

### Comparison

|                    | DynamoDB                        | Aurora DSQL                                                         | Aurora Serverless v2 (0 ACU)                      | Neon (Frankfurt) |
|--------------------|---------------------------------|---------------------------------------------------------------------|---------------------------------------------------|------------------|
| Idle cost          | ~$0                             | ~$0 (free tier)                                                     | ~$1–3 storage, + VPC endpoint/NAT costs if needed | $0 (free tier)   |
| ~50 tenants        | ~$1–5                           | ~$2–10 (⚠️ DPU per query is hard to predict; measure)               | $45+ when awake (min 0.5 ACU ≈ $0.06/h)           | $0–19            |
| Dev ergonomics     | Poor for relational + reporting | Good (SQL), some quirks                                             | Best (plain Postgres)                             | Best + branching |
| Quarkus/Kotlin fit | Enhanced client OK; no Panache  | JDBC + Hibernate via DSQL connector; spike needed                   | Native Hibernate/Panache/Flyway                   | Native           |
| Lock-in            | High                            | Medium (Postgres-ish SQL portable)                                  | Low                                               | Low              |
| eu-north-1         | ✅                              | ✅                                                                  | ✅                                                | ❌ (Frankfurt)   |
| Cold path          | none                            | none                                                                | ~15 s resume                                      | ~0.5–1 s         |
| Tenant isolation   | key prefix + IAM LeadingKeys    | `tenant_id` column enforced in app (Hibernate filter / `@TenantId`) | Postgres RLS possible                             | RLS possible     |

### Recommendation: Aurora DSQL

It is the only option that is simultaneously SQL/relational, scale-to-zero with no resume penalty, no VPC/NAT, and in Stockholm. Use shared schema with a `tenant_id` column on every table, Hibernate 6 `@TenantId` discriminator multitenancy (or a mandatory filter) plus tests asserting cross-tenant reads fail. UUIDv7 primary keys. Encrypt personnummer at the application layer (KMS data key / envelope encryption, store ciphertext + a keyed HMAC for lookup), since DSQL has no pgcrypto. Reporting: plain SQL for in-app reports; export to S3 later if needed. First task: a 1-day spike proving Quarkus + Hibernate ORM + Flyway (or Liquibase) + DSQL JDBC connector on Lambda, including retry on serialization (OC000/40001) errors.

Revisit if: the spike shows Hibernate/Flyway friction that costs more than a day a month; DPU costs per tenant exceed ~$1/month; we need RLS, triggers or extensions. ~~Fallback is Aurora Serverless v2 (accepting ~$40+/month) or Neon.~~ **Superseded by DECISIONS.md (2026-10-29, Database):** the fallback is DynamoDB.

## 2. Quarkus on Lambda runtime

### Options

- **JVM (Java 21/25 managed runtime) + SnapStart** — SnapStart supports arm64 since July 2024 ([aws-news](https://aws-news.com/article/2024-07-18-aws-lambda-now-supports-snapstart-for-java-functions-that-use-the-arm64-architecture)); regional availability is listed in the [SnapStart docs](https://docs.aws.amazon.com/lambda/latest/dg/snapstart.html) ⚠️ confirm eu-north-1 in the console (expected, not verified here). Typical Quarkus restore ~300–700 ms cold (vs 2–4 s without), warm latency fine. SnapStart is free for Java (caching/restore charges apply only to Python/.NET). Standard `mvn/gradle package` → zip; simple CI. Caveats: needs published versions/aliases; DB connections and random seeds must be re-established after restore (Quarkus has SnapStart hooks; use a priming hook).
- **Native (Mandrel/GraalVM, arm64, `provided.al2023`)** — cold start ~150–300 ms, lower memory (128–256 MB OK), cheapest per invocation. Build: 3–8 min, 6–8 GB RAM, must build on arm64 (GitHub `ubuntu-24.04-arm` runners or container build). Reflection config for Kotlin, Jackson, Hibernate and the AWS SDK occasionally breaks; failures appear only in the native build — costly debugging for AI agents.

|                     | JVM + SnapStart       | Native (Mandrel arm64)                  |
|---------------------|-----------------------|-----------------------------------------|
| Cold start          | ~0.3–0.7 s ⚠️ measure | ~0.15–0.3 s                             |
| Build time / CI     | ~1 min, any runner    | 3–8 min, arm64 runner, high RAM         |
| Kotlin/library fit  | Everything works      | Reflection/registration issues possible |
| Idle cost           | $0                    | $0                                      |
| Cost at small scale | Free tier covers it   | Free tier covers it                     |

Recommendation: **JVM + SnapStart on arm64**, 1024 MB to start. Keep the code native-compatible (Quarkus extensions only) so native is a switch, not a rewrite.

**One "lambdalith" vs many functions:** one REST function using `quarkus-amazon-lambda-http` (JAX-RS/RESTEasy Reactive routes, normal Quarkus dev mode locally). Fewer cold starts, one SnapStart snapshot, simplest deploys. Separate functions only for different trigger types (scheduled jobs via EventBridge Scheduler, SQS consumers) — they can share the same artifact with a different handler.

**API Gateway HTTP API vs Function URL:** HTTP API ($1.00–1.11/million requests, JWT authorizer validates Cognito tokens before Lambda runs, custom domain, throttling) vs Function URL (free, but auth is IAM or none, no custom domain without CloudFront). Recommend **HTTP API** with JWT authorizer; cost at this scale is cents. Alternative: CloudFront → Function URL with OAC, if we want one domain for app + API (revisit later).

Revisit if: p95 cold start > 1 s is user-visible, or Lambda cost grows enough that native's memory saving matters.

## 3. Auth

- **Cognito** — Lite and Essentials include 10,000 MAU free per month; Essentials is $0.015/MAU beyond that ([Cognito pricing](https://aws.amazon.com/cognito/pricing/), [costgoat](https://costgoat.com/pricing/amazon-cognito)). Idle $0. Essentials adds passkeys, email OTP, managed login. Lock-in: user pool migration is painful (password hashes not exportable). Available in eu-north-1 ✅. SMS MFA costs extra; prefer email OTP/passkeys/TOTP.
- **Alternatives** — Auth0/Clerk (free tiers ~7.5k–10k MAU, US-centric, extra vendor), Keycloak (needs a server → breaks idle budget), Supabase/Neon auth (only if DB moves there).
- **BankID** — Swedish users expect it, especially owners/admins. Direct integration requires an agreement with a bank, a relying-party certificate and certification; agencies quote SEK 80k–250k ([weapp.se](https://weapp.se/en/integrations/what-does-a-bankid-integration-cost)). Brokers (Idura/Criipto, Signicat) expose BankID as OIDC: Idura lists ~€0.013 per authentication plus a monthly fee ⚠️ ([Idura pricing](https://idura.eu/pricing/verify)). Cognito can federate an OIDC IdP, so BankID can be added later as a login option without changing the backend. BankID is also the natural path for RUT-related identity verification and signing, but not required for the MVP.

Recommendation: **Cognito Essentials** (email + passkey / email OTP), one user pool, `tenant_id` and role in custom claims (pre-token-generation Lambda) or looked up server-side. Add BankID via Idura/Criipto OIDC federation when customers ask; budget ~€0.01–0.02 per login + monthly fee. Revisit if MAU > 10k or federation/UX limits of managed login hurt.

## 4. Front-end

- Hosting: **S3 (private) + CloudFront with OAC**, ACM certificate in us-east-1 (free). CloudFront free tier (1 TB, 10M requests/month) makes this ~$0 idle.
- Framework: keep simple — **Vite + a mainstream SPA framework** (React or Svelte; pick the one the agents produce most reliably — React is the safe default) as an installable PWA (`vite-plugin-pwa`/Workbox). No SSR (would need compute).
- **MVP offline scope (DECISIONS.md, Personal data inventory):** the MVP caches only static app assets. Offline storage of personal data (addresses, access codes, notes) on devices is out of MVP until data categories, protection and deletion rules are decided. The text below describes a later option, not MVP.
- Offline (later option): field staff need today's job list, addresses, notes and the ability to log time/check off jobs without coverage. MVP: cache the app shell + today's/this week's assignments in IndexedDB; queue mutations (time reports, status changes) in an outbox and sync with idempotency keys; last-write-wins with server-side conflict flag. Avoid full offline-first sync engines until needed. Note iOS PWA limits (background sync unsupported, storage may be evicted) — sync on app open/foreground.

Revisit if: native features (background location, reliable push on iOS, camera-heavy flows) become core → consider Capacitor wrapper.

## 5. IaC and CI

|                                              | AWS CDK (TypeScript)                               | AWS CDK (Kotlin/Java)                               | SAM                                                   | Terraform/OpenTofu           |
|----------------------------------------------|----------------------------------------------------|-----------------------------------------------------|-------------------------------------------------------|------------------------------|
| Fit for Lambda + DSQL + Cognito + CloudFront | Good, L2 constructs                                | Same constructs, verbose, slower synth (JVM + jsii) | Good for functions/API; weaker for CloudFront/Cognito | Good, broad                  |
| AI-agent ergonomics                          | Best (most examples, fast synth, `cdk diff`)       | OK                                                  | OK (YAML)                                             | Good (large corpus)          |
| State                                        | CloudFormation (no state to manage)                | same                                                | same                                                  | State backend (S3) to manage |
| New-service lag (e.g. DSQL)                  | L1 available quickly ⚠️ check `AWS::DSQL::Cluster` | same                                                | via CFN                                               | provider usually quick       |
| Local testing                                | —                                                  | —                                                   | `sam local`                                           | —                            |

**Superseded by DECISIONS.md (2026-10-29, IaC language): CDK is written in Kotlin.** Original recommendation: AWS CDK in TypeScript in an `infra/` folder. TypeScript CDK is the best-documented and fastest-iterating flavour; using Kotlin for CDK buys language consistency but adds JVM/jsii overhead and fewer examples. Terraform is fine but adds state management for no gain on an AWS-only stack. Quarkus dev mode (not SAM local) for local backend work.

CI: **GitHub Actions with OIDC** (`aws-actions/configure-aws-credentials`, IAM role trusted for `repo:<org>/micro-fsm:ref:refs/heads/main` and environments). Pipeline: build + test → `cdk diff` on PR → `cdk deploy` dev on merge to main; staging/prod via GitHub environments with approval. No long-lived keys.

## 6. Hidden fixed costs

| Item                                               | Cost                                          | Notes                                                                                                                          |
|----------------------------------------------------|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| Route 53 hosted zone                               | $0.50/month                                   | + domain (~$10–15/year for .se via registrar; .se not sold by Route 53 ⚠️ — register elsewhere, delegate NS)                   |
| KMS customer-managed key                           | $1.00/month per key                           | One CMK for personnummer envelope encryption. Use AWS-managed keys elsewhere (free). Requests $0.03/10k; cache data keys.      |
| Secrets Manager                                    | $0.40/secret/month                            | Avoid: DSQL uses IAM tokens (no DB password). Use **SSM Parameter Store standard SecureString** (free) for broker/API secrets. |
| CloudWatch Logs                                    | $0.50/GB ingest (+ storage)                   | Default retention is forever — set 14–30 days in CDK; log JSON at INFO.                                                        |
| NAT gateway                                        | ~$35/month + data                             | **Avoid**: keep Lambda out of VPC (possible because DSQL is public+IAM).                                                       |
| VPC interface endpoints                            | ~$7–8/month each/AZ                           | Avoid for same reason.                                                                                                         |
| WAF on CloudFront/API                              | $5/month per web ACL + rules                  | Skip at MVP; use API throttling.                                                                                               |
| CloudWatch alarms / dashboards                     | $0.10/alarm; first 10 free; dashboards 3 free | Keep within free.                                                                                                              |
| Cognito, Lambda, API GW, S3, CloudFront, DSQL, SES | ~$0 idle (free tiers)                         | SES needs domain verification; sandbox exit is free.                                                                           |
| AWS Budgets                                        | first 2 free                                  | Set a $5 budget alert.                                                                                                         |
| Public IPv4                                        | $0.005/h each                                 | Not used in this design.                                                                                                       |
| CDK bootstrap bucket/ECR                           | cents                                         | Lifecycle rule on assets.                                                                                                      |

Estimated idle bill (recommended stack, one environment): Route 53 $0.50 + KMS $1.00 + S3/logs/CloudWatch ~$0.10–0.30 + DSQL/Lambda/API/Cognito/CloudFront $0 ≈ **$1.60–1.80/month**. A second environment (dev + prod) adds ~$1 (another KMS key) unless dev shares the CMK or uses an AWS-managed key — recommended: separate accounts later, but share one hosted zone. ⚠️ eu-north-1 prices may differ slightly.

## 7. Recommended stack

| Layer          | Choice                                                                                               | Idle cost            |
|----------------|------------------------------------------------------------------------------------------------------|----------------------|
| Region         | eu-north-1 (CloudFront/ACM global, cert in us-east-1)                                                | —                    |
| Database       | Aurora DSQL, shared schema, `tenant_id` everywhere, UUIDv7 keys                                      | ~$0                  |
| Data access    | Quarkus Hibernate ORM (Panache Kotlin) + DSQL JDBC connector; Flyway/Liquibase (spike)               | —                    |
| Backend        | Kotlin + Quarkus, one lambdalith via `quarkus-amazon-lambda-http`, Java 21+ JVM, arm64, SnapStart    | $0                   |
| Background     | Same artifact, extra handlers via EventBridge Scheduler / SQS                                        | $0                   |
| API            | API Gateway HTTP API + Cognito JWT authorizer                                                        | $0                   |
| Auth           | Cognito Essentials (email OTP / passkeys); BankID later via Idura/Criipto OIDC                       | $0                   |
| Encryption     | KMS CMK, app-level envelope encryption for personnummer + HMAC lookup                                | $1.00                |
| Secrets/config | SSM Parameter Store (standard)                                                                       | $0                   |
| Front-end      | Vite + React PWA, S3 + CloudFront (OAC), static asset caching only in MVP (no offline personal data) | ~$0                  |
| Email          | SES (eu-north-1)                                                                                     | $0                   |
| DNS            | Route 53 hosted zone, .se registered externally                                                      | $0.50                |
| Observability  | CloudWatch Logs (14–30 d retention), a few free alarms, AWS Budgets alert                            | ~$0.10–0.30          |
| IaC            | AWS CDK (Kotlin; supersedes TypeScript, see DECISIONS.md)                                            | —                    |
| CI/CD          | GitHub Actions + OIDC role, cdk diff on PR, deploy on merge                                          | $0                   |
| **Total idle** |                                                                                                      | **≈ $1.6–1.8/month** |

## Key uncertainties to resolve early

1. Quarkus Hibernate + Flyway + DSQL compatibility (spike, 1 day).
2. Real DPU consumption per typical request → per-tenant cost.
3. SnapStart availability in eu-north-1 and measured Quarkus restore time.
4. DSQL current feature list (FK enforcement, RLS, transaction limits) in eu-north-1.
5. BankID broker monthly fees and contract terms.
