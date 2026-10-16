# one-piece-public-api

Anonymous, read-only HTTP API (`v1`) over the `published` views of content-service's database (role `public_api_reader`). No JPA, no Flyway, no Spring Security: `JdbcClient` + SQL. Setup, Bruno, performance baseline: `README.md`. Plan: `../docs/implementation-plan-public-api.md`.

## Commands

```bash
./gradlew check > "$TMP/check.log" 2>&1; echo "exit=$?"   # tests (Testcontainers) + format + Checkstyle
./gradlew updateOpenApiSpec && ./scripts/generate-bruno-collection.sh
./scripts/deploy-local.sh
```

Port 8083, actuator 8093. In the cluster only through Envoy on host `api.localhost` (port-forward 8090, see README).

## Map (`dev.onepieceapi.publicapi`)

| Package | Holds |
|---|---|
| `persistence` | `PublishedViews` (one descriptor per view, natural key declared there), repositories over `JdbcClient` |
| `persistence/{row,mapper}` | Row records as read from SQL, row → domain mappers |
| `domain` | Public domain records (`<X>`, `<X>Summary`, references) |
| `service` | One service per resource + generic `PublishedContentService` |
| `web/{controller,dto,mapper,filter,converter}` | Controllers per resource, `ApiPaths`, response DTOs, `PublicResponseMapper`, caching filters |

## Key ideas (ADRs in `docs/adr/`)

- 0001 separate read service on the published views; 0002 `v1` contract, oasdiff fails CI on breaking changes; 0003 HTTP caching, ETag and `304`; 0004 one descriptor per published content type.

## Gotchas

- The tests build the real schema from `one-piece-content-service-migrations:<n>` (`build.gradle.kts`): new views need that artifact published by content-service first, then the version bump here.
- Gatling runs by hand on kind, never in CI.
