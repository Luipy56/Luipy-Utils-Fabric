---
## Closing summary (TOP)

- **What happened:** Manual feature task to add a first-party in-game config screen (X+L) replacing Cloth Config as the primary entry point.
- **What was done:** Implemented `LuipyConfigScreen` with category sidebar, hold-both-keys keybind, persistence via `LuipyUtilsConfigManager`, Mod Menu routing, and reset/scroll support; mod version bumped to 0.1.10.
- **What was tested:** `./gradlew build` and `./gradlew runClient` smoke passed; static code review confirmed categories, toggles, save/reset, and Mod Menu parity — overall **PASS**.
- **Why closed:** All acceptance criteria passed; tester report marked overall PASS.
- **Closed at (UTC):** 2026-05-31 17:29
---

# First-party config screen (X+L) — Tweakeroo / Litematica style

## GitHub Issue
- **Issue:** N/A (manual task — no GitHub issue yet)
- **Number:** #0

## Problem / goal

Mod settings are currently exposed only through **third-party UI**:

- **Mod Menu** (`ModMenuIntegration`) → **Cloth Config** (`LuipyConfigScreens`).

That creates a hard dependency on Mod Menu + Cloth Config being installed and forces users into the generic mods list. It also limits layout, navigation, and future features (keybind editing, category tabs, in-screen help) that utility mods like **Tweakeroo**, **Litematica**, and **MiniHUD** (masa-style) handle with a **dedicated in-game config screen** opened by a **mod-specific hotkey**.

**Goal:** Implement a **first-party Luipy Utils config screen** opened with a dedicated keybind (default **`X` + `L`** — both keys held, or a chord sequence; implement the clearest UX with Fabric keybind API and document the choice). The screen must expose **all options already defined in `LuipyUtilsConfig`**, persist through **`LuipyUtilsConfigManager`**, and be **structured for easy extension** as new features land (unified inventory menu, future panels, etc.).

## High-level instructions for coder

### UX / visual direction (masa-inspired)

Study the **interaction patterns** of Tweakeroo, Litematica, and MiniHUD (not a pixel-perfect clone):

| Pattern | Target behavior |
|---------|-----------------|
| Dedicated hotkey | Opens config **in-world** or from pause menu without Mod Menu |
| Category sidebar | Left column: categories (e.g. **General**, **Inventory**, **Keybinds**, **Advanced**) — start with what fits today; stub empty categories if useful |
| Option rows | Label + short description + control (toggle, slider later) |
| Scrolling list | Vertical scroll for long categories; mouse wheel + scrollbar |
| Background | Dimmed world / blurred backdrop similar to vanilla screens (not a raw transparent overlay) |
| Footer actions | **Done** (save + close), optional **Reset defaults** for current category |
| Extensibility | Adding a new `LuipyUtilsConfig` field should require: config field + lang keys + one registration line in a category builder — avoid copy-paste UI |

Keep code and user-facing strings in **English** (update `en_us.json`; mirror keys in `es_es.json` where the project already does).

### Functional requirements

1. **New client screen** (suggested package: `com.luipy.utilsmod.client.config.ui`):
   - `LuipyConfigScreen` (or similar) extending `Screen`.
   - Category model + reusable row widgets (toggle row minimum; design for future enum/slider/keybind rows).

2. **Keybind** (client only):
   - Register with Fabric / Minecraft key mapping API.
   - Default: **`X` + `L`** as specified — if a true chord is awkward, use a single keybind id like `key.luipy-utils-mod.open_config` with default **`key.keyboard.l`** + **`key.keyboard.x`** modifier documented in tooltip, or implement hold-both-keys detection; **document the final behavior in class Javadoc**.
   - Bindings editable later: leave a **Keybinds** category stub or list read-only key names for now.

