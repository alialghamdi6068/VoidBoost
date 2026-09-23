package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public final class VoidBoostAI {
    private static final int LOCKED_TIER = 0;
    private static final long SAMPLE_INTERVAL_NANOS = 500_000_000L;
    private static final int SAMPLE_EVERY_FRAMES = 8;

    private static final int MODE_MAXIMUM = 0;
    private static final int MODE_AGGRESSIVE = 1;
    private static final int MODE_EXTREME = 2;
    private static final int MODE_EMERGENCY = 3;

    private static final int AGGRESSIVE_FPS = 75;
    private static final int EXTREME_FPS = 60;
    private static final int EMERGENCY_FPS = 45;

    private static boolean initialized;
    private static Minecraft client;
    private static long sampleStartNanos;
    private static int sampledFrames;
    private static int frameDivider;
    private static int mode;

    private VoidBoostAI() {}

    public static void initialize(Minecraft minecraft) {
        if (initialized || minecraft == null) {
            return;
        }

        client = minecraft;
        initialized = true;
        mode = MODE_MAXIMUM;
        VoidBoostRuntime.enableMaximumPerformanceProfile();
        sampleStartNanos = System.nanoTime();
    }

    /**
     * Ultra-light frame sampler. The expensive clock read is performed once
     * every few frames, not on every rendered frame.
     */
    public static void sampleFrame() {
        if (!initialized) {
            return;
        }

        sampledFrames++;
        if (++frameDivider < SAMPLE_EVERY_FRAMES) {
            return;
        }
        frameDivider = 0;

        long now = System.nanoTime();
        if (now - sampleStartNanos < SAMPLE_INTERVAL_NANOS) {
            return;
        }

        long elapsed = Math.max(1L, now - sampleStartNanos);
        int fps = (int) Math.min(1000L,
                (sampledFrames * 1_000_000_000L) / elapsed);

        sampledFrames = 0;
        sampleStartNanos = now;

        int nextMode;
        if (fps < EMERGENCY_FPS) {
            nextMode = MODE_EMERGENCY;
        } else if (fps < EXTREME_FPS) {
            nextMode = MODE_EXTREME;
        } else if (fps < AGGRESSIVE_FPS) {
            nextMode = MODE_AGGRESSIVE;
        } else {
            nextMode = MODE_MAXIMUM;
        }

        if (nextMode != mode) {
            mode = nextMode;
            VoidBoostRuntime.setEmergencyPerformance(mode >= MODE_EMERGENCY);
        }
    }

    public static boolean emergencyMode() {
        return mode >= MODE_EMERGENCY;
    }

    public static int level() {
        return LOCKED_TIER;
    }

    /**
     * Culls only low-value visual clutter. Players, living entities and
     * projectiles are deliberately untouched so PvP/gameplay visibility stays
     * intact. The check only activates when the adaptive controller is under
     * sustained frame pressure.
     */
    public static boolean shouldCullEntity(Entity entity) {
        if (client == null || client.player == null || mode == MODE_MAXIMUM) {
            return false;
        }

        if (!(entity instanceof ItemEntity)
                && !(entity instanceof ExperienceOrb)
                && !(entity instanceof AreaEffectCloud)) {
            return false;
        }

        double distanceSq = entity.distanceToSqr(client.player);
        double limit = switch (mode) {
            case MODE_AGGRESSIVE -> 96.0D;
            case MODE_EXTREME -> 64.0D;
            default -> 40.0D;
        };

        return distanceSq > limit * limit;
    }

    public static int performanceMode() {
        return mode;
    }
}
