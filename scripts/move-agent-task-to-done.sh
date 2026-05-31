#!/usr/bin/env bash
# Archive a CLOSED- task file under autoagents/tasks/done/YYYY/MM/DD/
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 autoagents/tasks/CLOSED-<N>-YYYYMMDD-HHMM-<slug>.md" >&2
  exit 1
fi

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$1"
if [[ "$SRC" != /* ]]; then
  SRC="${ROOT}/${SRC}"
fi

if [[ ! -f "$SRC" ]]; then
  echo "ERROR: file not found: $SRC" >&2
  exit 1
fi

bn="$(basename "$SRC")"
if [[ ! "$bn" =~ ^CLOSED-[0-9]+-([0-9]{8})- ]]; then
  echo "ERROR: expected CLOSED-<N>-YYYYMMDD-HHMM-<slug>.md, got: $bn" >&2
  exit 1
fi

ymd="${BASH_REMATCH[1]}"
dest_dir="${ROOT}/autoagents/tasks/done/${ymd:0:4}/${ymd:4:2}/${ymd:6:2}"
mkdir -p "$dest_dir"
mv "$SRC" "${dest_dir}/${bn}"
echo "Archived: autoagents/tasks/done/${ymd:0:4}/${ymd:4:2}/${ymd:6:2}/${bn}"
