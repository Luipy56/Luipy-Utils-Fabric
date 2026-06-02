# Luipy Utils — features implementadas

Resumen del estado actual del mod en código. Para visión y backlog futuro ver [`product-vision.md`](product-vision.md).

**Mod version:** `0.1.21` (ver `gradle.properties`)  
**Minecraft:** 1.20.1 · **Loader:** Fabric 0.16.x · **API:** Fabric API

---

## Resumen rápido

| Área | Feature | Tier | Estado |
|------|---------|------|--------|
| Inventario | Unified Menu (R) | B | ✅ |
| Inventario | Panel ender 3×9 | B | ✅ |
| Inventario | Panel craft 3×3 + recipe book | B | ✅ (toggle, default off) |
| Inventario | Shulker Shift+RClick | B | ✅ |
| Inventario | Workstations columna izquierda | B | 📋 Planificado |
| Mundo | Block highlight (overlay) | A | ✅ |
| Mundo | Highlight textura custom | A | 🔨 En progreso |
| Mundo | Highlight perfiles ×3 | A | 📋 Planificado |
| Features | Enchantment full preview | A | ✅ |
| UX | Config first-party (X+R) | A | ✅ |
| UX | Chat log en fallos | A | ⚠️ Parcial |
| UX | HUD icono ender (hotbar) | A | ✅ |
| Red | Presencia mod en servidor | B | ✅ |

**Leyenda:** ✅ Implementado · 🔨 WIP/UNTESTED · 📋 Task FEAT · ⚠️ Parcial

---

## 1. Unified Menu (menú unificado)

**Tecla:** **R** (edge detect; también registrada en Controles)  
**Pantalla:** título *Unified Menu* · fondo atenuado como inventario vanilla

### Contenido del menú

- **Inventario jugador compacto:** main 3×9, hotbar, offhand — **sin** columna armadura ni 2×2 craft.
- **Panel ender chest (opcional):** 27 slots → inventario ender del jugador. Toggle `showEnderChestWithInventory` (default on).
- **Panel mesa de crafteo (opcional):** grid 3×3 + result + recipe book. Toggle `showCraftingTableWithInventory` (default off).

### Apertura y gates

- **E** vanilla no se intercepta — solo **R** abre este menú.
- Requiere **mod en servidor** o **singleplayer** integrado (`S2C_SERVER_PRESENT` / SP check).
- Si ender panel activo y gate falla → no abre (bloqueo total hoy).
- Gates ender (`EnderGateEvaluation`):
  - `alwaysAllowVirtualOpen`, o
  - llevar ítem ender chest en inventario (`requireEnderChestItem`), o
  - bloque ender chest cargado cerca (~48 bloques Chebyshev, `requireNearbyEnderChestBlock`).

### Archivos clave

- `LuipyUnifiedMenu.java`, `LuipyUnifiedScreen.java`
- `LuipyUnifiedMenuOpener.java`, `LuipyUnifiedMenuKeybinds.java`
- `UnifiedMenuOpeners.java` (servidor)
- `LuipyMenuTypes.UNIFIED`

### Networking

- Cliente → `c2s_open_unified_menu` → servidor abre menú.

---

## 2. Shulker virtual desde inventario

**Gestura:** Shift + botón derecho sobre shulker en slot de inventario del jugador (pantalla contenedor abierta).

- Abre `LuipyShulkerMenu` / `LuipyShulkerScreen` (27 + inventario jugador).
- Persiste contenido en NBT del ítem al cerrar (servidor).
- Requiere mod en servidor o SP.
- Toggle: `allowOpenShulkerFromInventory`.

**Archivos:** `ShulkerOpenMixin.java`, `ShulkerBoxOpener.java`, `LuipyShulkerMenu.java`, `C2S_OPEN_SHULKER`.

---

## 3. Block highlight (énfasis de bloques)

**Client-only.** Escala con **número de tipos de bloque** configurados, no con bloques en mundo.

### Comportamiento

- Lista comma-separated en config (`blockHighlightIds`).
- `BlockHighlightModelPlugin` registra overrides por block state.
- Modelo **compuesto** (`HighlightCompositeUnbakedModel` / `HighlightCompositeBakedModel`): textura vanilla del bloque en el centro + frame `highlight_emphasis.png` encima.
- Toggle: `blockHighlightEnabled`.
- Recarga vía `reloadResourcePacks()` al cambiar lista.
- World tab: campo de IDs; **Done** aplica solo si se visitó World y hubo cambios (sin botón Apply).

### Archivos

- `BlockHighlightManager.java`, `BlockHighlightModelPlugin.java`
- `HighlightComposite*.java`, `VanillaBlockModelLookup.java`
- `assets/.../textures/block/highlight_emphasis.png`

---

## 4. Enchantment preview

**Client-only.** Funciona en **servidores vanilla** (sin mod server).

