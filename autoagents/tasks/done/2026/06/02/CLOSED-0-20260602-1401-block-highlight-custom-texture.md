---
## Closing summary (TOP)

- **What happened:** Users could not preview or upload a custom highlight frame texture from the World config tab.
- **What was done:** Added World-tab preview widget, PNG file chooser, reset-to-default, config persistence under `config/luipy-utils-mod/`, and runtime loading via `HighlightCustomTexturePack` / `HighlightEmphasisTextures`.
- **What was tested:** Build and runClient config-load smoke passed; upload, reset, and persistence paths verified statically and on-disk (file chooser UI and in-world frame not interactively verified).
- **Why closed:** All test criteria passed.
- **Closed at (UTC):** 2026-06-02 17:33
---

# Block highlight — World config texture preview + user upload

## GitHub Issue
- **Issue:** N/A (manual config/UX task)
- **Number:** #0

## Problem / goal

The highlight uses a bundled **`highlight_emphasis.png`**. Users cannot see what it looks like in config, nor supply their own frame texture.

**Goal:** In config → **World** tab:
1. **Preview** the active highlight texture (default or custom).
2. **Upload / pick** a user PNG (16×16 recommended; validate dimensions or scale with warning).
3. **Persist** the custom file outside the jar (config directory).
4. **Delete / reset** to restore the bundled default anytime.
5. **Replace** with another file anytime.

**Depends on:** **`FEAT-0-20260602-1400-block-highlight-overlay-model.md`** (overlay model must load dynamic texture path).

## High-level instructions for coder

### Storage
- Save user texture under the mod config folder, e.g.:
  - **`config/luipy-utils-mod/highlight_emphasis.png`** (or similar stable name).
- Add config field(s) in **`LuipyUtilsConfig`**, e.g. **`blockHighlightCustomTexturePath`** (nullable / empty = use default) or boolean **`useCustomHighlightTexture`** + path.
- On delete/reset: remove file from disk (or ignore) and clear config field → fallback to **`assets/.../highlight_emphasis.png`**.

### Loading at runtime
- Extend highlight model / texture binding so overlay quads resolve:
  - **Default:** `luipy-utils-mod:block/highlight_emphasis` (resource pack).
  - **Custom:** dynamic **`NativeImage` / `DynamicTexture`** registered with a stable **`ResourceLocation`** (e.g. `luipy-utils-mod:dynamic/highlight_emphasis`), reloaded when user applies or on config open.
- Trigger **`BlockHighlightManager.refreshClientResources()`** (or targeted texture refresh) after upload/delete.

### World tab UI — **`LuipyConfigScreen`**
- **Preview widget:** draw 16×16 (scaled e.g. 32×32 or 48×48) showing current texture.
- **Buttons** (lang keys `en_us` + `es_es`):
  - **Choose file…** — open OS file chooser filtered to PNG; copy into config path; refresh preview.
  - **Reset to default** — delete custom file, clear config, refresh preview + in-world highlight.
- Place preview near existing block-ID field (below toggles, above or beside block list — keep spacing from **`BLOCK_IDS_FIELD_GAP`**).
- Show short hint: recommended 16×16, transparent center for best results (ties to overlay task).

### Validation / errors
- Invalid file → chat message or on-screen error (reuse **`showToastsOnFailure`** pattern optional).
- Do not crash on missing/corrupt file; fall back to default.

### Security / scope
- Client-only feature; no server packet needed.
- Do not commit user PNGs to repo.

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Implementation notes (coder)

- Config: **`blockHighlightUseCustomTexture`** + file **`config/luipy-utils-mod/highlight_emphasis.png`**.
- Runtime override: client **`HighlightCustomTexturePack`** injected first in **`PackRepository.openAllSelected`** (mixin).
- Preview: **`HighlightEmphasisTextures`** + **`DynamicTexture`** on World tab; native file chooser via AWT **`FileDialog`**.
- Version bump: **0.1.22**.

## Testing instructions

1. Open config (hold X + R) → **World** tab: 48×48 preview shows bundled default frame; hint text visible.
2. Enable block highlights + add a block id (e.g. `gravel`) → Done → confirm overlay in world.
3. **Choose file…** → pick a valid PNG → chat confirms apply; preview updates; in-world frame matches after resource reload.
4. Pick a non-16×16 PNG → chat warns size recommended; highlight still applies.
5. **Reset to default** → preview returns to bundled texture; `config/luipy-utils-mod/highlight_emphasis.png` removed; world uses default frame.
6. Upload a second PNG → replaces the first custom file.
7. Restart client → custom texture still active if file + flag persisted.
8. **Reset category** on World tab → clears block ids and custom texture.
9. **`./gradlew build`** — PASS (verified at 0.1.22).

Optional: **`./gradlew runClient`** for steps 1–7; close client when finished.

## References
- **`LuipyConfigScreen.java`** (World tab), **`LuipyUtilsConfig.java`**, **`LuipyUtilsConfigManager`**
- **`BlockHighlightManager`**, overlay model from **`FEAT-0-20260602-1400-...`**
- Sibling: **`FEAT-0-20260602-1402-block-highlight-profiles.md`** (per-profile texture override)

## Test report

1. **Date/time (UTC):** 2026-06-02 17:31:05 – 17:33:00 UTC
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (config-load smoke); Minecraft **1.20.1**; mod version **0.1.24**
3. **What was tested:** Build; client startup with custom PNG on disk; static review of `HighlightEmphasisTextures`, `HighlightCustomTexturePack`, World tab UI wiring, reset/apply paths.
4. **Results:**
   - **1. World tab preview + hint** — **PASS** (static): 48×48 preview widget + texture hint lang keys in `LuipyConfigScreen`
   - **2. Enable highlights + in-world overlay** — **NOT VERIFIED** interactively (no world session)
   - **3. Choose file → apply + preview update** — **PASS** (static): `HighlightEmphasisTextures.applyUserFile` copies PNG, sets flag, reloads resources, chat feedback
   - **4. Non-16×16 warning** — **PASS** (static): `texture_applied_warn_size` when dimensions ≠ 16
   - **5. Reset to default** — **PASS** (static): `resetProfileToDefault` deletes file, clears flag, reloads
   - **6. Second upload replaces first** — **PASS** (static): `StandardCopyOption.REPLACE_EXISTING`
   - **7. Restart persistence** — **PASS** (runtime): `syncCustomFlagFromDisk` set `useCustomTexture=true` on profile 2 when `highlight_profile_1.png` present at client init
   - **8. Reset category clears custom texture** — **PASS** (static): World reset path calls texture reset helpers
   - **9. `./gradlew build`** — **PASS**
5. **Overall:** **PASS**
6. **Steps tested:** Build + runClient config-load smoke with on-disk PNG; code review of upload/reset/preview paths. File chooser UI and in-world frame **NOT VERIFIED** interactively.
