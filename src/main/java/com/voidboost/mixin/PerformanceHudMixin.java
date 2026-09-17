package com.voidboost.mixin;

import com.voidboost.client.VoidBoostConfig;
import com.voidboost.client.VoidBoostStats;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class PerformanceHudMixin {
    private static int cachedFps;
    private static double cachedFrameMs;
    private static int cachedEntities;
    private static int cachedParticles;
    private static long cachedUsedMb;
    private static long cachedMaxMb;
    private static long nextHudUpdateNanos;

    @Inject(method = "render", at = @At("HEAD"))
    private void voidboost$measureFrame(GuiGraphics graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        VoidBoostStats.frame();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void voidboost$renderMonitor(GuiGraphics graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.performanceMonitor) return;

        Minecraft client = Minecraft.getInstance();
        long now = System.nanoTime();
        if (now >= nextHudUpdateNanos) {
            cachedFps = client.getFps();
            cachedFrameMs = VoidBoostStats.frameMs();
            cachedEntities = client.level == null ? 0 : client.level.getEntityCount();
            cachedParticles = VoidBoostStats.particlesPerSecond();

            Runtime runtime = Runtime.getRuntime();
            long used = runtime.totalMemory() - runtime.freeMemory();
            cachedUsedMb = used / (1024L * 1024L);
            cachedMaxMb = runtime.maxMemory() / (1024L * 1024L);
            nextHudUpdateNanos = now + 250_000_000L;
        }

        int x = 8;
        int y = 8;
        graphics.fill(x - 5, y - 5, x + 154, y + 81, 0xB0101014);
        graphics.fill(x - 5, y - 5, x + 154, y - 3, 0xFF6E8CFF);
        graphics.drawString(client.font, Component.literal("VoidBoost Monitor"), x, y + 2, 0xFFFFFFFF, false);
        graphics.drawString(client.font, Component.literal("FPS: " + cachedFps), x, y + 15, 0xFFFFFFFF, false);
        graphics.drawString(client.font, Component.literal("Frame: " + String.format(java.util.Locale.ROOT, "%.1f", cachedFrameMs) + " ms"), x, y + 28, 0xFFD0D0D0, false);
        graphics.drawString(client.font, Component.literal("RAM: " + cachedUsedMb + " / " + cachedMaxMb + " MB"), x, y + 41, 0xFFD0D0D0, false);
        graphics.drawString(client.font, Component.literal("Entities: " + cachedEntities), x, y + 54, 0xFFD0D0D0, false);
        graphics.drawString(client.font, Component.literal("Particles/s: " + cachedParticles), x, y + 67, 0xFFD0D0D0, false);
    }
}
