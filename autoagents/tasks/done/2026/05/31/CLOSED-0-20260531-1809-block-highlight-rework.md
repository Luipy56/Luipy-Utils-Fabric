---
## Closing summary (TOP)

- **What happened:** The block highlight feature from the prior overlay task was broken in-game (invisible wireframes, ~2 FPS) due to full-chunk scanning and per-position wireframe rendering.
- **What was done:** Replaced the scan + wireframe path with a Fabric Model Loading API plugin that applies a shared emphasis model/texture to configured block types only; removed `BlockHighlightRenderer` and position caching; mod version bumped to `0.1.14`.
- **What was tested:** `./gradlew build` and `runClient` smoke passed; static review confirmed new reload/model path, config Apply wiring, and removal of old scan/renderer; in-world visual and FPS playtest deferred to manual session.
- **Why closed:** All automated and static verification criteria passed; tester marked overall **PASS**.
- **Closed at (UTC):** 2026-05-31 18:29
---

# Block highlight rework — replace broken wireframe scan (2 FPS, invisible)

## GitHub Issue
- **Issue:** N/A (manual bugfix / redesign — no GitHub issue)
- **Number:** #0

## Problem / goal

The **block highlight** feature shipped in `CLOSED-0-20260531-1708-block-highlight-overlay` is **broken in practice**:

| Symptom | Report |
|---------|--------|
| Config / Apply | Chat confirms blocks loaded correctly (`N blocks loaded`) |
| Visual | **No visible highlight** in the world when the toggle is on |
| Performance | Game drops to **~2 FPS** after enabling via config Apply button |

**Root cause (current design — do not patch incrementally):**

- `BlockHighlightManager` **full-chunk scans** every loaded section and stores **every matching `BlockPos`** in a `HashSet` (catastrophic for common blocks like `gravel`, `clay`, etc.).
- `BlockHighlightRenderer` draws a **wireframe box per cached position every frame** via `WorldRenderEvents.AFTER_ENTITIES` + `RenderType.lines()` — expensive at scale and reportedly **not visible** anyway on 1.20.1.

**Goal:** **Remove or gut** the scan + outline approach and **re-implement highlighting from a render-time / resource perspective** so that:

1. Listed blocks are **clearly visible** (stand out from terrain).
2. **No FPS collapse** when the feature is on (target: negligible cost vs vanilla).
3. Config UX stays: toggle + comma+space ID list + **Apply** (`blockHighlightEnabled`, `blockHighlightIds` — parsing already works).

## High-level instructions for coder

### Phase 0 — Research (required before coding)

Search how Fabric **1.20.1** mods and vanilla resource loading handle per-block visual overrides. Document the chosen approach in code comments (1–2 paragraphs in the manager class).

**Investigate at least:**

| Approach | Notes |
|----------|--------|
| **Programmatic / “hot” resource pack** | Inject a client resource pack at runtime with block model or texture overrides for configured ids only; reload via `Minecraft.getInstance().reloadResourcePacks()` or Fabric reload listeners. Study vanilla pack structure (`assets/<ns>/blockstates`, `models/block`, `textures/block`) for 1.20.1. |
| **Model / texture override mixin** | Hook block model bake or `BlockModelRenderer` / `ModelBlockRenderer` to swap texture or tint for `Set<Block>` targets — no world scan. |
| **Colormap / tint index** | Biome colormap-style tint for specific blocks (limited but cheap). |
| **Reference mods** | How x-ray / ore-highlight / minimap mods tint blocks (avoid copying cheat behaviour — only the **render hook** pattern). |

Pick **one primary strategy** (texture override or hot pack is preferred per task owner). **Reject** the current “cache all positions + draw lines” design entirely.

### Phase 1 — Tear down broken path

- Delete or disable **`BlockHighlightRenderer`** wireframe loop over `highlightedPositions`.
- Remove **full-world chunk scanning** from **`BlockHighlightManager`** (no `HashSet<BlockPos>` of every match, no periodic rescan of all sections).
- Keep: **ID parsing**, **Apply** chat feedback, config fields, **`LuipyConfigScreen`** World category wiring.

### Phase 2 — New implementation (recommended direction)

**Preferred: render-time texture / model emphasis**

When `blockHighlightEnabled` && parsed block set non-empty:

1. **Apply** builds `Set<Block>` (already works).
2. On Apply / toggle / disable:
   - **Enable path:** register overrides (resource pack add, model wrapper, or mixin tint) for **only** those blocks.
   - **Disable path:** remove overrides and **reload client resources** so blocks return to vanilla look.
3. **Highlight look:** use a **custom highlight texture** (e.g. solid bright border overlay, or full-bright recolour PNG you ship under `assets/luipy-utils-mod/textures/highlight/…`) applied to all faces of targeted blocks — must be obvious in caves and on surface.
4. **No per-block-position storage** for rendering. Cost should scale with **number of configured block types**, not **number of blocks in world**.

**If using a dynamic resource pack:**

- Generate or select JSON/models at Apply time **or** ship generic “highlighted” models that reference a marker texture and use blockstate redirects for listed ids.
- Follow Minecraft **1.20.1** pack format; test reload without restart.
- Handle unknown ids gracefully (already implemented).

**If using mixin / renderer hook:**

- Minimal mixin; document mappings (Mojang names).
- Respect `masterEnabled` and feature toggle.

### Phase 3 — Config & UX

- Keep **`, `** delimiter parsing unchanged unless you add a migration note.
- Apply button should trigger **resource reload** + user message (`N blocks highlighted` / reload ok).
- Toggle off → immediate restore vanilla textures.

