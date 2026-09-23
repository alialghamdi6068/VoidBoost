# VoidBoost

Client-side performance and PvP rendering optimizer for Minecraft Java 1.21.11 on Fabric.

**Developer:** VoidFlame

## Performance profile

VoidBoost is an **always-on Tier 0** optimizer focused on removing avoidable client work without taking ownership of Minecraft or Sodium video settings.

- **No settings menu.**
- **No FPS/VSync/render-distance overrides.**
- **No Sodium settings overrides.**
- **Early ordinary-particle rejection:** normal particles are rejected before Minecraft creates the particle instance.
- **Low-FPS emergency mode:** a tiny local controller samples performance every 500 ms and temporarily expands particle filtering only while sustained FPS pressure is detected. It uses no ML model, allocations, network calls, or background worker.
- **Gameplay-safe particle handling:** always-visible particles remain protected.
- **Zero network dependency:** no external AI/API is used.
- **Optional monitor:** press **O** to show/hide diagnostics; when hidden, the monitor does not collect its statistics.
- **Client-only:** no server installation is required.

## CPU/RAM protection

The adaptive controller is intentionally not a heavyweight machine-learning system. A real model would consume CPU/RAM and compete with Minecraft for the resources VoidBoost is supposed to preserve. Instead, the controller uses hysteresis: it enters emergency filtering below 45 FPS and leaves it at 58 FPS, preventing constant switching around the threshold.

The controller never changes Minecraft or Sodium video options, render distance, VSync, FPS limit, or simulation settings.

## Sodium compatibility

VoidBoost does not inject into Sodium internals and does not take ownership of Sodium's video settings. You can freely change Sodium options while playing.

Minecraft 1.21.11 is on Sodium's maintained 0.8.x branch. Sodium 0.8.14 is a stable release for 1.21.11.

## Requirements

- Minecraft Java 1.21.11
- Fabric Loader 0.18.5 or newer
- Fabric API
- Java 21

## Build

GitHub Actions builds the project with Java 21 and uploads the resulting JAR artifact.

A local Gradle installation with Java 21 can build the project with `gradle build`.

The production JAR is generated under `build/libs/`.

## Verification checklist

- Java 21 compilation/build.
- Fabric metadata and client entrypoint validation through the Gradle build.
- Mixin configuration included in the production build.
- Clean client-only architecture.
- No Sodium-internal mixins.
- No renderer-setting ownership.
- No external network/API dependency.
- Adaptive controller uses a single lightweight client-thread sample path.
- Optional monitor is disabled by default.
- Latest CI build must finish successfully before release.

## Performance note

VoidBoost does not promise a fixed FPS number. Actual performance depends on hardware, resolution, Sodium settings, shaders, resource packs, world complexity and other installed mods.

The goal is to remove avoidable client work while leaving the player's renderer configuration under their control.
