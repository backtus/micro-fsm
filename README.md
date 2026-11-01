# micro-fsm

Field service management for small Nordic cleaning and moving companies: week planning for the owner, a "My day" view for field staff, and an invoice basis with the Swedish RUT deduction split. Serverless on AWS, designed to cost about $0 when idle.

> **Status: shelved (2026-10-29), published as open source on 2026-11-01.** The project was explored as a micro-SaaS and then put on hold; see [`docs/STATE.md`](docs/STATE.md) and [`docs/DECISIONS.md`](docs/DECISIONS.md) for why. It is a prototype and a reference, not a product. No support or maintenance is promised, and nothing here is tax or legal advice.

It was built with an agent-driven "software factory" workflow (planner, builder and reviewer agents working through pull requests), so the docs and ADRs show the reasoning behind every choice.

## What is here

| Path                    | What                                                                                                                                                                                                                           |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `web/`                  | Clickable Swedish prototype (React, Vite, plain CSS Modules). Owner week view, field worker "Min dag", RUT invoice basis. All data is invented; there is no backend and no real personnummer.                                  |
| `backend/`              | Quarkus + Kotlin skeleton for AWS Lambda: tenant-scoped job slice, OCC retry on SQLSTATE 40001, and an in-house `Migrator` that works with Aurora DSQL (no advisory locks, one DDL per transaction, lint test for DSQL rules). |
| `docs/research/`        | Sources-backed research: Swedish RUT rules, Skatteverket XML and Fortnox/Spiris integrations, AWS architecture options and costs, Norwegian regulation and competitors.                                                        |
| `docs/product/brief.md` | Product brief and MVP scope. The competitor section is a dated snapshot, see the caveat in the file.                                                                                                                           |
| `docs/DECISIONS.md`     | ADRs: Aurora DSQL, Lambda with SnapStart, Cognito, encryption of personal data, why no Flyway, and so on.                                                                                                                      |

## Try it

Demo (Node 26):

```
npm --prefix web ci
npm --prefix web run dev      # http://localhost:5173
```

Backend (JDK 17+ can run the wrapper; Gradle provisions JDK 25). Tests need a container runtime because Quarkus Dev Services starts Postgres:

```
./gradlew build
./gradlew :backend:quarkusDev
```

More commands, Podman notes and backend conventions are in [`AGENTS.md`](AGENTS.md).

## Caveats

- The RUT calculation is provisional (rounding to whole kronor is not yet checked against Skatteverket's `Begaran.xsd`).
- Competitor and market information is a snapshot from October 2026 and may be outdated or wrong.
- The backend is a spike: tenant comes from an `X-Tenant-Id` header and there is no authentication.

## License

[MIT](LICENSE).
