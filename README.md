# VoidBoost

Client-side performance toolkit for Minecraft Java 1.21.11 on Fabric.

## Current scope

- Client-only configuration stored locally in `config/voidboost.json`.
- VoidBoost settings entry from Video Settings.
- Particle suppression toggle.
- Reduced-particle toggle foundation.
- Optional dynamic render-distance control based on FPS.
- MAX FPS preset.
- Fabric 1.21.11 + Java 21 target.

VoidBoost does not synchronize these settings with a server or other players.

## Build

Use Java 21 and run:

```text
gradle build
```

The production JAR is generated under `build/libs/`.
