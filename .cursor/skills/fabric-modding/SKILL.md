---
name: fabric-modding
description: Guía para programar mods Fabric en este repo (Minecraft 1.20.4, split main/client, Mojang mappings, Loom 1.6 + Gradle 8.7 + Java 17). Usar al implementar features, networking, mixins, registro de contenido o al depurar fallos solo en servidor o solo en cliente.
disable-model-invocation: true
---

# Fabric modding (Luipy Utils)

## Antes de tocar código

1. Confirmar el **lado lógico**: ¿solo cliente, solo servidor, o ambos? → colocar código en `src/client` vs `src/main` (ver reglas `.cursor/rules/fabric-client-server-split.mdc`).
2. Confirmar **APIs**: preferir **Fabric API** (`net.fabricmc.fabric.api.*`) antes que reflexión o acceso directo a internals.
3. Tras cambiar versiones en `gradle.properties`, ejecutar `./gradlew clean build` y, si hace falta fuentes, `./gradlew genSources`.

## Flujo típico de una feature

- **Datos comunes** (registros, lógica de juego, comandos servidor): `LuipyUtilsMod.onInitialize()` o clases llamadas desde ahí.
- **Solo cliente** (UI, teclas, render): `LuipyUtilsModClient.onInitializeClient()` o clases bajo `com.luipy.utilsmod.client`.
- **Sincronización cliente–servidor**: diseñar payloads/handlers explícitos; no asumir que el cliente tiene el mundo autoritativo.

## Mixins

- Añadir clase en el paquete correcto (`mixin` vs `client.mixin`) y **registrarla** en el `.mixins.json` correspondiente.
- Probar en **servidor dedicado** (`./gradlew runServer`) además de cliente si el mixin toca clases compartidas.

## Calidad y revisiones

- Nombres y logs: usar constante `LuipyUtilsMod.MOD_ID` para el logger o prefijos de mensaje.
- No añadir dependencias a mods arbitrarios sin declararlas en `fabric.mod.json` (`depends` / `suggests`) y en `build.gradle`.

## Migración futura (nota breve)

Si se pasa a **Loom 1.16+** y **JDK 21**: cambiar plugin a `net.fabricmc.fabric-loom-remap`, Gradle 9.x, quitar o ajustar `org.gradle.java.home` a JDK 21, y seguir la plantilla oficial en [fabric-example-mod](https://github.com/FabricMC/fabric-example-mod) rama acorde a la versión de Minecraft.
