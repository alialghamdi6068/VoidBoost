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

    private static volatile int effectiveEntityDistance = 40;
    private static volatile int effectiveParticleBudget = 100;
    private static volatile int effectiveRenderDistance = 10;
    private static volatile boolean performanceEnabled;

    private static long lastUpdate;
    private static long lastLoadSample;

    private VoidBoostAI() {}

    public static void tick(Minecraft client) {
        long now = System.nanoTime();
        if (now - lastUpdate < FAST_INTERVAL_NS || client.level == null) return;
        lastUpdate = now;

        VoidBoostConfig c = VoidBoostConfig.get();
        performanceEnabled = c.performanceMode;

        if (!c.performanceMode) {
            smoothedPressure *= 0.8;
            effectiveEntityDistance = c.maxEntityDistance;
            effectiveParticleBudget = c.particleLimitPercent;
            effectiveRenderDistance = c.maxRenderDistance;
            return;
        }

        // Tier 0 never downgrades. Load is measured only to tighten the active profile.
        int fps = Math.max(1, client.getFps());
        smoothedFps = smoothedFps * 0.55 + fps * 0.45;

        int target = Math.max(60, Math.min(240, c.dynamicTargetFps));
        double fpsPressure = clamp((target - smoothedFps) / Math.max(30.0, target), 0.0, 1.0);

        // FPS reacts every 25 ms. More expensive load metrics are sampled every 100 ms.
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

        updateEffectiveBudgets(c);
        applyTier0Options(client, c);
    }

    private static void updateEffectiveBudgets(VoidBoostConfig c) {
        int configuredEntity = Math.max(32, Math.min(128, c.maxEntityDistance));
        int entityBase = Math.max(32, configuredEntity - 24);
        if (smoothedPressure >= 0.70) {
            effectiveEntityDistance = 32;
        } else if (smoothedPressure >= 0.45) {
            effectiveEntityDistance = Math.max(32, entityBase - 8);
        } else {
            effectiveEntityDistance = entityBase;
        }

        int configuredParticles = Math.max(1, Math.min(100, c.particleLimitPercent));
        // Respect the user's particle limit. Only severe load can tighten it further.
        if (smoothedPressure >= 0.85) {
            effectiveParticleBudget = Math.min(configuredParticles, 5);
        } else if (smoothedPressure >= 0.65) {
            effectiveParticleBudget = Math.min(configuredParticles, 25);
        } else {
            effectiveParticleBudget = configuredParticles;
        }

        int configuredRender = Math.max(4, Math.min(32, c.maxRenderDistance));
        int base = Math.max(4, configuredRender - 2);
        if (smoothedPressure >= 0.75 || smoothedRamPressure >= 0.88) {
            effectiveRenderDistance = 4;
        } else if (smoothedPressure >= 0.55 || smoothedRamPressure >= 0.82) {
            effectiveRenderDistance = Math.max(4, base - 2);
        } else if (smoothedPressure >= 0.35 || smoothedRamPressure >= 0.76) {
            effectiveRenderDistance = Math.max(4, base - 1);
        } else {
            effectiveRenderDistance = base;
        }
    }

    /** Applies the permanent Tier 0 performance profile. */
    private static void applyTier0Options(Minecraft client, VoidBoostConfig c) {
        try {
            if (c.dynamicRenderDistance && client.options.renderDistance().get() > effectiveRenderDistance) {
                client.options.renderDistance().set(effectiveRenderDistance);
            }

            // FPS is managed by the normal config option pipeline. Do not override
            // the user's manual 60/120/144/165/180/240/Unlimited selection here.
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
     * Cached hot-path distance. Players and projectiles are exempt in EntityRenderMixin.
     */
    public static int entityDistance(int configured) {
        return performanceEnabled ? effectiveEntityDistance : configured;
    }

    /**
     * Cached hot-path particle budget.
     */
    public static int particleBudget(int configured) {
        return performanceEnabled ? Math.min(configured, effectiveParticleBudget) : configured;
    }

    /**
     * Cached adaptive render-distance limit.
     */
    public static int renderDistanceLimit(int configured) {
        return performanceEnabled ? Math.min(configured, effectiveRenderDistance) : configured;
    }

    public static double fps() { return smoothedFps; }
    public static double pressure() { return smoothedPressure; }
    public static double ramPressure() { return smoothedRamPressure; }
    public static double entityPressure() { return smoothedEntityPressure; }
}
