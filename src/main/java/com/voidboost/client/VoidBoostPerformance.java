package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Lightweight client-only performance controller. */
public final class VoidBoostPerformance {
    private static int tickCounter;
    private static int lastDistance = -1;

    private VoidBoostPerformance() {}

    public static void tick(Minecraft client) {
        if (client.level == null) return;
        if (++tickCounter < 20) return;
        tickCounter = 0;

        VoidBoostConfig config = VoidBoostConfig.get();
        if (!config.dynamicRenderDistance) return;

        double fps = client.getFps();
        int target = Math.max(30, config.dynamicTargetFps);
        int current = client.options.renderDistance().get();
        int next = current;

        if (fps < target - 15 && current > 4) next = current - 1;
        else if (fps > target + 25 && current < 20) next = current + 1;

        if (next != current && next != lastDistance) {
            client.options.renderDistance().set(next);
            lastDistance = next;
        }
    }

    public static boolean shouldRenderParticle() {
        VoidBoostConfig c = VoidBoostConfig.get();
        return !c.disableParticles && !c.reducedParticles;
    }

    public static boolean shouldRenderReducedParticle() {
        return VoidBoostConfig.get().reducedParticles && !VoidBoostConfig.get().disableParticles;
    }
}
