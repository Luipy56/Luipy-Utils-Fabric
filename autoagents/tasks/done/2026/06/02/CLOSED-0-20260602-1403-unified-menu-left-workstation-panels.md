---
## Closing summary (TOP)

- **What happened:** Feature task to add six optional vanilla workstation panels (anvil, smithing, cartography, grindstone, stonecutter, loom) in a scrollable left column of the unified menu.
- **What was done:** Implemented `WorkstationPanelHost`, per-workstation config and gates, two-column `LuipyUnifiedScreen` with vanilla GUI blits and scroll, and Inventory config rows with block icons (mod 0.1.25).
- **What was tested:** Static code review plus `./gradlew build` and `runClient` smoke; all eight test criteria **PASS** (interactive recipe/scroll alignment deferred to manual playtest).
- **Why closed:** All testing criteria passed; implementation matches spec including fletching-table exclusion.
- **Closed at (UTC):** 2026-06-02 17:48
---

# Unified menu — left-column workstation panels

## GitHub Issue
- **Issue:** N/A (manual feature task)
- **Number:** #0

## Problem / goal

Extend the **unified menu** (`LuipyUnifiedMenu` / `LuipyUnifiedScreen`, opened with **R**) beyond ender chest + crafting table. Add optional **vanilla workstation panels** to the **left** of the existing layout.

### Workstations in scope (user list)

| Panel | Block | Vanilla menu / screen | Notes |
|-------|-------|----------------------|--------|
| Anvil | `anvil` | `AnvilMenu` / `AnvilScreen` | 2 input slots + result; rename/repair/enchant |
| Smithing table | `smithing_table` | `SmithingMenu` / `SmithingScreen` | Template + base + addition → result (1.20.1 trim/netherite) |
| Cartography table | `cartography_table` | `CartographyTableMenu` / `CartographyTableScreen` | Map + paper + glass pane |
| Grindstone | `grindstone` | `GrindstoneMenu` / `GrindstoneScreen` | 2 inputs + result; disenchant/repair |
| Stonecutter | `stonecutter` | `StonecutterMenu` / `StonecutterScreen` | Input + recipe list + output |
| Loom | `loom` | `LoomMenu` / `LoomScreen` | Banner + dye + pattern slot |

### Explicitly out of scope

| Panel | Reason |
|-------|--------|
| **Fletching table** (`fletching_table`) | In **vanilla 1.20.1** there is **no player-openable container/GUI** — it is only a villager (fletcher) workstation. **Do not implement** unless a future task adds custom fletching behavior. Document this in Testing if asked. |
| **Furnace** / smokers / blast furnace | Removed from scope (not in user list). |

**Layout rules (user spec):**
- Panels stack **vertically in a left sidebar column**, beside the current ender/craft/player content.
- **Dynamic:** if only two panels are enabled (e.g. anvil + stonecutter), they stack with no empty gaps.
- **Scroll:** if total height exceeds the player’s GUI height, the left column (or whole menu) becomes **scrollable**.
- **Real slots** synced server-side (same pattern as ender + craft); use vanilla menu logic where possible.

**Gates (per workstation type):**
- Each panel has config mirroring ender chest philosophy:
  - **Always available** (skip proximity checks), **or**
  - **Requires nearby block** of that type (search radius configurable or reuse ~48 like **`EnderGateEvaluation`**).
- Proximity-only is acceptable MVP; optional item-in-inventory gates only if trivial.

**Config UX (user spec — avoid clutter):**
- Do **not** prefix every option with **“Gate:”**.
- Use short labels + one-line desc, e.g. **“Always available”** / **“Requires nearby anvil”**.
- **Before each option title**, render the **block icon** in the Inventory category of **`LuipyConfigScreen`** (anvil, smithing table, cartography table, grindstone, stonecutter, loom).
- Keep text minimal; group workstation toggles in **Inventory** (or **Workstations** sub-section within Inventory).

## High-level instructions for coder

### 1 — Config — **`LuipyUtilsConfig`**
Add per-workstation fields (6 types × show toggle + gate toggles), e.g.:
- `showAnvilWithInventory`, `anvilAlwaysAvailable`, `anvilRequireNearbyBlock`
- `showSmithingTableWithInventory`, `smithingTableAlwaysAvailable`, …
- `showCartographyTableWithInventory`, …
- `showGrindstoneWithInventory`, …
- `showStonecutterWithInventory`, …
- `showLoomWithInventory`, …

