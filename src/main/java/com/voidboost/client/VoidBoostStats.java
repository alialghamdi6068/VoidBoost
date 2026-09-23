package com.voidboost.client;

public final class VoidBoostStats {
    private static long windowStartNanos;
    private static int particles;
    private static int particlesPerSecond;
    private static double smoothedFrameMs;

    private VoidBoostStats() {}

    /**
     * This method is called only while the optional monitor is enabled.
     */
    public static void particleAttempt() {
        particles++;
    }

    /**
     * Records one frame interval while the optional monitor is enabled.
     */
    public static void recordFrameMs(double ms) {
        if (ms <= 0.0D || ms > 1000.0D) return;
        smoothedFrameMs = smoothedFrameMs == 0.0D
                ? ms
                : smoothedFrameMs * 0.9D + ms * 0.1D;
    }

    /**
     * Rolls the particle counter into its one-second display window.
     */
    public static void updateParticleWindow(long now) {
        if (windowStartNanos == 0L) windowStartNanos = now;
        if (now - windowStartNanos >= 1_000_000_000L) {
            particlesPerSecond = particles;
            particles = 0;
            windowStartNanos = now;
        }
    }

    public static void reset() {
        windowStartNanos = System.nanoTime();
        particles = 0;
        particlesPerSecond = 0;
        smoothedFrameMs = 0.0D;
    }

    public static int particlesPerSecond() {
        return particlesPerSecond;
    }

    public static double frameMs() {
        return smoothedFrameMs;
    }
}
