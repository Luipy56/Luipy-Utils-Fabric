#!/usr/bin/env bash
# Authenticate gh for autoagents (Issues: Read and write on AGENT_GH_REPO).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="${ROOT}/autoagents/.env"
REPO="${AGENT_GH_REPO:-Luipy56/Luipy-Utils-McMod}"

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI not found. Install GitHub CLI first." >&2
  exit 1
fi

echo "GitHub repo: $REPO"
echo "Copy autoagents/.env.example to autoagents/.env and set GH_TOKEN if needed."
echo ""
gh auth status || gh auth login
gh auth setup-git 2>/dev/null || true
echo ""
echo "Testing issue list access..."
gh issue list --repo "$REPO" --state open -L 3
