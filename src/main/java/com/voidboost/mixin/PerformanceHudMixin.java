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
        final int width = 194;
        final int height = 84;

        // Solid, high-contrast panel so text stays readable over any world.
        graphics.fill(x, y, x + width, y + height, 0xEC080A10);
        graphics.fill(x, y, x + width, y + 2, 0xFF9A7CFF);
        graphics.fill(x, y + 2, x + 3, y + height, 0xFF9A7CFF);
        graphics.fill(x + width - 1, y + 2, x + width, y + height, 0xFF242632);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF242632);

        int textX = x + 10;
        int valueX = x + 66;

        graphics.drawString(client.font, "VOIDBOOST", textX, y + 7, 0xFFFFFFFF, false);
        int monitorWidth = client.font.width("MONITOR");
        graphics.drawString(
                client.font,
                "MONITOR",
                x + width - monitorWidth - 10,
                y + 7,
                0xFFE0E0EA,
                false
        );

        drawRow(graphics, client, "FPS", cachedFpsText, textX, valueX, y + 23, 0xFFFFFFFF);
        drawRow(graphics, client, "FRAME", cachedFrameText, textX, valueX, y + 36, 0xFFE8E8F0);
        drawRow(graphics, client, "CPU", cachedCpuText, textX, valueX, y + 49, 0xFFE8E8F0);
        drawRow(graphics, client, "RAM", cachedRamText, textX, valueX, y + 62, 0xFFE8E8F0);
        drawRow(graphics, client, "ENTITIES", cachedEntitiesText, textX, valueX, y + 75, 0xFFCACAD6);
    }

    @Unique
    private static void drawRow(
            GuiGraphics graphics,
            Minecraft client,
            String label,
            String value,
            int labelX,
            int valueX,
            int y,
            int valueColor
    ) {
        graphics.drawString(client.font, label, labelX, y, 0xFF8F91A0, false);
        graphics.drawString(client.font, value, valueX, y, valueColor, false);
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
