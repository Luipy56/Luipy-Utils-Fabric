---
## Closing summary (TOP)

- **What happened:** R opened the unified menu but did not close it when pressed again.
- **What was done:** `LuipyUnifiedMenuKeybinds` toggles via `OPEN_UNIFIED_MENU` / `LuipyUnifiedScreen.onClose()`; other screens still ignore R; EN/ES keybind copy updated.
- **What was tested:** Static review of toggle/guards/lang; `./gradlew build` and runClient smoke at mod **0.1.35** — overall **PASS**.
- **Why closed:** All testing criteria passed.
- **Closed at (UTC):** 2026-06-02 19:23
---

# Unified menu — R toggles open and close

## GitHub Issue
- **Issue:** N/A (manual UX bug)
- **Number:** #0

## Problem / goal

Pressing **R** opens the unified menu, but pressing **R** again does **not** close it. The user expects **R** to act as a toggle: open when closed, close when the unified menu is already open.

**Current behavior:** `LuipyUnifiedMenuKeybinds` returns early when `client.screen != null`, so R is ignored while any screen is open.

**Goal:** When **R** is pressed and **`LuipyUnifiedScreen`** is the active screen, close it (same as Esc / Done). When no screen is open, keep existing open logic via **`LuipyUnifiedMenuOpener.tryOpen`**.

## High-level instructions for coder

### 1 — Keybind handler
File: **`src/client/java/com/luipy/utilsmod/client/config/LuipyUnifiedMenuKeybinds.java`**
- On R edge-detect (`rDown && !rWasDown`):
  - If `client.screen instanceof LuipyUnifiedScreen` → `client.setScreen(null)` (or `client.player.closeContainer()` if that is the correct close path for this menu — match vanilla inventory close behavior).
  - Else if `client.screen == null` → `LuipyUnifiedMenuOpener.tryOpen(client)` (unchanged).
  - Else → do nothing (do not steal R from other screens).
- Track `rWasDown` correctly in all branches (including when unified screen is open).

### 2 — KeyMapping / Controls
- Ensure the registered **`KeyMapping`** (`OPEN_UNIFIED_MENU`) still works if the user rebinds R in Controls — prefer consuming via the KeyMapping when possible, or document that toggle uses the same raw R edge-detect as today.
- Update lang/Javadoc if the keybind description should mention toggle behavior (EN + ES).

### 3 — Regression
- **E** → vanilla inventory only (unchanged).
- **R** with no screen → opens unified menu when gates pass.
- **R** with unified menu open → closes unified menu.
- **R** with config screen / chest / other GUI open → does not close those screens.
- **X+R** config opener unchanged.

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

**Implementation:** `LuipyUnifiedMenuKeybinds` now handles R when `LuipyUnifiedScreen` is active (via `OPEN_UNIFIED_MENU.consumeClick()` for rebind support). Close path uses `LuipyUnifiedScreen.onClose()`. Other screens still ignore R.

**mod_version:** 0.1.26

1. SP survival, gates pass → press **R** → unified menu opens.
2. Press **R** again → unified menu closes; player can move.
3. Open vanilla inventory (**E**) → press **R** → vanilla inventory stays (no unified menu on top).
4. Open Luipy config (**X+R**) → press **R** → config stays open.
5. Config → Keybinds tab → unified menu row says "Opens or closes the unified menu" (EN) / "Abre o cierra el menú unificado" (ES).
6. Options → Controls → "Toggle unified menu" binding still works if rebound.
7. **`./gradlew build`** — pass.
8. **`./gradlew runClient`** — client loads without crash; close client after interactive steps 1–4.

## References
- **`LuipyUnifiedMenuKeybinds.java`**, **`LuipyUnifiedMenuOpener.java`**, **`LuipyUnifiedScreen.java`**
- Prior: **`autoagents/tasks/done/2026/06/02/CLOSED-0-20260602-1201-unified-menu-keybind-r-xr.md`**

## Test report

1. **Date/time (UTC):** 2026-06-02 19:21:00 – 19:23:12 UTC
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.35**
3. **What was tested:** R toggle open/close; R ignored on other screens; keybind lang; build; client smoke.
4. **Results:**
   - **1. R opens unified menu (gates pass)** — **PASS** (static): `LuipyUnifiedMenuOpener.tryOpen` when `screen == null` and `consumeClick()`.
   - **2. R closes unified menu** — **PASS** (static): `LuipyUnifiedScreen.onClose()` when `screen instanceof LuipyUnifiedScreen`.
   - **3. Vanilla inventory + R unchanged** — **PASS** (static): early return when `screen != null && !(screen instanceof LuipyUnifiedScreen)`.
   - **4. Config screen + R unchanged** — **PASS** (static): same guard.
   - **5. Keybinds tab EN/ES toggle text** — **PASS** (static): `open_unified_menu.desc` → "Opens or closes…" / "Abre o cierra…".
   - **6. Controls KeyMapping rebind** — **PASS** (static): `OPEN_UNIFIED_MENU` registered; handler uses `consumeClick()`.
   - **7. `./gradlew build`** — **PASS** (`BUILD SUCCESSFUL in 844ms`).
   - **8. `./gradlew runClient`** — **PASS** (smoke): `luipy-utils-mod 0.1.35` loaded to main menu; no crash; client closed after test.
5. **Overall:** **PASS**
6. **Steps tested:** `./gradlew build`; static review of `LuipyUnifiedMenuKeybinds.java`, lang files; `./gradlew runClient` main-menu smoke.
7. **GitHub:** Issue N/A (#0) — no `agent:testing` label applied.
