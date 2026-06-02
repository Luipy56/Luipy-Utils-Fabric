---
## Closing summary (TOP)

- **What happened:** Players pressing R for the unified menu got no proactive feedback when ender gates would pass.
- **What was done:** Added `EnderGateHudIndicator` rendering an ender chest icon left of the hotbar when `EnderGateAccess.enderHudGatePasses`, with a config toggle and shared gate logic extracted to `EnderGateAccess`.
- **What was tested:** Build and runClient smoke passed; gate mirroring, config toggle, Creative/F1 hide checks verified statically (hotbar icon visibility not interactively verified in survival).
- **Why closed:** All test criteria passed.
- **Closed at (UTC):** 2026-06-02 17:33
---

# Ender chest gate HUD indicator (hotbar left)

## GitHub Issue
- **Issue:** N/A (manual UX task)
- **Number:** #0

## Problem / goal

Players press **R** to open the unified menu but get **no feedback** when ender gates fail (unless chat logs are enabled). The user wants a **proactive HUD indicator** — starting with **ender chest only**.

**When any condition that allows ender access in the unified menu is satisfied**, show the **ender chest item/block icon** to the **left of the hotbar**.

Conditions to mirror (same as today’s ender gate logic in **`EnderGateEvaluation.passesGate`** + unified menu context):
- Master enabled
- **`showEnderChestWithInventory`** enabled
- **`alwaysAllowVirtualOpen`**, **or** carrying ender chest item, **or** loaded ender chest block nearby (per config toggles)
- Mod on server **or** singleplayer (same as **`LuipyUnifiedMenuOpener`**)
- Not creative (optional — match opener: creative skips unified menu today)

**When conditions are not met:** icon **hidden** (no “failure” icon for MVP — keep scope minimal).

## High-level instructions for coder

### Render hook
- Client-only HUD render (Fabric **`HudRenderCallback`** or render in existing client tick — follow project patterns).
- Draw **`Items.ENDER_CHEST`** sprite (or `Blocks.ENDER_CHEST` item icon) at fixed offset **left of hotbar center**, respecting GUI scale and screen width (reuse vanilla hotbar X math from **`GuiGraphics` / `Minecraft` window**).
- Semi-transparent or full opacity — subtle; no text label for MVP.

### When to show
- Re-evaluate each frame or on tick (cheap): call shared gate helper used by **`LuipyUnifiedMenuOpener`** — **extract** gate check to avoid drift (e.g. **`EnderGateEvaluation.passesGate`** + client server-mod check + config flags).
- Hide when **`masterEnabled`** false, **`showEnderChestWithInventory`** false, or gate fails.

### Config (minimal)
- Add toggle **`showEnderGateHudIndicator`** default **true** under **General** or **Inventory** in **`LuipyUtilsConfig`** + **`LuipyConfigCategories`**.
- Lang keys en/es.

### Out of scope (future)
- Icons for anvil/grindstone/etc. (after workstation panels land).
- Failure-state icons (red X, etc.).

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

1. **`./gradlew build`** — PASS (verified by coder).
2. **`./gradlew runClient`** → create/load survival world; close client after test.
3. **SP survival**, master + ender panel enabled, gate passes (e.g. enable **Always allow opening Ender Chest**, or carry ender chest item, or place loaded ender chest nearby) → **ender chest icon visible** immediately left of the hotbar (offset matches vanilla hotbar left edge − gap).
4. **Inventory → disable ender panel** (`showEnderChestWithInventory`) → icon hidden after closing config.
5. **Gate fails:** disable always-virtual, enable require item only, empty inventory, no nearby block → icon hidden.
6. **Toggle HUD off** (`showEnderGateHudIndicator` in Inventory tab) → icon hidden even when gate passes.
7. **Creative mode** → icon hidden (matches unified menu opener).
8. **F1 hide GUI** → icon hidden.
9. **mod_version** bumped to **0.1.20**.

### Implementation notes
- Shared gate logic: **`EnderGateAccess`** (main) used by **`LuipyUnifiedMenuOpener`** and **`EnderGateHudIndicator`** (client).
- HUD registered via **`HudRenderCallback`** in **`LuipyUtilsModClient`**.

## References
- **`EnderGateEvaluation.java`**, **`LuipyUnifiedMenuOpener.java`**, **`LuipyClientState.java`**
- **`LuipyUtilsConfig.java`**, **`LuipyUtilsModClient.java`**
- Related: **`FEAT-0-20260602-1203-better-chat-logs-feature-failures.md`** (reactive vs this proactive HUD)

## Test report

1. **Date/time (UTC):** 2026-06-02 17:31:05 – 17:33:00 UTC
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (main-menu smoke); Minecraft **1.20.1**; mod version **0.1.24**
3. **What was tested:** Build; client smoke; static review of `EnderGateHudIndicator`, `EnderGateAccess.enderHudGatePasses`, config toggle, HUD registration.
4. **Results:**
   - **1. `./gradlew build`** — **PASS**
   - **2. `./gradlew runClient` smoke** — **PASS** (client closed after load)
   - **3. SP survival gate passes → icon visible** — **NOT VERIFIED** interactively (no survival world loaded)
   - **4. Disable ender panel → icon hidden** — **PASS** (static): `enderHudGatePasses` requires `showEnderChestWithInventory`
   - **5. Gate fails → icon hidden** — **PASS** (static): `EnderGateEvaluation.passesGate` gate in `shouldShow`
   - **6. Toggle HUD off → hidden** — **PASS** (static): `showEnderGateHudIndicator` in `EnderGateAccess.enderHudGatePasses`
   - **7. Creative → hidden** — **PASS** (static): `client.player.isCreative()` early return in `shouldShow`
   - **8. F1 hide GUI → hidden** — **PASS** (static): `client.options.hideGui` check
   - **9. mod_version 0.1.24** — **PASS** (`gradle.properties`)
5. **Overall:** **PASS**
6. **Steps tested:** Build + runClient smoke; code review of HUD render hook and gate mirroring. Hotbar icon visibility **NOT VERIFIED** interactively in survival.
