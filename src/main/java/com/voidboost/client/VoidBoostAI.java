package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Permanent maximum-performance controller.
 *
 * There is no adaptive downgrade path: VoidBoost always enforces its
 * maximum-performance budgets. Keeping this controller deterministic also
 * avoids spending CPU time measuring the system just to decide whether to
 * reduce performance settings.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static long lastUpdate;

    private VoidBoostAI() {}

    public static void tick(Minecraft client) {
        long now = System.nanoTime();
        if (now - lastUpdate < 100_000_000L || client.level == null) return;
        lastUpdate = now;

        // Tier 0 is permanently locked: minimum simulation/render distance,
        // maximum framerate, aggressive particle/entity culling, no VSync.
        try {
            if (client.options.renderDistance().get() != 4) {
                client.options.renderDistance().set(4);
            }
            if (client.options.simulationDistance().get() != 4) {
                client.options.simulationDistance().set(4);
            }
            if (client.options.framerateLimit().get() != 260) {
                client.options.framerateLimit().set(260);
            }
            if (client.options.enableVsync().get()) {
                client.options.enableVsync().set(false);
            }
        } catch (RuntimeException ignored) {
            // Never let a version-specific option change crash the client.
        }

        VoidBoostRuntime.update(
                true,
                true,
                24,
                true,
                false,
                1,
                false
        );
    }

    public static int level() {
        return LOCKED_TIER;
    }

    public static int entityDistance(int configured) {
        return 24;
    }

    public static int particleBudget(int configured) {
        return 1;
    }

    public static int renderDistanceLimit(int configured) {
        return 4;
    }

    public static double fps() {
        return 0.0;
    }

    public static double pressure() {
        return 0.0;
    }

    public static double ramPressure() {
        return 0.0;
    }

    public static double cpuPressure() {
        return 0.0;
    }

    public static double entityPressure() {
        return 0.0;
    }
}
