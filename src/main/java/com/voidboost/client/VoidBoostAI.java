package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Lightweight local performance controller.
 *
 * This is deliberately heuristic rather than a machine-learning model:
 * a real ML loop would add CPU work and allocations to the exact hot path
 * VoidBoost is trying to protect. The controller samples only every 500 ms
 * and changes one independent optimization switch when the client is under
 * sustained frame pressure.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static final long SAMPLE_INTERVAL_NANOS = 500_000_000L;
    private static final int EMERGENCY_FPS_ON = 45;
    private static final int EMERGENCY_FPS_OFF = 58;

    private static boolean initialized;
    private static long sampleDeadline;
    private static int sampledFrames;
    private static boolean emergencyMode;

    private VoidBoostAI() {}

    public static void initialize(Minecraft client) {
        if (initialized || client == null) {
            return;
        }

        initialized = true;
        VoidBoostRuntime.enableMaximumPerformanceProfile();
        sampleDeadline = System.nanoTime() + SAMPLE_INTERVAL_NANOS;
    }

    /**
     * Called once per rendered GUI frame. The hot path is only a counter and
     * deadline check; no allocations, strings, collections, or API calls.
     */
    public static void sampleFrame() {
        if (!initialized) {
            return;
        }

        sampledFrames++;
        long now = System.nanoTime();

        if (now < sampleDeadline) {
            return;
        }

        int fps = (int) ((sampledFrames * 1_000_000_000L)
                / Math.max(1L, now - (sampleDeadline - SAMPLE_INTERVAL_NANOS)));

        sampledFrames = 0;
        sampleDeadline = now + SAMPLE_INTERVAL_NANOS;

        if (!emergencyMode && fps < EMERGENCY_FPS_ON) {
            emergencyMode = true;
        } else if (emergencyMode && fps >= EMERGENCY_FPS_OFF) {
            emergencyMode = false;
        }

        VoidBoostRuntime.setEmergencyPerformance(emergencyMode);
    }

    public static boolean emergencyMode() {
        return emergencyMode;
    }

    public static int level() {
        return LOCKED_TIER;
    }
}
