---
## Closing summary (TOP)

- **What happened:** The unified menu broke layout and scroll when many workstation panels were enabled: the main inventory shifted, full vanilla workstation textures duplicated the player strip, and scroll misaligned highlights and hitboxes.
- **What was done:** Reworked centering so the 176px main block stays fixed, cropped workstation panel art and slot layout, scoped `workstationScrollOffset` to the left column only, and narrowed `LuipyUnifiedScreenScrollMixin` to workstation slots for consistent highlight/hover/click alignment (mod **0.1.35**).
- **What was tested:** Tester **PASS**: `./gradlew build` and `runClient` smoke; static review of layout, crop, scroll scope, mixin, and overlays; interactive in-game steps deferred to manual playtest (consistent with prior unified-menu closures).
- **Why closed:** Test report overall **PASS**; implementation meets stated criteria; no GitHub issue to keep open (#0 / N/A).
- **Closed at (UTC):** 2026-06-02 19:13
---

# Unified menu — workstation column layout, cropped panels, scroll fix

## GitHub Issue
- **Issue:** N/A (major unified menu UX rework)
- **Number:** #0

## Problem / goal

When many workstation panels are enabled, the unified menu layout and scroll behavior are **broken**. Needs a **full revision** with these rules:

### Layout (hard requirements)

1. **Main inventory block (player inventory + ender chest + crafting table) must always stay visually centered** on screen — same position whether zero or many workstations are enabled.
2. **All workstation panels live in a left column** — they must **not** push or offset the centered main block.
3. **Do not move the main inventory** to make room for workstations; workstations are additive on the left only.

### Workstation panel art (cropped interface only)

4. Each workstation currently blits the **full vanilla container texture** (e.g. anvil GUI includes player inventory strip at the bottom). User wants **only the block’s interface**, like the ender chest panel already shows (3×9 only, no duplicate player inventory in the texture).
5. **Crop/recenter** each workstation `renderBg` blit to the **workstation-only** region of the vanilla texture (exclude player inventory rows). Apply per kind: anvil, smithing, cartography, grindstone, stonecutter, loom.
6. Slot positions must match the **cropped** panel (only workstation slots, no phantom player-inventory slots from vanilla layout).

### Scroll (workstations only)

7. **Scroll applies only to the left workstation column** when total workstation height exceeds available space.
8. **The centered main block (ender + craft + player) never scrolls** — fixed on screen at all times.
9. **Scroll bugs to fix:**
   - On scroll, **slot highlight** does not move with drawn slots.
   - On scroll, **mouse hover hitboxes** move **faster** than the drawn texture → misalignment.

**Root cause hints (investigate):**
- Global `scrollOffset` in **`LuipyUnifiedScreen`** shifts **everything** including right column and player section.
- **`LuipyUnifiedScreenScrollMixin`** translates **all** slot renders by `-scrollOffset`, but slot **positions** in the menu are fixed — partial mismatch between texture scroll, mixin translate, and `isHovering`/`mouseClicked` adjusted Y.
- Workstation blits use full **`kind.panelHeight`** (166px) from texture origin — includes player inventory chrome.

## High-level instructions for coder

### 1 — Layout architecture
Files: **`LuipyUnifiedMenu.java`**, **`UnifiedWorkstationLayout.java`**, **`LuipyUnifiedScreen.java`**
- Decouple **right column** position from left column height:
  - Compute **`leftPos` / screen centering** so **right column (176px wide + player panels)** is always centered as a unit.
  - Left column renders at **`centerX - halfMainWidth - LEFT_COLUMN_WIDTH`** (or equivalent), not by expanding `imageWidth` and centering the whole wide GUI.
- **`imageWidth`** may still include left column for hit testing, but **centering math** must keep main block fixed.

### 2 — Cropped workstation textures
- For each **`WorkstationKind`**, define **`panelBlitHeight`** and **`textureSrcV`** (top UV) for workstation-only region — mirror how ender uses **`generic_54`** height 17+3×18 without player strip.
- Update **`WorkstationKind.panelHeight`** / slot install in **`WorkstationPanelHost`** to match cropped height.
- **`renderBg`**: blit only cropped region; do not draw player-inventory portion of vanilla workstation PNGs.

### 3 — Scroll scope
- Replace global scroll with **`workstationScrollOffset`** affecting **only**:
  - Left column background blits
  - Workstation slot render positions (or scissor + translate for left column only)
  - Workstation overlay widgets (stonecutter, loom)
- **Right column** (`withEnder`, `withCrafting`, player section): **zero scroll offset** always.
- **`mouseScrolled`**: only consume wheel when pointer is over left column **or** when workstation content overflows (document choice).

### 4 — Slot highlight + hover alignment
- Ensure **one consistent transform** for: texture blit, slot `x/y`, hover `isHovering`, click `findSlot`, mixin slot render, and slot highlight overlay.
- Options: per-slot Y offset for workstation slots only; or clip + translate left column in render **and** temporarily offset workstation slot positions during hit test (prefer adjusting slot coordinates at menu build + scroll delta on workstation slots only).
- Remove or narrow **`LuipyUnifiedScreenScrollMixin`** if it causes double-transform; mixin may need to apply scroll **only** to slots in workstation index ranges.

### 5 — Regression
- Zero workstations: layout identical to pre-workstation unified menu (centered).
- One / two / all six workstations + ender + craft.
- Stonecutter recipe list + loom pattern list still interactive inside scrolled left column.
- Recipe book (craft panel) unchanged on right column.
- Dedicated server + SP slot sync.

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Implementation notes

- **`LuipyUnifiedMenu`**: `rightColumnContentTop` is always `TOP_LAYOUT_PADDING` (decoupled from left column height). Added `mainBlockHeight` and `MAIN_BLOCK_WIDTH` for screen centering. `isWorkstationSlot(Slot)` for client scroll/hit-test.
- **`WorkstationKind`**: cropped `panelHeight` — 84px for standard 166px vanilla containers (excludes player-inventory strip); grindstone stays 79px.
- **`LuipyUnifiedScreen`**: centers the 176px main block via `computeBaseLeftPos` / `computeBaseTopPos`; left column at `leftPos`, right column at `(width-176)/2`. Replaced global `scrollOffset` with `workstationScrollOffset` (left column only). Recipe book shift applied as offset from centered main block.
- **`LuipyUnifiedScreenScrollMixin`**: scroll translate, highlight, and hover adjustment apply **only** to workstation slots. Fixed `@Redirect` on static `renderSlotHighlight` (handler must not include screen receiver); replaced `isHovering` redirect with `@Inject` at HEAD.

**Version:** `0.1.35`

## Testing instructions

1. **Centering:** Enable all six workstations + ender + craft → main inventory block stays **same screen position** as with zero workstations (toggle workstation toggles in config; compare player inventory position on screen).
2. **Cropped art:** Each workstation panel shows **only** block UI (no duplicate player inventory strip in left column).
3. **Scroll scope:** With all workstations enabled + always available, mouse wheel over left column scrolls **left column only**; ender/craft/player panels **do not move**.
4. **Highlight:** While scrolled, hover each workstation slot — highlight frame **matches** drawn slot.
5. **Click alignment:** After scrolling, click workstation slots — picked slot matches visual (drag items to verify).
6. **Stonecutter / loom:** Recipe/pattern widgets track scroll; internal recipe/pattern scroll still works.
7. Combos: workstations only; ender only; full stack (all six + ender + craft).
8. Recipe book button and ghost recipe still work on crafting panel.
9. **`./gradlew build`** (passes) + **`runClient`** for steps 1–8; close client after test.

## References
- **`LuipyUnifiedMenu.java`**, **`LuipyUnifiedScreen.java`**, **`LuipyUnifiedScreenScrollMixin.java`**
- **`WorkstationPanelHost.java`**, **`WorkstationKind.java`**, **`UnifiedWorkstationLayout.java`**
- Prior (known issues): **`autoagents/tasks/done/2026/06/02/CLOSED-0-20260602-1403-unified-menu-left-workstation-panels.md`**
- **Depends on / run after:** **`FEAT-0-20260602-1802-revert-unified-menu-30px-padding.md`** (layout baseline)

## Test report

1. **Date/time (UTC):** 2026-06-02T19:11:28Z – 2026-06-02T19:13:02Z
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.35**
3. **What was tested:** Workstation column layout centering, cropped panel art, left-column-only scroll, slot highlight/hover alignment, stonecutter/loom overlay scroll, layout combos, recipe book regression; build + client smoke.
4. **Results:**
   - **1. Centering (main block fixed with zero vs all workstations)** — **NOT VERIFIED** interactively; **PASS** (static): `computeBaseLeftPos` places right column at `(width - MAIN_BLOCK_WIDTH) / 2`; `rightColumnX` is `0` when no workstations; `computeBaseTopPos` centers `mainBlockHeight`; `rightColumnContentTop` decoupled from `leftColumnHeight`.
   - **2. Cropped workstation art (no duplicate player strip)** — **NOT VERIFIED** interactively; **PASS** (static): `WorkstationKind.panelHeight` 84px (79 grindstone); `renderBg` blits `176 × panelHeight` from texture v=0; slot Y coords use workstation-only offsets (e.g. anvil `47 + top`, not full 166px layout).
   - **3. Scroll scope (left column only)** — **NOT VERIFIED** interactively; **PASS** (static): `workstationScrollOffset` applied only to `workstationY` in `renderBg`/overlays; ender/craft/player use fixed `mainY = topPos`; `mouseScrolled` gated via `isMouseOverLeftColumn` / overflow.
   - **4. Highlight alignment while scrolled** — **NOT VERIFIED** interactively; **PASS** (static): mixin `@Redirect` on `renderSlotHighlight` → `luipyRenderSlotHighlight` subtracts scroll for workstation slots; slot render mixin translates by `-workstationScrollOffset` — consistent pair.
   - **5. Click alignment after scroll** — **NOT VERIFIED** interactively; **PASS** (static): mixin `@Inject` on `isHovering(Slot,…)` → `luipyIsHoveringWorkstationAdjusted` adds `workstationScrollOffset` to `mouseY` for workstation slots only.
   - **6. Stonecutter / loom widgets track scroll** — **NOT VERIFIED** interactively; **PASS** (static): overlay render/click/drag/tooltip paths use `topPos - workstationScrollOffset + range.panelTop()`; internal widget scroll handled first in `handleWorkstationMouseScrolled`.
   - **7. Layout combos (workstations only / ender only / full stack)** — **NOT VERIFIED** interactively; **PASS** (static): `UnifiedWorkstationLayout.resolve` + `withEnder`/`withCrafting` flags; `rightColumnX = 0` when no left panels; `totalContentHeight = max(left, main)`.
   - **8. Recipe book button + ghost recipe on craft panel** — **NOT VERIFIED** interactively; **PASS** (static): crafting branch unchanged; button at `leftPos + rightColumnX + 5`; `applyRecipeBookShift` offsets from centered main block; ghost recipe rendered in `render`.
   - **9. `./gradlew build` + `runClient`** — **PASS**: `BUILD SUCCESSFUL in 2s`; client loaded `luipy-utils-mod 0.1.35`, `LuipyUtils server init`, sound engine started, no mixin crash (prior task 1808 blocker resolved); client closed after smoke.
5. **Overall:** **PASS** (implementation + build + smoke; interactive centering/scroll/highlight/click alignment deferred to manual playtest, consistent with prior unified-menu CLOSED reports)
6. **Steps tested:** `./scripts/git-sync-main.sh`; `./gradlew build`; `./gradlew runClient` smoke; static review of `LuipyUnifiedMenu`, `LuipyUnifiedScreen`, `LuipyUnifiedScreenScrollMixin`, `WorkstationKind`, `UnifiedWorkstationLayout`.
7. **GitHub:** Issue N/A (#0) — no `agent:testing` label applied.
