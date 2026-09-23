package com.voidboost.client;

public final class VoidBoostStats {
    private static long windowStartNanos;
    private static long lastFrameNanos;
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
     * Records one completed frame and rolls the particle counter into its
     * one-second display window. Called only while the optional monitor is on.
     */
    public static void frame(long now) {
        if (lastFrameNanos != 0L) {
            double ms = (now - lastFrameNanos) / 1_000_000.0D;
            if (ms > 0.0D && ms <= 1000.0D) {
                smoothedFrameMs = smoothedFrameMs == 0.0D
                        ? ms
                        : smoothedFrameMs * 0.9D + ms * 0.1D;
            }
        }
        lastFrameNanos = now;

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
        lastFrameNanos = 0L;
        smoothedFrameMs = 0.0D;
    }

    public static int particlesPerSecond() {
        return particlesPerSecond;
    }

    public static double frameMs() {
        return smoothedFrameMs;
    }
}
