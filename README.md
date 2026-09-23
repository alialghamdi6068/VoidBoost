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

- **Tier 0 is permanently locked.**
- Uses lightweight local rules instead of an external AI service.
- Keeps the normal particle pipeline untouched until sustained low FPS is detected.
- Can reduce particle workload during emergency performance pressure.
- Includes an optional, low-overhead performance monitor.
- Does not modify Sodium settings or inject into Sodium internals.
- Does not use network requests, external APIs, background workers, or per-entity renderer hooks.
- Keeps diagnostic collection disabled while the monitor is hidden.

## Performance controller

VoidBoost checks Minecraft's existing FPS value from the client tick.

| State | Behavior |
| --- | --- |
| Normal | Minecraft's normal particle behavior |
| Low FPS | Counts consecutive low-FPS samples |
| Emergency | Filters non-priority particles to reduce workload |
| Recovery | Returns to normal after sustained recovery |

Current thresholds:

- Sample interval: **10 client ticks**
- Enter emergency: **below 45 FPS for 3 samples**
- Leave emergency: **58+ FPS for 4 samples**

The controller is intentionally conservative about when it activates. It does not run a machine-learning model or continuously analyze the render loop.

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
- Particle activity

Diagnostic values are refreshed at most every **500 ms**. File I/O is only used when loading or changing the monitor preference.

## Design goals

### Zero unnecessary overhead

VoidBoost avoids:

- Network/API calls
- External AI services
- Background threads
- Continuous render-loop AI logic
- Per-entity renderer hooks
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

GitHub Actions also builds and verifies the project automatically.

## Verification

The project is checked for:

- Java 21 compilation
- Correct Fabric metadata
- Client-only environment
- Required mixins only
- No Sodium-internal mixins
- No renderer-setting ownership
- No external network/API dependency
- Tick-based performance controller
- Disabled monitor statistics when hidden
- Production JAR generation
- Client startup with VoidBoost installed

## Important note

VoidBoost has a **1000 FPS target**, not a 1000 FPS guarantee.

Actual FPS depends on hardware, resolution, Sodium configuration, shaders, resource packs, world complexity, Java runtime behavior, and other installed mods.

The target is to remove avoidable overhead first and only apply workload reductions that have a direct performance purpose.

## License

VoidBoost is released under the **MIT License**.

Copyright © 2026 VoidFlame.
