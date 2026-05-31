---
## Closing summary (TOP)

- **What happened:** Manual feature task to add a client-side block highlight overlay driven by a configurable comma-separated block ID list.
- **What was done:** Added `blockHighlightEnabled` / `blockHighlightIds` config, World category UI with Apply button, `BlockHighlightManager` parsing and incremental chunk scan, and `BlockHighlightRenderer` green wireframe via `WorldRenderEvents.AFTER_ENTITIES`.
- **What was tested:** `./gradlew build` and `./gradlew runClient` smoke passed; code review confirmed parsing, unknown-ID handling, performance guards, and client-only scope — overall **PASS** (in-world visual outlines deferred to manual playtest).
- **Why closed:** All acceptance criteria passed; tester report marked overall PASS.
- **Closed at (UTC):** 2026-05-31 17:29
---

# Block highlight overlay — configurable block ID list

## GitHub Issue
- **Issue:** N/A (manual task — no GitHub issue yet)
- **Number:** #0

## Problem / goal

Players often need to **spot specific blocks in the world** (ores, gravel patches, clay, calcite, etc.) without memorizing textures or using external tools. Luipy Utils should offer a **client-side highlight** for a user-defined set of blocks.

**Goal:** Add a new feature controlled from the mod config screen:

1. A **text field** where the player lists block IDs as **Minecraft resource ids** (namespace optional), separated by **`, `** (comma + space).
   - Examples: `redstone_ore, gravel, calcite, clay`
   - Also accept `minecraft:redstone_ore` if present; normalize on parse.
2. Matching blocks **in render distance** are **visually highlighted** in the world so they stand out from surrounding terrain.
3. Optional but recommended: a **Confirm / Apply** button (or **Reload highlights**) that **re-parses the list**, validates IDs, refreshes the in-memory block set, and **updates rendering** without restarting the game.

Highlighting is **client-only**, cosmetic, and must respect **`masterEnabled`** (and a dedicated toggle for this feature).

## High-level instructions for coder

### Config model

Extend **`LuipyUtilsConfig`** (persisted JSON via **`LuipyUtilsConfigManager`**) with at least:

| Field | Type | Purpose |
|-------|------|---------|
| `blockHighlightEnabled` | `boolean` | Master toggle for this feature |
| `blockHighlightIds` | `String` | Raw list, e.g. `"redstone_ore, gravel, calcite, clay"` |

Optional later-friendly fields (stub or implement if cheap):

- `blockHighlightColor` / per-block colors — **out of scope** unless trivial; single default highlight style is fine for v1.

**Parsing rules:**

- Split on **`, `** (comma + space). Trim each token; ignore empty tokens.
- Resolve each token with `ResourceLocation.tryParse(id)`; if no namespace, assume **`minecraft:`**.
- Resolve block via `BuiltInRegistries.BLOCK` (or 1.20.1 equivalent). **Unknown IDs:** skip with user-visible warning in chat or config screen (do not crash).
- Cache parsed `Set<Block>` on client after Apply; do not re-parse every frame.

### Config UI

Wire into the **first-party config screen** when available (`FEAT-0-20260531-1656-first-party-config-screen.md`):

- Category suggestion: **World** or **Visual**.
- Controls:
  - Toggle: **Enable block highlights**
  - Multiline or single-line text input: **Block IDs** (placeholder + tooltip with examples)
  - Button: **Apply / Reload highlights** — parses list, saves config, refreshes client cache, shows summary (`N blocks loaded, M unknown`)

Until the first-party screen exists, may temporarily add the field to **`LuipyConfigScreens`** (Cloth) — prefer extending the new UI if that task is already merged.

**Lang keys** (English in `en_us.json`, mirror in `es_es.json`):

- Feature name, description, text field label, Apply button, parse error messages, unknown block warnings.

### World rendering (client)

Implement **client-only** highlight in `src/client/java`:

**Behavior:**

- When enabled and list non-empty, draw a **visible emphasis** on every **matching block** the client knows about within loaded chunks (respect view distance / frustum — do not scan the entire world each tick naively).
- Highlight must read clearly on varied biomes (not identical to vanilla block outline for targeted blocks only — use a consistent accent, e.g. bright box outline, semi-transparent face tint, or pulsing edge — pick one approach and document it).

**Technical guidance (Fabric 1.20.1):**

- Prefer **Fabric API world render events** (`WorldRenderEvents` / `WorldRenderContext`) or a minimal mixin on block model rendering — avoid heavy per-block BlockEntity creation.
- Iterate **loaded sections/chunks** periodically (e.g. every N ticks or on chunk load/unload + Apply), build or update a **sparse set of BlockPos** or chunk-section bitsets for O(1) lookup during render.
- **Performance:** no full-world scan every frame; cap work per tick when rebuilding; disable entirely when toggle off or `masterEnabled` false.
- **Multiplayer:** client-only visual; no server packet required for v1. Server must not depend on highlight state.

**Edge cases:**

