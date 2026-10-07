# ADR-0004: One generic read path, one descriptor per entity

## Context

The service read a single entity, the Devil Fruit Type, with a repository, a service, a
controller, a sort converter, a request and a search mapper all named after it. The
content framework (content-service ADR-0004, plan DF0) adds entities one after another:
copying that stack for each would repeat the same rules - language check, id or slug, old
slug to `301`, sort, paging, conditional GET - and let the copies drift apart.

## Decision

- **Descriptor, data only.** `PublishedView<D, S>` names what is specific to an entity: its
  `entity_type` in `published.content_slug`, its view, its detail and list columns and the
  two row readers. One constant per entity in `PublishedViews`.
- **One repository bean** (`PublishedContentRepository`) takes the descriptor as an
  argument. Its SQL is composed only from the fixed fragments of the descriptor and of the
  class; every caller value stays a bind parameter (plan D3).
- **One service with the rules** (`PublishedContentService<D, S>`, Template Method): a thin
  subclass per entity binds the types and the descriptor, so a controller never sees
  persistence.
- **Domain generics:** `ContentSort` (fields common to every entity: `name`, `romaji`,
  `publishedAt`, plan D11), `ContentSearch`, `ContentLookup<T>` (`Found` / `Moved`),
  `PublishedContent` (`id`, `slug`).
- **Web:** `ContentListRequest`, `ContentSortConverter` and `ContentSearchMapper` are
  shared; `ContentResponses` answers the conditional GET and turns a lookup into `200` or
  `301`. The **controller stays concrete per entity**: its paths, descriptions and
  `operationId`s are the contract, and each endpoint delegates in one line.
- **Scope:** the Devil Fruit Type moved onto it with the contract unchanged (byte-identical
  `openapi.yaml`, same bodies and headers). Filters by relation and embedded relations
  come with the Devil Fruit (DF8), the first entity that needs them.

## Alternatives considered

- **A generic controller** (`/v1/{lang}/{entity}`): one class for all, but descriptions
  and schemas of each entity would be lost or hidden in an OpenAPI customizer, and the
  routes would stop being explicit.
- **An interface per entity repository** (Spring Data style): the SQL of each would still
  be written and tested by hand, only the shell shared.
- **Descriptor with behaviour** (strategies for sort or filters): nothing varies yet, and
  the first real difference will show what the right seam is.

## Consequences

- A new entity costs a descriptor, two rows, a domain record, a thin service and a
  controller; the rules and their tests are written once.
- The generic repository is covered end to end by the integration tests of each entity's
  view, not by a test-only view.
- A sort field not common to every entity will need the sort to become part of the
  descriptor.
