#!/usr/bin/env bash
set -euo pipefail

if [[ "${1:-}" != "--profile" || -z "${2:-}" || $# -gt 3 ]]; then
  echo "Usage: $0 --profile PROFILE [LOCAL_PORT]" >&2
  exit 1
fi

profile="$2"
port="${3:-5433}"
if [[ ! "$port" =~ ^[0-9]{1,5}$ ]] || (( 10#$port < 1 || 10#$port > 65535 )); then
  echo "Local port must be between 1 and 65535." >&2
  exit 1
fi
port="$((10#$port))"

for command in aws session-manager-plugin; do
  if ! command -v "$command" >/dev/null 2>&1; then
    echo "Install $command before starting the database tunnel." >&2
    exit 1
  fi
done

aws_options=(--region us-east-1 --profile "$profile")
identity="$(aws "${aws_options[@]}" sts get-caller-identity --query '[Account,Arn]' --output text)"
read -r account arn <<< "$identity"
if [[ "$account" != "209556026827" || "$arn" == *:root ]]; then
  echo "Use a personal IAM profile for the AnalyticalRx development account (209556026827)." >&2
  exit 1
fi

db_host="analytical-rx-phase1-dev-db.c6ry4mqo6ydb.us-east-1.rds.amazonaws.com"
echo "Opening direct RDS access through SSM on localhost:$port. Press Ctrl+C to close the tunnel."
exec aws "${aws_options[@]}" ssm start-session \
  --target i-0ee64eae7d8505bfc \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters "host=$db_host,portNumber=5432,localPortNumber=$port"
