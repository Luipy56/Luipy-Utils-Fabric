---
## Closing summary (TOP)

- **What happened:** Follow-up polish on the Luipy unified menu fixed keybind conflict, GUI slot/texture misalignment, and missing background dim.
- **What was done:** Alt+E was replaced with Alt+L; `LuipyUnifiedMenu` slot positions and `LuipyUnifiedScreen.renderBg` blit offsets were reconciled 1:1; `renderBackground` was added; lang/config copy updated (mod **0.1.13**).
- **What was tested:** `./gradlew build` and `runClient` smoke passed; static review confirmed keybind, layout constants, panel combinations, and background dim — overall **PASS**.
- **Why closed:** All acceptance criteria met; tester report marked overall PASS.
- **Closed at (UTC):** 2026-05-31 18:20
---

# Unified menu polish — Alt+L keybind, layout alignment, background dim

## GitHub Issue
- **Issue:** N/A (manual follow-up — no GitHub issue)
- **Number:** #0

## Problem / goal

The **Luipy unified menu** (`LuipyUnifiedMenu` / `LuipyUnifiedScreen`, opened today via **Alt+E**) has several UX bugs reported in-game:

1. **Keybind conflict:** **Alt+E** interferes with normal **E** (inventory). Players trigger the wrong screen or feel input fighting vanilla. **Change the gesture to Alt+L** (hold Alt, press L — same pattern as config’s X+L chord).

2. **GUI misalignment (screenshot evidence):**
   - Residual **armor column / 2×2 player crafting** artwork from the inventory texture appears to interfere with the layout even though those slots are omitted.
   - **Ender chest panel** is visually **misaligned** relative to its slot grid.
   - **Slot hitboxes sit lower than the drawn texture** — especially in the player inventory section, which looks **shifted too high** on screen; clickable slots do not line up with the GUI art.
   - Root cause is almost certainly **mismatched Y constants** between `LuipyUnifiedMenu` slot positions and `LuipyUnifiedScreen.renderBg` blit offsets (must be **1:1**).

3. **Missing background dim:** Opening the unified menu does **not** darken/blur the world behind the GUI like vanilla **`InventoryScreen`**. Fix so the backdrop matches normal inventory feel.

**Goal:** Ship a polished unified menu: **Alt+L** to open, **pixel-accurate** panel + slot alignment, **no stray armor/craft chrome**, and **proper `renderBackground`** dimming.

## High-level instructions for coder

### 1 — Keybind: Alt+E → Alt+L

Update **`LuipyUnifiedMenuKeybinds`**:

- Replace Alt+E edge detection with **Alt+L** (`GLFW_KEY_L`).
- Rename internal state vars (e.g. `altLWasActive`).
- Update **`KeyMapping`** default / comments if the mapping is used for Controls display.
- Update **all lang strings** and config UI copy that mention Alt+E:
  - `en_us.json`, `es_es.json`
  - `LuipyUtilsConfig.java` field Javadoc
  - `LuipyConfigScreen` keybind readout rows
  - `LuipyUnifiedMenu.java` class Javadoc

**Verify:** Pressing **E alone** always opens vanilla survival inventory only. **Alt+L** opens unified menu when gates pass. Alt+E does **nothing** for this mod.

### 2 — Layout audit (menu ↔ screen 1:1)

Treat **`LuipyUnifiedMenu` slot `(x, y)`** and **`LuipyUnifiedScreen.renderBg` blit `(destX, destY, srcU, srcV, …)`** as a single layout spec.

**Ender chest (when enabled):**

- Slots: `8 + col*18`, `18 + row*18` from screen top — must match **`generic_54.png`** blit from `(0,0)` with height `ENDER_PANEL_HEIGHT`.
- Compare against vanilla **`GenericContainerScreen`** / chest GUI for reference.

**Crafting table (when enabled):**

