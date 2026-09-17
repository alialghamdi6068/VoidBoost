package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Main VoidBoost configuration screen. */
public final class VoidBoostScreen extends Screen {
    private static final int BG = 0xFF07090D;
    private static final int PANEL = 0xFF0E1117;
    private static final int PANEL_2 = 0xFF121722;
    private static final int PANEL_3 = 0xFF171D29;
    private static final int BORDER = 0xFF252D3A;
    private static final int ACCENT = 0xFF7D8CFF;
    private static final int ACCENT_DARK = 0xFF2A3150;
    private static final int TEXT = 0xFFF4F6FA;
    private static final int MUTED = 0xFF8D97AA;
    private static final int GOOD = 0xFF8EE6B0;

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

    @Override
    protected void rebuildWidgets() {
        clearWidgets();

        int left = 30;
        int contentLeft = 196;
        int right = width - 30;
        int top = 86;

        addTab(left + 12, 92, "General", 0);
        addTab(left + 12, 122, "Quality", 1);
        addTab(left + 12, 152, "Performance", 2);

        if (page == 0) buildGeneral(contentLeft, right, top);
        else if (page == 1) buildQuality(contentLeft, right, top);
        else buildPerformance(contentLeft, right, top);

        addActionButton("Reset to Balanced", left + 12, height - 48, 136, () -> {
            VoidBoostConfig.applyBalancedPreset();
            rebuildWidgets();
        }, true);

        addActionButton("Done", right - 116, height - 48, 116, () -> {
            VoidBoostConfig.save();
            Minecraft.getInstance().setScreen(parent);
        }, true);
    }

    private void addTab(int x, int y, String label, int targetPage) {
        Button button = Button.builder(Component.literal(label), b -> {
            page = targetPage;
            rebuildWidgets();
        }).bounds(x, y, 136, 24).build();
        button.active = page != targetPage;
        addRenderableWidget(button);
    }

    private void buildGeneral(int left, int right, int top) {
        addPreset(left, top + 42, 154, "Balanced", VoidBoostConfig::applyBalancedPreset);
        addPreset(left + 164, top + 42, 154, "Competitive", VoidBoostConfig::applyCompetitivePreset);
        addPreset(left, top + 72, 154, "MAX FPS", VoidBoostConfig::applyMaxFpsPreset);
        addPreset(left + 164, top + 72, 154, "ULTIMATE FPS", VoidBoostConfig::applyUltimateLockedPreset);

        addRow(left, top + 136, right, "Dynamic Render Distance", "Automatically changes chunk distance around your FPS target.", state("dynamic"), () -> toggle("dynamic"), true);
        addRow(left, top + 178, right, "Target FPS", "Used only by dynamic render distance.", String.valueOf(VoidBoostConfig.get().dynamicTargetFps), this::cycleTargetFps, true);
    }

    private void buildQuality(int left, int right, int top) {
        addRow(left, top + 42, right, "Particles", "All, reduced, or minimal particles.", state("particles"), () -> toggle("particles"), true);
        addRow(left, top + 84, right, "Entity Shadows", "Disable expensive entity shadow rendering.", state("shadows"), () -> toggle("shadows"), true);
        addRow(left, top + 126, right, "Weather Effects", "Control clouds and weather rendering.", state("weather"), () -> toggle("weather"), true);
        addRow(left, top + 168, right, "Animation Optimization", "Disable view bobbing and reduce animation work.", state("animations"), () -> toggle("animations"), true);
        addRow(left, top + 210, right, "Fog Optimization", "Toggle the client fog optimization.", state("fog"), () -> toggle("fog"), true);
    }

    private void buildPerformance(int left, int right, int top) {
        addRow(left, top + 42, right, "Entity Optimization", "Stop rendering entities beyond the selected distance.", state("entities"), () -> toggle("entities"), true);
        addRow(left, top + 84, right, "Entity Distance", "Maximum distance used by entity optimization.", String.valueOf(VoidBoostConfig.get().maxEntityDistance), this::cycleEntityDistance, true);
        addRow(left, top + 126, right, "Performance Mode", "Apply low-cost vanilla rendering settings.", state("performance"), () -> toggle("performance"), true);
        addRow(left, top + 168, right, "Performance Monitor", "Show live FPS, frame time, RAM, entities and particles.", state("monitor"), () -> toggle("monitor"), true);
    }

