package com.voidboost.mixin;

import com.voidboost.client.VoidBoostConfig;
import com.voidboost.client.VoidBoostStats;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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
    private static String cachedFpsText = "FPS: 0";
    @Unique
    private static String cachedEntitiesText = "Entities: 0";
    @Unique
    private static String cachedParticlesText = "Blocked/s: 0";
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
            cachedFrameText = formatFrameMs(VoidBoostStats.frameMs());
            cachedFpsText = "FPS: " + cachedFps;

            cachedEntities = client.level == null ? 0 : client.level.getEntityCount();
            cachedEntitiesText = "Entities: " + cachedEntities;

            Runtime runtime = Runtime.getRuntime();
            long used = runtime.totalMemory() - runtime.freeMemory();
            long max = runtime.maxMemory();
            cachedRamText = (used / (1024L * 1024L)) + " / " + (max / (1024L * 1024L)) + " MB";

            cachedParticles = VoidBoostStats.particlesPerSecond();
            cachedParticlesText = "Blocked/s: " + cachedParticles;

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

        graphics.drawString(client.font, "VoidBoost Monitor", x, y + 2, 0xFFFFFFFF, false);
        graphics.drawString(client.font, cachedFpsText, x, y + 15, 0xFFFFFFFF, false);
        graphics.drawString(client.font, "Frame: " + cachedFrameText, x, y + 28, 0xFFD0D0D0, false);
        graphics.drawString(client.font, "RAM: " + cachedRamText, x, y + 41, 0xFFD0D0D0, false);
        graphics.drawString(client.font, cachedCpuText, x, y + 54, 0xFFD0D0D0, false);
        graphics.drawString(client.font, cachedEntitiesText, x, y + 67, 0xFFD0D0D0, false);
        graphics.drawString(client.font, cachedParticlesText, x, y + 80, 0xFFD0D0D0, false);
    }

    @Unique
    private static String formatFrameMs(double frameMs) {
        if (frameMs <= 0.0D) {
            return "0.0 ms";
        }

        int tenths = (int) Math.round(frameMs * 10.0D);
        return (tenths / 10) + "." + (tenths % 10) + " ms";
    }
}
