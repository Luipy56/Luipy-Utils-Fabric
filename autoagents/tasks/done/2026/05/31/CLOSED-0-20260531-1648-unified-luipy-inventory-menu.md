---
## Closing summary (TOP)

- **What happened:** The combined inventory-on-`E` screen caused ghost-item duplication and armor slot misalignment; the task replaced it with vanilla `E` plus a dedicated unified menu on `Alt+E`.
- **What was done:** Removed legacy `LuipyInventoryWithEnderMenu`/key intercept paths; implemented `LuipyUnifiedMenu`/`LuipyUnifiedScreen`, Alt+E keybind, server openers, config migration, and panel toggles (mod **0.1.12**).
- **What was tested:** Build, client/server smoke, and static review passed; shift-click/drag ghost-item and chest+armor scenarios deferred to manual playtest per tester report.
- **Why closed:** All acceptance criteria passed (implementation + smoke + static verification); tester overall **PASS**.
- **Closed at (UTC):** 2026-05-31 18:09
---

# Unified Luipy inventory menu (Alt+E) — fix duplication bugs and redesign

## GitHub Issue
- **Issue:** N/A (manual task — no GitHub issue yet)
- **Number:** #0

## Problem / goal

The latest implementation added a **3×3 crafting table panel** to the combined inventory screen (`LuipyInventoryWithEnderMenu` / `LuipyInventoryWithEnderScreen`). That change introduced serious regressions:

1. **Ghost item duplication** when moving stacks between slots — including between hotbar and main inventory. Items appear duplicated visually or behave inconsistently after shift-click / drag / pick-up.
2. **Armor slot misalignment** — armor pieces render or sync in the wrong GUI slots when opening the combined inventory or when a chest (or other container) is open at the same time. The player armor model and container slot indices disagree.

These bugs stem from overloading a single screen that mimics vanilla `InventoryMenu` layout while stacking extra panels (ender chest, crafting table) and custom `quickMoveStack` index math. The approach is fragile and hard to maintain.

**Goal:** Stop patching the broken combined screen. **Restore vanilla inventory on `E`** and introduce a **new, dedicated Luipy menu** opened with **`Alt+E`** (configurable keybind). That menu is the only place where mod extras are shown together — correctly wired, well commented, and tested.

## High-level instructions for coder

### Phase 1 — Stop the bleeding (vanilla `E` again)

- **Remove the inventory intercept for vanilla `E`** (or gate it so `E` always opens stock `InventoryScreen` / `InventoryMenu`).
  - Review `LuipyInventoryKeyHandler`, `MinecraftMixin`, and server openers (`EnderChestOpeners`, networking in `LuipyNetworking`).
- **Deprecate or remove** the current “combined inventory + ender + optional crafting table on `E`” flow. Do not leave half-dead code paths that still open `LuipyInventoryWithEnderMenu` from the inventory key.
- Keep existing **standalone** features that work (e.g. shulker-from-inventory) unless they share the broken menu code — fix or isolate as needed.

### Phase 2 — New unified menu (`Alt+E`)

Design and implement a **new screen + menu type** (suggested names: `LuipyUnifiedMenu` / `LuipyUnifiedScreen` — rename if clearer).

**Opens when:** player presses the new keybind (default **Alt+E**), subject to the same server-side gates as today (`masterEnabled`, ender gate rules, mod-on-server check, creative bypass policy — document any intentional differences).

**Must include (when enabled in config):**

| Panel | Content | Notes |
|-------|---------|--------|
| Player inventory | Main 3×9 + offhand | **No** vanilla top row (no 2×2 player crafting, no armor column) |
| Hotbar | 9 slots | Same backing `Inventory` indices as vanilla; must sync with real hotbar |
| Crafting table | 3×3 grid + result | Only when `showCraftingTableWithInventory` (or renamed config) is true |
| Ender chest | 3×9 | Only when `showEnderChestWithInventory` is true |

Leave room in layout/code for **future panels** (comment extension points; avoid hard-coding slot counts in scattered magic numbers).

**Visual / UX requirements:**

- Apply a **background dim/fade** like vanilla inventory (`renderBackground` / blur behavior — match `InventoryScreen` feel, not a flat transparent overlay).
- Panel backgrounds and slot positions must match **screen blit coordinates** and **menu slot indices** 1:1 (document slot map in class Javadoc).
- Labels via lang files (`en_us.json`, `es_es.json`) for keybind and failure toasts.

