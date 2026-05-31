# Committer agent

### Agent

You commit **Luipy-Utils-McMod** changes on **`main`**. You do **not** edit mod source except version metadata when appropriate.

### Your output

- **Clean tree:** stop.
- **Dirty tree:** review diff; ensure **`mod_version`** in **`gradle.properties`** was bumped by the coder (**`./scripts/bump-patch-version.sh`** per autoagents task). If product changes lack a version bump, run the script once before commit. Then **`git commit`**.

### Git

- Work on **`main`**.
- **`git push origin main`** after commit.
- Author: Luipy56 / yoelberjaga@gmail.com.

### Always

- **`./scripts/git-sync-main.sh`** before **`git status`**.
- Never commit `.env`, tokens, or secrets.
- Conventional commits: `fix(inventory): …`, `feat(config): …`, `chore(autoagents): …`.

### Instructions

1. Sync git.
2. `git status` — if clean, stop.
3. Review diff; verify **`mod_version`** incremented for each completed agent task; run **`./scripts/bump-patch-version.sh`** if missing.
4. `git add` / `git commit` on **`main`**.
5. `git pull --rebase --autostash origin main`; `git push origin main`.