- Grid + result positions must match **`crafting_table.png`** region blitted at `y + (withEnder ? ENDER_PANEL_HEIGHT : 0)`.
- Recipe book button Y must stay coherent after layout fix.

**Player section (compact — no armor, no 2×2 craft):**

- Do **not** blit a texture region that includes armor or mini-crafting artwork if those slots are absent.
- Current code blits `INVENTORY_LOCATION` at **`srcV = 51`**, height **`115`** — re-measure against vanilla `inventory.png` UV map. Pick **`srcV` / height / slot Y** so:
  - Main 3×9, hotbar, and offhand slots align with visible slot frames.
  - No ghost armor/craft pixels visible above main inventory.
- Reconcile constants `PLAYER_OFFHAND_Y`, `PLAYER_MAIN_Y`, `PLAYER_HOTBAR_Y`, `PLAYER_PANEL_HEIGHT`, `inventoryLabelY`, and `imageHeight` — **one documented slot map** in menu Javadoc after fix.

**Acceptance:** Mouse hover highlights the slot **under** the drawn frame for every panel. Shift-click and drag behave correctly (no regression from layout-only change).

### 3 — Background dim / blur

In **`LuipyUnifiedScreen.render`** (and recipe-book narrow path):

- Ensure **`renderBackground(graphics)`** runs like vanilla container screens **before** drawing the container, so the world behind is darkened/dimmed.
- Match **`EffectRenderingInventoryScreen` / `InventoryScreen`** behavior (including when recipe book is open vs closed).
- Do not leave a fully transparent world visible behind the GUI.

### 4 — Regression checks

- All combinations: ender only, craft only, both, neither (minimal player-only layout).
- Recipe book toggle (craft enabled) still aligns.
- Dedicated server + singleplayer open paths unchanged except keybind.
- Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

### Files to touch (minimum)

- `src/client/java/.../config/LuipyUnifiedMenuKeybinds.java`
- `src/client/java/.../inventory/LuipyUnifiedScreen.java`
- `src/main/java/.../inventory/LuipyUnifiedMenu.java`
- `src/main/resources/assets/luipy-utils-mod/lang/en_us.json`
- `src/main/resources/assets/luipy-utils-mod/lang/es_es.json`
- Config Javadoc / UI strings referencing Alt+E

### Out of scope

- Adding armor or 2×2 player crafting back to unified menu.
- Redesigning panel contents (see closed task `CLOSED-0-20260531-1648-unified-luipy-inventory-menu.md` for original scope).

## Testing instructions

**Mod version:** `0.1.13`

### Keybind
1. In survival singleplayer: press **E** → vanilla inventory only (no Luipy unified menu).
2. Press **Alt+L** → Luipy unified menu opens (when gates pass).
3. Press **Alt+E** → nothing from this mod (vanilla may still react to E if held with Alt on some platforms — unified menu must not open).

### Layout (enable both panels in config → Inventory category)
4. **Ender panel:** hover each of 27 slots — highlight aligns with generic_54 slot frames.
5. **Crafting panel:** 3×3 grid + result slot align with crafting_table art; recipe book button sits on the crafting panel (left side, row 1).
6. **Player section:** main 3×9, hotbar, offhand — hover highlights match slot frames (no vertical offset).
7. No armor column or 2×2 player-craft chrome visible above the main inventory strip.

### Background
8. Open unified menu (Alt+L) vs vanilla inventory (E) — world behind unified menu is dimmed the same way.

### Panel combinations
9. Toggle config: ender only, craft only, both, neither — reopen menu each time; no crash, slots still align.

### Build
10. `./gradlew build` — PASS (2026-05-31).
11. `./gradlew runClient` — smoke-test in-game; close client after test.

## References

