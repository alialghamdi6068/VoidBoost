package com.voidboost.mixin;

import com.voidboost.client.VoidBoostConfig;
import com.voidboost.client.VoidBoostStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.DeltaTracker;

@Mixin(Gui.class)
public abstract class PerformanceHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void voidboost$renderMonitor(GuiGraphics graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.performanceMonitor) return;

        Minecraft client = Minecraft.getInstance();
        int fps = client.getFps();
        double frameMs = VoidBoostStats.frameMs();
        int entities = client.level == null ? 0 : client.level.getEntityCount();
        int particles = VoidBoostStats.particlesPerSecond();

        int x = 6;
        int y = 6;
        graphics.fill(x - 3, y - 3, x + 142, y + 64, 0x99000000);
        graphics.drawString(client.font, Component.literal("VoidBoost Monitor"), x, y, 0xFFFFFF, false);
        graphics.drawString(client.font, Component.literal("FPS: " + fps), x, y + 12, 0xFFFFFF, false);
        graphics.drawString(client.font, Component.literal(String.format("Frame: %.1f ms", frameMs)), x, y + 24, 0xFFFFFF, false);
        graphics.drawString(client.font, Component.literal("Entities: " + entities), x, y + 36, 0xFFFFFF, false);
        graphics.drawString(client.font, Component.literal("Particles/s: " + particles), x, y + 48, 0xFFFFFF, false);
    }
}