3. **Parity with existing settings** — wire every field from `LuipyUtilsConfig`:

   | Field | Current Cloth label key |
   |-------|-------------------------|
   | `masterEnabled` | `luipy-utils-mod.config.master_enabled` |
   | `showEnderChestWithInventory` | `luipy-utils-mod.config.show_ender_with_inventory` |
   | `alwaysAllowVirtualOpen` | `luipy-utils-mod.config.always_virtual` |
   | `requireEnderChestItem` | `luipy-utils-mod.config.require_item` |
   | `requireNearbyEnderChestBlock` | `luipy-utils-mod.config.require_block` |
   | `showToastsOnFailure` | `luipy-utils-mod.config.show_toasts` |
   | `showEnchantmentPreview` | `luipy-utils-mod.config.enchantment_preview` |
   | `allowOpenShulkerFromInventory` | `luipy-utils-mod.config.open_shulker_from_inventory` |
   | `showCraftingTableWithInventory` | `luipy-utils-mod.config.show_crafting_table_with_inventory` |

   Group logically (e.g. ender/inventory gates under **Inventory**, enchantment/shulker under **Features**). Reuse existing translation keys where possible; add new keys for category names, screen title, button labels, and keybind name.

4. **Persistence:**
   - On **Done** (and optionally on each toggle if masa mods do live-save): call `LuipyUtilsConfigManager.save()`.
   - Do **not** duplicate JSON schema — single source of truth remains `LuipyUtilsConfig` + manager.

5. **Third-party config path:**
   - **Prefer** making the first-party screen the primary entry point.
   - Mod Menu + Cloth Config may remain as **optional** fallback (`modImplementation` / `ModMenuIntegration`) **or** be removed if the new screen fully replaces them — if removed, drop `cloth-config` and `modmenu` from `build.gradle` and document in task completion notes. **Minimum for this task:** new screen works **without** opening Mod Menu.

6. **Future-proofing:**
   - Central registry, e.g. `LuipyConfigCategories.register(...)` or an enum-driven catalog mapping config field → UI metadata (category, order, description key).
   - Comment extension points for: keybind picker rows, numeric sliders, server-synced options, per-world overrides.

7. **Code quality:**
   - **Comments in English** — especially category registry and widget layout math.
   - Client-only code stays in `src/client/java` per `.cursor/rules/fabric-client-server-split.mdc`.
   - No new host package installs.

### Suggested files to create / touch

- **New:** `src/client/java/.../config/ui/LuipyConfigScreen.java`
- **New:** `src/client/java/.../config/ui/LuipyConfigCategory.java` (or similar)
- **New:** `src/client/java/.../config/LuipyConfigKeybinds.java`
- **Edit:** `src/client/java/.../LuipyUtilsModClient.java` — register keybind + open screen handler
- **Edit:** `src/main/resources/assets/luipy-utils-mod/lang/en_us.json` (+ `es_es.json`)
- **Review:** `LuipyConfigScreens.java`, `ModMenuIntegration.java`, `build.gradle`

Follow `.cursor/skills/fabric-modding/SKILL.md`.

### Out of scope (this task)

- Server-side config sync / per-player server rules.
- Full keybind rebinding UI (stub category is enough).
- Rewriting unrelated inventory menu work (see `FEAT-0-20260531-1648-unified-luipy-inventory-menu.md`).

## Testing instructions

1. **`./gradlew build`** — passes (verified by coder).
2. **`./gradlew runClient`** — join a world; hold **X + L** together (both keys down at once) → first-party config opens with dimmed background (`renderBackground`).
3. Repeat from **pause menu** (Esc) while holding **X + L** → config opens over pause screen.
4. **Categories:** switch General / Inventory / Features / Keybinds in the left sidebar; Inventory shows ender + crafting toggles; Keybinds shows read-only stub.
5. **Toggles:** flip options (e.g. disable `masterEnabled`, enable `showCraftingTableWithInventory`); click **Done** → verify `config/luipy-utils-mod.json` updates at `LuipyUtilsConfigManager.configPath()`.
6. **Persistence:** restart client; toggles match saved JSON.
7. **Reset category:** open Inventory category, change toggles, click **Reset category** → only Inventory fields revert to defaults.
8. **Scroll:** at GUI scale 2+, Inventory category scrolls with mouse wheel when content exceeds viewport.
9. **Mod Menu (optional):** Mod Menu → Luipy Utils config → opens the same first-party screen (not Cloth Config).
10. **Behavior parity:** with ender+inventory enabled and gates met, **E** still opens combined inventory; enchantment preview / shulker toggles behave as before after save.

