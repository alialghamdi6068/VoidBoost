package com.voidboost.client;

/**
 * Small immutable-style runtime snapshot used by VoidBoost's render hooks.
 *
 * The hot paths only read volatile primitives. VoidBoost never writes
 * Minecraft or Sodium video settings.
 */
public final class VoidBoostRuntime {
    private static volatile boolean entityCullingEnabled;
    private static volatile double entityDistanceSquared = 24.0D * 24.0D;
    private static volatile int particleMode;
    private static volatile int particleKeepPercent = 100;

    private VoidBoostRuntime() {}

    public static void update(
            boolean performanceEnabled,
            boolean entityOptimization,
            int entityDistance,
            boolean disableParticles,
            boolean reducedParticles,
            int particleBudget
    ) {
        entityCullingEnabled = performanceEnabled && entityOptimization;

        int distance = Math.max(24, Math.min(128, entityDistance));
        entityDistanceSquared = (double) distance * distance;

        if (!performanceEnabled) {
            particleMode = 0;
        } else if (disableParticles) {
            particleMode = 2;
        } else if (reducedParticles) {
            particleMode = 1;
        } else {
            particleMode = 0;
        }

        particleKeepPercent = Math.max(1, Math.min(100, particleBudget));
    }

    public static boolean entityCullingEnabled() {
        return entityCullingEnabled;
    }

    public static double entityDistanceSquared() {
        return entityDistanceSquared;
    }

    public static int particleMode() {
        return particleMode;
    }

    public static int particleKeepPercent() {
        return particleKeepPercent;
    }
}
