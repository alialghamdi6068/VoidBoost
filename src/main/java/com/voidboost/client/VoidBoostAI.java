package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Lightweight local performance controller.
 *
 * Tier 0 is permanently selected. The controller is intentionally rule-based,
 * not an external AI service or machine-learning model. It samples Minecraft's
 * own FPS counter from the client tick and applies a conservative emergency
 * workload reduction only after repeated low-FPS samples.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static final int TICKS_PER_SAMPLE = 10;
    private static final int LOW_FPS = 45;
    private static final int RECOVERY_FPS = 58;
    private static final int LOW_SAMPLES_TO_ENTER = 3;
    private static final int GOOD_SAMPLES_TO_EXIT = 4;

    private static boolean initialized;
    private static int ticksUntilSample;
    private static int lowSamples;
    private static int goodSamples;
    private static boolean emergencyMode;

    private VoidBoostAI() {}

    public static void initialize(Minecraft client) {
        if (initialized || client == null) return;
        initialized = true;
        ticksUntilSample = TICKS_PER_SAMPLE;
        lowSamples = 0;
        goodSamples = 0;
        emergencyMode = false;
    }

    public static void tick(Minecraft client) {
        if (!initialized || client == null || client.level == null) return;
        if (--ticksUntilSample > 0) return;
        ticksUntilSample = TICKS_PER_SAMPLE;

        int fps = client.getFps();
        if (fps <= 0) return;

        if (!emergencyMode) {
            goodSamples = 0;
            if (fps < LOW_FPS) {
                lowSamples = Math.min(LOW_SAMPLES_TO_ENTER, lowSamples + 1);
                if (lowSamples >= LOW_SAMPLES_TO_ENTER) {
                    emergencyMode = true;
                    lowSamples = 0;
                }
            } else {
                lowSamples = 0;
            }
            return;
        }

        lowSamples = 0;
        if (fps >= RECOVERY_FPS) {
            goodSamples = Math.min(GOOD_SAMPLES_TO_EXIT, goodSamples + 1);
            if (goodSamples >= GOOD_SAMPLES_TO_EXIT) {
                emergencyMode = false;
                goodSamples = 0;
            }
        } else {
            goodSamples = 0;
        }
    }

    public static boolean emergencyMode() {
        return emergencyMode;
    }

    public static int level() {
        return LOCKED_TIER;
    }
}