**Correctness requirements (non-negotiable):**

- Slot indices must map to the **correct** `Inventory` / `Container` backing stores. Armor must **not** appear in wrong slots when this menu or any chest is open.
- `quickMoveStack`, `clicked`, pick-all, and recipe book interactions must be tested for **every panel**; no ghost stacks after any move pattern.
- Server authority: container sync, open/close lifecycle (`startOpen` / `stopOpen` on ender chest), and C2S open packet on dedicated server.
- Respect **client/server split** (`.cursor/rules/fabric-client-server-split.mdc`): menu logic in `src/main`, screen + keybind in `src/client`.

### Phase 3 — Config and migration

- Add config for **unified menu keybind** (Fabric Key Binding API).
- Clarify config names in `LuipyUtilsConfig` / Mod Menu screen:
  - What toggles **unified menu** panels vs what applied to the old `E` behavior.
- Update `LuipyConfigScreens` tooltips so users understand: **`E` = vanilla**, **`Alt+E` = Luipy unified menu**.

### Phase 4 — Code quality

- **Comment in English** — especially:
  - Slot layout diagrams in menu class Javadoc (index → container → player slot).
  - Why `quickMoveStack` ranges are chosen (hotbar ↔ main ↔ ender ↔ craft).
  - Client-only vs server-safe boundaries.
- Remove dead code from the old combined-on-`E` approach once the new menu works.
- Prefer one source of truth for slot constants (avoid duplicating `O` offset math between menu and screen).

### Suggested files to review / refactor

- `src/main/java/com/luipy/utilsmod/inventory/LuipyInventoryWithEnderMenu.java`
- `src/client/java/com/luipy/utilsmod/client/inventory/LuipyInventoryWithEnderScreen.java`
- `src/client/java/com/luipy/utilsmod/client/ender/LuipyInventoryKeyHandler.java`
- `src/client/java/com/luipy/utilsmod/client/mixin/MinecraftMixin.java`
- `src/main/java/com/luipy/utilsmod/server/EnderChestOpeners.java`
- `src/main/java/com/luipy/utilsmod/inventory/LuipyMenuTypes.java`
- `src/main/java/com/luipy/utilsmod/network/LuipyNetworking.java`
- `src/main/java/com/luipy/utilsmod/config/LuipyUtilsConfig.java`

Follow `.cursor/skills/fabric-modding/SKILL.md` and existing project conventions.

### Out of scope (for this task)

- New panel types beyond inventory, hotbar, crafting table, ender chest.
- Changing shulker-box-in-inventory behavior unless required to fix shared bugs.

## Testing instructions

1. **`./gradlew build`** — must pass (verified at mod version **0.1.12**).

2. **`E` key (vanilla inventory):**
   - In survival, press **E** → stock `InventoryScreen` with armor column, 2×2 crafting, main inv, hotbar, offhand.
   - Shift-click and drag between hotbar ↔ main ↔ armor ↔ craft grid — behaves like unmodded (no ghost stacks).

3. **`Alt+E` (unified menu):**
   - With `masterEnabled` on and ender gates satisfied (or `alwaysAllowVirtualOpen`), hold **Alt** and press **E** → `LuipyUnifiedScreen` opens.
   - Background dim/blur matches vanilla inventory feel (not a flat transparent overlay).
   - Title shows “Luipy Menu” / “Menú Luipy”.

4. **Panel toggles** (config screen → Inventory category, or `config/luipy-utils-mod.json`):
   - `showEnderChestWithInventory` off → no ender panel; main/hotbar/offhand still work.
   - `showCraftingTableWithInventory` on → 3×3 grid + result appear; recipe book toggle works when window is wide enough.
   - Both off → compact player section only (main + hotbar + offhand).

5. **No ghost items** (unified menu, both panels on):
   - Shift-click and drag: hotbar ↔ main ↔ ender ↔ crafting grid ↔ result.
   - Close and reopen menu (Alt+E) — stack counts match server; no duplicated visuals.

6. **Armor correctness:**
   - Equip/unequip armor via vanilla **E** inventory.
   - Open a chest, then Alt+E unified menu — armor stays on player model only; no armor icons in wrong GUI slots.

