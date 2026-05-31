# Agent loop (autoagents)

Orchestrator: **`./autoagents/autoagents-loop.sh`** (from repo root).

## Roles

| Step | Prompt | Trigger |
|------|--------|---------|
| 001 log / GitHub reviewer | `autoagents/001-gh-reviewer.md` | Open issues without FEAT tasks; optional log triage |
| 010 feature coder | `autoagents/010-feature-coder.md` | `FEAT-*.md` |
| 002 coder | `autoagents/002-coder/CODER.md` | `NEW-*.md`, `WIP-*.md` |
| 012 handoff | `autoagents/012-feature-coder-handoff.md` | `WIP-*.md` ready for test |
| 020 tester | `autoagents/020-test.md` | `UNTESTED-*.md`, `TESTING-*.md` |
| 030 closing reviewer | `autoagents/030-closing-reviewer.md` | `CLOSED-*.md` |
| 040 committer | `autoagents/040-committer.md` | Uncommitted changes on `main` |

Task conventions: **`autoagents/TASKS-README.md`**.

## GitHub labels

| Label | Meaning |
|-------|---------|
| `agent:planned` | 001 created FEAT task |
| `agent:wip` | Coder working |
| `agent:untested` | Ready for tester |
| `agent:testing` | Tester active |

## Environment

Copy **`autoagents/.env.example`** → **`autoagents/.env`** (never commit `.env`).

Key vars: `AGENT_GH_REPO`, `AGENT_GIT_BRANCH`, `GH_TOKEN`, `AGENT_COMMITTER_USE_CURSOR`.
