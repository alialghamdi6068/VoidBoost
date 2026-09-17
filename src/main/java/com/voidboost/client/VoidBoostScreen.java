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
        int y = 70;

        addRenderableWidget(Button.builder(Component.literal("MAX FPS"), b -> {
            VoidBoostConfig.applyMaxFpsPreset();
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(left, y, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Balanced"), b -> {
            VoidBoostConfig.applyBalancedPreset();
            VoidBoostConfig.save();
            rebuildWidgets();
        }).bounds(right, y, 150, 20).build());

        addToggle(left, y + 27, "Particles", "particles");
        addToggle(right, y + 27, "Dynamic Render", "dynamic");
        addToggle(left, y + 54, "Entity Optimization", "entities");
        addToggle(right, y + 54, "Entity Shadows", "shadows");
        addToggle(left, y + 81, "Weather Effects", "weather");
        addToggle(right, y + 81, "Animation Optimization", "animations");
        addToggle(left, y + 108, "Fog Optimization", "fog");
        addToggle(right, y + 108, "Performance Mode", "performance");

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            VoidBoostConfig.save();
            Minecraft.getInstance().setScreen(parent);
        }).bounds(this.width / 2 - 100, this.height - 35, 200, 20).build());
    }

    private void addToggle(int x, int y, String title, String key) {
        addRenderableWidget(Button.builder(toggleLabel(title, key), b -> {
            VoidBoostConfig c = VoidBoostConfig.get();
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
            b.setMessage(toggleLabel(title, key));
        }).bounds(x, y, 150, 20).build());
    }

    private static Component toggleLabel(String title, String key) {
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
        return Component.literal(title + ": " + (on ? "ON" : "OFF"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 28, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Client-side FPS & PvP optimization"), this.width / 2, 45, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }
}