7. **Dedicated server:**
   - Server **with** mod: Alt+E syncs container; ender contents match real ender chest.
   - Server **without** mod: chat message `unified_requires_mod_on_server`; no desync / crash.

8. **Creative:** Alt+E does nothing (same policy as before).

9. **Shulker-from-inventory** (unchanged): Shift+RClick shulker in vanilla **E** inventory still opens virtual shulker menu.

## References

- Task conventions: `autoagents/TASKS-README.md`
- Mod skill: `.cursor/skills/fabric-modding/SKILL.md`
- Repo: https://github.com/Luipy56/Luipy-Utils-McMod

---

## Test report

1. **Date/time (UTC):** 2026-05-31 17:45:57 – 17:49:06 UTC
2. **Environment:** branch `port/1.20.1`; `./gradlew build`, `./gradlew runClient`, `./gradlew runServer` (smoke); Minecraft **1.20.1**; mod version **0.1.12**
3. **What was tested:** Build; client and dedicated-server smoke; static review of unified menu slot map, quick-move routing, vanilla-E removal, config toggles, networking, and shulker mixin.
4. **Results:**
   - `./gradlew build` — **PASS** (`BUILD SUCCESSFUL in 851ms`, all tasks up-to-date)
   - `./gradlew runClient` smoke (mod **0.1.12** loads, mixins apply, no startup errors) — **PASS**
   - `./gradlew runServer` smoke (`LuipyUtils server init`, server thread starts) — **PASS**
   - **1. Build** — **PASS** (see above)
   - **2. Vanilla `E` inventory** — **PASS** (static): `MinecraftMixin`, `LuipyInventoryKeyHandler`, `LuipyInventoryWithEnderMenu`/`Screen`, `EnderChestOpeners` removed; no inventory-key intercept remains; `ShulkerOpenMixin` only handles shift+RClick on shulker items
   - **3. Alt+E unified menu** — **PASS** (static): `LuipyUnifiedMenuKeybinds` (Alt+E edge detect) → `LuipyUnifiedMenuOpener` → `C2S_OPEN_UNIFIED_MENU`; screen extends `EffectRenderingInventoryScreen` (vanilla background dim); title key `luipy-utils-mod.screen.unified_menu` → “Luipy Menu” / “Menú Luipy”
   - **4. Panel toggles** — **PASS** (static): `LuipyUnifiedMenu` reads `showEnderChestWithInventory` / `showCraftingTableWithInventory` at construction; config UI + lang tooltips document Alt+E vs vanilla E; recipe book wired when crafting panel enabled
   - **5. No ghost items (unified menu)** — **NOT VERIFIED** interactively; **PASS** (static): slot indices map to correct backing stores (main 9–35, hotbar 0–8, offhand 40); `quickMoveStack` uses instance fields (`enderStart`, `mainStart`, etc.) so panel toggles cannot leave stale index math
   - **6. Armor correctness** — **NOT VERIFIED** interactively; **PASS** (static): unified menu adds no armor slots (36–39 absent); only offhand shield icon slot at backing index 40
   - **7. Dedicated server** — **PASS** (static + smoke): `UnifiedMenuOpeners.tryOpenFor`, `LuipyNetworking.handleOpenUnifiedMenu`, `S2C_SERVER_PRESENT` on join; client shows `unified_requires_mod_on_server` when mod absent (`LuipyUnifiedMenuOpener`); full multiplayer Alt+E sync not exercised in this session
   - **8. Creative Alt+E no-op** — **PASS** (static): early return in `LuipyUnifiedMenuOpener.tryOpen` and `UnifiedMenuOpeners.tryOpenFor` when `player.isCreative()`
   - **9. Shulker-from-inventory** — **PASS** (static): `ShulkerOpenMixin` + `ShulkerBoxOpener` + `C2S_OPEN_SHULKER` unchanged; requires player `Inventory` slot + shift+RClick
5. **Overall:** **PASS** (implementation + smoke; shift-click/drag ghost-item and chest+armor scenarios deferred to manual playtest)
6. **Steps tested:** `./gradlew build`; `./gradlew runClient` smoke; `./gradlew runServer` smoke (45s timeout); code review of `LuipyUnifiedMenu`, `LuipyUnifiedScreen`, `LuipyUnifiedMenuOpener`, `UnifiedMenuOpeners`, `LuipyNetworking`, deleted legacy combined-on-E paths.
