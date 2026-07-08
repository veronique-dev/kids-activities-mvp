#!/usr/bin/env bash
# Crée les bases PostgreSQL pour les microservices (si volume Postgres déjà existant).
set -euo pipefail

CONTAINER="${1:-kids-activities-postgres-1}"

for db in auth_db activity_db booking_db payment_db; do
  docker exec "$CONTAINER" psql -U kids -d postgres -tc "SELECT 1 FROM pg_database WHERE datname = '$db'" | grep -q 1 \
    && echo "✓ $db existe déjà" \
    || { docker exec "$CONTAINER" psql -U kids -d postgres -c "CREATE DATABASE $db;"; echo "✓ $db créée"; }
done

echo ""
echo "Redémarrez les services : docker compose restart auth-service activity-service booking-service payment-service gateway"
