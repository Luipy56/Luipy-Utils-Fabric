#!/usr/bin/env bash
# Ephemeral LAN dashboard for autoagents task progress.
exec python3 "$(cd "$(dirname "$0")" && pwd)/autoagents-dashboard.py" "$@"
