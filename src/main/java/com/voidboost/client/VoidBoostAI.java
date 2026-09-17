package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Local adaptive performance controller. It deliberately avoids a heavyweight
 * ML model: the controller uses cheap telemetry and a hysteresis state machine
 * so the optimizer itself does not become a source of frame-time overhead.
 */
public final class VoidBoostAI {
    private static double smoothedFps = 120.0;
    private static double smoothedPressure;
    private static double smoothedRamPressure;
    private static double smoothedEntityPressure;
    private static int level;
    private static int stableTicks;
    private static long lastUpdate;

    private VoidBoostAI() {}

    public static void tick(Minecraft client) {
        long now = System.nanoTime();
        if (now - lastUpdate < 250_000_000L || client.level == null) return;
        lastUpdate = now;

        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.performanceMode) {
            level = 0;
            smoothedPressure *= 0.8;
            return;
        }

        int fps = Math.max(1, client.getFps());
        smoothedFps = smoothedFps * 0.82 + fps * 0.18;

        int target = Math.max(60, Math.min(240, c.dynamicTargetFps));
        double fpsPressure = clamp((target - smoothedFps) / Math.max(30.0, target), 0.0, 1.0);

        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        double ramPressure = max <= 0 ? 0.0 : clamp((double) used / max, 0.0, 1.0);
        smoothedRamPressure = smoothedRamPressure * 0.82 + ramPressure * 0.18;

        int entities = client.level.getEntityCount();
        double entityPressure = clamp((entities - 80.0) / 320.0, 0.0, 1.0);
        smoothedEntityPressure = smoothedEntityPressure * 0.82 + entityPressure * 0.18;

        double pressure = Math.max(fpsPressure,
                Math.max(Math.max(0.0, smoothedRamPressure - 0.78) * 2.8,
                        smoothedEntityPressure * 0.75));
        smoothedPressure = smoothedPressure * 0.85 + pressure * 0.15;

        int strength = c.maxFpsPreset || c.ultimateLocked ? 5 : c.competitiveMode ? 4 : 3;
        int desired;
        if (smoothedPressure >= 0.82) desired = 4;
        else if (smoothedPressure >= 0.60) desired = Math.min(4, Math.max(3, strength));
        else if (smoothedPressure >= 0.38) desired = Math.min(3, Math.max(2, strength - 1));
        else if (smoothedPressure >= 0.18) desired = Math.min(2, Math.max(1, strength - 2));
        else desired = 0;

        if (desired == level) {
            stableTicks++;
            return;
        }

        // Hysteresis: four consecutive samples are required before changing tier.
        if (++stableTicks < 4) return;
        level = desired;
        stableTicks = 0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int level() {
        return level;
    }

    public static int entityDistance(int configured) {
        if (!VoidBoostConfig.get().performanceMode) return configured;
        return switch (level) {
            case 4 -> Math.max(32, configured - 24);
            case 3 -> Math.max(32, configured - 16);
            case 2 -> Math.max(32, configured - 8);
            default -> configured;
        };
    }

    public static int particleBudget(int configured) {
        if (!VoidBoostConfig.get().performanceMode) return configured;
        return switch (level) {
            case 4 -> Math.min(configured, 5);
            case 3 -> Math.min(configured, 12);
            case 2 -> Math.min(configured, 25);
            default -> configured;
        };
    }

    public static int renderDistanceLimit(int configured) {
        if (!VoidBoostConfig.get().performanceMode) return configured;
        return switch (level) {
            case 4 -> Math.max(4, configured - 2);
            case 3 -> Math.max(4, configured - 1);
            default -> configured;
        };
    }

    public static double fps() {
        return smoothedFps;
    }

    public static double pressure() {
        return smoothedPressure;
    }

    public static double ramPressure() {
        return smoothedRamPressure;
    }

    public static double entityPressure() {
        return smoothedEntityPressure;
    }
}
