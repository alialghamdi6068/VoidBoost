package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Minimal VoidBoost runtime state.
 *
 * VoidBoost intentionally has no settings screen and never overwrites
 * Minecraft/Sodium video options. The player's renderer settings remain
 * completely under their control.
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
        VoidBoostStats.reset();
    }

    public static void tick(Minecraft client) {
        // Deliberately empty. Never fight Sodium or vanilla video settings.
    }

    public static void togglePerformanceMonitor() {
        INSTANCE.performanceMonitor = !INSTANCE.performanceMonitor;
        VoidBoostStats.reset();
    }

    /**
     * Kept as a harmless compatibility hook for older integrations.
     */
    public static void markDirty() {
        // No persistent settings exist.
    }
}
