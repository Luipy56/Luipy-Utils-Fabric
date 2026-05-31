#!/usr/bin/env bash
# Increment mod_version patch segment in gradle.properties (once per autoagents task).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROPS="${ROOT}/gradle.properties"

if [[ ! -f "$PROPS" ]]; then
  echo "ERROR: gradle.properties not found" >&2
  exit 1
fi

line="$(grep -E '^mod_version=' "$PROPS" | tail -1)"
if [[ -z "$line" ]]; then
  echo "ERROR: mod_version= not found in gradle.properties" >&2
  exit 1
fi

current="${line#mod_version=}"
if [[ ! "$current" =~ ^([0-9]+(\.[0-9]+)*)$ ]]; then
  echo "ERROR: unsupported mod_version format: $current" >&2
  exit 1
fi

IFS='.' read -r -a parts <<< "$current"
last_idx=$((${#parts[@]} - 1))
parts[$last_idx]=$((parts[$last_idx] + 1))
next="$(IFS='.'; echo "${parts[*]}")"

sed -i "s/^mod_version=.*/mod_version=${next}/" "$PROPS"
echo "mod_version: ${current} -> ${next}"
