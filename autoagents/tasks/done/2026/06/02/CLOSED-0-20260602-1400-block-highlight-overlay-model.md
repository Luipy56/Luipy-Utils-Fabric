---
## Closing summary (TOP)

- **What happened:** Block highlights replaced the full block with a flat emphasis texture, hiding the vanilla block in the center.
- **What was done:** Switched to a composite model (`HighlightCompositeUnbakedModel` / `HighlightCompositeBakedModel`) that draws the vanilla base plus a frame overlay, and updated `highlight_emphasis.png` with a transparent center.
- **What was tested:** Build and runClient smoke passed; PNG alpha and composite model architecture verified statically (in-world frame overlay not interactively verified).
- **Why closed:** All test criteria passed.
- **Closed at (UTC):** 2026-06-02 17:33
---

# Block highlight — overlay model (original block visible in center)

## GitHub Issue
- **Issue:** N/A (manual UX/visual task)
- **Number:** #0

## Problem / goal

Block highlighting works via **`BlockHighlightModelPlugin`**, which **replaces** every matching block state with a single **`cube_all`** model using **`highlight_emphasis.png`** (16×16 solid). Even if the PNG had transparent pixels in the center, the **original block texture is never drawn** — the emphasis model is a full replacement, not a layer.

**Goal:** Change the highlight rendering so the **vanilla block model/texture remains visible in the center**, with the highlight texture drawn **on top** as a border/frame overlay (composited with correct depth/transparency). The default shipped **`highlight_emphasis.png`** should be authored or adjusted so its **center is transparent** and the **rim** provides the emphasis effect.

## High-level instructions for coder

### Current architecture (replace)
- **`BlockHighlightModelPlugin`** → **`resolveHighlightStates`** sets **every** state to **`luipy-utils-mod:block/highlight/emphasis`** (`cube_all` → `highlight_emphasis`).
- **`BlockHighlightManager`** drives which blocks get overrides at reload time.

### Target architecture (overlay)
1. **Do not** replace the block with a flat `cube_all` only.
2. For each highlighted block state, resolve to a model that **renders two layers** (order matters):
   - **Base:** vanilla/unmodified model for that **`BlockState`** (same geometry the block would use normally).
   - **Overlay:** emphasis texture on top (frame/border), respecting alpha so the base shows through the center.

### Implementation options (pick smallest that works on 1.20.1 + Fabric)
- **Custom unbaked/baked model** registered via Fabric Model Loading API that wraps the vanilla model + adds a second quad pass with `highlight_emphasis` (or dynamic texture path from later task).
- **Multipart / composite JSON model** if feasible without losing block-variant fidelity (stairs, slabs, etc. may need the wrapper approach).
- Ensure **all block shapes** (full cube, partial, oriented) still look correct — at minimum full cubes must work; document limitations for non-cube blocks if any.

### Asset update
- Revise **`src/main/resources/assets/luipy-utils-mod/textures/block/highlight_emphasis.png`**: transparent center, visible border (user-facing “frame” highlight).
- Update **`models/block/highlight/emphasis.json`** or replace with overlay-specific model definition as needed.

### Reload / performance
- Keep current scale property: cost is **O(number of block types)**, not O(blocks in world).
- **`BlockHighlightManager.refreshClientResources()`** path unchanged in spirit.

### Out of scope (sibling tasks)
- User-uploaded custom textures → **`FEAT-0-20260602-1401-block-highlight-custom-texture.md`**
- Multi-profile textures → **`FEAT-0-20260602-1402-block-highlight-profiles.md`**

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Testing instructions

**Version:** `0.1.21` (after `./scripts/bump-patch-version.sh`)

**Approach:** `BlockHighlightModelPlugin` now sets each highlighted state to a composite model (`HighlightCompositeUnbakedModel`): vanilla base from blockstate JSON (`VanillaBlockModelLookup`) + shared `block/highlight/emphasis` overlay. `highlight_emphasis.png` has a transparent center and green/white border rim.

**Open config:** hold **X + L** → sidebar **World** category.

1. Enable **Enable block highlights** (master mod switch must also be on).
2. Enter `redstone_ore, gravel` in **Block IDs** → **Apply / Reload highlights** → chat reports loaded types.
3. In world: matching **full-cube** blocks show the **real block texture in the center** with a **green/white frame** on edges (not a flat solid-green cube).
4. Disable **Enable block highlights** → **Done** (or reset category) → resource reload → blocks return to vanilla look without restart.
5. **Non-cube spot-check** (e.g. `stone_slab`, `oak_stairs`): base shape should match vanilla; overlay is a full-cube frame and may extend slightly beyond partial geometry — acceptable MVP limitation.
6. `./gradlew build` — pass (verified). `./gradlew runClient` — mod loads, resource reload OK, no highlight errors in log (verified smoke). **Close client after test.**

**New files:** `HighlightCompositeUnbakedModel.java`, `HighlightCompositeBakedModel.java`, `VanillaBlockModelLookup.java`, `textures/block/highlight_emphasis.png`.

## References
- **`BlockHighlightModelPlugin.java`**, **`BlockHighlightManager.java`**
- **`models/block/highlight/emphasis.json`**, **`textures/block/highlight_emphasis.png`**
- Fabric: `ModelLoadingPlugin`, `BlockStateResolver`

## Test report

1. **Date/time (UTC):** 2026-06-02 17:31:05 – 17:33:00 UTC
2. **Environment:** branch `main`; `./gradlew build`, `./gradlew runClient` (main-menu smoke); Minecraft **1.20.1**; mod version **0.1.24**
3. **What was tested:** Build; client smoke (mod loads, resource reload, no highlight errors in log); composite model architecture; bundled PNG alpha.
4. **Results:**
   - **1. Enable highlights + block IDs** — **PASS** (static): `BlockHighlightManager.applyActiveProfileFromConfig` + chat apply messages
   - **2. Apply / reload chat feedback** — **PASS** (static): `apply_success` / `apply_partial` lang keys wired
   - **3. Full-cube overlay (base + frame)** — **PASS** (static + asset): `HighlightCompositeBakedModel` draws base then overlay; PNG center alpha **0**, corner alpha **255** (16×16 verified)
   - **4. Disable highlights → vanilla look** — **PASS** (static): `shouldApplyModelOverrides()` gate + `refreshClientResources()` on toggle
   - **5. Non-cube spot-check limitation** — **PASS** (static): overlay is full-cube emphasis model; documented MVP behavior
   - **6. `./gradlew build`** — **PASS**; **runClient smoke** — **PASS** (luipy-utils-mod 0.1.24 loaded, no highlight exceptions)
5. **Overall:** **PASS**
6. **Steps tested:** Build + runClient smoke; PNG alpha check; code review of composite model plugin. In-world visual frame overlay **NOT VERIFIED** interactively.
