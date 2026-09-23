package com.voidboost.client;

import net.minecraft.client.Minecraft;

/**
 * Lightweight local performance controller.
 *
 * The controller deliberately does not run a machine-learning model. It only
 * samples Minecraft's existing FPS value twice per second and changes one
 * proven workload-reduction switch when sustained frame pressure is detected.
 * This keeps VoidBoost's own CPU/RAM cost negligible during normal play.
 */
public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static final int TICKS_PER_SAMPLE = 10;
    private static final int ENTER_EMERGENCY_FPS = 45;
    private static final int EXIT_EMERGENCY_FPS = 58;

    private static boolean initialized;
    private static int ticksUntilSample;
    private static boolean emergencyMode;

    private VoidBoostAI() {}

    public static void initialize(Minecraft client) {
        if (initialized || client == null) {
            return;
        }

        initialized = true;
        ticksUntilSample = TICKS_PER_SAMPLE;
        emergencyMode = false;
    }

    /**
     * Called from the client tick, not the render loop.
     * The controller performs a real decision only twice per second.
     */
    public static void tick(Minecraft client) {
        if (!initialized || client == null) {
            return;
        }

        if (--ticksUntilSample > 0) {
            return;
        }

        ticksUntilSample = TICKS_PER_SAMPLE;

        int fps = client.getFps();
        if (!emergencyMode) {
            if (fps > 0 && fps < ENTER_EMERGENCY_FPS) {
                emergencyMode = true;
            }
        } else if (fps >= EXIT_EMERGENCY_FPS) {
            emergencyMode = false;
        }
    }

    public static boolean emergencyMode() {
        return emergencyMode;
    }

    public static int level() {
        return LOCKED_TIER;
    }
}
