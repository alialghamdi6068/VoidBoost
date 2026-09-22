package com.voidboost.client;

import com.sun.management.OperatingSystemMXBean;
import net.minecraft.client.Minecraft;

import java.lang.management.ManagementFactory;

/**
 * Local adaptive performance controller.
 *
 * Tier 0 is permanently locked as the maximum-performance profile. Metrics
 * are sampled cheaply so the controller can react quickly without becoming
 * another source of frame-time overhead.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static final long FAST_INTERVAL_NS = 50_000_000L;
    private static final long LOAD_SAMPLE_INTERVAL_NS = 250_000_000L;

    private static final OperatingSystemMXBean OS_BEAN = getOperatingSystemBean();

    private static double smoothedFps = 240.0;
    private static double smoothedPressure;
    private static double smoothedRamPressure;
    private static double smoothedCpuPressure;
    private static double smoothedEntityPressure;

    private static volatile int effectiveEntityDistance = 64;
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
            effectiveEntityDistance = clampInt(c.maxEntityDistance, 32, 128);
            effectiveParticleBudget = clampInt(c.particleLimitPercent, 1, 100);
            effectiveRenderDistance = clampInt(c.maxRenderDistance, 4, 32);
            return;
        }

        int fps = Math.max(1, client.getFps());
        smoothedFps = smoothedFps * 0.70 + fps * 0.30;

        // Tier 0 targets the full high-FPS range rather than settling around 120 FPS.
        int target = Math.max(120, Math.min(240, c.dynamicTargetFps));
        double fpsPressure = clamp((target - smoothedFps) / Math.max(30.0, target), 0.0, 1.0);

        // Expensive OS metrics are sampled less often; the FPS signal stays responsive.
        if (now - lastLoadSample >= LOAD_SAMPLE_INTERVAL_NS) {
            lastLoadSample = now;

            Runtime runtime = Runtime.getRuntime();
            long maxHeap = runtime.maxMemory();
            long usedHeap = runtime.totalMemory() - runtime.freeMemory();
            double heapPressure = maxHeap <= 0 ? 0.0 : clamp((double) usedHeap / maxHeap, 0.0, 1.0);

            double physicalRamPressure = heapPressure;
            double processCpuPressure = 0.0;
            if (OS_BEAN != null) {
                long totalMemory = OS_BEAN.getTotalMemorySize();
                long freeMemory = OS_BEAN.getFreeMemorySize();
                if (totalMemory > 0L) {
                    physicalRamPressure = clamp((double) (totalMemory - freeMemory) / totalMemory, 0.0, 1.0);
                }

                double cpu = OS_BEAN.getProcessCpuLoad();
                if (cpu >= 0.0) {
                    processCpuPressure = clamp(cpu, 0.0, 1.0);
                }
            }

            double ramPressure = Math.max(heapPressure, physicalRamPressure);
            smoothedRamPressure = smoothedRamPressure * 0.65 + ramPressure * 0.35;
            smoothedCpuPressure = smoothedCpuPressure * 0.65 + processCpuPressure * 0.35;

            int entities = client.level.getEntityCount();
            double entityPressure = clamp((entities - 40.0) / 180.0, 0.0, 1.0);
            smoothedEntityPressure = smoothedEntityPressure * 0.60 + entityPressure * 0.40;
        }

        double ramLoad = Math.max(0.0, smoothedRamPressure - 0.76) * 3.0;
        double cpuLoad = Math.max(0.0, smoothedCpuPressure - 0.72) * 3.0;
        double entityLoad = smoothedEntityPressure * 0.80;
        double pressure = Math.max(fpsPressure, Math.max(Math.max(ramLoad, cpuLoad), entityLoad));
        smoothedPressure = smoothedPressure * 0.60 + clamp(pressure, 0.0, 1.0) * 0.40;

        updateEffectiveBudgets(c);
        applyTier0Options(client, c);
    }

    private static void updateEffectiveBudgets(VoidBoostConfig c) {
        int configuredEntity = clampInt(c.maxEntityDistance, 32, 128);

        // React before FPS becomes severely low. This lets Tier 0 recover FPS sooner.
        if (smoothedPressure >= 0.35) {
            effectiveEntityDistance = 32;
        } else if (smoothedPressure >= 0.20) {
            effectiveEntityDistance = Math.max(32, configuredEntity - 16);
        } else if (smoothedPressure >= 0.10) {
            effectiveEntityDistance = Math.max(32, configuredEntity - 8);
        } else {
            effectiveEntityDistance = configuredEntity;
        }

        int configuredParticles = clampInt(c.particleLimitPercent, 1, 100);
        if (smoothedPressure >= 0.70) {
            effectiveParticleBudget = Math.min(configuredParticles, 10);
        } else if (smoothedPressure >= 0.45) {
            effectiveParticleBudget = Math.min(configuredParticles, 25);
        } else if (smoothedPressure >= 0.20) {
            effectiveParticleBudget = Math.min(configuredParticles, 50);
        } else {
            effectiveParticleBudget = configuredParticles;
        }

        int configuredRender = clampInt(c.maxRenderDistance, 4, 32);
        if (smoothedPressure >= 0.60 || smoothedRamPressure >= 0.88 || smoothedCpuPressure >= 0.92) {
            effectiveRenderDistance = 4;
        } else if (smoothedPressure >= 0.35 || smoothedRamPressure >= 0.82 || smoothedCpuPressure >= 0.84) {
            effectiveRenderDistance = Math.max(4, configuredRender - 2);
        } else if (smoothedPressure >= 0.20 || smoothedRamPressure >= 0.76 || smoothedCpuPressure >= 0.76) {
            effectiveRenderDistance = Math.max(4, configuredRender - 1);
        } else {
            effectiveRenderDistance = configuredRender;
        }
    }

    /** Applies the permanent Tier 0 performance profile. */
    private static void applyTier0Options(Minecraft client, VoidBoostConfig c) {
        try {
            if (c.dynamicRenderDistance && client.options.renderDistance().get() > effectiveRenderDistance) {
                client.options.renderDistance().set(effectiveRenderDistance);
            }
        } catch (RuntimeException ignored) {
            // Version-specific option changes must never crash the client.
        }
    }

    private static OperatingSystemMXBean getOperatingSystemBean() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        return bean instanceof OperatingSystemMXBean os ? os : null;
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int level() {
        return LOCKED_TIER;
    }

    public static int entityDistance(int configured) {
        return performanceEnabled ? effectiveEntityDistance : configured;
    }

    public static int particleBudget(int configured) {
        return performanceEnabled ? Math.min(configured, effectiveParticleBudget) : configured;
    }

    public static int renderDistanceLimit(int configured) {
        return performanceEnabled ? Math.min(configured, effectiveRenderDistance) : configured;
    }

    public static double fps() { return smoothedFps; }
    public static double pressure() { return smoothedPressure; }
    public static double ramPressure() { return smoothedRamPressure; }
    public static double cpuPressure() { return smoothedCpuPressure; }
    public static double entityPressure() { return smoothedEntityPressure; }
}
