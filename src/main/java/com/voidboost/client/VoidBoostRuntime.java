package com.voidboost.client;

/**
 * Immutable-style hot-path snapshot for VoidBoost's rendering hooks.
 *
 * The snapshot is refreshed from the client tick thread. Rendering hooks only
 * read primitive volatile fields, avoiding config lookups and adaptive-controller
 * work inside per-entity/per-particle paths.
 */
public final class VoidBoostRuntime {
    private static volatile boolean entityCullingEnabled;
    private static volatile double entityDistanceSquared = 128.0 * 128.0;

    // 0 = no filtering, 1 = sampled/no stats, 2 = blocked/no stats,
    // 3 = sampled/with stats, 4 = blocked/with stats.
    private static volatile int particleMode;
    private static volatile int particleKeepPercent = 100;

    private VoidBoostRuntime() {}

    public static void update(
            boolean performanceEnabled,
            boolean entityOptimization,
            int entityDistance,
            boolean disableParticles,
            boolean reducedParticles,
            int particleBudget,
            boolean performanceMonitor
    ) {
        entityCullingEnabled = performanceEnabled && entityOptimization;
        int distance = Math.max(32, Math.min(128, entityDistance));
        entityDistanceSquared = (double) distance * distance;

        if (!performanceEnabled) {
            particleMode = 0;
        } else if (disableParticles) {
            particleMode = performanceMonitor ? 4 : 2;
        } else if (reducedParticles) {
            particleMode = performanceMonitor ? 3 : 1;
        } else {
            particleMode = 0;
        }

        particleKeepPercent = Math.max(1, Math.min(100, particleBudget));
    }

    public static boolean entityCullingEnabled() { return entityCullingEnabled; }
    public static double entityDistanceSquared() { return entityDistanceSquared; }
    public static int particleMode() { return particleMode; }
    public static int particleKeepPercent() { return particleKeepPercent; }
}
