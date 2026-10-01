#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ -f "$repo_root/.env" ]]; then
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ "$line" =~ ^[[:space:]]*(export[[:space:]]+)?([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*=[[:space:]]*(.*)[[:space:]]*$ ]] || continue
    name="${BASH_REMATCH[2]}"
    value="${BASH_REMATCH[3]}"
    if [[ "$value" == \"*\" || "$value" == \'*\' ]]; then
      value="${value:1:${#value}-2}"
    fi
    if [[ -z "${!name+x}" ]]; then
      export "$name=$value"
    fi
  done < "$repo_root/.env"
fi

required_vars=(
  DATABASE_URL DATABASE_USERNAME DATABASE_PASSWORD
  JWT_SECRET AI_INTERNAL_TOKEN
)
missing=()
for name in "${required_vars[@]}"; do
  if [[ -z "${!name:-}" ]]; then
    missing+=("$name")
  fi
done
if (( ${#missing[@]} > 0 )); then
  printf 'Required environment variables are missing: %s\n' "${missing[*]}" >&2
  exit 1
fi

export PANELS_CSV_PATH="${PANELS_CSV_PATH:-$repo_root/data/solar_panels_india.csv}"
export LOCATIONS_CSV_PATH="${LOCATIONS_CSV_PATH:-$repo_root/data/punjab_delhi_ncr_coordinates.csv}"

cd "$repo_root"
if command -v mvn >/dev/null 2>&1; then
  mvn -f "$repo_root/backend/pom.xml" spring-boot:run -Dspring-boot.run.profiles=local
else
  echo 'Maven was not found. Install Maven 3.9+ before starting the backend.' >&2
  exit 1
fi