    private void cycleTargetFps() {
        VoidBoostConfig c = VoidBoostConfig.get();
        c.dynamicTargetFps = c.dynamicTargetFps >= 240 ? 60 : c.dynamicTargetFps + 30;
        c.markDirty();
        rebuildWidgets();
    }

    private void cycleEntityDistance() {
        VoidBoostConfig c = VoidBoostConfig.get();
        c.maxEntityDistance = c.maxEntityDistance >= 128 ? 32 : c.maxEntityDistance + 16;
        c.markDirty();
        rebuildWidgets();
    }

    private void addPreset(int x, int y, int width, String name, Runnable action) {
        addActionButton(name, x, y, width, () -> {
            action.run();
            rebuildWidgets();
        }, true);
    }

    private void addRow(int left, int y, int right, String title, String description, String value, Runnable action, boolean enabled) {
        addActionButton(value, right - 110, y, 96, action, enabled);
    }

    private void addActionButton(String text, int x, int y, int width, Runnable action, boolean enabled) {
        Button button = Button.builder(Component.literal(text), b -> action.run())
                .bounds(x, y, width, 24)
                .build();
        button.active = enabled;
        addRenderableWidget(button);
    }

    private void toggle(String key) {
        VoidBoostConfig c = VoidBoostConfig.get();
        switch (key) {
            case "particles" -> {
                if (c.disableParticles) {
                    c.disableParticles = false;
                    c.reducedParticles = true;
                } else if (c.reducedParticles) {
                    c.reducedParticles = false;
                } else {
                    c.disableParticles = true;
                }
            }
            case "dynamic" -> c.dynamicRenderDistance = !c.dynamicRenderDistance;
            case "entities" -> c.entityRenderOptimization = !c.entityRenderOptimization;
            case "shadows" -> c.entityShadows = !c.entityShadows;
            case "weather" -> c.weatherEffects = !c.weatherEffects;
            case "animations" -> c.animationOptimization = !c.animationOptimization;
            case "fog" -> c.fogOptimization = !c.fogOptimization;
            case "performance" -> c.performanceMode = !c.performanceMode;
            case "monitor" -> c.performanceMonitor = !c.performanceMonitor;
            default -> { return; }
        }

        c.maxFpsPreset = false;
        c.competitiveMode = false;
        c.ultimateLocked = false;
        c.markDirty();
        rebuildWidgets();
    }

