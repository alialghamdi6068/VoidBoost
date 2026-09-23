# VoidBoost

Client-side performance and PvP rendering optimizer for Minecraft Java 1.21.11 on Fabric.

**Developer:** VoidFlame

## Features

- **Always-on Tier 0** performance optimizations.
- **No settings menu.** VoidBoost is intentionally automatic.
- **Sodium-safe video settings.** VoidBoost never changes Render Distance, Simulation Distance, FPS limit, VSync, graphics quality, mipmaps, ambient occlusion, biome blend, or other Sodium/vanilla video options.
- **Optional performance monitor.** Press **O** to show or hide the diagnostic HUD. The key can be changed from Minecraft Controls.
- Aggressive particle culling.
- Distance-based culling for non-critical entities.
- Players and projectiles are preserved for PvP visibility.
- Item drops and XP orbs use a tighter culling distance.
- No external AI service and no network dependency at runtime.
- Client-only. No server installation is required.

## Requirements

- Minecraft Java 1.21.11
- Fabric Loader 0.18.5 or newer
- Fabric API
- Java 21

## Compatibility

VoidBoost does not inject into Sodium internals and does not take ownership of Sodium's settings. You can change Sodium options normally while playing; VoidBoost's independent culling hooks continue to run without resetting those options.

Minecraft 1.21.11 is supported by Sodium's maintained 0.8.x branch.

## Build

The repository includes a GitHub Actions build workflow. A local Gradle installation with Java 21 can build the project with:

`gradle build`

The production JAR is generated under `build/libs/`. Publish the remapped production JAR, not the sources JAR.

## Release checklist

Before publishing a release, verify on a clean Minecraft 1.21.11 Fabric instance:

1. Start with Fabric API and VoidBoost.
2. Confirm the game reaches the title screen and a world without crashes.
3. Change Sodium Render Distance, Simulation Distance, FPS limit, VSync and quality settings.
4. Confirm VoidBoost does not reset any of them.
5. Enter a world with normal particles and entities.
6. Confirm non-critical distant entities are culled while players/projectiles remain visible.
7. Press **O** and verify the monitor opens and closes.
8. Test with Sodium installed.
9. Test with the intended Sodium/Iris combination if you plan to list it as supported.
10. Build the production remapped JAR and inspect the generated artifact before upload.

## Important

VoidBoost does not promise a fixed FPS number. Actual performance varies with hardware, resolution, Sodium settings, shaders, resource packs, world complexity and other installed mods.