### Performance acceptance

- With `gravel, stone, dirt` in a flat world at render distance 12: **stable FPS** (no 2 FPS regression).
- With a single rare block (`ancient_debris`): still visible and cheap.

### Code quality

- **Comments in English** — especially why the old approach was removed and how reload works.
- Client-only in `src/client/java`; config fields stay in `LuipyUtilsConfig`.
- **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

### Files (starting point)

| File | Action |
|------|--------|
| `src/client/java/.../highlight/BlockHighlightManager.java` | Refactor — parse + apply reload only |
| `src/client/java/.../highlight/BlockHighlightRenderer.java` | Remove or replace |
| **New** e.g. `BlockHighlightResourcePack.java`, `BlockHighlightModels.java`, or mixin | Implement chosen approach |
| `src/client/java/.../config/ui/LuipyConfigScreen.java` | Wire Apply to new reload |
| `src/main/resources/assets/luipy-utils-mod/` | Highlight textures / generated models if needed |
| `LuipyUtilsModClient.java` | Register reload listeners |

### Out of scope

- Server-side sync of highlight lists.
- Entity / fluid highlighting.
- True x-ray (seeing ores through stone) — only **emphasize** blocks that are already exposed to the renderer.

## Testing instructions

**Version:** `0.1.14` (after `./scripts/bump-patch-version.sh`)

**Approach:** Fabric Model Loading API (`BlockHighlightModelPlugin`) registers block-state resolvers at reload time for configured block types only. Each resolver maps all states to shared emphasis model `luipy-utils-mod:block/highlight/emphasis` (bright green + white border texture). No chunk scan, no wireframe renderer. Apply / toggle / reset triggers `Minecraft.reloadResourcePacks()`.

**Open config:** hold **X + L** → sidebar **World** category.

1. Enable **Enable block highlights** (master mod switch must also be on).
2. Enter `redstone_ore, gravel` in **Block IDs** → **Apply / Reload highlights** → chat reports loaded types → matching blocks in world render as **bright green cubes with white border** (not vanilla texture).
3. Same session with many gravel/stone/dirt nearby: FPS stays playable (F3; no ~2 FPS regression vs old wireframe scan).
4. Disable **Enable block highlights** → **Done** (or reset category) → resource reload → blocks return to **vanilla textures** without restart.
5. Add invalid token `not_a_block` → Apply → chat partial success; valid ids still highlight.
6. Change list to `clay` only → Apply → previous types normal, clay uses emphasis model.
7. `./gradlew build` — pass (verified). `./gradlew runClient` — mod loads, resource reload succeeds (verified smoke). **Close client after test** (see `.cursor/rules/runclient-close-after-test.mdc`).

**Removed:** `BlockHighlightRenderer` (wireframe), chunk scanning / `HashSet<BlockPos>` cache in `BlockHighlightManager`.

## References

- Broken implementation: `src/client/java/com/luipy/utilsmod/client/highlight/`
- Original (closed) task: `autoagents/tasks/done/2026/05/31/CLOSED-0-20260531-1708-block-highlight-overlay.md`
- Config: `LuipyUtilsConfig.blockHighlightEnabled`, `blockHighlightIds`
- Minecraft 1.20.1 resource pack docs / Fabric resource reload APIs
- Task conventions: `autoagents/TASKS-README.md`

---

## Test report

1. **Date/time (UTC):** 2026-05-31 18:28:54 – 18:29:19 UTC
2. **Environment:** branch `port/1.20.1`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.14**
3. **What was tested:** Build; client smoke; static review of reworked highlight path (`BlockHighlightManager`, `BlockHighlightModelPlugin`), removal of wireframe/chunk scan, World category Apply wiring, emphasis model assets, config reload on toggle/reset.
4. **Results:**
   - `./gradlew build` — **PASS** (BUILD SUCCESSFUL, 11 tasks)
   - `./gradlew runClient` smoke (mod `luipy-utils-mod 0.1.14` loads, initial resource reload OK, no highlight-related errors) — **PASS**
   - `BlockHighlightRenderer` / wireframe / `HashSet<BlockPos>` chunk scan removed — **PASS** (no matches in `src/`)
   - Model-plugin approach: `BlockHighlightModelPlugin` registers resolvers → `block/highlight/emphasis` + `highlight_emphasis.png` — **PASS**
   - World category: toggle + Block IDs field + Apply → `BlockHighlightManager.applyFromConfig` — **PASS** (`LuipyConfigScreen`)
   - Apply chat success / partial (unknown ids) — **PASS** (`apply_success` / `apply_partial` lang keys + `parseBlockIds`)
   - Toggle off / reset category → `reloadFromConfig` + `reloadResourcePacks()` — **PASS**
   - `masterEnabled` gate — **PASS** (`shouldApplyModelOverrides()`)
   - Cost scales with configured block **types** only (no per-position cache) — **PASS**
   - In-world visual: bright green emphasis on `redstone_ore`, `gravel` — **NOT VERIFIED** interactively (no GUI automation); model/texture/reload path verified in code and assets
   - FPS with many common blocks (no ~2 FPS regression) — **NOT VERIFIED** in-game; old per-frame wireframe + full scan path confirmed removed
5. **Overall:** **PASS** (build + smoke + static verification; visual/FPS playtest deferred to manual session)
6. **Steps tested:** `git-sync-main.sh`; UNTESTED→TESTING rename; `./gradlew build`; `./gradlew runClient` smoke (client closed after); code/asset review of highlight rework vs testing instructions.
