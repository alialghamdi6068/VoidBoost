package com.voidboost.client;

public final class VoidBoostStats {
    private static long lastFrameNanos;
    private static double smoothedFrameMs;

    private VoidBoostStats() {}

    /**
     * Records one completed frame for the optional monitor.
     * This code is never touched while the monitor is disabled.
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
    }

    public static void reset() {
        lastFrameNanos = 0L;
        smoothedFrameMs = 0.0D;
    }

    public static double frameMs() {
        return smoothedFrameMs;
    }
}
