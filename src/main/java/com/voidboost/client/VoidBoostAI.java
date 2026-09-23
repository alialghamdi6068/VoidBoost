package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Lightweight local performance state.
 *
 * VoidBoost deliberately keeps Tier 0 locked. The old per-tick FPS controller
 * was removed because it did not change any renderer workload and therefore
 * only added CPU work. Performance-critical code should not poll FPS unless
 * the result changes something observable.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;

    private VoidBoostAI() {}

    public static void initialize(Minecraft client) {
        // Tier 0 is intentionally static. No background polling is required.
    }

    public static void tick(Minecraft client) {
        // Kept as a compatibility no-op for callers from older builds.
    }

    public static boolean emergencyMode() {
        return false;
    }

    public static int level() {
        return LOCKED_TIER;
    }
}
