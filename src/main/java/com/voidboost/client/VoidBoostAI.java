package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Local adaptive controller. It uses cheap runtime telemetry instead of a remote
 * model so it adds no network latency, API cost, or heavyweight ML dependency.
 */
public final class VoidBoostAI {
    private static double smoothedFps = 120.0;
    private static double smoothedFramePressure = 0.0;
    private static int level;
    private static int stableTicks;
    private static long lastUpdate;

    private VoidBoostAI() {}

    public static void tick(Minecraft client) {
        long now = System.nanoTime();
        if (now - lastUpdate < 250_000_000L || client.level == null) return;
        lastUpdate = now;

        int fps = Math.max(1, client.getFps());
        smoothedFps = smoothedFps * 0.82 + fps * 0.18;

        double framePressure = Math.max(0.0, Math.min(1.0, (120.0 - smoothedFps) / 120.0));
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        double ramPressure = max <= 0 ? 0.0 : Math.min(1.0, (double) used / max);
        double pressure = Math.max(framePressure, Math.max(0.0, ramPressure - 0.78) * 3.0);
        smoothedFramePressure = smoothedFramePressure * 0.85 + pressure * 0.15;

        int strength = VoidBoostConfig.get().aiReactionStrength;
        int desired;
        if (smoothedFramePressure > 0.70) desired = Math.min(4, 1 + strength / 2);
        else if (smoothedFramePressure > 0.45) desired = Math.min(3, strength);
        else if (smoothedFramePressure > 0.22) desired = Math.min(2, Math.max(1, strength - 1));
        else desired = 0;

        if (desired == level) {
            stableTicks++;
            return;
        }

        // Hysteresis prevents the controller from bouncing settings every update.
        if (++stableTicks < 4) return;
        level = desired;
        stableTicks = 0;
    }

    public static int level() {
        return level;
    }

    public static int entityDistance(int configured) {
        if (!VoidBoostConfig.get().aiOptimization) return configured;
        return switch (level) {
            case 4 -> Math.max(32, configured - 24);
            case 3 -> Math.max(32, configured - 16);
            case 2 -> Math.max(32, configured - 8);
            default -> configured;
        };
    }

    public static int particleBudget(int configured) {
        if (!VoidBoostConfig.get().aiOptimization) return configured;
        return switch (level) {
            case 4 -> Math.min(configured, 10);
            case 3 -> Math.min(configured, 20);
            case 2 -> Math.min(configured, 35);
            default -> configured;
        };
    }

    public static int renderDistanceLimit(int configured) {
        if (!VoidBoostConfig.get().aiOptimization) return configured;
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
        return smoothedFramePressure;
    }
}
