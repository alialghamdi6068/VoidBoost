# VoidBoost

Client-side performance and PvP rendering optimizer for Minecraft Java 1.21.11 on Fabric.

**Developer:** VoidFlame

## Features

- Balanced, Competitive, MAX FPS and ULTIMATE FPS presets.
- Client-side configuration saved locally in `config/voidboost.json`.
- Particle suppression and configurable particle reduction.
- Entity render-distance optimization.
- Adaptive render distance that reacts to FPS, CPU/RAM pressure and entity load.
- Entity shadows, weather, cloud, vignette and ambient-occlusion performance controls.
- Animation optimization for PvP-focused rendering.
- Fog optimization toggle.
- Optional in-game performance monitor with FPS, frame time, RAM, entity count and blocked-particle rate.
- Dedicated VoidBoost menu opened with **O** from the title screen or while in-game.
- Maximum-performance Tier 0 controller with no external AI service or network dependency.
- Performance-mode vanilla options are restored when the mode is disabled.
- No server-side installation or configuration synchronization.
- Designed to run alongside Sodium on Fabric 1.21.11. VoidBoost avoids Sodium internals and uses vanilla rendering seams, so its optimizations can remain active when Sodium is installed. Sodium 0.8.x is the supported branch for Minecraft 1.21.11. citeturn0search1turn0search0
- Max Framerate uses the supported presets: 60, 120, 144, 165, 180, 240 and Unlimited.

## Requirements

- Minecraft Java 1.21.11
- Fabric Loader 0.18.5 or newer
- Fabric API
- Java 21

## Build

Use Java 21 and run:

```text
./gradlew build
```

On Windows:

```text
gradlew.bat build
```

The production JAR is generated under `build/libs/`. For a release upload, use the remapped JAR rather than the sources JAR.

## Important

VoidBoost changes client rendering/settings only. Actual FPS gains depend on hardware, graphics settings, world complexity and the other mods installed. The project does not claim a guaranteed FPS increase.

## Release checklist

Before publishing a build, verify all of the following on a clean Minecraft 1.21.11 Fabric instance:

1. Launch with Fabric API and VoidBoost only.
2. Launch again with Sodium 0.8.x installed.
3. Open the VoidBoost menu with **O** and test every tab/control.
4. Test the 60, 120, 144, 165, 180, 240 and Unlimited FPS presets.
5. Test render distance from 4 through 32 and confirm Dynamic Distance reacts under load.
6. Enter a world with many entities and particles and confirm the client remains stable.
7. Disable FPS Boost and confirm the captured vanilla video settings are restored.
8. Build with the Gradle wrapper and upload only the production remapped JAR.

VoidBoost is client-only and does not require installation on a server.
