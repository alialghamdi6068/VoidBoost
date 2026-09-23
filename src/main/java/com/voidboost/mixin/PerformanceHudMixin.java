package com.voidboost.mixin;

import com.voidboost.client.VoidBoostConfig;
import com.voidboost.client.VoidBoostStats;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

@Mixin(Gui.class)
public abstract class PerformanceHudMixin {
    @Unique
    private static int cachedFps;
    @Unique
    private static String cachedFrameText = "0.0 ms";
    @Unique
    private static int cachedEntities;
    @Unique
    private static int cachedParticles;
    @Unique
    private static String cachedRamText = "0 / 0 MB";
    @Unique
    private static String cachedCpuText = "CPU: --";
    @Unique
    private static long nextHudUpdateNanos;

    @Inject(method = "render", at = @At("HEAD"))
    private void voidboost$measureFrame(
            GuiGraphics graphics,
            DeltaTracker tickCounter,
            CallbackInfo ci
    ) {
        if (VoidBoostConfig.isPerformanceMonitorEnabled()) {
            VoidBoostStats.frame();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void voidboost$renderMonitor(
            GuiGraphics graphics,
            DeltaTracker tickCounter,
            CallbackInfo ci
    ) {
        if (!VoidBoostConfig.isPerformanceMonitorEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        long now = System.nanoTime();

        if (now >= nextHudUpdateNanos) {
            cachedFps = client.getFps();
            cachedFrameText = String.format(
                    java.util.Locale.ROOT,
                    "%.1f ms",
                    VoidBoostStats.frameMs()
            );
            cachedEntities = client.level == null ? 0 : client.level.getEntityCount();

            Runtime runtime = Runtime.getRuntime();
            long used = runtime.totalMemory() - runtime.freeMemory();
            long max = runtime.maxMemory();
            cachedRamText = (used / (1024L * 1024L)) + " / " + (max / (1024L * 1024L)) + " MB";

            cachedParticles = VoidBoostStats.particlesPerSecond();

            OperatingSystemMXBean osBean =
                    ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double cpu = osBean == null ? -1.0D : osBean.getProcessCpuLoad();
            cachedCpuText = "CPU: " + (cpu < 0.0D ? "--" : Math.round(cpu * 100.0D) + "%");

            nextHudUpdateNanos = now + 250_000_000L;
        }

        int x = 8;
        int y = 8;

        graphics.fill(x - 5, y - 5, x + 168, y + 94, 0xB0101014);
        graphics.fill(x - 5, y - 5, x + 168, y - 3, 0xFF9A7CFF);

        graphics.drawString(client.font, Component.literal("VoidBoost Monitor"), x, y + 2, 0xFFFFFFFF, false);
        graphics.drawString(client.font, Component.literal("FPS: " + cachedFps), x, y + 15, 0xFFFFFFFF, false);
        graphics.drawString(client.font, Component.literal("Frame: " + cachedFrameText), x, y + 28, 0xFFD0D0D0, false);
        graphics.drawString(client.font, Component.literal("RAM: " + cachedRamText), x, y + 41, 0xFFD0D0D0, false);
        graphics.drawString(client.font, Component.literal(cachedCpuText), x, y + 54, 0xFFD0D0D0, false);
        graphics.drawString(client.font, Component.literal("Entities: " + cachedEntities), x, y + 67, 0xFFD0D0D0, false);
        graphics.drawString(client.font, Component.literal("Blocked/s: " + cachedParticles), x, y + 80, 0xFFD0D0D0, false);
    }
}