- Fluids, air, invalid IDs — ignore.
- Blocks behind other blocks: outline should still help (wireframe/box through occluding geometry is acceptable for utility mods if performance allows; otherwise document limitation).
- Spectator / creative — feature may stay enabled unless it causes confusion; document choice.

### Integration

- Register client tick/render hooks in **`LuipyUtilsModClient`**.
- Gate on **`LuipyUtilsConfigManager.get()`** and **`LuipyClientState`** / **`masterEnabled`** consistent with other client features.
- **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

### Code quality

- **Comments in English:** parsing rules, cache invalidation, render pass, chunk iteration strategy.
- Follow **`.cursor/rules/fabric-client-server-split.mdc`** — no render code in `src/main`.
- Follow **`.cursor/skills/fabric-modding/SKILL.md`**.

### Suggested files

- **Edit:** `LuipyUtilsConfig.java`, `LuipyUtilsConfigManager.java` (if needed)
- **New:** `src/client/java/.../highlight/BlockHighlightManager.java` (parse + cache)
- **New:** `src/client/java/.../highlight/BlockHighlightRenderer.java` (world render)
- **Edit:** config UI (first-party screen or `LuipyConfigScreens.java`)
- **Edit:** `LuipyUtilsModClient.java`, lang files

### Out of scope

- Entity highlighting, item frames, ore vein prediction.
- Sharing highlight lists via server or other players.
- X-ray / seeing through stone (only **emphasize** declared block types, not reveal hidden ones unless naturally visible).

### Dependencies on other tasks

- Config text field + Apply button fits best after **`FEAT-0-20260531-1656-first-party-config-screen.md`**; if not merged, ship minimal Cloth Config string field first, then migrate.

## Testing instructions

**Version:** `0.1.9` (after `./scripts/bump-patch-version.sh`)

**Open config:** hold **X + L** → sidebar **World** category.

1. Enable **Enable block highlights** (master mod switch must also be on).
2. Enter `redstone_ore, gravel` in **Block IDs** → **Apply / Reload highlights** → chat reports loaded types; matching blocks in render distance show a **green wireframe box** outline.
3. Add invalid token `not_a_block` to the list → Apply → chat shows partial success with unknown id; valid ids still highlight.
4. Clear the text field or disable the toggle → Apply / wait one tick → outlines disappear; watch FPS (no per-frame full-world scan).
5. Change list to `clay` only → Apply → previous highlights replaced without restart.
6. Test `minecraft:clay` and `clay` both resolve to the same highlight.
7. Multiplayer (server without mod): highlights remain client-only; no server dependency.
8. `./gradlew build` — pass (verified). `./gradlew runClient` — smoke test config UI + highlights in a creative world with exposed ores/gravel.

**Implementation notes:** `BlockHighlightManager` parses on `, ` delimiter, scans 2 chunks/tick within render distance, rescans every 200 ticks or on chunk load/player chunk change. `BlockHighlightRenderer` draws via `WorldRenderEvents.AFTER_ENTITIES` with camera-relative coords.

## References

- Config model: `src/main/java/com/luipy/utilsmod/config/LuipyUtilsConfig.java`
- Client entry: `src/client/java/com/luipy/utilsmod/client/LuipyUtilsModClient.java`
- Config UI task: `autoagents/tasks/FEAT-0-20260531-1656-first-party-config-screen.md`
- Task conventions: `autoagents/TASKS-README.md`
- Repo: https://github.com/Luipy56/Luipy-Utils-McMod

---

## Test report

1. **Date/time (UTC):** 2026-05-31 17:25:38 – 17:29:00 UTC
2. **Environment:** branch `port/1.20.1`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.10**
3. **What was tested:** Build; client smoke; static review of `BlockHighlightManager`, `BlockHighlightRenderer`, World category UI, parsing rules, performance guards.
4. **Results:**
   - `./gradlew build` — **PASS**
   - `./gradlew runClient` smoke (mod loads) — **PASS**
   - World category: toggle + Block IDs field + Apply button — **PASS** (`LuipyConfigScreen.rebuildWorldWidgets`)
   - Parse delimiter `, ` + `minecraft:` default namespace — **PASS** (`parseBlockIds` / `resolveId`)
   - Unknown id handling + chat partial message — **PASS** (`applyFromConfig` → `apply_partial` lang key)
   - `masterEnabled` gate — **PASS** (`isFeatureActive()`)
   - Incremental chunk scan (2/tick, 200-tick rescan, chunk load/unload) — **PASS** (`BlockHighlightManager` constants + hooks)
   - Green wireframe via `WorldRenderEvents.AFTER_ENTITIES` — **PASS** (renderer uses `LevelRenderer.renderLineBox`, green RGB)
   - Client-only / no server dependency — **PASS** (all code in `src/client`)
   - In-world visual confirmation of outlines — **NOT VERIFIED** interactively (no GUI automation); render path and cache logic verified in code
5. **Overall:** **PASS** (implementation + smoke; visual in-world check deferred to manual playtest)
6. **Steps tested:** Build + runClient smoke; code review of highlight manager/renderer and config Apply wiring.
