package com.voidboost.client;

import net.minecraft.client.Minecraft;

public final class PerformanceHooks {
    private static int ticks;

    private PerformanceHooks() {}

    public static void tick(Minecraft client) {
        if (client.level == null) return;
        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.maxFpsPreset) return;
        if (++ticks < 20) return;
        ticks = 0;
        if (c.dynamicRenderDistance) {
            int target = Math.max(60, c.dynamicTargetFps);
            int fps = client.getFps();
            int distance = client.options.renderDistance().get();
            if (fps < target - 20 && distance > 4) client.options.renderDistance().set(distance - 1);
            else if (fps > target + 30 && distance < 16) client.options.renderDistance().set(distance + 1);
        }
    }
}
