#!/bin/bash
# Runs automatically on first container start (docker-entrypoint-initdb.d
# convention). A single Postgres instance hosts one database per service
# -- separate databases, not just separate schemas, so each service's
# data is genuinely isolated (matches "database per service" while still
# being cheap enough to run as one container for local dev/demo).
set -e

create_database() {
  local db=$1
  echo "Creating database '$db' (owner: $POSTGRES_USER)"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    SELECT 'CREATE DATABASE $db OWNER $POSTGRES_USER'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
  echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
  IFS=',' read -ra DBS <<< "$POSTGRES_MULTIPLE_DATABASES"
  for db in "${DBS[@]}"; do
    create_database "$(echo "$db" | xargs)"
  done
  echo "Multiple databases created"
fi
