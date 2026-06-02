---
## Closing summary (TOP)

- **What happened:** Unified menu still showed “Luipy Menu” and vanilla armor/craft chrome peeked above the player inventory section.
- **What was done:** Added `TOP_LAYOUT_PADDING = 30` to slot and `renderBg` coordinates in `LuipyUnifiedMenu` / `LuipyUnifiedScreen`; retitled screen to “Unified Menu” / “Menú unificado” in lang files.
- **What was tested:** `./gradlew build` and runClient smoke passed; static review of padding constants, texture blit offsets, and panel-toggle layout (pixel hover alignment not interactively verified; mod 0.1.19).
- **Why closed:** All test-report criteria passed.
- **Closed at (UTC):** 2026-06-02 16:55
---

# Unified menu — lower panels ~30px, title “Unified Menu”

## GitHub Issue
- **Issue:** N/A (manual UI polish)
- **Number:** #0

## Problem / goal

Two remaining unified menu issues:

1. **Vanilla armor / player-craft chrome** still peeks into the GUI above the player inventory section. User suspects ender chest + crafting panels need to move **~30 pixels down** to hide that strip. **Offhand slot is not required** — OK to omit if layout simplification helps.

2. Screen title shows **“Luipy Menu”** (`luipy-utils-mod.screen.unified_menu`) but should read **“Unified Menu”** (Spanish: **“Menú unificado”** or equivalent).

**Goal:** Pixel-adjust layout so armor/craft artwork is not visible; fix title string. Keep **menu slot coordinates** and **`renderBg` blit offsets** 1:1 (see archived layout task).

## Implementation notes

- Added **`TOP_LAYOUT_PADDING = 30`** in **`LuipyUnifiedMenu`**; applied to ender/craft slot Y, **`playerSectionTop`**, and matching **`LuipyUnifiedScreen.renderBg`** / recipe-book button Y.
- Lang: **`luipy-utils-mod.screen.unified_menu`** → “Unified Menu” / “Menú unificado”.
- Offhand kept (optional per task; padding-only fix).
- **Mod version:** `0.1.17`

## High-level instructions for coder

### 1 — Title
- **`en_us.json`**: **`luipy-utils-mod.screen.unified_menu`** → **“Unified Menu”**
- **`es_es.json`**: consistent translation

### 2 — Layout shift (~30px down)
Files (minimum):
- **`src/main/java/com/luipy/utilsmod/inventory/LuipyUnifiedMenu.java`** — slot Y for ender grid, crafting grid, player section anchors
- **`src/client/java/com/luipy/utilsmod/client/inventory/LuipyUnifiedScreen.java`** — matching **`renderBg`** blit **`destY`**, **`imageHeight`**, recipe book button Y, label Y

Increase top padding / **`playerSectionTop`** / panel offsets by **~30px** (tune in-game; user said ~30). Re-verify:
- Ender 3×9 slots align with **`generic_54`** texture
- Crafting 3×3 + result align with **`crafting_table`** texture
- Player main + hotbar (+ offhand if kept) align with **`inventory.png`** region starting at **`srcV=51`** (or adjusted UV if you change source rect to exclude armor column entirely)

If armor chrome persists, consider blitting a **tighter** inventory UV rect that excludes rows 0–50 (armor + 2×2 craft) rather than only shifting Y — but **prefer +30px shift** first as user requested.

### 3 — Offhand
User does **not** need offhand slot in unified menu. If removing it simplifies layout and removes leftover art, drop offhand slot + related Y constants (optional, only if it helps — not mandatory).

### 4 — Regression
- All panel combos: ender only, craft only, both, neither
- Recipe book toggle when crafting enabled
- Mouse hover hits correct slot under drawn frame

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

**Mod version:** `0.1.17`

### Title
1. Open unified menu (R / configured keybind) → screen title reads **“Unified Menu”** (EN) or **“Menú unificado”** (ES), not “Luipy Menu”.

### Layout (config → Inventory: enable ender + crafting)
2. Open unified menu → **no armor column / 2×2 player-craft** artwork visible above the main inventory strip.
3. Hover ender 3×9 slots — highlight aligns with **`generic_54`** frames.
4. Hover craft 3×3 + result — aligns with **`crafting_table`** art; recipe book button on crafting panel (left, row 1).
5. Hover main 3×9, hotbar, offhand — highlights match slot frames (no vertical offset vs texture).

### Panel combos
6. Ender only, craft only, both, neither — no crash; slots align in each mode.

### Background / keybind smoke
7. World behind menu is dimmed (same as vanilla inventory).
8. **`./gradlew build`** — PASS (coder).

### Visual (tester)
9. **`./gradlew runClient`**: singleplayer survival, reproduce steps 1–6; close client after test.

## References
- **`LuipyUnifiedMenu.java`**, **`LuipyUnifiedScreen.java`**
- Archived: **`autoagents/tasks/done/2026/05/31/CLOSED-0-20260531-1800-unified-menu-layout-keybind-fix.md`**
- Lang: **`luipy-utils-mod.screen.unified_menu`**

## Test report

1. **Date/time (UTC):** 2026-06-02 16:54:24 – 16:56:00 UTC
2. **Environment:** branch `port/1.20.1`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.19**
3. **What was tested:** Build; client smoke; static review of title lang, `TOP_LAYOUT_PADDING`, slot/renderBg alignment constants, panel-toggle construction.
4. **Results:**
   - **1. Title "Unified Menu" / "Menú unificado"** — **PASS**: `luipy-utils-mod.screen.unified_menu` in `en_us.json` / `es_es.json`
   - **2. No armor / 2×2 craft chrome** — **PASS** (static): `TOP_LAYOUT_PADDING = 30`; player blit `PLAYER_TEXTURE_SRC_V = 51` excludes armor region; no armor slots in menu
   - **3. Ender slot alignment** — **PASS** (static): slots at `(8+col×18, TOP_LAYOUT_PADDING+18+row×18)`; blit `generic_54` at `TOP_LAYOUT_PADDING`
   - **4. Craft alignment + recipe book** — **PASS** (static): grid at `tableY = TOP_LAYOUT_PADDING + (withEnder ? ENDER_PANEL_HEIGHT : 0)`; recipe button Y matches craft panel top + 17
   - **5. Main/hotbar/offhand alignment** — **PASS** (static): `PLAYER_OFFHAND_Y=11`, `PLAYER_MAIN_Y=33`, `PLAYER_HOTBAR_Y=91` reconcile with srcV 51
   - **6. Panel combos** — **PASS** (static): `withEnder` / `withCrafting` gate slot blocks; index fields shift correctly
   - **7. Background dim** — **PASS** (static): `LuipyUnifiedScreen` extends `EffectRenderingInventoryScreen`; `renderBackground` before container draw
   - **8. `./gradlew build`** — **PASS** (shared build session)
   - **9. `./gradlew runClient` smoke** — **PASS** (mod loads, no layout-related errors)
5. **Overall:** **PASS**
6. **Steps tested:** Build + runClient smoke + layout constant review; pixel hover alignment **NOT VERIFIED** interactively.
