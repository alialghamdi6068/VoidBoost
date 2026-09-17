package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class VoidBoostScreen extends Screen {
    private final Screen parent;

    public VoidBoostScreen(Screen parent) {
        super(Component.literal("VoidBoost"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int y = this.height / 4 + 12;

        addRenderableWidget(Button.builder(label("Particles"), b -> {
            VoidBoostConfig.get().disableParticles = !VoidBoostConfig.get().disableParticles;
            VoidBoostConfig.save();
            b.setMessage(label("Particles"));
        }).bounds(left, y, 150, 20).build());

        addRenderableWidget(Button.builder(label("Dynamic Render Distance"), b -> {
            VoidBoostConfig.get().dynamicRenderDistance = !VoidBoostConfig.get().dynamicRenderDistance;
            VoidBoostConfig.save();
            b.setMessage(label("Dynamic Render Distance"));
        }).bounds(right, y, 150, 20).build());

        addRenderableWidget(Button.builder(label("Reduced Particles"), b -> {
            VoidBoostConfig.get().reducedParticles = !VoidBoostConfig.get().reducedParticles;
            VoidBoostConfig.save();
            b.setMessage(label("Reduced Particles"));
        }).bounds(left, y + 26, 150, 20).build());

        addRenderableWidget(Button.builder(label("MAX FPS Preset"), b -> {
            VoidBoostConfig.get().maxFpsPreset = !VoidBoostConfig.get().maxFpsPreset;
            if (VoidBoostConfig.get().maxFpsPreset) {
                VoidBoostConfig.get().disableParticles = true;
                VoidBoostConfig.get().reducedParticles = false;
                VoidBoostConfig.get().dynamicRenderDistance = true;
            }
            VoidBoostConfig.save();
            b.setMessage(label("MAX FPS Preset"));
        }).bounds(right, y + 26, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            VoidBoostConfig.save();
            Minecraft.getInstance().setScreen(parent);
        }).bounds(this.width / 2 - 100, this.height - 40, 200, 20).build());
    }

    private static Component label(String name) {
        VoidBoostConfig c = VoidBoostConfig.get();
        boolean on = switch (name) {
            case "Particles" -> c.disableParticles;
            case "Dynamic Render Distance" -> c.dynamicRenderDistance;
            case "Reduced Particles" -> c.reducedParticles;
            case "MAX FPS Preset" -> c.maxFpsPreset;
            default -> false;
        };
        return Component.literal(name + ": " + (on ? "ON" : "OFF"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 35, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Client-side performance controls"), this.width / 2, 52, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }
}
