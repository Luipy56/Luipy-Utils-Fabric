# Config — ender chest block icon on Inventory rows

## GitHub Issue
- **Issue:** N/A (manual config UX polish)
- **Number:** #0

## Problem / goal

A recent task added **block icons** before workstation config rows in the Inventory tab (`LuipyConfigCategories.addWorkstationEntries` passes `blockItem` per row). The **ender chest** options were **not** updated and still render without an icon.

**Goal:** Show the **ender chest item/block icon** before the ender-related config rows, matching the workstation row style (icon + label + On/Off toggle).

## Rows that need the icon

At minimum:
- **`show_ender_with_inventory`** — “Ender chest panel in unified menu”
- **`show_ender_gate_hud`** — “Ender chest HUD indicator”

Optionally (same visual group — user said “enderchest” singular; apply icon to all ender-chest-themed rows if it reads well):
- **`always_virtual`**, **`require_item`**, **`require_block`** (gate toggles)

**Recommendation:** Icon on **`show_ender_with_inventory`** and **`show_ender_gate_hud`** for sure; use **`Items.ENDER_CHEST`** (or block item) consistently with workstation pattern.

## High-level instructions for coder

### 1 — Entry metadata
File: **`src/client/java/com/luipy/utilsmod/client/config/ui/LuipyConfigCategories.java`**
- Add `Items.ENDER_CHEST` (or equivalent) as `iconItem` on the relevant **`LuipyConfigBooleanEntry`** constructors — same overload used by workstation entries.

### 2 — Rendering
File: **`src/client/java/com/luipy/utilsmod/client/config/ui/LuipyConfigScreen.java`**
- Confirm Inventory tab label rendering already draws `entry.iconItem()` when non-null (workstation path). No duplicate icon logic unless Inventory non-workstation rows skip icons today — fix if needed.

### 3 — Regression
- Workstation icons unchanged.
- Crafting table row unchanged (out of scope unless user later asks).

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

**Implementation:** Added `Items.ENDER_CHEST` as `iconItem` on all five ender-themed Inventory rows (`show_ender_with_inventory`, `show_ender_gate_hud`, `always_virtual`, `require_item`, `require_block`). `LuipyConfigScreen` already renders `iconItem` for non-World tabs — no screen changes needed.

**Version:** `0.1.27`

1. Open config → **Inventory** tab → ender chest panel row shows **ender chest icon** left of title (aligned with anvil/smithing rows).
2. **Ender chest HUD indicator** row shows same icon style.
3. Gate toggles (`always_virtual`, `require_item`, `require_block`) also show ender chest icon.
4. Workstation rows still show their block icons.
5. Toggle rows still work (On/Off toggles change config as before).
6. **`./gradlew build`** passes; optional **`runClient`** visual check; close client after test.

## References
- **`LuipyConfigCategories.java`**, **`LuipyConfigBooleanEntry.java`**, **`LuipyConfigScreen.java`**
- Prior: **`autoagents/tasks/done/2026/06/02/CLOSED-0-20260602-1403-unified-menu-left-workstation-panels.md`**