- En mesa de encantamientos, al hover sobre opción muestra **todos** los encantamientos que se aplicarían.
- Replica seed RNG del servidor vía slot sincronizado `enchantmentSeed`.
- Toggle: `showEnchantmentPreview`.

**Archivo:** `EnchantmentScreenMixin.java`

---

## 5. Configuración first-party

**Apertura:** mantener **X + R** (in-game o menú pausa). Mod Menu opcional como entrada alternativa.

### Categorías

| Pestaña | Contenido |
|---------|-----------|
| General | Master switch, chat log |
| Inventory | Ender panel, craft panel, gates ender, HUD ender |
| Features | Enchant preview, shulker from inventory |
| World | Block highlight toggle + lista IDs |
| Keybinds | R, X+R (solo lectura) |

- Persistencia: `config/luipy-utils-mod.json` (`LuipyUtilsConfigManager`).
- Reset por categoría.
- Lang: `en_us.json`, `es_es.json`.

**Archivos:** `LuipyConfigScreen.java`, `LuipyConfigCategories.java`, `LuipyConfigKeybinds.java`

---

## 6. HUD — indicador ender

Icono de **ender chest** a la izquierda del hotbar cuando:

- `showEnderGateHudIndicator` + ender panel activos
- Gate ender pasa (`EnderGateAccess.enderHudGatePasses`)
- Servidor tiene mod o es SP
- No creativo, GUI visible

**Archivo:** `EnderGateHudIndicator.java`

---

## 7. Networking y presencia en servidor

| Canal | Dirección | Uso |
|-------|-----------|-----|
| `c2s_open_unified_menu` | C→S | Abrir menú unificado |
| `c2s_open_shulker` | C→S | Abrir shulker virtual (slot index) |
| `s2c_server_present` | S→C | Marca `LuipyClientState.serverHasLuipyMod` al join |

Al desconectar se limpia el flag de presencia.

**Archivo:** `LuipyNetworking.java`, `LuipyClientState.java`

---

## 8. Config schema actual (`LuipyUtilsConfig`)

```text
masterEnabled
showEnderChestWithInventory
alwaysAllowVirtualOpen
requireEnderChestItem
requireNearbyEnderChestBlock
showToastsOnFailure
showEnderGateHudIndicator
showEnchantmentPreview
allowOpenShulkerFromInventory
showCraftingTableWithInventory
blockHighlightEnabled
blockHighlightIds
```

---

## En progreso (tasks autoagents)

| Task | Descripción |
|------|-------------|
| `WIP-1401` | Preview + upload textura highlight custom (World) |
| `UNTESTED-1400` | Overlay model (puede estar mergeado — ver `HighlightComposite*.java`) |
| `UNTESTED-1404` | HUD ender (puede estar mergeado — ver `EnderGateHudIndicator`) |
| `FEAT-1402` | 3 perfiles highlight + hotkey ciclo |
| `FEAT-1403` | Workstations: anvil, smithing, cartography, grindstone, stonecutter, loom |
| `FEAT-1203` | Mejores chat logs en fallos silenciosos |

Ver `autoagents/tasks/` para estado exacto del pipeline (WIP → UNTESTED → testing → closed).

---

## Completado recientemente (archivo `done/`)

- Merge `port/1.20.1` → `main`
- Keybinds R / X+R
- Copy config + spacing World tab
- Fix shulker desde inventario
- Block highlight World UX (Done inteligente)
- Layout menú unificado + título “Unified Menu”
- Block highlight rework + overlay (según commits/tasks)

Rutas: `autoagents/tasks/done/2026/06/02/`, `.../2026/05/31/`.

---

## Dependencias (`fabric.mod.json`)

| Dependencia | Uso actual |
|-------------|------------|
| Fabric Loader + API | Core |
| **Mod Menu** | Entrypoint opcional → `LuipyConfigScreen` |
| **Cloth Config** | Declarada; config principal ya no depende de ella (`LuipyConfigScreens.java` legacy) |

Objetivo documentado en product-vision: reducir a `suggests` donde sea posible.

---

## Lo que el mod **no** hace hoy

- Workstations en menú unificado
- Perfiles múltiples de highlight
- Textura highlight subida por usuario (en curso)
- Config de servidor / admin overrides
- Waypoints, minimap
- Sort, búsqueda, peek, depósito rápido
- Rebind desde pantalla config
- Mensajes chat exhaustivos en todos los fallos (task pendiente)

---

## Cómo probar manualmente (smoke)

1. `./gradlew runClient`
2. **R** → Unified Menu (SP, gates según config)
3. **E** → inventario vanilla
4. **X+R** → config
5. World → IDs highlight → Done
6. Shift+RClick shulker en inventario
7. Mesa encantamientos → hover opciones
8. Cerrar cliente al terminar

---

## Referencias

- [`docs/product-vision.md`](product-vision.md)
- [`docs/agent-loop.md`](agent-loop.md)
- [`autoagents/TASKS-README.md`](../autoagents/TASKS-README.md)
