package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Local adaptive performance controller.
 *
 * Tier 0 is permanently locked as the maximum-performance profile. Metrics
 * are sampled cheaply so the controller can react quickly without becoming
 * another source of frame-time overhead.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static final long FAST_INTERVAL_NS = 25_000_000L;
    private static final long LOAD_SAMPLE_INTERVAL_NS = 100_000_000L;

    private static double smoothedFps = 144.0;
    private static double smoothedPressure;
    private static double smoothedRamPressure;
    private static double smoothedEntityPressure;
    private static int level = LOCKED_TIER;
    private static long lastUpdate;
    private static long lastLoadSample;

    private VoidBoostAI() {}

    public static void tick(Minecraft client) {
        long now = System.nanoTime();
        if (now - lastUpdate < FAST_INTERVAL_NS || client.level == null) return;
        lastUpdate = now;

        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.performanceMode) {
            level = LOCKED_TIER;
            smoothedPressure *= 0.8;
            return;
        }

        // Tier 0 never downgrades. Load is still measured for the monitor.
        level = LOCKED_TIER;

        int fps = Math.max(1, client.getFps());
        smoothedFps = smoothedFps * 0.55 + fps * 0.45;

        int target = Math.max(60, Math.min(240, c.dynamicTargetFps));
        double fpsPressure = clamp((target - smoothedFps) / Math.max(30.0, target), 0.0, 1.0);

        // FPS reacts every 25 ms. More expensive load metrics are cached at 100 ms.
        if (now - lastLoadSample >= LOAD_SAMPLE_INTERVAL_NS) {
            lastLoadSample = now;

            Runtime runtime = Runtime.getRuntime();
            long max = runtime.maxMemory();
            long used = runtime.totalMemory() - runtime.freeMemory();
            double ramPressure = max <= 0 ? 0.0 : clamp((double) used / max, 0.0, 1.0);
            smoothedRamPressure = smoothedRamPressure * 0.65 + ramPressure * 0.35;

            int entities = client.level.getEntityCount();
            double entityPressure = clamp((entities - 40.0) / 180.0, 0.0, 1.0);
            smoothedEntityPressure = smoothedEntityPressure * 0.60 + entityPressure * 0.40;
        }

        double pressure = Math.max(fpsPressure,
                Math.max(Math.max(0.0, smoothedRamPressure - 0.76) * 3.0,
                        smoothedEntityPressure * 0.80));
        smoothedPressure = smoothedPressure * 0.60 + pressure * 0.40;

        applyTier0Options(client, c);
    }

    /** Applies the permanent Tier 0 performance profile. */
    private static void applyTier0Options(Minecraft client, VoidBoostConfig c) {
        try {
            if (c.dynamicRenderDistance) {
                int configured = Math.max(4, Math.min(32, c.maxRenderDistance));
                int render = renderDistanceLimit(configured);
                if (client.options.renderDistance().get() > render) {
                    client.options.renderDistance().set(render);
                }

                // Keep simulation work at the minimum while Tier 0 is active.
                if (client.options.simulationDistance().get() > 4) {
                    client.options.simulationDistance().set(4);
                }
            }

            // Unlimited is represented by Minecraft's 260 sentinel.
            if (c.maxFpsPreset || c.ultimateLocked) {
                int targetFps = Math.max(30, Math.min(260, c.targetFps));
                if (client.options.framerateLimit().get() != targetFps) {
                    client.options.framerateLimit().set(targetFps);
                }
            }
        } catch (RuntimeException ignored) {
            // Version-specific option changes must never crash the client.
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int level() {
        return LOCKED_TIER;
    }

    /**
     * CPU/entity pressure dynamically tightens render distance for non-critical
     * entities. Players and projectiles are exempt in EntityRenderMixin.
     */
    public static int entityDistance(int configured) {
        if (!VoidBoostConfig.get().performanceMode) return configured;

        int base = Math.max(32, configured - 24);
        if (smoothedPressure >= 0.70) return 32;
        if (smoothedPressure >= 0.45) return Math.max(32, base - 8);
        return base;
    }

    /**
     * Keep particle work tiny under Tier 0, with an emergency zero-particle path
     * only when the frame/load pressure is very high.
     */
    public static int particleBudget(int configured) {
        if (!VoidBoostConfig.get().performanceMode) return configured;
        if (smoothedPressure >= 0.85) return 1;
        return Math.min(configured, 5);
    }

    /**
     * RAM/FPS pressure can lower chunk rendering automatically without changing
     * the user's configured maximum permanently.
     */
    public static int renderDistanceLimit(int configured) {
        if (!VoidBoostConfig.get().performanceMode) return configured;

        int base = Math.max(4, configured - 2);
        if (smoothedPressure >= 0.75 || smoothedRamPressure >= 0.88) return 4;
        if (smoothedPressure >= 0.55 || smoothedRamPressure >= 0.82) return Math.max(4, base - 2);
        if (smoothedPressure >= 0.35 || smoothedRamPressure >= 0.76) return Math.max(4, base - 1);
        return base;
    }

    public static double fps() { return smoothedFps; }
    public static double pressure() { return smoothedPressure; }
    public static double ramPressure() { return smoothedRamPressure; }
    public static double entityPressure() { return smoothedEntityPressure; }
}
