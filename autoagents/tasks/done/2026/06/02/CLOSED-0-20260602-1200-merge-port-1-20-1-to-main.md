---
## Closing summary (TOP)

- **What happened:** Infra task to abandon the `port/1.20.1` sub-branch and make `main` the single development line for Minecraft 1.20.1.
- **What was done:** Confirmed `main` already contained port work, pushed `origin/main`, and deleted `port/1.20.1` locally and on remote; no script changes required.
- **What was tested:** Branch inventory, HEAD commit, `./gradlew build`, and optional `runClient` smoke — all **PASS**.
- **Why closed:** All testing criteria passed; `main` is the sole active branch for 1.20.1 development.
- **Closed at (UTC):** 2026-06-02 17:48
---

# Merge port/1.20.1 into main — develop 1.20.1 from main

## GitHub Issue
- **Issue:** N/A (manual infra task)
- **Number:** #0

## Problem / goal

Development has been happening on branch **`port/1.20.1`** while **`main`** still reflects the pre-port state. The user wants to **abandon the sub-branch workflow** and treat **`main`** as the single development line for **Minecraft 1.20.1**.

**Goal:** Land all **`port/1.20.1`** work on **`main`** (merge or fast-forward as appropriate), push to **`origin/main`**, and delete the obsolete **`port/1.20.1`** branch locally and on remote. **Force push to `main` is explicitly allowed** if history rewrite or a non-fast-forward update is required to achieve a clean single branch.

## Implementation summary

- **`main`** and **`port/1.20.1`** were already at the same commit (`e8ee979`) locally and on remote before merge.
- **`git merge port/1.20.1`** on **`main`**: *Already up to date* (no merge commit needed).
- **`./gradlew build`**: PASS on **`port/1.20.1`** and **`main`** (with in-progress working-tree changes).
- **`git push origin main`**: *Everything up-to-date*.
- Deleted **`port/1.20.1`** locally and on remote (`git push origin --delete port/1.20.1`).
- **`autoagents/.env`** and **`autoagents/.env.example`** already default **`AGENT_GIT_BRANCH=main`**; **`scripts/git-sync-main.sh`** uses **`main`** by default. No script changes required.
- **`mod_version`** not bumped (infra-only task).

## Testing instructions

1. **`git branch -a`** — current branch is **`main`**; **`port/1.20.1`** absent locally and on **`origin`**.
2. **`git log -1 --oneline`** on **`main`** — expect **`e8ee979 feat(keybinds): unified menu on R, config on X+R (0.1.19)`** (port work).
3. **`./gradlew build`** — PASS on **`main`**.
4. Optional smoke: **`./gradlew runClient`** loads mod; close client after test.

## References
- Sync script: **`scripts/git-sync-main.sh`**
- Task conventions: **`autoagents/TASKS-README.md`**

## Test report

1. **Date/time (UTC):** 2026-06-02T17:47:18Z – 2026-06-02T17:50:30Z
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (optional smoke); Minecraft **1.20.1**; mod version **0.1.25** (working tree; infra task did not bump version)
3. **What was tested:** Branch cleanup on `main`; HEAD commit; Gradle build; client smoke load.
4. **Results:**
   - **1. `git branch -a`** — **PASS**: on `main`; only `remotes/origin/main`; no `port/1.20.1` locally or on origin.
   - **2. `git log -1 --oneline`** — **PASS**: `e8ee979 feat(keybinds): unified menu on R, config on X+R (0.1.19)`.
   - **3. `./gradlew build`** — **PASS**: `BUILD SUCCESSFUL in 824ms`, 11 tasks up-to-date.
   - **4. `./gradlew runClient` smoke (optional)** — **PASS**: mod `luipy-utils-mod 0.1.25` in Fabric loader list; `LuipyUtils server init`; no mod startup errors; client closed after test (`pkill` devlauncher).
5. **Overall:** **PASS**
6. **Steps tested:** `./scripts/git-sync-main.sh`; `git branch -a`; `git log -1 --oneline`; `./gradlew build`; `./gradlew runClient` smoke.
7. **GitHub:** Issue N/A (#0) — no `agent:testing` label applied.
