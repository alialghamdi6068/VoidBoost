# VoidBoost

Client-side performance and PvP rendering toolkit for Minecraft Java 1.21.11 on Fabric.

**Developer:** VoidFlame

## Features

- Balanced, Competitive, MAX FPS and ULTIMATE FPS presets.
- Client-side configuration saved locally in `config/voidboost.json`.
- Particle suppression and configurable particle reduction.
- Entity render-distance optimization.
- Dynamic render distance that reacts to current FPS.
- Entity shadows, weather, cloud, vignette and ambient-occlusion performance controls.
- Animation optimization for PvP-focused rendering.
- Fog optimization toggle.
- Optional in-game performance monitor with FPS, frame time, RAM, entity count and blocked-particle rate.
- Dedicated VoidBoost menu opened with **O** from the title screen or while in-game.
- Performance-mode vanilla options are restored when the mode is disabled.
- No server-side installation or configuration synchronization.
- Designed to run alongside Sodium on Fabric 1.21.11. VoidBoost avoids Sodium internals and uses vanilla rendering seams, including block-entity distance culling, so its optimizations can remain active when Sodium is installed.
- Max Framerate uses the supported presets: 60, 120, 144, 165, 180, 240 and Unlimited.

## Requirements

- Minecraft Java 1.21.11
- Fabric Loader 0.18.5 or newer
- Fabric API
- Java 21

## Build

Use Java 21 and run:

```text
gradle build
```

The production JAR is generated under `build/libs/`.

## Important

VoidBoost changes client rendering/settings only. Actual FPS gains depend on hardware, graphics settings, world complexity and the other mods installed. The project does not claim a guaranteed FPS increase.
