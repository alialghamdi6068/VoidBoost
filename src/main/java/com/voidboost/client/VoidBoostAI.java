package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Local adaptive performance controller. It is intentionally lightweight: no
 * network, no external API and no heavyweight ML model.
 *
 * Tier 0 is the permanent maximum-performance tier. The controller may still
 * measure load for the monitor, but it never drops to a weaker tier.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;

    private static double smoothedFps = 144.0;
    private static double smoothedPressure;
    private static double smoothedRamPressure;
    private static double smoothedEntityPressure;
    private static int level = LOCKED_TIER;
    private static long lastUpdate;

    private VoidBoostAI() {}

    public static void tick(Minecraft client) {
        long now = System.nanoTime();
        if (now - lastUpdate < 25_000_000L || client.level == null) return;
        lastUpdate = now;

        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.performanceMode) {
            level = LOCKED_TIER;
            smoothedPressure *= 0.8;
            return;
        }

        // Tier 0 is permanently locked: maximum performance is always active.
        level = LOCKED_TIER;

        int fps = Math.max(1, client.getFps());
        smoothedFps = smoothedFps * 0.55 + fps * 0.45;

        int target = Math.max(60, Math.min(240, c.dynamicTargetFps));
        double fpsPressure = clamp((target - smoothedFps) / Math.max(30.0, target), 0.0, 1.0);

        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        double ramPressure = max <= 0 ? 0.0 : clamp((double) used / max, 0.0, 1.0);
        smoothedRamPressure = smoothedRamPressure * 0.65 + ramPressure * 0.35;

        int entities = client.level.getEntityCount();
        double entityPressure = clamp((entities - 40.0) / 180.0, 0.0, 1.0);
        smoothedEntityPressure = smoothedEntityPressure * 0.60 + entityPressure * 0.40;

        double pressure = Math.max(fpsPressure,
                Math.max(Math.max(0.0, smoothedRamPressure - 0.76) * 3.0,
                        smoothedEntityPressure * 0.80));
        smoothedPressure = smoothedPressure * 0.60 + pressure * 0.40;

        // Keep the strongest tier applied even when dynamic render distance is off.
        applyAdaptiveOptions(client, c);
    }

    /** Applies the permanent Tier 0 performance profile. */
    private static void applyAdaptiveOptions(Minecraft client, VoidBoostConfig c) {
        try {
            if (c.dynamicRenderDistance) {
                int configured = Math.max(4, Math.min(32, c.maxRenderDistance));
                int render = renderDistanceLimit(configured);
                if (client.options.renderDistance().get() > render) {
                    client.options.renderDistance().set(render);
                }

                int configuredSimulation = client.options.simulationDistance().get();
                int desiredSimulation = 4;
                desiredSimulation = Math.max(4, Math.min(configuredSimulation, desiredSimulation));
                if (configuredSimulation != desiredSimulation) {
                    client.options.simulationDistance().set(desiredSimulation);
                }
            }

            // Keep the user's configured FPS limit. Unlimited is Minecraft's 260 sentinel.
            if (c.maxFpsPreset || c.ultimateLocked) {
                client.options.framerateLimit().set(Math.max(30, Math.min(260, c.targetFps)));
            }
        } catch (RuntimeException ignored) {
            // A version-specific option must never crash the client.
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int level() {
        return LOCKED_TIER;
    }

    public static int entityDistance(int configured) {
        if (!VoidBoostConfig.get().performanceMode) return configured;
        return Math.max(32, configured - 24);
    }

    public static int particleBudget(int configured) {
        if (!VoidBoostConfig.get().performanceMode) return configured;
        return Math.min(configured, 5);
    }

    public static int renderDistanceLimit(int configured) {
        if (!VoidBoostConfig.get().performanceMode) return configured;
        return Math.max(4, configured - 2);
    }

    public static double fps() { return smoothedFps; }
    public static double pressure() { return smoothedPressure; }
    public static double ramPressure() { return smoothedRamPressure; }
    public static double entityPressure() { return smoothedEntityPressure; }
}