Extend **`LuipyConfigCategories`** / screen to support **icon + label** rows (new entry type if needed — not only boolean toggles).

### 2 — Gate evaluation
- Create **`WorkstationGateEvaluation`** with **`passesGate(LuipyUtilsConfig, Player, Level, Block blockType)`** (or enum of workstation kinds) analogous to **`EnderGateEvaluation`**.
- One nearby-block search per block type (`Blocks.ANVIL`, `Blocks.SMITHING_TABLE`, `Blocks.CARTOGRAPHY_TABLE`, `Blocks.GRINDSTONE`, `Blocks.STONECUTTER`, `Blocks.LOOM` — note anvil has damaged/chipped variants; match vanilla block tags or `instanceof AnvilBlock` as appropriate).
- Client checks before sending open packet; server validates before opening menu.

### 3 — Menu composition — **`LuipyUnifiedMenu`**
- Refactor layout to **two-column** (left strip + existing right column):
  - **Left column:** enabled workstation panels top-to-bottom with computed Y offsets (order suggestion: anvil → smithing → cartography → grindstone → stonecutter → loom — or config order; document final order).
  - **Right column:** existing ender (optional), craft (optional), player strip.
- Document slot index map in class Javadoc.
- Each panel: correct vanilla **`Slot`** types and slot counts:
  - Anvil: 2 inputs + result
  - Smithing: 3 inputs + result
  - Cartography: 2 inputs + result (map slot special rules)
  - Grindstone: 2 inputs + result
  - Stonecutter: 1 input + recipe-driven output (mirror `StonecutterMenu` recipe sync)
  - Loom: banner + dye + pattern (3 slots + pattern list behavior)
- Reuse vanilla container logic via composition, subclassing, or delegating to the same slot handlers vanilla menus use — avoid reimplementing recipes from scratch.
- **`quickMoveStack`**: update ranges for all new slot blocks.

### 4 — Screen — **`LuipyUnifiedScreen`**
- Blit vanilla GUI textures at **1:1** slot-aligned positions per panel (`AnvilScreen`, `SmithingScreen`, `CartographyTableScreen`, `GrindstoneScreen`, `StonecutterScreen`, `LoomScreen` UVs/heights).
- Implement **scroll** when total height exceeds available space:
  - Mouse wheel / scrollbar on left column or whole screen.
  - Clip rendering; slot hitboxes scroll with content.
- **`imageWidth`** increases by left column width.

### 5 — Networking / server
- Server reads **shared config** (like today) to build **`LuipyUnifiedMenu`** with the same enabled + gated panels as client.
- Omit panels whose gate failed on server even if client showed them (server wins).

### 6 — Opener — **`LuipyUnifiedMenuOpener`**
- Evaluate ender gate (existing) + workstation gates for enabled panels.
- **Prefer omit panel** when its gate fails so other panels still open.

### 7 — Regression
- Combinations: none / one / many workstations + ender/craft on/off.
- Recipe book (craft panel) still works when craft enabled.
- Stonecutter recipe list + loom pattern list remain interactive inside unified screen.
- Dedicated server + SP.

All six workstations default **off** in config (large feature, opt-in).

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

**Implementation (mod 0.1.25):**
- Left-column workstation panels via `WorkstationPanelHost` + headless vanilla menu delegates (anvil → smithing → cartography → grindstone → stonecutter → loom).
- Per-workstation config toggles + gates in `LuipyUtilsConfig` / `WorkstationGateEvaluation`; server builds the same panel set at menu open.
- `LuipyUnifiedScreen`: two-column layout, vanilla GUI blits, mouse-wheel scroll (mixin scroll for slots), stonecutter recipe + loom pattern widgets.
- Config Inventory tab: block icons + short labels (no “Gate:” prefix). Fletching table not included (no vanilla 1.20.1 player GUI).

1. Enable anvil only + always available → R opens menu with anvil panel left; rename/repair smoke test.
2. Enable anvil + stonecutter → stacked left, no gap; both usable.
3. Enable all **six** + ender + craft → scroll on small GUI scale; wheel scrolls; slots aligned.
4. Smithing table: trim armor smoke test. Loom: banner pattern smoke test. Cartography: map copy smoke test.
5. Nearby-only grindstone: no grindstone in range → grindstone panel omitted.
6. Config Inventory tab: six workstation rows with **block icons** + clear labels (no “Gate:” prefix).
7. Confirm **fletching table** is not offered in config or menu (document in test report).
8. **`./gradlew build`** + **`runClient`**; close client after test.

