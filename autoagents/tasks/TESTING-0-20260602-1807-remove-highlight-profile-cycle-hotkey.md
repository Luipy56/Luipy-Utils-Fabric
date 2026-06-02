# Block highlight — remove profile cycle hotkey (H)

## GitHub Issue
- **Issue:** N/A (feature removal / UX simplification)
- **Number:** #0

## Problem / goal

The user **does not want** the in-game option to **cycle / rotate** the active highlight profile with a hotkey. Remove the feature entirely and remove it from config **Keybinds** readout.

**Today:**
- **`BlockHighlightProfileKeybinds`** — **H** key cycles profiles via **`BlockHighlightManager.cycleActiveProfile()`**
- **`KeyMapping`** `key.luipy-utils-mod.cycle_highlight_profile` registered in Controls
- Config → **Keybinds** tab shows cycle hotkey rows (lang keys `keybind.cycle_highlight_profile` + desc)

**Keep:**
- Multiple profiles in World tab (name, block IDs, enabled, Set active, texture)
- **Set active** button in config to choose active profile
- Profile enable/disable switches

**Remove:**
- H key handler and **`BlockHighlightProfileKeybinds`** registration (delete class or gut if nothing left)
- **`KeyMapping`** registration and Controls entry
- Keybinds tab rows for cycle profile
- Lang keys for cycle hotkey (EN + ES) — or leave orphaned keys unused; prefer cleanup
- **`BlockHighlightManager.cycleActiveProfile()`** if only used by hotkey (remove or keep for future — remove if dead code)
- Action bar message **`profile_cycled`** if only used by cycle hotkey

## High-level instructions for coder

1. Remove **`BlockHighlightProfileKeybinds.register()`** call from client init.
2. Delete or empty **`BlockHighlightProfileKeybinds.java`**.
3. Remove cycle rows from **`LuipyConfigScreen`** Keybinds tab render.
4. Clean **`en_us.json`** / **`es_es.json`**: `keybind.cycle_highlight_profile`, `keybind.cycle_highlight_profile.desc`, `key.luipy-utils-mod.cycle_highlight_profile`, optionally `profile_cycled`.
5. Verify active profile changes only via config **Set active** + Done/save path.

Run **`./scripts/bump-patch-version.sh`** once before UNTESTED-.

## Implementation (coder)

- Deleted **`BlockHighlightProfileKeybinds.java`**; removed registration from **`LuipyUtilsModClient`**.
- Removed **`BlockHighlightManager.cycleActiveProfile()`**.
- Keybinds tab: removed cycle profile rows in **`LuipyConfigScreen`**.
- Lang cleanup (EN/ES): `profile_cycled`, `keybind.cycle_highlight_profile*`, `key.luipy-utils-mod.cycle_highlight_profile`.
- **`mod_version`**: `0.1.28` (bump script).

## Testing instructions

1. In world, press **H** → **nothing** happens (no profile change, no action-bar chat).
2. Options → Controls → **no** “Cycle block highlight profile” binding.
3. Config → **Keybinds** → only config (X) and unified menu (R) rows; no cycle profile text.
4. World tab → enable Profile 2 with distinct block IDs → **Set active** → **Done** → highlights match Profile 2 only.
5. **`./gradlew build`** — PASS (coder). **`./gradlew runClient`** — tester smoke; close client after test.

## References
- **`BlockHighlightProfileKeybinds.java`**, **`BlockHighlightManager.java`**, **`LuipyConfigScreen.java`**
- Prior: **`autoagents/tasks/done/2026/06/02/CLOSED-0-20260602-1402-block-highlight-profiles.md`**
