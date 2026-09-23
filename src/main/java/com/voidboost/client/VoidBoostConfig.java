package com.voidboost.client;

/**
 * Minimal runtime state for VoidBoost.
 *
 * VoidBoost never owns Minecraft/Sodium video settings. The only user-facing
 * state is the optional diagnostic monitor.
 */
public final class VoidBoostConfig {
    private static final VoidBoostConfig INSTANCE = new VoidBoostConfig();

    public boolean performanceMonitor;

    private VoidBoostConfig() {}

    public static VoidBoostConfig get() {
        return INSTANCE;
    }

    public static void load() {
        INSTANCE.performanceMonitor = false;
    }

    public static void tick(net.minecraft.client.Minecraft client) {
        // Intentionally empty: VoidBoost must not overwrite Sodium settings.
    }

    public static void togglePerformanceMonitor() {
        INSTANCE.performanceMonitor = !INSTANCE.performanceMonitor;
    }

    public static void markDirty() {
        // Kept for compatibility with older integrations.
    }
}
