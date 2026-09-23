package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Always-on VoidBoost controller.
 *
 * This controller owns only VoidBoost's independent optimizations. It never
 * changes Minecraft or Sodium video settings.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static long lastUpdateNanos;

    private VoidBoostAI() {}

    public static void tick(Minecraft client) {
        long now = System.nanoTime();
        if (client.level == null || now - lastUpdateNanos < 250_000_000L) {
            return;
        }

        lastUpdateNanos = now;

        VoidBoostRuntime.update(
                true,
                true,
                24,
                true,
                false,
                1
        );
    }

    public static int level() {
        return LOCKED_TIER;
    }
}
