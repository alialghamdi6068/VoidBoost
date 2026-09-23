package com.voidboost.client;

/**
 * Minimal VoidBoost runtime state.
 *
 * VoidBoost intentionally has no settings screen and never overwrites
 * Minecraft or Sodium video options.
 */
public final class VoidBoostConfig {
    private static boolean performanceMonitor;

    private VoidBoostConfig() {}

    public static boolean isPerformanceMonitorEnabled() {
        return performanceMonitor;
    }

    public static void load() {
        performanceMonitor = false;
        VoidBoostStats.reset();
    }

    public static void togglePerformanceMonitor() {
        performanceMonitor = !performanceMonitor;
        VoidBoostStats.reset();
    }
}
