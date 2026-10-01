#!/usr/bin/env bash
set -euo pipefail

service_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

load_env_file() {
  local env_file="$1"
  [[ -f "$env_file" ]] || return 0
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ "$line" =~ ^[[:space:]]*(export[[:space:]]+)?([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*=[[:space:]]*(.*)[[:space:]]*$ ]] || continue
    local name="${BASH_REMATCH[2]}"
    local value="${BASH_REMATCH[3]}"
    if [[ "$value" == \"*\" || "$value" == \'*\' ]]; then
      value="${value:1:${#value}-2}"
    fi
    if [[ -z "${!name+x}" ]]; then
      export "$name=$value"
    fi
  done < "$env_file"
}

load_env_file "$service_root/../.env"
load_env_file "$service_root/.env"

required_vars=(AI_INTERNAL_TOKEN GEMINI_API_KEY GEMINI_MODEL)
missing=()
for name in "${required_vars[@]}"; do
  if [[ -z "${!name:-}" ]]; then
    missing+=("$name")
  fi
done
if (( ${#missing[@]} > 0 )); then
  printf 'AI service configuration is missing: %s\n' "${missing[*]}" >&2
  exit 1
fi

cd "$service_root"
if command -v uv >/dev/null 2>&1; then
  uv run --with-requirements requirements.txt python -m uvicorn main:app --reload --port 8000
elif command -v python >/dev/null 2>&1; then
  python -m uvicorn main:app --reload --port 8000
elif command -v python3 >/dev/null 2>&1; then
  python3 -m uvicorn main:app --reload --port 8000
else
  echo 'Python was not found. Install Python 3.11+ or uv before starting the AI service.' >&2
  exit 1
fi
