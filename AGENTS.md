# micro-fsm

Field Service Management Micro-SaaS for small Swedish (Nordic) cleaning and moving companies. Built by the software factory (`~/dev/software-factory`); follow its `AGENTS.md` and `policies/`.

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
  - **Podman** (no Docker): before running tests/dev, `export DOCKER_HOST=unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')` and `export TESTCONTAINERS_RYUK_DISABLED=true`. `podman` is at `/opt/podman/bin`; the machine must be running (`podman machine start`).
- Backend notes: tenant comes from the `X-Tenant-Id` header (spike); migrations are one DDL each (`MigrationLintTest` enforces DSQL rules).
