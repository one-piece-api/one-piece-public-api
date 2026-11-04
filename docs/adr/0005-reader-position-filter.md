# ADR-0005: The reader-position filter (`asOfChapter` / `asOfEpisode`)

Extends ADR-0001 (read service), ADR-0002 (`v1` contract), ADR-0004 (descriptors). Product
rules: workflow revision 17, 3.7 and 3.8; the library is `one-piece-reader-time`.

## Context

A reader who has read up to chapter N (or watched up to episode N) must not be told what
comes later: a character not met yet, a name, a type or a status learned afterwards, a
list entry or paragraph that ends or starts later. The same rules decide the preview in
content-service (its ADR-0013), so both must give the same answer.

## Decision

- **Parameters.** `asOfChapter=N` or `asOfEpisode=N` on every resource, N inclusive; none =
  the full truth, unchanged; both = `400 VALIDATION_FAILED`.
- **Two paths.** Without the filter: the SQL of the view pages, searches, filters and sorts
  (ADR-0004), unchanged. With it: the rows of the section are read once, each is resolved
  at N with the library, and the rest is done in memory. `ReaderSight` holds, per request,
  where the reader meets each content and what is known at N of every revealable field
  (Character, Devil Fruit, Bounty): one read each, so names, slugs, types and validity
  are the same in rows, details and links.
- **Hidden means absent.** A content not met is left out of lists, search and counts and
  its detail is `404` (a `403` would confirm it exists); a link to it is `null` or
  dropped. Entries, points, paragraphs, gallery and reveals are trimmed to N, on the
  reader's axis only.
- **Slugs at N.** The canonical slug is the one known at N; another slug the reader may
  know by then redirects there (`301`, query kept); a slug of the future is `404`.
- **List filters, two families.** The filter value (id or slug) is first resolved to an id
  through `ReaderSight`: a content not met, a slug not known at N or no such content gives
  an empty page. A *fixed* filter (on a value that never changes: the arc of a chapter,
  the holder of a bounty) stays SQL with that id. A filter on a value learned by degrees
  (fruit `type`, character `status`, `species`, `affiliation`, `affiliationKind`,
  `devilFruit`, bounty `validity`) is a test in memory on the row at N, declared in the
  descriptor next to the SQL one (`ContentFilter.atN`). The lists of the characters it
  tests are read once for the request (`CharacterEntries`) and seen with the same rules as
  the detail, so a filter and the detail cannot disagree.
- **Counts and embedded lists at N.** `memberCount` counts the characters met that belong
  to the affiliation at N, the same set the `affiliation` filter lists; a Devil Fruit
  Type lists the fruits whose type at N is that type; fruit users and inverse relatives
  are ordered by name at N.
- **`q` and sort.** SQL as today for entities whose name is fixed; for Character and Devil
  Fruit (name and romaji revealed by degrees) in memory on the values at N, with
  `java.text.Collator` of the path language, then id. `sort=publishedAt` keeps the
  database order.
- **Not filtered:** images (`/v1/images/{id}`): UUIDs that no filtered response lists.

## Alternatives considered

- **SQL for everything, with views at N:** the values at N come from reveal tables and
  windows with chapter and episode axes; one SQL path per axis and entity would duplicate
  the library's rules and drift from the preview.
- **Filter the unfiltered response afterwards (`403` or masked fields):** leaks existence
  and counts.
- **Per-row detail reads:** N+1 queries; the filtered path reads each section once.
- **Postgres collations for the sort at N:** the value to sort is not in the database.

## Consequences

- A filtered list reads the whole section of the entity: fine for a catalogue of
  hundreds, but measured costly (P5, 2026-11-04): about 35 ms of CPU per request at 500
  rows, about 8 rps per 0.5 CPU replica, detail included (README, "Prestazioni"). Accepted
  while there is no client; the way out is caching the resolved section or resolving in SQL.
- The order by name at N can differ from Postgres on spaces and punctuation: a known,
  accepted limit.
- Every new revealable field, entity or list filter must say which family it is in; a
  test compares the filtered path with the full truth at the highest N.
- The query string is part of the URL, so caches and ETags (ADR-0003) treat each N as its
  own resource.
