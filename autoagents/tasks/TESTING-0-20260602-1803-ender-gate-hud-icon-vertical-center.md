# Ender gate HUD icon — vertically center on hotbar

## GitHub Issue
- **Issue:** N/A (manual HUD polish)
- **Number:** #0

## Problem / goal

The **ender chest icon** left of the hotbar (`EnderGateHudIndicator`) is **anchored to the top** of the hotbar row. It should be **vertically centered** relative to the hotbar slot row (16px item height).

**Current code:** `HOTBAR_Y_OFFSET = 22` places the icon at a fixed Y without centering against hotbar geometry.

## High-level instructions for coder

File: **`src/client/java/com/luipy/utilsmod/client/ender/EnderGateHudIndicator.java`**
- Compute hotbar Y the same way vanilla does (or mirror known hotbar bottom offset).
- Render the 16×16 item icon so its **vertical center** aligns with the hotbar row center (typically `hotbarTop + (18 - 16) / 2` or equivalent depending on vanilla hotbar slot vs item render offsets).
- Keep horizontal position: left of hotbar with **`ICON_GAP`** unchanged unless centering fix requires a 1px tweak.
- Respect GUI scale; hide when **`hideGui`**, creative, gate fails (unchanged).

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

1. **`./gradlew build`** — PASS (verified by coder).
2. **`./gradlew runClient`** → main-menu smoke — PASS; client closed after load.
3. **SP survival**, master + ender panel enabled, gate passes, HUD toggle on → ender chest icon **vertically centered** with hotbar items (not hugging top edge of hotbar background).
4. **GUI scale 2 / 3 / 4** (Video Settings) — icon stays aligned with hotbar items at each scale.
5. **Gate fails** or **HUD toggle off** (`showEnderGateHudIndicator`) → icon hidden.
6. **Creative mode** or **F1 hide GUI** → icon hidden.
7. **mod_version** bumped to **0.1.30**.

### Implementation notes
- Hotbar item Y mirrors vanilla `Gui.renderHotbar`: `hotbarTop = screenHeight - 22`, item Y = `hotbarTop + (22 - 16) / 2` (= `screenHeight - 19`, same as hotbar slot items).
- Horizontal position unchanged (`ICON_GAP = 4` left of hotbar background).

## References
- **`EnderGateHudIndicator.java`**, **`EnderGateAccess.java`**
- Prior: **`autoagents/tasks/done/2026/06/02/CLOSED-0-20260602-1404-ender-gate-hud-indicator.md`**