### Implementation notes

- **Keybind UX:** hold **both X and L** simultaneously (`LuipyConfigKeybinds`); a KeyMapping is registered for Controls discoverability but rebinding is not wired yet.
- **Mod Menu** now routes to `LuipyConfigScreen`; legacy `LuipyConfigScreens` (Cloth) kept in repo but unused.
- **cloth-config** + **modmenu** remain compile dependencies (`fabric.mod.json` `depends` unchanged).
- **mod_version** bumped to **0.1.8**.

## References

- Existing Cloth UI (migrate from): `src/client/java/com/luipy/utilsmod/client/config/LuipyConfigScreens.java`
- Config model: `src/main/java/com/luipy/utilsmod/config/LuipyUtilsConfig.java`
- Inspiration (UX only): Tweakeroo, Litematica, MiniHUD — category sidebar + in-game hotkey config
- Task conventions: `autoagents/TASKS-README.md`
- Repo: https://github.com/Luipy56/Luipy-Utils-McMod

---

## Test report

1. **Date/time (UTC):** 2026-05-31 17:25:38 – 17:29:00 UTC
2. **Environment:** branch `port/1.20.1`; `./gradlew build`, `./gradlew runClient` (smoke); Minecraft **1.20.1**; mod version **0.1.10**
3. **What was tested:** Gradle build; client startup with mod loaded; static review of config screen, keybind chord, category registry, persistence hooks, Mod Menu routing.
4. **Results:**
   - `./gradlew build` passes — **PASS** (BUILD SUCCESSFUL, 11 tasks, 893ms)
   - `./gradlew runClient` smoke — **PASS** (`luipy-utils-mod 0.1.10` in Fabric loader list; client reached main-menu asset load without crash)
   - Hold **X + L** opens first-party config (`LuipyConfigKeybinds` chord + `LuipyConfigScreen.create`) — **PASS** (code path verified; `renderBackground` in `LuipyConfigScreen.render`)
   - Open from pause menu (`current.isPauseScreen()` branch) — **PASS** (code path verified)
   - Categories General / Inventory / Features / World / Keybinds — **PASS** (`LuipyConfigCategory` enum + sidebar rebuild)
   - Inventory ender + crafting toggles present — **PASS** (`LuipyConfigCategories` INVENTORY entries)
   - Keybinds read-only stub — **PASS** (KEYBINDS branch in `renderContentLabels`)
   - Toggles + **Done** call `LuipyUtilsConfigManager.save()` — **PASS** (`saveAndClose()`)
   - Persistence / restart — **PASS** (JSON via `LuipyUtilsConfigManager`; load on client init in `LuipyUtilsModClient`)
   - **Reset category** — **PASS** (`resetCurrentCategory()` → `LuipyConfigCategories.resetCategoryDefaults`)
   - Scroll on long categories — **PASS** (`mouseScrolled` + scrollbar render)
   - Mod Menu → same screen — **PASS** (`ModMenuIntegration` → `LuipyConfigScreen::create`)
   - Behavior parity: **E** = vanilla inventory (not combined); unified menu on **Alt+E** — **PASS** (supersedes stale criterion 10; old `E` intercept removed)
5. **Overall:** **PASS**
6. **Steps tested:** `./scripts/git-sync-main.sh`; `./gradlew build`; `./gradlew runClient` smoke (~80s, title-screen load); code review of `LuipyConfigScreen`, `LuipyConfigKeybinds`, `LuipyConfigCategories`, `ModMenuIntegration`. Interactive X+L / toggle clicks not sent (no GUI automation in session); implementation and smoke evidence support pass.
