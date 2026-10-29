# Backlog

Ordered by priority. Move items to STATE.md when started.

## Now
- **Spike: DSQL + Quarkus/Kotlin** — prove Hibernate/Panache, Flyway, DSQL connector/IAM auth and OCC retries work from Lambda in eu-north-1; measure SnapStart arm64 cold start. Go/no-go for the DSQL ADR.
- **Demo for design partner** — a clickable, mobile-first prototype of "Min dag" and the week view with realistic Swedish sample data, usable locally (no AWS needed). Goal: something concrete to show, framed honestly as a prototype.

## Next
- Walking skeleton: job created → field worker's "Min dag" → mark done with hours → invoice basis with RUT split; deployed to dev with budget alarm, CI via GitHub Actions + OIDC.
- AWS account setup (Kent): dedicated account, budget alarm, OIDC deploy role.
- Recurring cleaning jobs; manager week view.
- RUT XML export (Begaran.xsd v6) + 31 Jan reminder; payment tracking (electronic payment required before claim).

## Later / ideas
- Fortnox integration (first post-MVP).
- Move-out cleaning (flyttstädning) specifics; then possibly moving.
- BankID via broker; Spiris; Denmark/Finland invoice breakdown.

## Follow-ups from review
- Backend: automated packaged-JAR resource-loading test for `Migrator` (current tests use exploded resources).
- Backend: document reset of local schemas created by the first migrator version (no `state` column).
