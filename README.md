# VoidBoost

<p align="center">
  <img src="https://raw.githubusercontent.com/alialghamdi6068/VoidBoost/main/voidboost-cover.svg" alt="VoidBoost — VoidFlame performance and PvP optimization">
</p>

**Client-side performance & PvP optimization mod for Minecraft Java 1.21.11 + Fabric.**

VoidBoost is built around one rule: **improve client performance without becoming another source of CPU, RAM, or rendering overhead.**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-3b3b3b?logo=minecraft)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Client--side-DBD0B8?logo=fabric)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## What VoidBoost does

- Enables aggressive client-side culling by default to reduce particle and entity rendering work.
- Suppresses all world particles, dropped items beyond 48 blocks, experience orbs beyond 32 blocks, and other non-player entities beyond 96 blocks.
- Never distance-culls player entities.
- Allows aggressive culling to be disabled in the local config if the missing visual effects or distant entities are undesirable.
- Includes an optional, low-overhead performance monitor.
- Does not modify Sodium settings or inject into Sodium internals.
- Does not use network requests, external APIs, or background workers.
- Keeps diagnostic collection disabled while the monitor is hidden.

## Aggressive performance mode

Aggressive culling is enabled by default. It applies fixed, low-cost rendering limits; it does not poll FPS or wait for a low-FPS threshold.

| Content | Render limit |
| --- | --- |
| World particles | Suppressed |
| Dropped items | 48 blocks |
| Experience orbs | 32 blocks |
| Other non-player entities | 96 blocks |

The distances are measured from the camera, not from the world's origin. Player entities are exempt from the general entity limit so distant players remain visible.

To restore particles and uncapped entity rendering, set `aggressive_culling=false` in `config/voidboost.properties` and restart Minecraft. Disabling culling also restores particle effects such as impacts and ambient visuals.

FPS gains depend on the scene and hardware. These limits can help when particle or entity rendering is the bottleneck; they do not guarantee a specific FPS or a large gain in every world.

## Performance Monitor

The monitor is **OFF by default**.

Press **O**:

- First press → **ON**
- Second press → **OFF**
- Repeat whenever needed

When enabled, it displays:

- FPS
- Frame time
- CPU usage
- Java RAM usage
- Entity count

Diagnostic values are refreshed at most every **500 ms**. File I/O is only used when loading or changing the monitor preference.

## Design goals

### Zero unnecessary overhead

VoidBoost avoids:

- Network/API calls
- External AI services
- Background threads
- FPS polling in the render loop
- One entity render-admission hook for distance culling
- Sodium-internal mixins
- Automatic video-setting changes
- Automatic Sodium-setting changes

### Sodium-friendly

VoidBoost does not take ownership of the player's renderer configuration.

Your Sodium and Minecraft video settings remain yours to control.

## Requirements

- Minecraft Java **1.21.11**
- Fabric Loader **0.18.5+**
- Fabric API **0.141.6+1.21.11**
- Java **21**

## Project structure

```text
VoidBoost/
├── .github/
│   └── workflows/
│       └── build.yml
├── src/
│   ├── main/
│   │   ├── java/com/voidboost/
│   │   │   ├── client/
│   │   │   │   ├── VoidBoostAI.java
│   │   │   │   ├── VoidBoostClient.java
│   │   │   │   ├── VoidBoostConfig.java
│   │   │   │   └── VoidBoostStats.java
│   │   │   └── mixin/
│   │   │       ├── ClientLevelMixin.java
│   │   │       ├── EntityRenderDispatcherMixin.java
│   │   │       └── PerformanceHudMixin.java
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       └── voidboost.mixins.json
├── build.gradle
├── gradle.properties
├── LICENSE
└── README.md
```

## Build

With Java 21 installed:

```bash
gradle build
```

The production JAR is generated in:

```text
build/libs/
```

For the production client smoke test:

```bash
gradle prodClient
```

GitHub Actions builds the project, checks that the production JAR contains its required metadata/classes/icon, and starts a production client with Sodium 0.8.11 through resource loading. Sodium is only included in the development smoke-test runtime; it is not required or bundled with VoidBoost. The workflow does not benchmark FPS.

## Verification

The GitHub Actions workflow checks:

- Java 21 compilation and production JAR generation
- Required mod metadata, mixin config, client entrypoint, and icon in the JAR
- Production client startup through resource loading without fatal mixin errors

The workflow does not benchmark FPS or automatically test culling distances.

## Important note

VoidBoost has a **1000 FPS target**, not a 1000 FPS guarantee.

Actual FPS depends on hardware, resolution, Sodium configuration, shaders, resource packs, world complexity, Java runtime behavior, and other installed mods.

The target is to remove avoidable overhead first and only apply workload reductions that have a direct performance purpose.

## License

VoidBoost is released under the **MIT License**.

Copyright © 2026 VoidFlame.
