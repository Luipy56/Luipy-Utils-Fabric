---
## Closing summary (TOP)

- **What happened:** Unified menu and config keybinds still used Alt+L / X+L, which conflicted with vanilla offhand (L) and felt awkward.
- **What was done:** Switched unified menu to plain **R** (edge-detect, no screen open) and config opener to **X+R**; updated keybind handlers, Controls entries, Javadoc, and en/es lang strings; removed Alt+L / X+L paths.
- **What was tested:** `./gradlew build` and runClient smoke passed; static review confirmed R / X+R handlers, lang readout, and vanilla **E** non-intercept (mod 0.1.19).
- **Why closed:** All test-report criteria passed.
- **Closed at (UTC):** 2026-06-02 16:55
---

# Keybind change — unified menu R (or Alt+R), config X+R

## GitHub Issue
- **Issue:** N/A (manual UX task)
- **Number:** #0

## Problem / goal

**Alt+L** is awkward because **L** already has a vanilla function (swap offhand). The user wants:

| Action | Current | Target |
|--------|---------|--------|
| Open unified menu | Hold **Alt + L** | Hold **R** alone, **or Alt + R** if **R** alone collides |
| Open config | Hold **X + L** | Hold **X + R** |

**Decision rule for unified menu:** Prefer plain **R** (edge-detect on key press while no screen is open). If **R** conflicts with vanilla gameplay (movement/strafe is already WASD, but **R** may be rebound or used by other mods), fall back to **Alt + R** using the same chord pattern as today’s Alt+L.

**Goal:** Update keybind detection, Controls screen entries, config readout, Javadoc, and **all lang strings** (`en_us.json`, `es_es.json`) to reflect the final chosen keys. **Vanilla E** must still open normal inventory only — never intercept **E**.

## High-level instructions for coder

### 1 — Unified menu opener
File: **`src/client/java/com/luipy/utilsmod/client/config/LuipyUnifiedMenuKeybinds.java`**
- Replace **Alt+L** (`GLFW_KEY_L` + Alt modifier) with **R** (`GLFW_KEY_R`) or **Alt+R** after collision check.
- Document the choice in a one-line comment (why R vs Alt+R).
- Update **`KeyMapping`** registration string if the default key is exposed in Controls.

### 2 — Config opener
File: **`src/client/java/com/luipy/utilsmod/client/config/LuipyConfigKeybinds.java`**
- Change chord from **X+L** to **X+R** (`GLFW_KEY_R` instead of `GLFW_KEY_L`).

### 3 — Copy and docs
Update every reference to Alt+L / X+L:
- **`src/main/resources/assets/luipy-utils-mod/lang/en_us.json`**
- **`src/main/resources/assets/luipy-utils-mod/lang/es_es.json`**
- **`src/main/java/com/luipy/utilsmod/config/LuipyUtilsConfig.java`** (Javadoc on unified-menu fields)
- **`src/main/java/com/luipy/utilsmod/inventory/LuipyUnifiedMenu.java`** (class Javadoc)
- **`src/client/java/com/luipy/utilsmod/client/ender/LuipyUnifiedMenuOpener.java`**
- **`src/client/java/com/luipy/utilsmod/client/config/ui/LuipyConfigScreen.java`** (Keybinds tab readout)

Use the **final** key names in strings (e.g. “hold R” or “hold Alt + R”, “hold X + R”).

### 4 — Regression
- **E** alone → vanilla survival inventory only.
- New unified-menu key → opens unified menu when gates pass.
- **X+R** → opens Luipy config from game or pause menu.
- No handler left for **Alt+L** / **X+L**.

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

**Implementation note:** Plain **R** chosen — unbound in vanilla 1.20.1 default controls (L is swap-offhand). Edge-detect on press with no screen open. Mod **0.1.16**.

1. Press **E** → vanilla inventory only (no unified menu).
2. Press **R** in-world (no screen open) → unified menu opens when gates pass.
3. **Alt+L** → mod does not open unified menu.
4. **X+R** → config screen opens from game or pause menu; **X+L** does nothing for this mod.
5. Config → **Keybinds** tab shows **press R** and **hold X + R**.
6. **`./gradlew build`** — PASS (coder verified).
7. Optional: **`./gradlew runClient`** — smoke test keys above; close client after test.

## References
- **`LuipyUnifiedMenuKeybinds.java`**, **`LuipyConfigKeybinds.java`**
- Prior task (archived): **`autoagents/tasks/done/2026/05/31/CLOSED-0-20260531-1800-unified-menu-layout-keybind-fix.md`**
- Sibling task (config copy): **`FEAT-0-20260602-1202-config-copy-and-world-tab-spacing.md`** — run **after** this task or update strings together.

## Test report

1. **Date/time (UTC):** 2026-06-02 16:54:24 – 16:56:00 UTC
2. **Environment:** branch `port/1.20.1`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.19**
3. **What was tested:** Build; client smoke; static review of R / X+R keybind handlers, removal of Alt+L / X+L, lang + config readout, vanilla-E non-intercept.
4. **Results:**
   - **1. E → vanilla inventory only** — **PASS** (static): no `GLFW_KEY_E` handler; unified menu opens on R edge-detect only when `client.screen == null`
   - **2. R → unified menu** — **PASS** (static): `LuipyUnifiedMenuKeybinds` edge-detect on `GLFW_KEY_R` → `LuipyUnifiedMenuOpener.tryOpen`
   - **3. Alt+L → no mod open** — **PASS** (static): no `GLFW_KEY_L` or Alt modifier handler in codebase
   - **4. X+R → config; X+L inert** — **PASS** (static): `LuipyConfigKeybinds` chord `X && R`; no X+L path
   - **5. Keybinds tab readout** — **PASS**: lang `open_unified_menu` = "Unified menu: press R", `open_config` = "Open config: hold X + R"
   - **6. `./gradlew build`** — **PASS** (`BUILD SUCCESSFUL in 2s`, 11 tasks)
   - **7. `./gradlew runClient` smoke** — **PASS** (`luipy-utils-mod 0.1.19` loads, `LuipyUtils server init`, no mod errors; client closed after test)
5. **Overall:** **PASS**
6. **Steps tested:** Gradle build + runClient smoke; keybind/config paths verified via source + lang (no GUI key automation).
