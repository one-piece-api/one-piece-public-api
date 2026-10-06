#!/usr/bin/env bash
# Loads the synthetic dataset of the performance baseline (perf/seed.sql) into the local
# cluster's content_service database. Usage: scripts/perf-seed.sh [count]

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COUNT="${1:-5000}"

# The owner role writes the internal tables; the local placeholder password is the one in
# the content-service's application-local.properties.
kubectl exec -i -n data one-piece-postgresql-0 -- \
  env PGPASSWORD=content-service-db-change-me-locally \
  psql -U content_service -d content_service -v "count=${COUNT}" <"$REPO_ROOT/perf/seed.sql"
