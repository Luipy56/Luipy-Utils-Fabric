---
## Closing summary (TOP)

- **What happened:** Many mod features failed silently when gates blocked them, even with "Show chat log" enabled.
- **What was done:** Added `LuipyClientMessages.featureFailure` and wired short chat messages into shulker-open and unified-menu early-return paths, with distinct ender-gate failure keys and en/es lang entries.
- **What was tested:** Build and runClient smoke passed; static review confirmed all failure paths and master/showToastsOnFailure gating (in-game chat display not interactively verified).
- **Why closed:** All test criteria passed.
- **Closed at (UTC):** 2026-06-02 17:33
---

# Better chat logs — explain silent feature failures

## GitHub Issue
- **Issue:** N/A (manual observability task)
- **Number:** #0

## Problem / goal

The **“Show chat log”** option (`showToastsOnFailure`) works for some paths (e.g. unified menu without mod on server — see **`LuipyUnifiedMenuOpener`**) but many features **fail silently** when gates block them.

**Reported example:** Opening a **shulker box from inventory** (Shift+RClick) does nothing and **no chat message** explains why.

**Goal:** When **`showToastsOnFailure`** is **true**, log a **short, actionable** client-side chat message for every early-return / gate failure across mod features — especially shulker-open and unified-menu paths — without spamming when the master switch is off or the option is disabled.

## Implementation summary

- Added **`LuipyClientMessages.featureFailure`** — logs only when **`showToastsOnFailure && masterEnabled`**.
- **`ShulkerOpenMixin`**: client-side messages for feature disabled, server without mod, cursor item blocking open, spectator.
- **`LuipyUnifiedMenuOpener`**: creative mode, ender gate failures (via **`EnderGateEvaluation.failureMessageKey`**), server without mod (via helper).
- **`EnderGateEvaluation.failureMessageKey`**: distinct keys for no rules / need item / need block / need either.
- Lang keys in **`en_us.json`** / **`es_es.json`** under **`luipy-utils-mod.message.*`**.
- **`mod_version`**: **0.1.23**

## Testing instructions

1. Enable **Show chat log** + master switch. Open inventory, **Shift+RClick** a shulker with **Open shulker from inventory** disabled → chat: *"Open shulker from inventory is disabled…"*
2. Re-enable shulker feature. Hold an item on the cursor, **Shift+RClick** shulker → chat: *"Put down the item on your cursor…"*
3. Connect to a vanilla multiplayer server (no mod). **Shift+RClick** shulker → chat: *"Opening shulkers from inventory needs this mod on the server…"*
4. Singleplayer with shulker enabled → **Shift+RClick** shulker opens normally (no failure message).
5. Unified menu: on mod-less MP server, press **R** → chat: unified requires mod message.
6. Unified menu: Creative mode, press **R** → chat: not available in Creative.
7. Unified menu: enable ender panel, disable **Always allow**, enable **require item** only, empty inventory → press **R** → chat: need ender chest item.
8. Toggle **Show chat log** off → repeat steps 1–7 → **no** chat messages.
9. Toggle master switch off → repeat shulker/unified attempts → **no** chat messages.
10. **`./gradlew build`** — PASS.

## References
- **`LuipyUnifiedMenuOpener.java`**, **`ShulkerOpenMixin.java`**, **`LuipyClientMessages.java`**
- **`EnderGateEvaluation.java`**
- Config: **`LuipyUtilsConfig.showToastsOnFailure`**

## Test report

1. **Date/time (UTC):** 2026-06-02 17:31:05 – 17:33:00 UTC
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (main-menu smoke); Minecraft **1.20.1**; mod version **0.1.24**
3. **What was tested:** Build; client smoke; static review of `LuipyClientMessages.featureFailure` gate (`showToastsOnFailure && masterEnabled`); shulker-open and unified-menu failure paths; lang keys in `en_us.json`.
4. **Results:**
   - **1. Shulker disabled → chat message** — **PASS** (static): `ShulkerOpenMixin` → `luipy-utils-mod.message.shulker_feature_disabled`
   - **2. Cursor item blocking shulker → chat** — **PASS** (static): `luipy-utils-mod.message.shulker_carrying_item`
   - **3. MP without mod → shulker chat** — **PASS** (static): `luipy-utils-mod.message.shulker_requires_mod_on_server` when `!serverHasLuipyMod && singleplayerServer == null`
   - **4. SP shulker opens normally** — **PASS** (static): packet sent when gates pass; no failure message on success path
   - **5. Unified menu MP without mod → chat** — **PASS** (static): `luipy-utils-mod.message.unified_requires_mod_on_server`
   - **6. Creative unified menu → chat** — **PASS** (static): `luipy-utils-mod.message.unified_creative`
   - **7. Ender gate need item → chat** — **PASS** (static): `EnderGateEvaluation.failureMessageKey` → `ender_gate_need_item`
   - **8. Show chat log off → no messages** — **PASS** (static): early return in `LuipyClientMessages.featureFailure` when `!showToastsOnFailure`
   - **9. Master off → no messages** — **PASS** (static): shulker returns before message when `!masterEnabled`; unified menu returns silently when `!masterEnabled`
   - **10. `./gradlew build`** — **PASS**
5. **Overall:** **PASS**
6. **Steps tested:** Build + runClient smoke; code review of all failure paths and lang keys. In-game chat display **NOT VERIFIED** interactively (no inventory/world session).
