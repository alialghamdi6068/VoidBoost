# VoidBoost

Client-side performance and PvP rendering optimizer for Minecraft Java 1.21.11 on Fabric.

**Developer:** VoidFlame

## Performance profile

VoidBoost is an always-on Tier 0 optimizer. It uses a deliberately small hot path and focuses on work that can be removed before Minecraft creates or render-schedules the expensive object.

- No settings menu.
- No FPS/VSync/render-distance overrides.
- No Sodium settings overrides.
- Early particle rejection: particle creation is cancelled before Minecraft creates the particle instance.
- Zero network dependency: no external AI/API is used.
- Optional monitor: press O to show/hide diagnostics; when hidden, the monitor does not collect per-frame statistics.
- Client-only: no server installation is required.

## Sodium compatibility

VoidBoost does not inject into Sodium internals and does not take ownership of Sodium's video settings. You can freely change Sodium options while playing.

Minecraft 1.21.11 is on Sodium's maintained 0.8.x branch.

## Requirements

- Minecraft Java 1.21.11
- Fabric Loader 0.18.5 or newer
- Fabric API
- Java 21

## Build

The repository includes a GitHub Actions build workflow.

A local Gradle installation with Java 21 can build the project with:
gradle build

The production JAR is generated under build/libs/.

## Release checklist

Before publishing:

1. Build the production JAR successfully.
2. Test a clean Fabric 1.21.11 instance.
3. Test with Sodium installed.
4. Change Sodium video settings and confirm VoidBoost never resets them.
5. Test normal gameplay and PvP visibility.
6. Press O and verify the monitor opens/closes.
7. Inspect the generated JAR before upload.

## Performance note

VoidBoost does not promise a fixed FPS number. Actual performance depends on hardware, resolution, Sodium settings, shaders, resource packs, world complexity and other installed mods.

The goal is to remove avoidable client work without taking control away from the player's renderer settings.
