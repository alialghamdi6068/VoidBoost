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
        rebuildVoidBoostWidgets();
    }

    private void rebuildVoidBoostWidgets() {
        clearWidgets();

        int left = 42;
        int contentLeft = 194;
        int contentRight = this.width - 42;
        int top = 58;

        addTab(left, 58, "General", 0);
        addTab(left, 86, "Quality", 1);
        addTab(left, 114, "Performance", 2);

        if (page == 0) buildGeneral(contentLeft, contentRight, top);
        else if (page == 1) buildQuality(contentLeft, contentRight, top);
        else buildPerformance(contentLeft, contentRight, top);

        addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
            if (!VoidBoostConfig.get().ultimateLocked) {
                VoidBoostConfig.applyBalancedPreset();
                VoidBoostConfig.save();
                rebuildVoidBoostWidgets();
            }
        }).bounds(contentLeft, this.height - 34, 92, 22).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            VoidBoostConfig.save();
            Minecraft.getInstance().setScreen(parent);
        }).bounds(contentRight - 92, this.height - 34, 92, 22).build());
    }

    private void addTab(int x, int y, String name, int targetPage) {
        Button button = Button.builder(Component.literal(name), b -> {
            page = targetPage;
            rebuildVoidBoostWidgets();
        }).bounds(x, y, 126, 22).build();
        button.active = page != targetPage;
        addRenderableWidget(button);
    }

    private void buildGeneral(int left, int right, int top) {
        addHeader(left, top, "General", "Choose how aggressively VoidBoost should optimize the client.");
        addSection(left, top + 48, right, "Optimization Presets", "Presets change multiple settings at once.");

        addPreset(left, top + 80, 150, "Balanced", VoidBoostConfig::applyBalancedPreset);
        addPreset(left + 160, top + 80, 150, "Competitive", VoidBoostConfig::applyCompetitivePreset);
        addPreset(left, top + 108, 150, "MAX FPS", VoidBoostConfig::applyMaxFpsPreset);
        addPreset(left + 160, top + 108, 150, "ULTIMATE FPS", VoidBoostConfig::applyUltimateLockedPreset);

        addSection(left, top + 150, right, "Dynamic Rendering", "Automatically adjusts render distance around your FPS target.");
        addToggleRow(left, top + 181, right, "Dynamic Render Distance", "Keeps FPS stable by adapting chunk distance.", "dynamic");
        addValueRow(left, top + 216, right, "Target FPS", String.valueOf(VoidBoostConfig.get().dynamicTargetFps), () -> {
            VoidBoostConfig c = VoidBoostConfig.get();
            if (c.ultimateLocked) return;
            c.dynamicTargetFps = c.dynamicTargetFps >= 240 ? 60 : c.dynamicTargetFps + 30;
            VoidBoostConfig.save();
            rebuildVoidBoostWidgets();
        });
    }

    private void buildQuality(int left, int right, int top) {
        addHeader(left, top, "Quality", "Control visual effects that can affect rendering cost.");
        addSection(left, top + 48, right, "Visual Effects", "Lower-cost rendering without changing gameplay behavior.");
        addToggleRow(left, top + 80, right, "Particles", "Reduce or disable non-essential particles.", "particles");
        addToggleRow(left, top + 115, right, "Entity Shadows", "Disable simple shadows beneath entities.", "shadows");
        addToggleRow(left, top + 150, right, "Weather Effects", "Reduce weather rendering and cloud work.", "weather");
        addToggleRow(left, top + 185, right, "Animation Optimization", "Reduce unnecessary view and animation work.", "animations");
        addToggleRow(left, top + 220, right, "Fog Optimization", "Reduce fog work when performance mode is active.", "fog");
    }

    private void buildPerformance(int left, int right, int top) {
        addHeader(left, top, "Performance", "Fine-tune the systems that have the largest FPS impact.");
        addSection(left, top + 48, right, "Rendering", "Client-side culling and performance controls.");
        addToggleRow(left, top + 80, right, "Entity Optimization", "Stop rendering distant entities beyond the configured range.", "entities");
        addValueRow(left, top + 115, right, "Entity Distance", String.valueOf(VoidBoostConfig.get().maxEntityDistance), () -> {
            VoidBoostConfig c = VoidBoostConfig.get();
            if (c.ultimateLocked) return;
            c.maxEntityDistance = c.maxEntityDistance >= 128 ? 32 : c.maxEntityDistance + 16;
            VoidBoostConfig.save();
            rebuildVoidBoostWidgets();
        });
        addToggleRow(left, top + 150, right, "Performance Mode", "Applies VoidBoost's low-overhead rendering profile.", "performance");
        addToggleRow(left, top + 185, right, "Performance Monitor", "Shows FPS, frame time, entities and particles per second.", "monitor");

        addSection(left, top + 230, right, "Status", "Current VoidBoost configuration.");
        addStatus(left, top + 262, right);
    }

    private void addHeader(int left, int top, String title, String subtitle) {
        // Header is drawn in render(); widgets start below it.
    }

    private void addSection(int left, int y, int right, String title, String subtitle) {
        // Section labels are drawn in render(); this method keeps layout coordinates together.
    }

    private void addPreset(int x, int y, int width, String name, Runnable action) {
        Button button = Button.builder(Component.literal(name), b -> {
            action.run();
            VoidBoostConfig.save();
            rebuildVoidBoostWidgets();
        }).bounds(x, y, width, 22).build();
        button.active = !VoidBoostConfig.get().ultimateLocked || name.equals("ULTIMATE FPS");
        addRenderableWidget(button);
    }

    private void addToggleRow(int left, int y, int right, String title, String description, String key) {
        Button button = Button.builder(Component.literal(state(key)), b -> {
            toggle(key);
            VoidBoostConfig.save();
            rebuildVoidBoostWidgets();
        }).bounds(right - 92, y, 92, 22).build();
        button.active = !VoidBoostConfig.get().ultimateLocked;
        addRenderableWidget(button);
    }

    private void addValueRow(int left, int y, int right, String title, String value, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(value), b -> action.run())
                .bounds(right - 92, y, 92, 22).build());
    }

    private void addStatus(int left, int y, int right) {
        addRenderableWidget(Button.builder(Component.literal(modeName()), b -> {})
                .bounds(right - 150, y, 150, 22).build());
    }

    private void toggle(String key) {
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

    private static String modeName() {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.ultimateLocked) return "ULTIMATE FPS";
        if (c.competitiveMode) return "COMPETITIVE";
        if (c.maxFpsPreset) return "MAX FPS";
        return "CUSTOM / BALANCED";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int left = 42;
        int contentLeft = 194;
        int right = this.width - 42;
        int top = 58;

        graphics.fill(0, 0, this.width, this.height, 0xFF0B0C10);
        graphics.fill(left, 38, right, this.height - 22, 0xFF12141A);
        graphics.fill(left, 38, contentLeft - 12, this.height - 22, 0xFF0F1116);
        graphics.fill(contentLeft - 1, 38, contentLeft, this.height - 22, 0xFF252832);

        graphics.drawString(this.font, Component.literal("VoidBoost"), left + 16, 48, 0xFFFFFFFF, false);
        graphics.drawString(this.font, Component.literal("Client Performance Suite"), left + 16, 62, 0xFF8E95A7, false);

        String title = page == 0 ? "General" : page == 1 ? "Quality" : "Performance";
        String subtitle = page == 0 ? "Presets and adaptive rendering" : page == 1 ? "Visual effects and quality controls" : "FPS-focused rendering controls";
        graphics.drawString(this.font, Component.literal(title), contentLeft + 16, top, 0xFFFFFFFF, false);
        graphics.drawString(this.font, Component.literal(subtitle), contentLeft + 16, top + 13, 0xFF8E95A7, false);

        if (page == 0) {
            drawSection(graphics, contentLeft, top + 48, "Optimization Presets", "Presets change multiple settings at once.");
            drawSection(graphics, contentLeft, top + 150, "Dynamic Rendering", "Automatically adjusts render distance around your FPS target.");
        } else if (page == 1) {
            drawSection(graphics, contentLeft, top + 48, "Visual Effects", "Lower-cost rendering without changing gameplay behavior.");
        } else {
            drawSection(graphics, contentLeft, top + 48, "Rendering", "Client-side culling and performance controls.");
            drawSection(graphics, contentLeft, top + 230, "Status", "Current VoidBoost configuration.");
        }

        if (page == 0) {
            drawRowText(graphics, contentLeft, top + 181, "Dynamic Render Distance", "Keeps FPS stable by adapting chunk distance.");
            drawRowText(graphics, contentLeft, top + 216, "Target FPS", "Preferred FPS target for adaptive rendering.");
        } else if (page == 1) {
            drawRowText(graphics, contentLeft, top + 80, "Particles", "Reduce or disable non-essential particles.");
            drawRowText(graphics, contentLeft, top + 115, "Entity Shadows", "Disable simple shadows beneath entities.");
            drawRowText(graphics, contentLeft, top + 150, "Weather Effects", "Reduce weather rendering and cloud work.");
            drawRowText(graphics, contentLeft, top + 185, "Animation Optimization", "Reduce unnecessary view and animation work.");
            drawRowText(graphics, contentLeft, top + 220, "Fog Optimization", "Reduce fog work when performance mode is active.");
        } else {
            drawRowText(graphics, contentLeft, top + 80, "Entity Optimization", "Stop rendering distant entities beyond the configured range.");
            drawRowText(graphics, contentLeft, top + 115, "Entity Distance", "Maximum distance used by entity rendering optimization.");
            drawRowText(graphics, contentLeft, top + 150, "Performance Mode", "Applies VoidBoost's low-overhead rendering profile.");
            drawRowText(graphics, contentLeft, top + 185, "Performance Monitor", "Shows FPS, frame time, entities and particles per second.");
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawSection(GuiGraphics graphics, int x, int y, String title, String subtitle) {
        graphics.drawString(this.font, Component.literal(title), x + 16, y + 4, 0xFFE8EAF0, false);
        graphics.drawString(this.font, Component.literal(subtitle), x + 16, y + 16, 0xFF777E90, false);
    }

    private void drawRowText(GuiGraphics graphics, int x, int y, String title, String description) {
        graphics.fill(x + 10, y - 4, this.width - 52, y + 27, 0xFF181B22);
        graphics.drawString(this.font, Component.literal(title), x + 18, y + 1, 0xFFE8EAF0, false);
        graphics.drawString(this.font, Component.literal(description), x + 18, y + 13, 0xFF777E90, false);
    }
}
