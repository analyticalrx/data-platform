#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$root_dir/.env"

if [[ -e "$env_file" ]]; then
  echo "Refusing to replace existing $env_file" >&2
  exit 1
fi

for command in aws jq; do
  if ! command -v "$command" >/dev/null 2>&1; then
    echo "$command is required" >&2
    exit 1
  fi
done

region=us-east-1
account="$(aws sts get-caller-identity --region "$region" --query Account --output text)"
if [[ "$account" != 209556026827 ]]; then
  echo "Expected AWS account 209556026827, got $account" >&2
  exit 1
fi

db_secret="$(aws secretsmanager get-secret-value \
  --region "$region" \
  --secret-id analytical-rx/phase1/dev/postgres/data-platform-password-rw \
  --query SecretString --output text)"
db_username="$(jq -er '.username' <<< "$db_secret")"
db_password="$(jq -er '.password' <<< "$db_secret")"
jwt_secret="$(aws secretsmanager get-secret-value \
  --region "$region" \
  --secret-id analytical-rx/phase1/dev/jwt-signing-secret \
  --query SecretString --output text)"

if [[ "$db_username" == *$'\n'* || "$db_password" == *$'\n'* || "$jwt_secret" == *$'\n'* ]]; then
  echo "A secret contains a newline and cannot be written to a VS Code env file" >&2
  exit 1
fi

umask 077
temp_file="$(mktemp "$root_dir/.env.tmp.XXXXXX")"
trap 'rm -f "$temp_file"' EXIT

{
  printf 'DB_JDBC_URL=jdbc:postgresql://127.0.0.1:5433/analyticalrx?sslmode=require\n'
  printf 'DB_USERNAME=%s\n' "$db_username"
  printf 'DB_PASSWORD=%s\n' "$db_password"
  printf 'JWT_SIGNING_SECRET=%s\n' "$jwt_secret"
  printf 'JWT_ACCESS_TOKEN_TTL_MS=1800000\n'
  printf 'JWT_ISSUER=analyticalrx-api\n'
  printf 'JWT_AUDIENCE=analyticalrx-frontend\n'
} > "$temp_file"

mv "$temp_file" "$env_file"
trap - EXIT
echo "Created $env_file with owner-only permissions"
