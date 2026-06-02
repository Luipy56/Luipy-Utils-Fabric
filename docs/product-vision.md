# Luipy Utils — visión de producto

Documento de referencia para diseño de features, tasks de autoagents y decisiones de arquitectura.  
**Versión del mod al redactar:** `0.1.21` · **Minecraft:** 1.20.1 · **Plataforma:** Fabric.

---

## Qué es Luipy Utils

Mod de **utilidades QoL vanilla-friendly** cuyo producto estrella es el **Unified Menu** (tecla **R**): un hub de inventario donde el jugador debe poder **vivir** en lugar de abrir **E** vanilla. Las mejoras “power user” existen como **capas opcionales** (toggles, gates, perfiles), nunca como trampas por defecto.

No es un mod de contenido (bloques, mobs, dimensiones). Tampoco compite con waypoints ni minimap — **fuera de scope explícito**.

---

## Pilares

| Pilar | Descripción | Prioridad |
|-------|-------------|-----------|
| **Hub de inventario** | Menú unificado, ender virtual, craft, shulker, workstations | Máxima |
| **Información y mundo** | Block highlight, preview de encantamientos, HUD de gates | Alta |
| **Gobernanza UX** | Master toggle + toggle por feature + chat log en fallos | Obligatorio en todo lo nuevo |
| **Servidor (futuro)** | Config admin que limita clientes; utilidades ligeras | Media — preparar schema |

---

## Principios de diseño

### Vanilla-friendly primero, power user opt-in

- Defaults conservadores: features fuertes **off** o con **gates** activos.
- Nada de auto-minar, auto-combat ni ventajas PvP opacas.
- Información (tooltips, highlights, peek) sí; automatización agresiva, con mucho cuidado.

### Patrón obligatorio por feature

1. Respeta `masterEnabled`.
2. Toggle propio en config (categoría adecuada).
3. Si falla y `showToastsOnFailure` → mensaje claro en chat.
4. Lang `en_us` + `es_es`.
5. Documentar **tier** (ver abajo).
6. Bump `mod_version` al cerrar task que cambie comportamiento.

### Config

- **Hoy:** JSON local en `config/luipy-utils-mod.json`.
- **Futuro:** política de servidor que **capa o bloquea** campos sensibles (p. ej. `alwaysAllowVirtualOpen`); el cliente muestra toggles grisados cuando el admin los prohíbe.
- **No** sync de presets de highlight entre jugadores por ahora.
- **No** rebind editable en config por ahora (chords fijos: **R**, **X+R**).
- **No** pestaña HUD/overlays separada por ahora (el HUD ender vive en Inventory/General).

### Dependencias

- Objetivo: **minimizar** dependencias opcionales (Mod Menu, Cloth Config) — config first-party con **X+R** como entrada principal.
- Fabric API + Loom toolchain son la base.

### Plataforma

- Permanecer en **1.20.1** hasta que el core (menú unificado + highlights + workstations) esté maduro.
- Invertir en GUIs custom y layout 1:1 slot↔textura; no apresurar port a 1.21+.

---

## Tiers técnicos (client vs servidor)

| Tier | Funciona sin mod en servidor | Ejemplos |
|------|------------------------------|----------|
| **A — Client puro** | Sí | Block highlight, enchant preview, HUD informativo (si no requiere datos server-only) |
| **B — Client + Luipy server** | No (degradación elegante) | Unified menu, shulker virtual, workstations con slots reales |

**Degradación en Tier B:** mensaje en chat + no abrir (no inventar contenedores “fantasma” que desincronicen). Objetivo a largo plazo: **maximizar Tier A**, pero el hub de inventario seguirá siendo Tier B por diseño.

---

## Producto estrella: Unified Menu

### Objetivo

El jugador abre **R** para casi todo: inventario compacto (sin armadura / 2×2 craft), ender opcional, craft opcional, y **columna izquierda de workstations** (planificado).

### Workstations planificadas (columna izquierda)

Anvil, smithing table, cartography table, grindstone, stonecutter, loom — apiladas dinámicamente, con **scroll** si no caben. Cada una: toggle + **always available** o **requires nearby block** (sin prefijo “Gate:” en UI; icono del bloque antes del título).

**Fuera de scope:** fletching table (sin GUI vanilla en 1.20.1), hornos/smoker salvo que se pida explícitamente.

### Gates (ender y workstations)

- Tienen sentido en **multijugador** (balance) y en **SP** (capricho configurable).
- Reutilizar patrón `EnderGateEvaluation` / `EnderGateAccess`.
- HUD proactivo (icono ender junto al hotbar) complementa chat reactivo.

---

## Roadmap por fases

### Fase 1 — Core sólido (actual → near term)

- [x] Menú unificado, keybinds, layout, título
- [x] Block highlight (modelo overlay compuesto)
- [x] HUD indicador ender
- [ ] Textura highlight custom + preview (World)
- [ ] 3 perfiles de highlight + hotkey ciclo
- [ ] Workstations columna izquierda
- [ ] Mejores chat logs en fallos silenciosos
- [ ] Quitar dependencias duras innecesarias (Mod Menu / Cloth)

### Fase 2 — Hub completo (vanilla-friendly)

Features Tier A/B con toggles off por defecto donde aplique:

- Búsqueda / filtro de ítems en menú unificado
- Ordenar inventario (sort por tipo, nombre, stack)
- Recetas ancladas en panel craft
- Peek de contenedor (mirar cofre/barrica/shulker colocado)
- Depósito rápido a cofre adyacente desde menú unificado
- Indicadores HUD para otros gates (workstations) cuando existan

