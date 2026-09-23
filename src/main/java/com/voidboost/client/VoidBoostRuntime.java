package com.voidboost.client;

/**
 * Immutable runtime switches for VoidBoost's hot-path hooks.
 *
 * The profile is configured once at client startup. Hot paths read simple
 * static primitives and never allocate or touch Minecraft/Sodium video options.
 */
public final class VoidBoostRuntime {
    private static boolean maximumPerformance;

    private VoidBoostRuntime() {}

    public static void enableMaximumPerformanceProfile() {
        maximumPerformance = true;
    }

    public static boolean maximumPerformance() {
        return maximumPerformance;
    }
}
