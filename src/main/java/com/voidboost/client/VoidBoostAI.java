package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Always-on VoidBoost controller.
 *
 * VoidBoost owns only its independent optimization switches. It never changes
 * Minecraft or Sodium video settings.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static boolean initialized;

    private VoidBoostAI() {}

    public static void initialize(Minecraft client) {
        if (initialized || client == null) {
            return;
        }

        initialized = true;
        VoidBoostRuntime.enableMaximumPerformanceProfile();
    }

    public static int level() {
        return LOCKED_TIER;
    }
}
