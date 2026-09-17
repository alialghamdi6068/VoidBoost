package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class VoidBoostScreen extends Screen {
    private final Screen parent;

    public VoidBoostScreen(Screen parent) {
        super(Component.literal("VoidBoost Performance"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int y = 62;
        boolean locked = VoidBoostConfig.get().ultimateLocked;

        Button ultimate = Button.builder(Component.literal(locked ? "ULTIMATE FPS: LOCKED" : "ULTIMATE FPS"), b -> {
            VoidBoostConfig.applyUltimateLockedPreset();
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(left, y, 310, 20).build();
        ultimate.active = !locked;
        addRenderableWidget(ultimate);

        Button maxFps = Button.builder(Component.literal("MAX FPS"), b -> {
            VoidBoostConfig.applyMaxFpsPreset();
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(left, y + 27, 150, 20).build();
        maxFps.active = !locked;
        addRenderableWidget(maxFps);

        Button balanced = Button.builder(Component.literal("Balanced"), b -> {
            VoidBoostConfig.applyBalancedPreset();
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(right, y + 27, 150, 20).build();
        balanced.active = !locked;
        addRenderableWidget(balanced);

        Button competitive = Button.builder(Component.literal("Competitive"), b -> {
            VoidBoostConfig.applyCompetitivePreset();
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(left, y + 54, 310, 20).build();
        competitive.active = !locked;
        addRenderableWidget(competitive);

        addToggle(left, y + 81, "Particles", "particles", locked);
        addToggle(right, y + 81, "Dynamic Render", "dynamic", locked);
        addToggle(left, y + 108, "Entity Optimization", "entities", locked);
        addToggle(right, y + 108, "Entity Shadows", "shadows", locked);
        addToggle(left, y + 135, "Weather Effects", "weather", locked);
        addToggle(right, y + 135, "Animation Optimization", "animations", locked);
        addToggle(left, y + 162, "Fog Optimization", "fog", locked);
        addToggle(right, y + 162, "Performance Mode", "performance", locked);
        addToggle(left, y + 189, "Performance Monitor", "monitor", locked);

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            VoidBoostConfig.save();
            Minecraft.getInstance().setScreen(parent);
        }).bounds(this.width / 2 - 100, this.height - 35, 200, 20).build());
    }

    private void addToggle(int x, int y, String title, String key, boolean locked) {
        Button button = Button.builder(toggleLabel(title, key, locked), b -> {
            VoidBoostConfig c = VoidBoostConfig.get();
            if (c.ultimateLocked) return;
            switch (key) {
                case "particles" -> c.disableParticles = !c.disableParticles;
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
            b.setMessage(toggleLabel(title, key, false));
        }).bounds(x, y, 150, 20).build();
        button.active = !locked;
        addRenderableWidget(button);
    }

    private static Component toggleLabel(String title, String key, boolean locked) {
        VoidBoostConfig c = VoidBoostConfig.get();
        boolean on = switch (key) {
            case "particles" -> c.disableParticles || c.reducedParticles;
            case "dynamic" -> c.dynamicRenderDistance;
            case "entities" -> c.entityRenderOptimization;
            case "shadows" -> c.entityShadows;
            case "weather" -> c.weatherEffects;
            case "animations" -> c.animationOptimization;
            case "fog" -> c.fogOptimization;
            case "performance" -> c.performanceMode;
            case "monitor" -> c.performanceMonitor;
            default -> false;
        };
        return Component.literal(title + ": " + (on ? "ON" : "OFF") + (locked ? " [LOCKED]" : ""));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 25, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal(
                VoidBoostConfig.get().ultimateLocked ? "Ultimate performance is locked" : "Client-side FPS & PvP optimization"), this.width / 2, 42, 0xAAAAAA);
        graphics.drawCenteredString(this.font, Component.literal("Made by alialghamdi6068"), this.width / 2, 51, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }
}