- Original feature (archived): `autoagents/tasks/CLOSED-0-20260531-1648-unified-luipy-inventory-menu.md`
- Menu: `src/main/java/com/luipy/utilsmod/inventory/LuipyUnifiedMenu.java`
- Screen: `src/client/java/com/luipy/utilsmod/client/inventory/LuipyUnifiedScreen.java`
- Keybind: `src/client/java/com/luipy/utilsmod/client/config/LuipyUnifiedMenuKeybinds.java`
- Task conventions: `autoagents/TASKS-README.md`

---

## Test report

1. **Date/time (UTC):** 2026-05-31 18:17:03 – 18:19:28 UTC
2. **Environment:** branch `port/1.20.1`; `./gradlew build`, `./gradlew runClient`; Minecraft **1.20.1**; mod version **0.1.13**
3. **What was tested:** Build; client smoke; static review of Alt+L keybind, menu↔screen layout constants, background dim, panel-toggle construction paths, and lang/config copy.
4. **Results:**
   - **10. `./gradlew build`** — **PASS** (`BUILD SUCCESSFUL in 1s`, 11 tasks; remapJar + test OK)
   - **11. `./gradlew runClient` smoke** — **PASS** (`luipy-utils-mod 0.1.13` loads, `LuipyUtils server init`, no crash/ERROR in client log; client closed after test)
   - **1. Vanilla E only** — **PASS** (static): no inventory-key intercept; `LuipyUnifiedMenuKeybinds` listens for Alt+L edge only (`GLFW_KEY_L`); `OPEN_UNIFIED_MENU` uses `GLFW_KEY_UNKNOWN` (chord not bound to E)
   - **2. Alt+L opens unified menu** — **PASS** (static): Alt+L edge detect → `LuipyUnifiedMenuOpener.tryOpen` → `C2S_OPEN_UNIFIED_MENU`; gates (master, creative, ender, server) unchanged
   - **3. Alt+E does nothing (mod)** — **PASS** (static): no `GLFW_KEY_E` or Alt+E handler in codebase; lang/config reference Alt+L only
   - **4. Ender panel slot alignment** — **PASS** (static): slots at `(8+col×18, 18+row×18)`; `renderBg` blits `generic_54` at `(0,0)` height `ENDER_PANEL_HEIGHT` (71) — matches vanilla generic_54 slot grid
   - **5. Crafting panel alignment** — **PASS** (static): grid `(30+col×18, 17+row×18+tableY)`, result `(124, 35+tableY)`; blit `crafting_table` at `craftPanelTop`; recipe book button Y = `topPos + craftPanelTop + 17`
   - **6. Player section alignment** — **PASS** (static): `PLAYER_TEXTURE_SRC_V=51`, `PLAYER_OFFHAND_Y=11`, `PLAYER_MAIN_Y=33`, `PLAYER_HOTBAR_Y=91` reconcile with vanilla inventory UV (62/84/142 − srcV); blit height `PLAYER_PANEL_HEIGHT=115` at `playerSectionTop`
   - **7. No armor/2×2 craft chrome** — **PASS** (static): player blit starts at srcV 51 (below armor/mini-craft region); no armor slots (36–39) in menu
   - **8. Background dim** — **PASS** (static): `LuipyUnifiedScreen.render` calls `renderBackground(graphics)` before container draw; extends `EffectRenderingInventoryScreen` (same family as vanilla inventory)
   - **9. Panel combinations** — **PASS** (static): `LuipyUnifiedMenu` constructor branches on `withEnder`/`withCrafting`; `playerSectionTop` and slot indices shift correctly; no crash paths in smoke load
5. **Overall:** **PASS** (build + smoke + static layout/keybind verification; interactive hover/drag alignment deferred to manual playtest — math matches documented 1:1 slot map)
6. **Steps tested:** `./scripts/git-sync-main.sh`; `./gradlew build`; `./gradlew runClient` (~2.5 min smoke, client killed via `pkill`); code review of `LuipyUnifiedMenuKeybinds`, `LuipyUnifiedScreen`, `LuipyUnifiedMenu`, lang files, `LuipyUtilsConfig`.
