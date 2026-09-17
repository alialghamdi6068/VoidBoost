package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Local adaptive performance controller. It is intentionally lightweight: no
 * network, no external API and no heavyweight ML model. It reacts quickly to
 * FPS drops while using smoothing/hysteresis to avoid oscillation.
 */
public final class VoidBoostAI {
    private static double smoothedFps = 120.0;
    private static double smoothedPressure;
    private static double smoothedRamPressure;
    private static double smoothedEntityPressure;
    private static int level;
    private static int stableTicks;
    private static long lastUpdate;
    private static boolean initialized;

    private VoidBoostAI() {}

    public static void tick(Minecraft client) {
        long now = System.nanoTime();
        if (now - lastUpdate < 100_000_000L || client.level == null) return;
        lastUpdate = now;

        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.performanceMode) {
            level = 0;
            stableTicks = 0;
            initialized = false;
            smoothedPressure *= 0.8;
            return;
        }

        int fps = Math.max(1, client.getFps());
        smoothedFps = smoothedFps * 0.78 + fps * 0.22;

        int target = Math.max(60, Math.min(240, c.dynamicTargetFps));
        double fpsPressure = clamp((target - smoothedFps) / Math.max(30.0, target), 0.0, 1.0);

        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        double ramPressure = max <= 0 ? 0.0 : clamp((double) used / max, 0.0, 1.0);
        smoothedRamPressure = smoothedRamPressure * 0.82 + ramPressure * 0.18;

        int entities = client.level.getEntityCount();
        double entityPressure = clamp((entities - 60.0) / 260.0, 0.0, 1.0);
        smoothedEntityPressure = smoothedEntityPressure * 0.82 + entityPressure * 0.18;

        double pressure = Math.max(fpsPressure,
                Math.max(Math.max(0.0, smoothedRamPressure - 0.76) * 3.0,
                        smoothedEntityPressure * 0.80));
        smoothedPressure = smoothedPressure * 0.84 + pressure * 0.16;

        int strength = c.ultimateLocked ? 5 : c.maxFpsPreset ? 5 : c.competitiveMode ? 4 : 3;

        // Start optimizing immediately instead of waiting for a performance drop.
        if (!initialized) {
            initialized = true;
            level = strength >= 5 ? 3 : strength >= 4 ? 2 : 1;
            stableTicks = 0;
            return;
        }

        // Severe frame spikes bypass hysteresis so sudden drops are handled fast.
        if (fps < target * 0.55 || (smoothedFps > 20.0 && fps < smoothedFps * 0.60)) {
            level = Math.min(4, Math.max(level + 1, strength >= 5 ? 3 : 2));
            stableTicks = 0;
            return;
        }

        int desired;
        if (smoothedPressure >= 0.82) desired = 4;
        else if (smoothedPressure >= 0.60) desired = Math.min(4, Math.max(3, strength));
        else if (smoothedPressure >= 0.38) desired = Math.min(3, Math.max(2, strength - 1));
        else if (smoothedPressure >= 0.18) desired = Math.min(2, Math.max(1, strength - 2));
        else desired = 0;

        // Never immediately return to an expensive state; recovery is gradual.
        if (desired > level) {
            level = desired;
            stableTicks = 0;
            return;
        }

        if (desired == level) {
            stableTicks++;
            return;
        }

        // Twelve consecutive samples are required before reducing optimization.
        if (++stableTicks < 12) return;
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
