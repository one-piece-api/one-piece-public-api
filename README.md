# one-piece-public-api

API HTTP pubblica, anonima e in sola lettura, sui contenuti **pubblicati** del workflow
editoriale (primo caso: Devil Fruit Type). Legge le viste dello schema `published` del
database di `one-piece-content-service` con il ruolo di sola lettura `public_api_reader`.

- **Piano di implementazione:** `docs/implementation-plan-public-api.md` (repo `one-piece-api`).
- **Decisioni architetturali di questo servizio:** `docs/adr/`.

Stato: scheletro (step P2) — nessun endpoint ancora, solo health; nessuna rotta esterna.

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
