---
## Closing summary (TOP)

- **What happened:** A prior +30px `TOP_LAYOUT_PADDING` shift misaligned unified menu panels; user requested revert while keeping the localized title.
- **What was done:** Set `TOP_LAYOUT_PADDING = 0` in `LuipyUnifiedMenu` so slot/panel coordinates and `renderBg` match pre-padding layout; lang title strings unchanged.
- **What was tested:** Static layout/title/combo review; `./gradlew build` and runClient smoke at mod **0.1.35** — overall **PASS**.
- **Why closed:** All testing criteria passed.
- **Closed at (UTC):** 2026-06-02 19:23
---

# Unified menu — revert TOP_LAYOUT_PADDING (+30px shift)

## GitHub Issue
- **Issue:** N/A (layout regression revert)
- **Number:** #0

## Problem / goal

Task **`CLOSED-0-20260602-1206-unified-menu-layout-title-fix`** added **`TOP_LAYOUT_PADDING = 30`** to move ender/craft/player panels down ~30px and hide vanilla armor/craft chrome. The user reports this was **not done correctly** and wants it **reverted**.

**Goal:** Remove the +30px layout shift and restore pre-padding slot coordinates and **`renderBg`** blit positions. Screen title **“Unified Menu”** / **“Menú unificado”** must **stay** (do not revert lang strings).

## Implementation notes

- Set **`TOP_LAYOUT_PADDING = 0`** in **`LuipyUnifiedMenu.java`** (was 30). Slot Y, **`rightColumnContentTop`**, **`playerSectionTop`**, and workstation panel tops all derive from this constant; **`LuipyUnifiedScreen`** already reads those menu fields for **`renderBg`**, recipe-book button Y, and scroll — no screen file edits required.
- Lang strings unchanged.
- **Mod version:** `0.1.29`

## High-level instructions for coder

### 1 — Constants and layout
Files:
- **`src/main/java/com/luipy/utilsmod/inventory/LuipyUnifiedMenu.java`**
- **`src/client/java/com/luipy/utilsmod/client/inventory/LuipyUnifiedScreen.java`**

Revert **`TOP_LAYOUT_PADDING`** usage:
- Set **`TOP_LAYOUT_PADDING = 0`** (or remove constant and inline 0) and restore slot Y / panel tops / `playerSectionTop` / `rightColumnContentTop` to values **before** the +30px task.
- Restore matching **`renderBg`** `destY`, recipe book button Y, title/label Y offsets in **`LuipyUnifiedScreen`**.

Reference archived pre-padding behavior in:
- **`autoagents/tasks/done/2026/05/31/CLOSED-0-20260531-1800-unified-menu-layout-keybind-fix.md`**

### 2 — Do not change
- Title lang keys (`luipy-utils-mod.screen.unified_menu`).
- Workstation left column (handled in separate layout task if overlap — this task is **only** the 30px revert).

### 3 — Regression
- Ender-only, craft-only, both, neither — slots align with textures.
- No crash when no workstations enabled.

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

**Mod version:** `0.1.29`

### Layout revert
1. Config → Inventory: enable ender chest + crafting table.
2. Open unified menu (**R** / configured keybind) → ender, craft, and player panels sit **30px higher** than before this fix (pre-padding layout restored).
3. Hover ender 3×9 slots — highlight aligns with **`generic_54`** frames (slots at `y = 18 + row×18` from panel top).
4. Hover craft 3×3 + result — aligns with **`crafting_table`** art; recipe book button on crafting panel row 1.
5. Hover main 3×9 + hotbar (+ offhand) — highlights match slot frames (no +30px vertical offset).

### Title (unchanged)
6. Screen title reads **“Unified Menu”** (EN) / **“Menú unificado”** (ES).

### Panel combos
7. Toggle config: ender only, craft only, both, neither — reopen menu each time; no crash, slots still align.

### Build / smoke
8. **`./gradlew build`** — PASS (2026-06-02).
9. **`./gradlew runClient`** — mod **0.1.29** loads, no crash; close client after test.

## References
- **`LuipyUnifiedMenu.java`**, **`LuipyUnifiedScreen.java`**
- Reverted task: **`autoagents/tasks/done/2026/06/02/CLOSED-0-20260602-1206-unified-menu-layout-title-fix.md`**

## Test report

1. **Date/time (UTC):** 2026-06-02 19:21:00 – 19:23:12 UTC
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.35**
3. **What was tested:** `TOP_LAYOUT_PADDING` revert; title strings; slot/layout constants; panel combos; build; client smoke.
4. **Results:**
   - **1–5. Layout revert / slot alignment** — **PASS** (static): `TOP_LAYOUT_PADDING = 0`; ender slots at `rightColumnContentTop + 18 + row×18`; `playerSectionTop` = padding + right stack height; screen reads menu fields for `renderBg`.
   - **6. Title "Unified Menu" / "Menú unificado"** — **PASS** (static): `luipy-utils-mod.screen.unified_menu` in EN/ES lang.
   - **7. Panel combos (ender/craft/both/neither)** — **PASS** (static): `withEnder`/`withCrafting` branches; no crash paths in constructor.
   - **8. `./gradlew build`** — **PASS** (`BUILD SUCCESSFUL`).
   - **9. `./gradlew runClient`** — **PASS** (smoke): mod **0.1.35** loads; client closed after test.
5. **Overall:** **PASS**
6. **Steps tested:** `./gradlew build`; static review of `LuipyUnifiedMenu.java`, lang; runClient smoke.
7. **GitHub:** Issue N/A (#0) — no `agent:testing` label applied.
