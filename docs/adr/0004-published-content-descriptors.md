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
- **Filters and relations (DF8).** A descriptor declares its filters by name:
  `ContentFilter` is two fixed conditions, by id and by slug (old slugs included, through
  `published.content_slug`). An embedded list is the same filter run without paging
  (`listAll`, by name): the type's `devilFruits` is the fruit view filtered by `type`. A
  summary of a single related content (the fruit's `type`) is plain columns of the view,
  joined in the database.

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

## Addendum - story structure (plan SS6)

- Saga, Arc, Volume, Chapter and Episode are five more descriptors (`naturalKey("number")`,
  slug = number) on the same repository and template; `sort=number` (default for them) maps
  to the natural key as `romaji` does.
- **Derived values** come from the span views of content-service (ADR-0003 addendum) as
  `chapters` / `episodes` spans on the detail: counts and ranges, never the list of children,
  which are reached through the `arc=` / `saga=` / `volume=` filters.
- **Embedded lists** keep using `listAll`, now ordered by the descriptor's `embeddedOrder`
  (number for arcs); a filter declared only for embedding (`volume` on arcs: arcs of the
  chapters online of a volume) is not exposed as a query parameter.
- **Template hook** `complete(detail)` on `PublishedContentService`: what a detail adds from
  other views (embedded arcs, release days per country read by `PublishedReleaseRepository`).
  `DevilFruitTypeService` keeps its own override.
- The cover of a volume is served by the same `v1/images/{id}`.

## Addendum - Bounty (Bounty plan B3)

- **Sorts in the descriptor**, as foreseen above: `PublishedView.sorts` maps a sort field to
  the columns it orders by on that entity. A field is either common (`name`, `romaji`,
  `number`, `publishedAt`) or available only where declared (`amount`); the service refuses
  an undeclared one with `400 VALIDATION_FAILED` before reading. The bounty orders `name` by
  `poster_name, amount` (the key text compared as text would put 100M before 30M), `amount`
  by `amount`, default `amount,desc`.
- **Value filters:** `ContentFilter.onValue(column)` filters by one value of a closed set
  (`validity`, `condition`); the controller reads it as an enum, so an unknown value is a
  `400` and only enum names reach the SQL.
- **Chain:** `PublishedBountyChainRepository` reads the chain in one `WITH RECURSIVE` query on
  `published.bounty`, walking `previous_id` both ways within the language, ordered by position
  (depth), not by amount; `BountyService.complete` adds it to the detail.
- The poster is served by the same `v1/images/{id}`.

## Addendum - Character (Character plan C2)

- **Child lists:** the lists inside a character version (epithets, aliases, species) come from
  child views of content-service, one row per entry with its `position`.
  `PublishedCharacterListRepository` reads them one query per list, for the detail only
  (`CharacterService.complete`); the list rows read none.
- **Child filters:** `ContentFilter.onChild(childView, ownerColumn, column, entityType)` keeps
  the contents whose child view names another content, by id or by any slug it has had
  (`species` on characters). It will serve `affiliation` and `devilFruit` too (C8, C10).
- An entry is in a language only when it is written in it: an epithet, or a species named
  through `published.species`, without text in that language is left out of it, as a
  subcategory.

## Addendum - Character relations (Character plan C8-C12)

- **Child filters count current entries only:** `affiliation` and `devilFruit` use `ContentFilter.onChild` with a
  condition on `former = false`; `species` has no such flag. A former member or user is not listed under the filter.
- **Derived sides are queries over published views, never stored:** an affiliation's `memberCount`, a fruit's
  `users` (current first) and a bounty holder's `bounties` (highest amount first) read the online characters or
  bounties that name the entity.
- **Relatives are stored once, read from both sides:** `relatives` of a character is the union of the entries it
  holds and, with the kind inverted (`GRANDPARENT` -> `GRANDCHILD`), those of online characters naming it. Each
  entry carries the relative's `gender`, so a client words the kind (`PARENT` + `MALE` = father). The invariant
  that a pair is linked once is content-service's (ADR-0009 there); the public API only reads it.
- **Trade-off:** the union and the inversion live in the public API's read model rather than in a view, so the
  `published` views stay one row per stored fact.
