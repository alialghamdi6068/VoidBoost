package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Lightweight always-on controller.
 *
 * VoidBoost deliberately does not modify Minecraft/Sodium video settings.
 * Users keep full control of render distance, simulation distance, FPS,
 * VSync, quality, and every other renderer option.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static long lastUpdate;

    private VoidBoostAI() {}

    public static void tick(Minecraft client) {
        long now = System.nanoTime();
        if (now - lastUpdate < 100_000_000L || client.level == null) return;
        lastUpdate = now;

        // Only apply VoidBoost's independent runtime culling.
        VoidBoostRuntime.update(
                true,
                true,
                24,
                true,
                false,
                1,
                VoidBoostConfig.get().performanceMonitor
        );
    }

    public static int level() {
        return LOCKED_TIER;
    }

    public static int entityDistance(int configured) {
        return 24;
    }

    public static int particleBudget(int configured) {
        return 1;
    }

    public static int renderDistanceLimit(int configured) {
        // Informational only. VoidBoost never changes the user's render distance.
        return configured;
    }

    public static double fps() {
        return 0.0;
    }

    public static double pressure() {
        return 0.0;
    }

    public static double ramPressure() {
        return 0.0;
    }

    public static double cpuPressure() {
        return 0.0;
    }

    public static double entityPressure() {
        return 0.0;
    }
}
