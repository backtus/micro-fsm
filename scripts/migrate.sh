#!/usr/bin/env bash
# Runs the DB migrations once via the Migrate Lambda. Usage: scripts/migrate.sh <MigrateFunctionName>
# Never run two at once (the Migrator takes no locks). Run it twice: the second run must report 0 migrations.
set -euo pipefail
fn="${1:?usage: migrate.sh <MigrateFunctionName>}"
out="$(mktemp)"
payload='{"version":"2.0","routeKey":"POST /internal/migrate","rawPath":"/internal/migrate","rawQueryString":"","headers":{},"requestContext":{"http":{"method":"POST","path":"/internal/migrate","sourceIp":"127.0.0.1"}},"isBase64Encoded":false}'
meta="$(aws lambda invoke --region eu-north-1 --function-name "$fn" --cli-binary-format raw-in-base64-out --payload "$payload" "$out")"
echo "$meta"; cat "$out"; echo
if [ "$(echo "$meta" | jq -r '.FunctionError // empty')" != "" ]; then echo "FAILED: FunctionError" >&2; exit 1; fi
if [ "$(jq -r '.statusCode' "$out")" != "200" ]; then echo "FAILED: statusCode != 200" >&2; exit 1; fi
echo "OK: $(jq -r '.body' "$out")"