### Fase 3 — Power user (opt-in)

- Autofill parcial de grid craft desde receta anclada
- Reglas de movimiento de ítems (shift-click inteligente solo dentro del menú Luipy)
- Perfiles de layout del menú (orden de paneles)
- Contador de bloques highlight en chunk / distancia al más cercano
- Tint por perfil de highlight (además de textura)

### Fase 4 — Servidor y admin

- `config/luipy-utils-mod-server.json` + `S2C_CONFIG_POLICY` al join
- Comando `/luipy status` (features activas, versión mod)
- Locks admin: always-open ender, workstations always-available, shulker-from-inventory
- Rate-limit suave en aperturas virtuales en MP (anti-spam)
- Logging estructurado para admins (intentos bloqueados)

### Fuera de scope (por ahora)

- Waypoints, minimap, mapas custom
- Redstone helpers
- Economía, protección de bloques, anti-cheat genérico
- Fletching table como panel

---

## Backlog de ideas (candidatas a FEAT)

Ideas alineadas con la visión. No están comprometidas; usar como pool para nuevas tasks.

### Hub e inventario (Tier B salvo nota)

| Idea | Tier | Notas |
|------|------|-------|
| **Búsqueda en menú unificado** | B | Campo texto; resalta slots; no mueve ítems |
| **Sort inventario** | B | Botones en footer del menú; respeta servidor |
| **Recetas pinned** | B | 3–5 iconos bajo panel craft; click autofill si hay materiales |
| **Depósito a cofre cercano** | B | Radio + gate; solo ítems que ya existen en el cofre |
| **Peek contenedor** | A/B | Sneak+mirar; Tier A si solo client estimate, B si server envía contenido |
| **Abrir shulker en hotbar con tecla** | B | Alternativa a Shift+RClick |
| **Panel brew stand / enchanting** | B | Solo si encaja en columna izquierda; evaluar UX |
| **Recordar scroll del menú** | A | Client preference entre sesiones |
| **Durabilidad baja en hotbar** | A | Overlay rojo bajo 10%; toggle General |
| **Comparar ítems en tooltip** | A | Shift al hover en menú unificado |

### Mundo y highlight (Tier A)

| Idea | Tier | Notas |
|------|------|-------|
| **Perfiles highlight** (en curso) | A | 3 nombres, listas, textura por perfil |
| **Hotkey ciclar perfil** | A | Con feedback chat breve |
| **Tint color por perfil** | A | Además de textura frame |
| **Contador en chunk** | A | “12 gravel nearby” — cuidado FPS |
| **Highlight entidades** | A | Ítem frames en suelo; scope distinto |
| **Import/export preset JSON** | A | Compartir listas sin sync server |

### Información vanilla (Tier A)

| Idea | Tier | Notas |
|------|------|-------|
| **Enchant preview** (hecho) | A | Funciona en servidores vanilla |
| **Preview yunque** | A | Coste XP + materiales antes de aplicar |
| **Preview smithing** | A | Resultado trim antes de tomar |
| **Lectern book peek** | A | Primera página sin abrir GUI completa |

### Servidor / Utils

| Idea | Tier | Notas |
|------|------|-------|
| **Server policy JSON** | Server | Caps sobre config cliente |
| **`/luipy reload`** | Server | Recarga policy sin reinicio |
| **`/luipy status`** | Server | Diagnóstico para jugadores/admins |
| **Broadcast versión mod** | Server | Extender `S2C_SERVER_PRESENT` |
| **Desactivar feature global** | Server | p. ej. shulker virtual off en SMP público |

### Infra y calidad

| Idea | Notas |
|------|-------|
| **Quitar `depends` Mod Menu / Cloth** | `suggests` + entrypoint opcional |
| **`LuipyClientMessages` centralizado** | Un solo helper para chat log |
| **Tests unitarios parsers** | block IDs, gates, config migration |
| **Modo degradado documentado** | Qué pasa en server sin mod |

---

## Jerarquía de config (futuro)

```
┌─────────────────────────────┐
│  Server policy (admin)      │  locks, caps, forced-off
└──────────────┬──────────────┘
               │ S2C on join
┌──────────────▼──────────────┐
│  Client config (JSON)       │  preferencias dentro de policy
└─────────────────────────────┘
```

Campos candidatos a **server lock**:

- `alwaysAllowVirtualOpen`
- `allowOpenShulkerFromInventory`
- Workstation `*AlwaysAvailable`
- `masterEnabled` por categoría (opcional, drástico)

Campos probablemente **solo cliente**:

- Block highlight profiles, texturas, HUD toggles cosméticos
- `showToastsOnFailure`, orden/sort UI preferences

---

## Referencias en repo

- Features implementadas: [`docs/implemented-features.md`](implemented-features.md)
- Loop autoagents: [`docs/agent-loop.md`](agent-loop.md)
- Config: `LuipyUtilsConfig.java`, `LuipyUtilsConfigManager.java`
- Menú: `LuipyUnifiedMenu.java`, `LuipyUnifiedScreen.java`
- Tasks activas: `autoagents/tasks/FEAT-*.md`, `WIP-*.md`, `UNTESTED-*.md`

---

## Decisiones abiertas

1. **Server sin mod:** ¿mantener hard gate (actual) o algún subconjunto client-only en el futuro?
2. **Orden de workstations** en columna izquierda: ¿fijo o configurable?
3. **Peek contenedor:** ¿sneak+mirar o tecla dedicada?

Actualizar este doc cuando se cierren decisiones o cambie la versión objetivo de Minecraft.