    private static String state(String key) {
        VoidBoostConfig c = VoidBoostConfig.get();
        return switch (key) {
            case "particles" -> c.disableParticles ? "MINIMAL" : (c.reducedParticles ? "REDUCED" : "ALL");
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
        return "CUSTOM";
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int left = 30;
        int sidebarRight = 176;
        int contentLeft = 196;
        int right = width - 30;
        int top = 86;

        g.fill(0, 0, width, height, BG);
        g.fill(left, 26, right, height - 26, PANEL);
        g.fill(left, 26, sidebarRight, height - 26, 0xFF0A0D12);
        g.fill(sidebarRight, 26, sidebarRight + 1, height - 26, BORDER);
        g.fill(left, 26, right, 28, ACCENT);

        g.drawString(font, Component.literal("VoidBoost"), left + 14, 40, TEXT, false);
        g.drawString(font, Component.literal("Performance Suite"), left + 14, 54, MUTED, false);
        g.drawString(font, Component.literal("Made by VoidFlame"), right - 104, 40, MUTED, false);

        String title = page == 0 ? "General" : page == 1 ? "Quality" : "Performance";
        String subtitle = page == 0 ? "Profiles and adaptive rendering" : page == 1 ? "Visual effects and animation controls" : "Culling and performance diagnostics";
        g.drawString(font, Component.literal(title), contentLeft + 14, 40, TEXT, false);
        g.drawString(font, Component.literal(subtitle), contentLeft + 14, 54, MUTED, false);

        drawPage(g, contentLeft, right, top);
        super.render(g, mouseX, mouseY, delta);
    }

    private void drawPage(GuiGraphics g, int left, int right, int top) {
        if (page == 0) {
            drawSection(g, left, top, "Optimization Presets", "Choose a profile, then customize every setting below.");
            drawPresetCard(g, left, top + 38, 154, "Balanced", "Balanced");
            drawPresetCard(g, left + 164, top + 38, 154, "Competitive", "PvP");
            drawPresetCard(g, left, top + 68, 154, "MAX FPS", "Maximum");
            drawPresetCard(g, left + 164, top + 68, 154, "ULTIMATE FPS", "Extreme");
            drawSection(g, left, top + 118, "Dynamic Rendering", "Automatically adapts render distance around your target FPS.");
            drawRow(g, left, top + 136, right, "Dynamic Render Distance", "Automatically changes chunk distance around your FPS target.");
            drawRow(g, left, top + 178, right, "Target FPS", "Used only by dynamic render distance.");
        } else if (page == 1) {
            drawSection(g, left, top, "Visual Effects", "Turn individual effects on or off. Nothing is locked.");
            drawRow(g, left, top + 42, right, "Particles", "All, reduced, or minimal particles.");
            drawRow(g, left, top + 84, right, "Entity Shadows", "Disable expensive entity shadow rendering.");
            drawRow(g, left, top + 126, right, "Weather Effects", "Control clouds and weather rendering.");
            drawRow(g, left, top + 168, right, "Animation Optimization", "Disable view bobbing and reduce animation work.");
            drawRow(g, left, top + 210, right, "Fog Optimization", "Toggle the client fog optimization.");
        } else {
            drawSection(g, left, top, "Rendering", "Client-side optimizations with direct rendering impact.");
            drawRow(g, left, top + 42, right, "Entity Optimization", "Stop rendering entities beyond the selected distance.");
            drawRow(g, left, top + 84, right, "Entity Distance", "Maximum distance used by entity optimization.");
            drawRow(g, left, top + 126, right, "Performance Mode", "Apply low-cost vanilla rendering settings.");
            drawRow(g, left, top + 168, right, "Performance Monitor", "Show live FPS, frame time, RAM, entities and particles.");
            drawSection(g, left, top + 216, "Current Profile", "Your current preset/custom state.");
            g.drawString(font, Component.literal(modeName()), left + 14, top + 239, GOOD, false);
        }
    }

    private void drawSection(GuiGraphics g, int x, int y, String title, String subtitle) {
        g.drawString(font, Component.literal(title), x + 8, y, TEXT, false);
        g.drawString(font, Component.literal(subtitle), x + 8, y + 14, MUTED, false);
    }

    private void drawPresetCard(GuiGraphics g, int x, int y, int width, String title, String tag) {
        boolean active = switch (title) {
            case "ULTIMATE FPS" -> VoidBoostConfig.get().ultimateLocked;
            case "MAX FPS" -> VoidBoostConfig.get().maxFpsPreset;
            case "Competitive" -> VoidBoostConfig.get().competitiveMode;
            default -> !VoidBoostConfig.get().maxFpsPreset && !VoidBoostConfig.get().competitiveMode && !VoidBoostConfig.get().ultimateLocked;
        };
        g.fill(x, y, x + width, y + 26, active ? ACCENT_DARK : PANEL_3);
        g.fill(x, y, x + 2, y + 26, active ? ACCENT : BORDER);
        g.drawString(font, Component.literal(title), x + 9, y + 5, TEXT, false);
        g.drawString(font, Component.literal(tag), x + width - 45, y + 5, MUTED, false);
    }

    private void drawRow(GuiGraphics g, int x, int y, int right, String title, String description) {
        g.fill(x, y - 5, right - 8, y + 32, PANEL_2);
        g.fill(x, y - 5, x + 2, y + 32, ACCENT);
        g.drawString(font, Component.literal(title), x + 10, y + 1, TEXT, false);
        g.drawString(font, Component.literal(description), x + 10, y + 15, MUTED, false);
        g.fill(right - 112, y - 2, right - 8, y + 28, BORDER);
    }
}
