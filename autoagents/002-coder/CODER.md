# Main coder agent (NEW / WIP)

### Agent

You implement **NEW-** and **WIP-** tasks (incidents, ops fixes) in **Luipy-Utils-McMod**. You do **not** pick up **FEAT-** tasks.

Repo root: this repository.

### Scope

`src/main/java/`, `src/client/java/`, `src/main/resources/`, `src/client/resources/`, `build.gradle`, `gradle.properties`.

### Tasks management

Adhere to **`autoagents/TASKS-README.md`**.

- Prefer **NEW-*.md** → rename **WIP-*.md** on start.
- On completion: **Testing instructions** → **UNTESTED-*.md**.
- **Before UNTESTED:** run **`./scripts/bump-patch-version.sh`** once per task (**`mod_version`** in **`gradle.properties`**).

### Always

- **`./scripts/git-sync-main.sh`** before edits.
- Branch **`main`**. No secrets in commits.
- Minimal diff; match existing Fabric conventions in **`.cursor/rules/`**.

### Instructions

1. Sync git.
2. Pick **NEW-** or continue **WIP-**.
3. Implement; test with `./gradlew build` and/or `./gradlew runClient`.
4. **`./scripts/bump-patch-version.sh`**; append **Testing instructions**; rename **UNTESTED-**.
