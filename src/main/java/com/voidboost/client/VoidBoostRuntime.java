package com.voidboost.client;

/**
 * Fixed VoidBoost performance profile used by the hot-path hooks.
 *
 * The profile never changes Minecraft or Sodium video settings.
 */
public final class VoidBoostRuntime {
    private static final boolean MAXIMUM_PERFORMANCE = true;
    private static boolean emergencyPerformance;

    private VoidBoostRuntime() {}

    public static void enableMaximumPerformanceProfile() {
        // Intentionally empty: the maximum profile is immutable after class load.
    }

    public static boolean maximumPerformance() {
        return MAXIMUM_PERFORMANCE;
    }

    public static boolean emergencyPerformance() {
        return emergencyPerformance;
    }

    public static void setEmergencyPerformance(boolean enabled) {
        emergencyPerformance = enabled;
    }
}
