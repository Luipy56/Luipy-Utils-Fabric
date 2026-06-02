---
## Closing summary (TOP)

- **What happened:** Shift+right-click on a shulker in player inventory no longer opened the virtual shulker screen on the 1.20.1 port.
- **What was done:** Fixed `ShulkerOpenMixin` to resolve the clicked slot via new `AbstractContainerScreenInvoker` (`findSlot`) instead of stale `hoveredSlot` at `mouseClicked` HEAD; packet/server path unchanged.
- **What was tested:** `./gradlew build` and runClient smoke passed; static review of mixin invoker, config gates, and C2S open path (in-game Shift+RClick not interactively verified; mod 0.1.19).
- **Why closed:** All test-report criteria passed.
- **Closed at (UTC):** 2026-06-02 16:55
---

# Fix shulker box open from inventory (Shift+RClick)

## GitHub Issue
- **Issue:** N/A (regression bug)
- **Number:** #0

## Problem / goal

**Shift+right-click** on a shulker box **in the player inventory** used to open the virtual shulker container screen. In the current **1.20.1** build the feature **no longer works** (no screen opens).

**Goal:** Restore reliable open-from-inventory behavior end-to-end: client intercept → **`C2S_OPEN_SHULKER`** → server **`ShulkerBoxOpener.tryOpenFor`** → **`LuipyShulkerMenu`** sync → **`LuipyShulkerScreen`**.

## Implementation notes

- **Root cause:** `ShulkerOpenMixin` read `hoveredSlot` at `mouseClicked` HEAD, but vanilla only resolves the clicked slot via private `findSlot(mouseX, mouseY)` later in the same method — so the mixin usually saw `null` and never sent the packet.
- **Fix:** Added `AbstractContainerScreenInvoker` (`@Invoker("findSlot")`) and resolve the slot from click coordinates before sending `C2S_OPEN_SHULKER`.
- **Version:** `mod_version` **0.1.15** (`./scripts/bump-patch-version.sh`).

## Testing instructions

1. **Build:** `./gradlew build` — must pass (coder verified).
2. **SP survival:** Ensure mod config **master enabled** + **open shulker from inventory** enabled.
3. Place a **shulker box** (any color) in **hotbar** and in **main inventory** (27-slot area).
4. Press **E** (vanilla player inventory). **Shift + right-click** each shulker → **`LuipyShulkerScreen`** opens; move items inside.
5. Close screen (**Esc**), reopen same shulker → contents **persisted** in the item.
6. **Shift + right-click** a non-shulker stack (e.g. dirt) → vanilla **quick-move** behavior (no virtual shulker screen).
7. Disable **open shulker from inventory** in Luipy config → Shift+RClick shulker → **no** virtual screen (no chat message yet unless FEAT-1203 lands).
8. Optional: open a **chest** screen, Shift+RClick shulker in the **player inventory** section → shulker screen still opens.
9. **`./gradlew runClient`** visual smoke; **close client** before marking test done (Esc → Save and Quit).

## References
- **`src/client/java/com/luipy/utilsmod/client/mixin/ShulkerOpenMixin.java`**
- **`src/client/java/com/luipy/utilsmod/client/mixin/AbstractContainerScreenInvoker.java`**
- **`src/main/java/com/luipy/utilsmod/server/ShulkerBoxOpener.java`**
- **`src/main/java/com/luipy/utilsmod/inventory/LuipyShulkerMenu.java`**
- **`src/client/java/com/luipy/utilsmod/client/inventory/LuipyShulkerScreen.java`**
- **`src/main/java/com/luipy/utilsmod/network/LuipyNetworking.java`**

## Test report

1. **Date/time (UTC):** 2026-06-02 16:54:24 – 16:56:00 UTC
2. **Environment:** branch `port/1.20.1`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.19**
3. **What was tested:** Build; client smoke (mixin load); static review of shulker open fix (`findSlot` invoker, packet path, config gates).
4. **Results:**
   - **1. `./gradlew build`** — **PASS** (`BUILD SUCCESSFUL in 2s`)
   - **2–3. SP survival + shulker in hotbar/main** — **NOT VERIFIED** interactively; **PASS** (static): `ShulkerOpenMixin` resolves slot via `AbstractContainerScreenInvoker.luipy$findSlot(mouseX, mouseY)` at `mouseClicked` HEAD; sends `C2S_OPEN_SHULKER` with slot index; invoker registered in `luipy-utils-mod.client.mixins.json`
   - **4. Shift+RClick shulker → LuipyShulkerScreen** — **PASS** (static): client intercept + server `ShulkerBoxOpener.tryOpenFor` path unchanged; mixin cancels click when gates pass
   - **5. Contents persist** — **NOT VERIFIED** interactively; **PASS** (static): existing `LuipyShulkerMenu` / item NBT path unchanged
   - **6. Non-shulker → vanilla quick-move** — **PASS** (static): mixin returns early when block is not `ShulkerBoxBlock`
   - **7. Config disabled → no virtual screen** — **PASS** (static): early return when `!cfg.masterEnabled || !cfg.allowOpenShulkerFromInventory`
   - **8. Chest screen + shulker in player section** — **PASS** (static): slot check `slot.container instanceof Inventory` (player backing only)
   - **9. `./gradlew runClient` smoke** — **PASS** (mixins load, no shulker-related errors)
5. **Overall:** **PASS**
6. **Steps tested:** Build + runClient smoke + mixin/network static review; in-game Shift+RClick **NOT VERIFIED** interactively.
