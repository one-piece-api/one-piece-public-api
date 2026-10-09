# one-piece-public-api

API HTTP pubblica, anonima e in sola lettura, sui contenuti **pubblicati** del workflow
editoriale (Devil Fruit Type, Devil Fruit e le loro immagini). Legge le viste dello schema `published` del
database di `one-piece-content-service` con il ruolo di sola lettura `public_api_reader`.

- **Piano di implementazione:** `docs/implementation-plan-public-api.md` (repo `one-piece-api`).
- **Decisioni architetturali di questo servizio:** `docs/adr/`.

Stato: API di lettura `v1` (step P3) con cache HTTP e richieste condizionali (P4), baseline di prestazioni (P5); Devil Fruit, filtro per tipo e immagini `v1/images/{id}.png` immutabili (Devil Fruit plan, DF8). Contratto: `openapi/openapi.yaml`, collezione Bruno in `bruno/`.

## Sviluppo locale

Prerequisito: credenziali per GitHub Packages (da cui arrivano `one-piece-exception` e, per i
test, le migrazioni di content-service), cioè un
[personal access token classic](https://github.com/settings/tokens) con il solo scope
`read:packages`, in `~/.gradle/gradle.properties`:

```properties
gpr.user=<utente GitHub>
gpr.token=<token>
```

Poi il cluster `kind` locale attivo (`./scripts/setup.sh` nel repo `onepiece-infrastructure`)
e il port-forward del database:

```bash
kubectl port-forward svc/one-piece-postgresql -n data 5433:5432
```

**Da IntelliJ IDEA:** esegui `PublicApiApplication`; il profilo `local` è quello di default.
Il servizio risponde su `http://localhost:8083/`, l'actuator su
`http://localhost:8093/actuator/health`. Da riga di comando: `./gradlew bootRun`.

**Test e formattazione** (i test avviano PostgreSQL con Testcontainers: serve Docker):

```bash
./gradlew check            # test + formattazione (spring-javaformat) + Checkstyle
./gradlew format           # applica la formattazione
```

**Verifica nel cluster `kind`:** `scripts/deploy-local.sh` (build immagine + `kind load` +
rollout restart del Deployment). Nel cluster il servizio è raggiungibile solo dall'interno:

```bash
kubectl port-forward svc/one-piece-public-api -n app 8083:80
kubectl port-forward deploy/one-piece-public-api -n app 8093:8081   # actuator
```

## Contratto API

Dopo una modifica all'API, in quest'ordine, poi si committano entrambi i risultati:

```bash
./gradlew updateOpenApiSpec               # rigenera openapi/openapi.yaml
./scripts/generate-bruno-collection.sh    # rigenera bruno/ (serve Node.js)
```

La CI fallisce se uno dei due è disallineato, e su una PR se la spec introduce una modifica
incompatibile rispetto a `main` (ADR-0002).

## Prestazioni

Scenari [Gatling](https://docs.gatling.io/) in `src/gatling/` (piano, D16), eseguiti a mano sul
cluster `kind` locale, mai in CI. Baseline e spike partono con 30 s di riscaldamento, esclusi
dalle misure e dalle asserzioni.

| Simulazione | Cosa misura | Default |
|---|---|---|
| `BaselineSimulation` | mix realistico: 70% dettaglio, 20% lista, 10% ricerca; metà rivalidate (`304`) | 7 utenti/s, circa 60 rps, 60 s |
| `SpikeSimulation` | carico ×5 per 30 s e ritorno, senza errori | 2 utenti/s, circa 17 → 85 rps |
| `HostileSimulation` | `q` alla lunghezza massima, pagine profonde e da 100, ordinamenti costosi | 5 utenti/s |

```bash
./scripts/perf-seed.sh                    # 5.000 contenuti sintetici in it/en (idempotente)
kubectl port-forward svc/one-piece-public-api -n app 8084:80
./gradlew gatlingRun --simulation=dev.onepieceapi.publicapi.perf.BaselineSimulation
```

Opzioni: `-Drate=<utenti/s>` (un utente fa circa 8,5 richieste), `-Dseconds=<durata>`,
`-DbaseUrl=<url>`. Report HTML in `build/reports/gatling/`. Va rieseguita quando cambiano
query o indici: le asserzioni falliscono se un p95 supera la soglia.

**Baseline del 2026-10-06** (1 replica, 5.000 contenuti, p95 misurato sull'origine):

| Richiesta | p95 | Soglia | Obiettivo D16 |
|---|---|---|---|
| dettaglio | 6–7 ms | 15 ms | 50 ms |
| `304` | 5–7 ms | 15 ms | 20 ms |
| lista | 14–16 ms | 35 ms | 100 ms |
| ricerca (`q`) | 34–55 ms | 110 ms | 150 ms |
| richieste ostili | 22–52 ms | 150 ms | — |

**Capacità:** circa 60–65 rps per replica, oltre la latenza esplode. Il limite è il PostgreSQL
condiviso (0,5 CPU, saturo e throttlato), non il servizio: le liste fanno pagina e conteggio
sulla join delle tre tabelle dietro la vista.
