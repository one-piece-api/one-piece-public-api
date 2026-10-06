# ADR-0001: A dedicated service for the public read API

## Context

The published content of the editorial workflow must be readable by anyone, without an
account, through a stable versioned contract (`docs/implementation-plan-public-api.md` in
`one-piece-api`, D1, D2, D14). `one-piece-content-service` already serves the editorial UI:
every request authenticated, answers that depend on the caller's permissions, a contract that
changes with the UI.

## Decision

- **A service of its own**, `one-piece-public-api`: read-only, published content only, the
  same answer for everyone. content-service keeps every editorial read and write.
- **Reads the `published` views of `content_service`** as the role `public_api_reader`
  (content-service ADR-0003, infrastructure ADR-0018): no copy of the data, no call to
  content-service, nothing but what is online visible. The connection is read-only and the
  pool small (5): two pods during a rolling update fit in the role's limit of 10 connections.
- **Stack** as the other services (Java 25, Spring Boot, Gradle, `one-piece-exception`),
  with three differences:
  - no Spring Security: nothing to authenticate;
  - no JPA: `JdbcClient` and plain SQL, full control of the queries, no persistence context;
  - no Flyway: the schema belongs to content-service. Its migrations, published as an
    artifact, build the same schema in this service's tests only.
- **Actuator on its own port** (8081, health only): the public route reaches port 8080
  alone, so the probes are never exposed. No context path: the gateway strips
  `/api/public` (plan D10).
- **Constraints kept for a static API later** (plan D14):
  1. a response depends only on its URL (the list's query parameters excepted);
  2. JSON is built by reusable mappers and DTOs, never in controllers, so a generator can
     produce the same bytes;
  3. no field relative to the moment of the request.

## Alternatives considered

- **Public endpoints inside content-service**: one process with two contracts and two
  audiences; public load and a public attack surface on the service that writes.
- **All reads in the public service**: it would need authentication and a copy of the
  editorial rules (visibility, allowed actions), losing the isolation.
- **HTTP calls to content-service**: public load lands on it, and its outages become this
  service's.
- **Own database fed by events**: eventual consistency, an outbox and replay to build.
  Kept as the path to a static API (D14), not needed now.

## Consequences

- A compromised public service holds no write path and sees only what is online.
- The views are a contract between two services sharing a database instance: a change to a
  column needs this service's release in step; the tests on the published migrations catch
  a break.
- One more deployment, image and CI pipeline to maintain.
