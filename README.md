# VoidBoost

Client-side maximum-performance and PvP rendering optimizer for Minecraft Java 1.21.11 on Fabric.

**Developer:** VoidFlame

## Features

- **Always-on Tier 0**: the strongest VoidBoost performance profile is applied automatically on launch.
- **No settings menu**: there are no user performance profiles or downgrade controls.
- **No performance HUD**: VoidBoost does not add an FPS/RAM/CPU overlay.
- Render distance locked to **4 chunks**.
- Simulation distance locked to **4 chunks**.
- FPS limit locked to **Unlimited**.
- VSync disabled for lower frame pacing/input latency overhead.
- Aggressive particle culling.
- Aggressive non-critical entity culling while players and projectiles remain visible.
- Entity shadows, weather, clouds, vignette and ambient-occlusion work reduced where supported.
- Animation and fog optimization.
- No external AI service or network dependency.
- Designed to run alongside Sodium without injecting into Sodium internals.
- No server-side installation or configuration synchronization.

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

## Runtime behavior

VoidBoost intentionally does not expose tuning controls. Every launch starts with the maximum-performance profile, and the controller continuously re-enforces the critical FPS/rendering limits during gameplay.

VoidBoost is client-only and does not require installation on a server.

## Release verification

Before publishing a build, verify on a clean Minecraft 1.21.11 Fabric instance:

1. Launch with Fabric API and VoidBoost only.
2. Confirm there is no VoidBoost settings screen or performance HUD.
3. Confirm render distance stays at 4 chunks.
4. Confirm simulation distance stays at 4 chunks.
5. Confirm the FPS limit remains Unlimited and VSync remains disabled.
6. Confirm particles are aggressively culled.
7. Confirm players and projectiles remain visible while distant non-critical entities are culled.
8. Launch with a supported Sodium 0.8.x build and verify rendering remains stable.
9. Build with the Gradle wrapper and publish only the production remapped JAR.

VoidBoost does not claim a guaranteed FPS number; actual performance depends on hardware, Minecraft settings, world complexity and the other installed mods.
