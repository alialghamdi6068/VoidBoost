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
    @Unique private static OperatingSystemMXBean osBean;

    @Unique private static String cachedFrameText = "0.0 ms";
    @Unique private static String cachedRamText = "0 / 0 MB";
    @Unique private static String cachedCpuText = "--";
    @Unique private static String cachedFpsText = "0";
    @Unique private static String cachedEntitiesText = "0";
    @Unique private static long nextHudUpdateNanos;

    @Inject(method = "render", at = @At("TAIL"))
    private void voidboost$renderMonitor(GuiGraphics graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        if (!VoidBoostConfig.isPerformanceMonitorEnabled()) return;

        Minecraft client = Minecraft.getInstance();
        long now = System.nanoTime();
        VoidBoostStats.frame(now);

        if (now >= nextHudUpdateNanos) {
            cachedFpsText = Integer.toString(client.getFps());
            cachedFrameText = formatFrameMs(VoidBoostStats.frameMs());

            int entities = client.level == null ? 0 : client.level.getEntityCount();
            cachedEntitiesText = Integer.toString(entities);

            Runtime runtime = Runtime.getRuntime();
            long used = runtime.totalMemory() - runtime.freeMemory();
            long max = runtime.maxMemory();
            cachedRamText = (used / 1048576L) + " / " + (max / 1048576L) + " MB";

            cachedCpuText = processCpuText();
            nextHudUpdateNanos = now + 500_000_000L;
        }

        final int x = 8;
        final int y = 8;
        final int width = 150;
        final int height = 62;

        graphics.fill(x, y, x + width, y + height, 0xB80A0C10);
        graphics.fill(x, y, x + 2, y + height, 0xFF9A7CFF);

        graphics.drawString(client.font, "VOIDBOOST", x + 8, y + 6, 0xFFFFFFFF, false);
        graphics.drawString(client.font, "MONITOR", x + width - 48, y + 6, 0xFFB8B8C8, false);

        graphics.drawString(client.font, cachedFpsText, x + 8, y + 18, 0xFFFFFFFF, false);
        graphics.drawString(client.font, "FPS", x + 30, y + 18, 0xFFB8B8C8, false);
        graphics.drawString(client.font, cachedFrameText, x + 8, y + 31, 0xFFD8D8E0, false);
        graphics.drawString(client.font, "FRAME", x + 42, y + 31, 0xFF8E8E9A, false);

        graphics.drawString(client.font, "CPU " + cachedCpuText, x + 8, y + 47, 0xFFD0D0D8, false);
        graphics.drawString(client.font, "RAM " + cachedRamText, x + 68, y + 47, 0xFFD0D0D8, false);
        graphics.drawString(client.font, "ENT " + cachedEntitiesText, x + 8, y + 59, 0xFFAAAAB8, false);
    }

    @Unique
    private static String processCpuText() {
        if (osBean == null) {
            osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        }
        double cpu = osBean == null ? -1.0D : osBean.getProcessCpuLoad();
        return cpu < 0.0D ? "--" : Math.round(cpu * 100.0D) + "%";
    }

    @Unique
    private static String formatFrameMs(double frameMs) {
        if (frameMs <= 0.0D) return "0.0 ms";
        int tenths = (int) Math.round(frameMs * 10.0D);
        return (tenths / 10) + "." + (tenths % 10) + " ms";
    }
}
