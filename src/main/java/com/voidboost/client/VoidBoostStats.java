package com.voidboost.client;

public final class VoidBoostStats {
    private static long windowStart = System.nanoTime();
    private static int particles;
    private static int particlesPerSecond;
    private static long lastFrameNanos;
    private static double smoothedFrameMs;

    private VoidBoostStats() {}

    public static void particleAttempt() {
        particles++;
    }

    public static void frame() {
        long now = System.nanoTime();
        if (lastFrameNanos != 0L) {
            double ms = (now - lastFrameNanos) / 1_000_000.0;
            smoothedFrameMs = smoothedFrameMs == 0.0 ? ms : smoothedFrameMs * 0.9 + ms * 0.1;
        }
        lastFrameNanos = now;
        if (now - windowStart >= 1_000_000_000L) {
            particlesPerSecond = particles;
            particles = 0;
            windowStart = now;
        }
    }

    public static int particlesPerSecond() {
        return particlesPerSecond;
    }

    public static double frameMs() {
        return smoothedFrameMs;
    }
}
