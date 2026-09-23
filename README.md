# VoidBoost

Client-side performance and PvP rendering optimizer for Minecraft Java 1.21.11 on Fabric.

**Developer:** VoidFlame

## Performance design

VoidBoost is an **always-on Tier 0** optimizer. Its core rule is simple: VoidBoost must not spend meaningful CPU/RAM unless that work has a direct performance purpose.

- **No settings menu.**
- **No FPS/VSync/render-distance overrides.**
- **No Sodium settings overrides.**
- **No Sodium-internal mixins.**
- **No background worker.**
- **No external AI/API/network dependency.**
- **No per-entity renderer hook.**
- **No render-loop AI sampling.**
- **No statistics collection while the monitor is hidden.**

### Adaptive controller

The local controller is intentionally lightweight instead of using a real machine-learning model. A heavyweight ML loop would consume CPU/RAM and compete with Minecraft for the resources VoidBoost is supposed to preserve.

The controller:

1. Runs on the client tick, not the render loop.
2. Makes a real performance decision only twice per second.
3. Reads Minecraft's existing FPS value instead of timing every rendered frame.
4. Enters emergency particle filtering below **45 FPS**.
5. Leaves emergency filtering at **58 FPS** to avoid rapid switching.

The emergency action has a direct purpose: reduce client particle workload when sustained frame pressure is detected. There is no entity scan, prediction model, large cache, or background task running only to make the AI look more advanced.

### Particle optimization

VoidBoost intercepts particle creation before the particle instance is created:

- Normal mode rejects particles Minecraft marks for the normal limiter.
- Emergency mode additionally rejects non-forced particles that bypass that limiter.
- `alwaysShow` particles are never blocked by VoidBoost.
- Optional particle statistics are collected only while the monitor is enabled.

### Optional monitor

Press **O** to show/hide the diagnostic monitor.

When the monitor is hidden, VoidBoost does not collect frame statistics, CPU statistics, RAM statistics, entity counts, or particle counters. The monitor is disabled by default.

When visible, diagnostic values are refreshed only four times per second.

## Sodium compatibility

VoidBoost does not inject into Sodium internals and never writes Minecraft/Sodium video options. The player's renderer configuration remains fully under their control.

Sodium is a high-performance rendering engine focused on improving frame rates and reducing micro-stutter.

## Requirements

- Minecraft Java 1.21.11
- Fabric Loader 0.18.5 or newer
- Fabric API
- Java 21

## Build

GitHub Actions builds the project with Java 21 and uploads the resulting JAR artifact.

A local Gradle installation with Java 21 can build the project with:

`gradle build`

The production JAR is generated under `build/libs/`.

## Verification checklist

Before release, verify:

- Java 21 compilation/build succeeds.
- Fabric metadata loads as a client-only mod.
- The mixin configuration contains only the required client hooks.
- No Sodium-internal mixins exist.
- No renderer-setting ownership exists.
- No network/API dependency exists.
- The adaptive controller is tick-based.
- The optional monitor performs no statistics work while hidden.
- The production JAR is generated successfully.
- The resulting game client starts with VoidBoost installed alongside Sodium.

## Performance note

VoidBoost does not promise a fixed FPS number. Actual performance depends on hardware, resolution, Sodium settings, shaders, resource packs, world complexity and other installed mods.

The goal is to remove avoidable VoidBoost overhead first, then apply only workload reductions with a direct rendering/performance purpose.
