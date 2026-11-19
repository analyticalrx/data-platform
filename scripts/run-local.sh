#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$root_dir/.env"

if [[ ! -f "$env_file" ]]; then
  bash "$root_dir/scripts/setup-vscode-env.sh" "$@"
fi

# Load backend settings as literal values so passwords are never shell code.
while IFS= read -r entry || [[ -n "$entry" ]]; do
  entry="${entry%$'\r'}"
  key="${entry%%=*}"
  if [[ "$entry" != *=* ]]; then
    continue
  fi
  case "$key" in
    DB_JDBC_URL|DB_USERNAME|DB_PASSWORD|JWT_SIGNING_SECRET|JWT_ACCESS_TOKEN_TTL_MS|JWT_ISSUER|JWT_AUDIENCE)
      value="${entry#*=}"
      case "$value" in
        \"*\"|\'*\') value="${value:1:${#value}-2}" ;;
      esac
      export "$key=$value"
      ;;
  esac
done < "$env_file"

cd "$root_dir"
echo "Starting Data Platform. Keep the SSM tunnel open in its own terminal."
exec ./mvnw spring-boot:run