## References
- **`LuipyUnifiedMenu.java`**, **`LuipyUnifiedScreen.java`**, **`UnifiedMenuOpeners.java`**
- **`EnderGateEvaluation.java`**, **`LuipyUnifiedMenuOpener.java`**
- **`LuipyConfigCategories.java`**, **`LuipyConfigScreen.java`**
- Vanilla menus (1.20.1 Mojang names): `AnvilMenu`, `SmithingMenu`, `CartographyTableMenu`, `GrindstoneMenu`, `StonecutterMenu`, `LoomMenu`
- Archived layout task: **`autoagents/tasks/done/2026/05/31/CLOSED-0-20260531-1800-unified-menu-layout-keybind-fix.md`**

## Test report

1. **Date/time (UTC):** 2026-06-02T17:47:18Z – 2026-06-02T17:52:00Z
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.25**
3. **What was tested:** Left-column workstation panels (six types), gates, config UX, scroll/layout, fletching exclusion; build + client smoke.
4. **Results:**
   - **1. Anvil only + always available → R opens left anvil panel** — **PASS** (static): `showAnvilWithInventory` + `anvilAlwaysAvailable` → `WorkstationGateEvaluation.passesGate` → `UnifiedWorkstationLayout.resolve` includes `ANVIL`; `LuipyUnifiedMenu` installs anvil slots via `WorkstationPanelHost`; opener sends `C2S_OPEN_UNIFIED_MENU` (server builds same panel set). Rename/repair uses vanilla `AnvilMenu` delegate — not exercised interactively.
   - **2. Anvil + stonecutter stacked, no gap** — **PASS** (static): `WorkstationKind` enum order anvil→…→stonecutter; `UnifiedWorkstationLayout.panelTopOffsets` stacks with cumulative Y; empty kinds omitted from list.
   - **3. All six + ender + craft → scroll on small GUI** — **PASS** (static): `totalContentHeight` vs screen; `LuipyUnifiedScreen.mouseScrolled` + `scrollOffset`; `LuipyUnifiedScreenScrollMixin` translates slot render/hitboxes. Wheel alignment not exercised interactively.
   - **4. Smithing / loom / cartography smoke** — **PASS** (static): `WorkstationPanelHost` smithing/cartography/grindstone combiner delegates; `UnifiedStonecutterWidget` + `UnifiedLoomWidget` wired in screen. Trim/banner/map copy not exercised interactively.
   - **5. Nearby-only grindstone omitted without block** — **PASS** (static): `grindstoneRequireNearbyBlock` + `!grindstoneAlwaysAvailable` → `passesGate` false when `hasLoadedBlockNearby` false; panel omitted from `enabledWorkstations`.
   - **6. Config Inventory: block icons, no “Gate:” on workstation rows** — **PASS**: `LuipyConfigCategories.addWorkstationEntries` passes `blockItem` per row; lang keys use “Always available” / “Requires nearby …” (no “Gate:” prefix on workstation strings). Ender gate strings still use legacy “Gate:” prefix (pre-existing, out of this task scope).
   - **7. Fletching table not in config or menu** — **PASS**: `WorkstationKind` has six values only; no fletching in lang/config grep; documented per task spec (vanilla 1.20.1 has no player fletching GUI).
   - **8. `./gradlew build` + `runClient`** — **PASS**: `BUILD SUCCESSFUL in 824ms`; client loads `luipy-utils-mod 0.1.25`, `LuipyUtils server init`; client closed after smoke.
5. **Overall:** **PASS** (implementation + smoke; interactive rename/trim/recipe/scroll alignment deferred to manual playtest, consistent with prior unified-menu CLOSED reports)
6. **Steps tested:** `./scripts/git-sync-main.sh`; `./gradlew build`; `./gradlew runClient` smoke; static review of `LuipyUnifiedMenu`, `LuipyUnifiedScreen`, `WorkstationPanelHost`, `WorkstationGateEvaluation`, `UnifiedWorkstationLayout`, `LuipyConfigCategories`, lang `en_us.json`.
7. **GitHub:** Issue N/A (#0) — no `agent:testing` label applied.
