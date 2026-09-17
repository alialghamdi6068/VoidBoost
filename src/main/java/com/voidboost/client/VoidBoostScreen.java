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

        Button ultimate = Button.builder(
                Component.literal(locked ? "ULTIMATE FPS: LOCKED" : "ULTIMATE FPS"),
                b -> {
                    VoidBoostConfig.applyUltimateLockedPreset();
                    VoidBoostConfig.save();
                    rebuildWidgets();
                }).bounds(left, y, 310, 20).build();
        ultimate.active = !locked;
        addRenderableWidget(ultimate);

        addRenderableWidget(Button.builder(Component.literal("MAX FPS"), b -> {
            VoidBoostConfig.applyMaxFpsPreset();
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(left, y + 27, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Balanced"), b -> {
            VoidBoostConfig.applyBalancedPreset();
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(right, y + 27, 150, 20).build());

        addToggle(left, y + 54, "Particles", "particles", locked);
        addToggle(right, y + 54, "Dynamic Render", "dynamic", locked);
        addToggle(left, y + 81, "Entity Optimization", "entities", locked);
        addToggle(right, y + 81, "Entity Shadows", "shadows", locked);
        addToggle(left, y + 108, "Weather Effects", "weather", locked);
        addToggle(right, y + 108, "Animation Optimization", "animations", locked);
        addToggle(left, y + 135, "Fog Optimization", "fog", locked);
        addToggle(right, y + 135, "Performance Mode", "performance", locked);

        Button maxFps = this.children().stream()
                .filter(w -> w instanceof Button)
                .map(w -> (Button) w)
                .skip(1)
                .findFirst()
                .orElse(null);
        if (locked && maxFps != null) maxFps.active = false;

        Button balanced = this.children().stream()
                .filter(w -> w instanceof Button)
                .map(w -> (Button) w)
                .skip(2)
                .findFirst()
                .orElse(null);
        if (locked && balanced != null) balanced.active = false;

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
            }
            c.maxFpsPreset = false;
            VoidBoostConfig.save();
            b.setMessage(toggleLabel(title, key, false));
        }).bounds(x, y, 150, 20).build();
        button.active = !locked;
        addRenderableWidget(button);
    }

    private static Component toggleLabel(String title, String key, boolean locked) {
        VoidBoostConfig c = VoidBoostConfig.get();
        boolean on = switch (key) {
            case "particles" -> c.disableParticles;
            case "dynamic" -> c.dynamicRenderDistance;
            case "entities" -> c.entityRenderOptimization;
            case "shadows" -> c.entityShadows;
            case "weather" -> c.weatherEffects;
            case "animations" -> c.animationOptimization;
            case "fog" -> c.fogOptimization;
            case "performance" -> c.performanceMode;
            default -> false;
        };
        return Component.literal(title + ": " + (on ? "ON" : "OFF") + (locked ? " [LOCKED]" : ""));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 25, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal(
                VoidBoostConfig.get().ultimateLocked
                        ? "Ultimate performance is locked"
                        : "Client-side FPS & PvP optimization"), this.width / 2, 42, 0xAAAAAA);
        graphics.drawCenteredString(this.font, Component.literal("Made by alialghamdi6068"), this.width / 2, 51, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }
}
