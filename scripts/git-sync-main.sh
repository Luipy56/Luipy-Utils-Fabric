#!/usr/bin/env bash
# Sync working tree with origin before agent edits.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BR="${AGENT_GIT_BRANCH:-main}"

cd "$ROOT"
git fetch origin "$BR" 2>/dev/null || true
git pull --rebase --autostash "origin" "$BR"
