package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class VoidBoostScreen extends Screen {
    private final Screen parent;
    private int page;

    public VoidBoostScreen(Screen parent) {
        super(Component.literal("VoidBoost"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearWidgets();
        int center = this.width / 2;
        int top = 44;

        addRenderableWidget(Button.builder(Component.literal("General"), b -> { page = 0; rebuildWidgets(); })
                .bounds(center - 180, top, 110, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Quality"), b -> { page = 1; rebuildWidgets(); })
                .bounds(center - 55, top, 110, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Performance"), b -> { page = 2; rebuildWidgets(); })
                .bounds(center + 70, top, 110, 22).build());

        if (page == 0) buildGeneral(center, top + 36);
        else if (page == 1) buildQuality(center, top + 36);
        else buildPerformance(center, top + 36);

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            VoidBoostConfig.save();
            Minecraft.getInstance().setScreen(parent);
        }).bounds(center - 100, this.height - 30, 200, 20).build());
    }

    private void buildGeneral(int center, int y) {
        addSection("Preset", center, y);
        addPreset(center - 155, y + 22, "Balanced", () -> VoidBoostConfig.applyBalancedPreset());
        addPreset(center + 5, y + 22, "Competitive", () -> VoidBoostConfig.applyCompetitivePreset());
        addPreset(center - 155, y + 49, "MAX FPS", () -> VoidBoostConfig.applyMaxFpsPreset());
        addPreset(center + 5, y + 49, "ULTIMATE FPS", () -> VoidBoostConfig.applyUltimateLockedPreset());

        addSection("Render Distance", center, y + 83);
        addToggle(center - 155, y + 105, "Dynamic Render", "dynamic");
        addSliderLike(center + 5, y + 105, "Entity Distance", VoidBoostConfig.get().maxEntityDistance);
    }

    private void buildQuality(int center, int y) {
        addSection("Visual Quality", center, y);
        addToggle(center - 155, y + 22, "Particles", "particles");
        addToggle(center + 5, y + 22, "Entity Shadows", "shadows");
        addToggle(center - 155, y + 49, "Weather Effects", "weather");
        addToggle(center + 5, y + 49, "Fog Optimization", "fog");
        addToggle(center - 155, y + 76, "Animation Optimization", "animations");
    }

    private void buildPerformance(int center, int y) {
        addSection("Performance", center, y);
        addToggle(center - 155, y + 22, "Entity Optimization", "entities");
        addToggle(center + 5, y + 22, "Performance Mode", "performance");
        addToggle(center - 155, y + 49, "Dynamic Render", "dynamic");
        addToggle(center + 5, y + 49, "Performance Monitor", "monitor");
        addSection("About", center, y + 87);
        addRenderableWidget(Button.builder(Component.literal("VoidBoost  •  Made by VoidFlame"), b -> {})
                .bounds(center - 155, y + 109, 310, 22).build());
    }

    private void addSection(String text, int center, int y) {
        addRenderableWidget(Button.builder(Component.literal("—  " + text + "  —"), b -> {})
                .bounds(center - 155, y, 310, 20).build());
    }

    private void addPreset(int x, int y, String name, Runnable action) {
        Button button = Button.builder(Component.literal(name), b -> {
            action.run();
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(x, y, 150, 22).build();
        button.active = !VoidBoostConfig.get().ultimateLocked || name.equals("ULTIMATE FPS");
        addRenderableWidget(button);
    }

    private void addToggle(int x, int y, String title, String key) {
        Button button = Button.builder(Component.literal(title + ": " + state(key)), b -> {
            VoidBoostConfig c = VoidBoostConfig.get();
            if (c.ultimateLocked) return;
            switch (key) {
                case "particles" -> { c.disableParticles = !c.disableParticles; c.reducedParticles = false; }
                case "dynamic" -> c.dynamicRenderDistance = !c.dynamicRenderDistance;
                case "entities" -> c.entityRenderOptimization = !c.entityRenderOptimization;
                case "shadows" -> c.entityShadows = !c.entityShadows;
                case "weather" -> c.weatherEffects = !c.weatherEffects;
                case "animations" -> c.animationOptimization = !c.animationOptimization;
                case "fog" -> c.fogOptimization = !c.fogOptimization;
                case "performance" -> c.performanceMode = !c.performanceMode;
                case "monitor" -> c.performanceMonitor = !c.performanceMonitor;
            }
            c.maxFpsPreset = false;
            c.competitiveMode = false;
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(x, y, 150, 22).build();
        button.active = !VoidBoostConfig.get().ultimateLocked;
        addRenderableWidget(button);
    }

    private void addSliderLike(int x, int y, String title, int value) {
        addRenderableWidget(Button.builder(Component.literal(title + ": " + value), b -> {
            VoidBoostConfig c = VoidBoostConfig.get();
            c.maxEntityDistance = c.maxEntityDistance >= 128 ? 32 : c.maxEntityDistance + 16;
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(x, y, 150, 22).build());
    }

    private static String state(String key) {
        VoidBoostConfig c = VoidBoostConfig.get();
        return switch (key) {
            case "particles" -> (c.disableParticles || c.reducedParticles) ? "ON" : "OFF";
            case "dynamic" -> c.dynamicRenderDistance ? "ON" : "OFF";
            case "entities" -> c.entityRenderOptimization ? "ON" : "OFF";
            case "shadows" -> c.entityShadows ? "ON" : "OFF";
            case "weather" -> c.weatherEffects ? "ON" : "OFF";
            case "animations" -> c.animationOptimization ? "ON" : "OFF";
            case "fog" -> c.fogOptimization ? "ON" : "OFF";
            case "performance" -> c.performanceMode ? "ON" : "OFF";
            case "monitor" -> c.performanceMonitor ? "ON" : "OFF";
            default -> "OFF";
        };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Intentionally do not call renderBackground(): Minecraft's screen renderer already
        // applies the frame blur, and calling it here causes "Can only blur once per frame".
        graphics.fill(0, 0, this.width, this.height, 0xE6101014);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Client-side performance & PvP optimization"), this.width / 2, 30, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, delta);
    }
}
