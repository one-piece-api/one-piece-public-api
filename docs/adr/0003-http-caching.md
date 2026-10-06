# ADR-0003: HTTP caching and the open door to a static API

## Context

Public reads dominate and change rarely (plan D13, D14): a publication is an editorial
event. The load balancer is 10 Mbps, so what a CDN or a browser can answer without reaching
the origin matters more than the speed of one request.

## Decision

- **Cache-Control by status**, from configuration (`public-api.cache.*`): `200` and `304`
  `max-age=60, public, stale-while-revalidate=60` (a publication shows within 2 minutes at
  most); `404` 30 s; `301` 1 h, explicit because browsers keep a bare `301` for ever and a
  romaji changed back would loop; every other status `no-store`. Set by one filter on every
  `GET`/`HEAD`, errors included, once the status is final (the body is held back until
  then: at most one page of 100 rows).
- **ETag = the published revision**, weak (`W/"<n>"`): one global counter kept by content-
  service's triggers (its ADR-0003), so every path that changes what is online - and every
  future entity - moves it. One counter, not one per resource: a list depends on many rows,
  and publications are rare.
- **`304` without reading the data:** the revision is read first and compared with
  `If-None-Match` by the framework (`WebRequest.checkNotModified`); the data query runs only
  when the caller is not current. Revision and data are read in **one `REPEATABLE READ`
  transaction**, so the ETag always describes the data sent with it
  (`PublishedSnapshotService`, a Template Method; `ConditionalGet` on the web side).
- **No cache inside the service, none at the gateway** in phase 1: only if the load test
  (P5) asks.
- **Static door kept open, nothing built** (as ADR-0001): a response depends only on its
  URL, JSON is built by reusable mappers, no field relative to now. Next steps, one at a
  time: a transactional outbox in content-service with a worker that purges the CDN on
  publication (delay ~0, longer TTLs); then the same worker writes the JSON files to Object
  Storage.

## Alternatives considered

- **`ShallowEtagHeaderFilter`**: computes the ETag from the body, so every request still
  pays the queries - no `304` saving.
- **ETag per resource** (a hash or `updated_at`): a list would need all its rows' stamps.
- **Strong ETag**: the same revision is served in several encodings (gzip), byte-for-byte
  different.
- **`Cache-Control` set by each controller**: errors raised by the shared exception library
  would escape it.

## Consequences

- Every publication invalidates every ETag once; a revalidation then costs one data query.
- A `404` carries the ETag too (the framework sets it on any conditional read): revalidating
  it with the same revision gives the same `404`'s state, never a wrong answer.
- `If-None-Match: *` is not treated as a match on `GET` (the framework's choice): harmless,
  no browser or CDN sends it for a read.
- The 2-minute worst case is the price of no purge; the outbox step removes it.
