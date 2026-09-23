package com.voidboost.client;

public final class VoidBoostStats {
    private static long windowStart = System.nanoTime();
    private static int particles;
    private static int particlesPerSecond;
    private static long lastFrameNanos;
    private static double smoothedFrameMs;

    private VoidBoostStats() {}

    public static void particleAttempt() {
        if (!VoidBoostConfig.get().performanceMonitor) return;
        particles++;
    }

    public static void frame() {
        if (!VoidBoostConfig.get().performanceMonitor) return;

        long now = System.nanoTime();

        if (lastFrameNanos != 0L) {
            double ms = (now - lastFrameNanos) / 1_000_000.0D;
            smoothedFrameMs = smoothedFrameMs == 0.0D
                    ? ms
                    : smoothedFrameMs * 0.9D + ms * 0.1D;
        }
        lastFrameNanos = now;

        if (now - windowStart >= 1_000_000_000L) {
            particlesPerSecond = particles;
            particles = 0;
            windowStart = now;
        }
    }

    public static void reset() {
        windowStart = System.nanoTime();
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
