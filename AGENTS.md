# micro-fsm

Field Service Management Micro-SaaS for small Swedish (Nordic) cleaning and moving companies. Built with an agent-driven "software factory" process (planner, builder and reviewer agents working through PRs).

## Context

- Users: small-company managers who often work in the field themselves; their field staff; their end customers (households, for RUT).
- Core value: replace Excel scheduling; automate RUT deductions and accounting integration.
- Constraint: 100% serverless on AWS, ~$0 when idle, region `eu-north-1`.

## State

Read `docs/STATE.md` first. Decisions in `docs/DECISIONS.md`, priorities in `docs/BACKLOG.md`, product definition in `docs/product/`.

## Conventions

- Backend: Kotlin + Quarkus on AWS Lambda. Go allowed for front-end tooling and scripts.
- Product-facing text in Swedish; code, docs and commits in English.
- Web (`web/`, Node 26): `npm --prefix web ci && npm --prefix web test && npm --prefix web run lint && npm --prefix web run build`; `npm --prefix web run dev` runs the demo. Plain CSS + CSS Modules, no UI framework.
- Build/test commands (JDK 25 is auto-provisioned by Gradle toolchains; any JDK ≥ 17 can run the wrapper):
  - `./gradlew build` — compile, test and package all JVM modules; Lambda zip at `backend/build/function.zip`.
  - `./gradlew :backend:test` — needs a container runtime (Quarkus Dev Services starts Postgres).
  - `./gradlew :backend:quarkusDev` — local dev with Dev Services Postgres.
  - Package for Aurora DSQL: `QUARKUS_PROFILE=dsql ./gradlew :backend:quarkusBuild` (the JDBC driver is a build-time setting).
  - **Podman** (no Docker): before running tests/dev, `export DOCKER_HOST=unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')` and `export TESTCONTAINERS_RYUK_DISABLED=true`. `podman` is at `/opt/podman/bin`; the machine must be running (`podman machine start`).
- CI (`.github/workflows/ci.yml`, actions pinned to major versions): runs the same `web` and `./gradlew build` commands as above on PRs and `main`; ubuntu runners have Docker, so no Podman env vars.
- Backend notes: tenant comes from the `X-Tenant-Id` header (spike); migrations are run by `Migrator` (no Flyway, no locks), one DDL each and no DML (`MigrationLintTest` enforces DSQL rules); retries on SQLSTATE 40001 go through `OccRetry` with the transaction inside the retry loop; in dev/test the Lambda HTTP mock serializes HTTP requests, so concurrency tests call the service layer.

## Infra (`infra/`, CDK in Kotlin)

- `./gradlew :infra:test` — CDK assertion tests (no AWS needed; they use a dummy asset). `cd infra && npx cdk synth -c budgetEmail=you@example.com` synthesizes without credentials (needs `backend/build/function.zip`, or `-c lambdaZip=<path>`; CLI tested: aws-cdk 2.1146.1). `budgetEmail` is required.
- Stack `MicroFsmSpikeStack` (eu-north-1): DSQL cluster, `Api` Lambda (java25, arm64, SnapStart, alias `live`) behind an HTTP API, `Migrate` Lambda (same zip, no SnapStart, not behind the API), 10 USD budget. Account comes from the CLI credentials (`CDK_DEFAULT_ACCOUNT`).
- Deploy steps (needs an AWS account; do not run without Kent's go-ahead):
  1. `QUARKUS_PROFILE=dsql ./gradlew :backend:quarkusBuild` (the zip must be the DSQL build).
  2. Once per account/region: `cd infra && npx cdk bootstrap aws://<account>/eu-north-1`.
  3. `cd infra && npx cdk deploy -c budgetEmail=you@example.com` (outputs ApiUrl, MigrateFunctionName, ClusterEndpoint).
  4. Migrate once (and after each release with new migrations), never at init: `aws lambda invoke --region eu-north-1 --function-name <MigrateFunctionName> --cli-binary-format raw-in-base64-out --payload '{"version":"2.0","routeKey":"POST /internal/migrate","rawPath":"/internal/migrate","rawQueryString":"","headers":{},"requestContext":{"http":{"method":"POST","path":"/internal/migrate","sourceIp":"127.0.0.1"}},"isBase64Encoded":false}' out.json && cat out.json` (the JSON response body contains `Applied n migration(s)`). The endpoint is enabled only on that function (`APP_MIGRATE_ENDPOINT_ENABLED=true`); on the public API it returns 404.
  5. Tear down: `cd infra && npx cdk destroy -c budgetEmail=you@example.com` (DSQL deletion protection and log-group retention are off in the spike).

## Repairing a failed migration

`Migrator` records each migration as `STARTED` before running it and `DONE` afterwards (table `schema_migration`). If a statement fails or a run dies mid-way, the row stays `STARTED` (an error does not prove the DDL rolled back) and every later run refuses with "Repair required". To repair (as the migration user, single writer, nothing else migrating):
1. Inspect the schema to see whether the statement of that version took effect.
2. It did: `UPDATE schema_migration SET state = 'DONE' WHERE version = <n>;`. It did not: `DELETE FROM schema_migration WHERE version = <n>;`.
3. Rerun the migrator. Never edit an applied migration file (checksum check) and never add a version lower than the highest applied one.
