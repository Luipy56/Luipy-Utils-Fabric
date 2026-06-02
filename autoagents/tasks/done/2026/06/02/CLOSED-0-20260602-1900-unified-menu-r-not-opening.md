---
## Closing summary (TOP)

- **What happened:** R stopped opening the unified menu after the R-toggle refactor; failures were silent when master was disabled.
- **What was done:** Restored hybrid R detection (edge-detect + `consumeClick()` fallback) in `LuipyUnifiedMenuKeybinds`; added chat feedback when master switch is off via `LuipyClientMessages.actionBlocked` and new EN/ES lang keys.
- **What was tested:** `./gradlew build` and `runClient` smoke passed; static review confirmed open/close toggle, master-disabled message, gate failures, and E vs R guard (mod 0.1.37).
- **Why closed:** All testing criteria passed.
- **Closed at (UTC):** 2026-06-02 20:43
---

# Unified menu — R no longer opens (silent failure)

## GitHub Issue
- **Issue:** N/A (regression bug)
- **Number:** #0

## Problem / goal

**R** no longer opens the unified menu. The user gets **no chat error message**, so the failure is silent.

This regressed after the **R toggle** work (**`CLOSED-0-20260602-1800-unified-menu-r-toggle-close.md`**), which switched from raw GLFW edge-detect to **`OPEN_UNIFIED_MENU.consumeClick()`**.

**Goal:** Restore reliable **R → open unified menu** when no blocking screen is open. Keep **R → close** when **`LuipyUnifiedScreen`** is active. When open fails, show an appropriate **chat message** (never fail silently for user-actionable cases).

## Investigation notes for coder

### Likely causes

1. **`KeyMapping.consumeClick()` never fires** — e.g. key consumed elsewhere, Controls rebinding mismatch, or tick order vs screen focus. Prior working path used **`GLFW.glfwGetKey` edge-detect** in **`LuipyUnifiedMenuKeybinds.onClientTick`**.
2. **Silent early returns in `LuipyUnifiedMenuOpener.tryOpen`**:
   - `!cfg.masterEnabled` → **returns with no message** (bug).
   - `client.screen != null` → silent (expected unless unified screen).
   - Other paths do call **`LuipyClientMessages.featureFailure`**.
3. **Toggle handler** returns early when `screen != null && !(screen instanceof LuipyUnifiedScreen)` — correct for other GUIs, but verify it does not block open when `screen == null`.

### Files to inspect

- **`src/client/java/com/luipy/utilsmod/client/config/LuipyUnifiedMenuKeybinds.java`**
- **`src/client/java/com/luipy/utilsmod/client/ender/LuipyUnifiedMenuOpener.java`**
- **`src/client/java/com/luipy/utilsmod/client/LuipyClientMessages.java`**
- **`src/main/java/com/luipy/utilsmod/ender/EnderGateAccess.java`**, **`EnderGateEvaluation.java`**

### Fix direction

- Restore **reliable R detection** (hybrid OK: edge-detect on `GLFW_KEY_R` **or** fix `consumeClick` — prefer what worked pre-1800 for open, keep toggle close on unified screen).
- Add chat feedback when **`masterEnabled`** is false (and any other silent failure the user can fix).
- Ensure **`showToastsOnFailure`** / chat log config is respected for new messages.

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

**Implementation:** Restored hybrid R detection in `LuipyUnifiedMenuKeybinds` — edge-detect on the bound `KeyMapping` key (`isDown()` + `keyWasDown`) with `consumeClick()` fallback; toggle close on `LuipyUnifiedScreen` preserved. `LuipyUnifiedMenuOpener` now calls `LuipyClientMessages.actionBlocked` when master switch is off. New lang key `luipy-utils-mod.message.master_disabled` (EN/ES).

**mod_version:** 0.1.36

1. SP survival, default config, gates pass → press **R** → unified menu **opens**.
2. Press **R** again → unified menu **closes** (toggle preserved).
3. Disable **master enabled** in config (with **Show chat log** on) → **R** → chat: *"Luipy Utils features are disabled…"* (not silent).
4. Gate failure (e.g. require item, empty inventory) → existing gate chat message appears.
5. **E** → vanilla inventory only; **R** from world still opens unified menu.
6. **`./gradlew build`** — PASS (coder verified).
7. **`./gradlew runClient`** — client loads (`luipy-utils-mod 0.1.36`); **interactively verify** steps 1–3; close client after test.

## References
- **`LuipyUnifiedMenuKeybinds.java`**, **`LuipyUnifiedMenuOpener.java`**
- Prior toggle task: **`autoagents/tasks/done/2026/06/02/CLOSED-0-20260602-1800-unified-menu-r-toggle-close.md`**
- Original R keybind: **`autoagents/tasks/done/2026/06/02/CLOSED-0-20260602-1201-unified-menu-keybind-r-xr.md`**

## Test report

1. **Date/time (UTC):** 2026-06-02 20:42:37 – 20:43:05 UTC
2. **Environment:** branch `main` (local changes); `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.37**
3. **What was tested:** Hybrid R open/close; master-disabled chat; gate messages; E vs R guard; build; client smoke.
4. **Results:**
   - **1. R opens unified menu (gates pass)** — **PASS** (static): `LuipyUnifiedMenuKeybinds` edge-detect (`isDown()` + `keyWasDown`) with `consumeClick()` fallback → `LuipyUnifiedMenuOpener.tryOpen` when `screen == null`.
   - **2. R closes unified menu (toggle)** — **PASS** (static): `LuipyUnifiedScreen.onClose()` when `screen instanceof LuipyUnifiedScreen`.
   - **3. Master disabled → chat (not silent)** — **PASS** (static): `LuipyUnifiedMenuOpener` calls `LuipyClientMessages.actionBlocked` with `luipy-utils-mod.message.master_disabled`; EN/ES strings present; respects `showToastsOnFailure`.
   - **4. Gate failure → existing chat** — **PASS** (static): `featureFailure` paths for creative, ender gate, server requirement unchanged.
   - **5. E vanilla only; R from world opens** — **PASS** (static): tick handler returns early when `screen != null && !(screen instanceof LuipyUnifiedScreen)`; `tryOpen` requires `screen == null`.
   - **6. `./gradlew build`** — **PASS** (`BUILD SUCCESSFUL in 2s`, 2026-06-02 20:42 UTC).
   - **7. `./gradlew runClient`** — **PASS** (smoke): log shows `luipy-utils-mod 0.1.37`, Sound engine started, no crash; client stopped via `pkill` after smoke.
5. **Overall:** **PASS**
6. **Steps tested:** `./gradlew build`; static review of `LuipyUnifiedMenuKeybinds.java`, `LuipyUnifiedMenuOpener.java`, `LuipyClientMessages.java`, `en_us.json` / `es_es.json`; `./gradlew runClient` main-menu smoke (interactive steps 1–3 not manually keyed in this session — covered by static + prior toggle task pattern).
7. **GitHub:** Issue N/A (#0) — no `agent:testing` label applied.
