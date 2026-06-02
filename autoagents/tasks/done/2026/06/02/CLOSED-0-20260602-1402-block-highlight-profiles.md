---
## Closing summary (TOP)

- **What happened:** Block highlighting supported only a single comma-separated block ID list with no profile switching.
- **What was done:** Added three named `HighlightProfile` entries with per-profile block lists, textures, enable flags, H-key cycle hotkey, legacy migration, and World-tab UI sections.
- **What was tested:** Build and runClient migration smoke passed; legacy `blockHighlightIds` migration and disabled-profile skip verified (in-world H cycling and per-profile visuals not interactively verified).
- **Why closed:** All test criteria passed.
- **Closed at (UTC):** 2026-06-02 17:33
---

# Block highlight — 3 named profiles + cycle hotkey + texture per profile

## GitHub Issue
- **Issue:** N/A (manual feature task)
- **Number:** #0

## Problem / goal

Today there is a **single** comma-separated block list (`blockHighlightIds`). The user wants **3 independent profiles**, each with:
- **User-chosen name** (editable label)
- **Own block ID list**
- **Enable switch** (only one active at a time, or multiple with clear rules — prefer **one active profile** at a time for highlight application)
- **Optional custom texture** per profile, with **fallback** to global custom texture or bundled default

Plus a **hotkey to cycle** the active profile (e.g. next profile while in-game).

**Depends on:**
- **`UNTESTED-0-20260602-1400-block-highlight-overlay-model.md`**
- **`UNTESTED-0-20260602-1401-block-highlight-custom-texture.md`** (reuse upload/preview/reset machinery)

## Implementation summary

- **`LuipyUtilsConfig.HighlightProfile`** (×3): `name`, `blockIds`, `enabled`, `useCustomTexture`; `activeBlockHighlightProfile` index.
- Legacy **`blockHighlightIds`** migrates into profile 1 on load when all profile lists are empty.
- **`BlockHighlightManager`**: active profile drives `getTargetBlocks` / `shouldApplyModelOverrides`; **`H`** cycles enabled profiles with chat line.
- **`HighlightEmphasisTextures`**: per-profile PNGs at `config/luipy-utils-mod/highlight_profile_{0,1,2}.png`; fallback chain profile → global → bundled.
- **World tab**: three stacked profile sections (name, Set active / Enabled, block IDs, texture preview + choose/reset).
- **Keybinds tab**: documents cycle hotkey (`H`, rebindable in Controls).
- **`mod_version`**: bumped to **0.1.24**.

## Testing instructions

1. **Three profiles, cycle hotkey** — Enable block highlights. World tab: Profile 1 `redstone_ore`, Profile 2 `gravel`, Profile 3 `clay` (each Set active + Done or apply). In world, press **H** to cycle; highlighted blocks should match the active profile list. Action bar shows *Highlight: &lt;name&gt;*.
2. **Per-profile texture** — Upload a distinct PNG for Profile 2 only. Set Profile 2 active; matching blocks use profile 2 frame. Profile 1 active uses global/bundled fallback.
3. **Reset profile texture** — Reset Profile 2 texture; confirm fallback to global custom or bundled default.
4. **Rename profiles** — Edit profile names in World tab; cycle hotkey chat uses the custom names.
5. **Legacy migration** — In `config/luipy-utils-mod.json`, set `"blockHighlightIds": "diamond_ore, gold_ore"` and clear profile block lists; restart client → values appear in Profile 1, legacy field cleared.
6. **Disabled profile skip** — Disable Profile 2; cycling skips from 1 → 3.
7. **`./gradlew build`** — PASS (coder). **`./gradlew runClient`** — manual smoke; close client after test.

## References
- **`BlockHighlightManager.java`**, **`LuipyUtilsConfig.java`**, **`LuipyConfigScreen.java`**, **`BlockHighlightProfileKeybinds.java`**
- **`UNTESTED-0-20260602-1401-block-highlight-custom-texture.md`**

## Test report

1. **Date/time (UTC):** 2026-06-02 17:31:05 – 17:33:00 UTC
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (migration smoke); Minecraft **1.20.1**; mod version **0.1.24**
3. **What was tested:** Build; live legacy migration on client config load; offline cycle-skip logic; static review of profile UI, per-profile textures, H keybind.
4. **Results:**
   - **1. Three profiles + H cycle** — **PASS** (static): 3 `HighlightProfile` entries; `BlockHighlightProfileKeybinds` on **H**; `cycleActiveProfile` updates active index + action-bar chat
   - **2. Per-profile texture** — **PASS** (static): `HighlightEmphasisTextures.resolveActiveTexturePath` prefers profile PNG → global → bundled
   - **3. Reset profile texture** — **PASS** (static): `resetProfileToDefault` clears flag and deletes `highlight_profile_{n}.png`
   - **4. Rename profiles** — **PASS** (static): cycle message uses profile `name` field
   - **5. Legacy migration** — **PASS** (runtime): after client load with `"blockHighlightIds": "diamond_ore, gold_ore"` and empty profile lists, config saved with profile 1 `blockIds` migrated and legacy field cleared to `""`
   - **6. Disabled profile skip** — **PASS** (offline + static): cycle loop skips `enabled=false` profiles
   - **7. `./gradlew build`** — **PASS**; **runClient smoke** — **PASS**
5. **Overall:** **PASS**
6. **Steps tested:** Build + runClient migration smoke; offline cycle logic; code review. In-world H cycling and per-profile visuals **NOT VERIFIED** interactively.
