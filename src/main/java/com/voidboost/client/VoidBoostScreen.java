package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Main VoidBoost configuration screen. */
public final class VoidBoostScreen extends Screen {
    private static final int BG = 0xFF080A0E;
    private static final int PANEL = 0xFF10131A;
    private static final int PANEL_ALT = 0xFF151922;
    private static final int BORDER = 0xFF252B36;
    private static final int ACCENT = 0xFF7B88FF;
    private static final int TEXT = 0xFFF3F5F8;
    private static final int MUTED = 0xFF8B94A5;

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
        int top = 78;

        addTab(left + 10, 96, "General", 0);
        addTab(left + 10, 126, "Quality", 1);
        addTab(left + 10, 156, "Performance", 2);

        if (page == 0) buildGeneral(contentLeft, right, top);
        else if (page == 1) buildQuality(contentLeft, right, top);
        else buildPerformance(contentLeft, right, top);

        addActionButton("Reset", left + 10, height - 46, 136, () -> {
            if (!VoidBoostConfig.get().ultimateLocked) {
                VoidBoostConfig.applyBalancedPreset();
                rebuildWidgets();
            }
        }, !VoidBoostConfig.get().ultimateLocked);

        addActionButton("Done", right - 116, height - 46, 116, () -> {
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
        addSection(left, top, "Optimization Presets", "Start with a profile, then customize anything you need.");
        addPreset(left, top + 38, 154, "Balanced", VoidBoostConfig::applyBalancedPreset);
        addPreset(left + 164, top + 38, 154, "Competitive", VoidBoostConfig::applyCompetitivePreset);
        addPreset(left, top + 68, 154, "MAX FPS", VoidBoostConfig::applyMaxFpsPreset);
        addPreset(left + 164, top + 68, 154, "ULTIMATE FPS", VoidBoostConfig::applyUltimateLockedPreset);

        addSection(left, top + 118, "Dynamic Rendering", "Automatically adapts chunk distance around your FPS target.");
        addRow(left, top + 158, right, "Dynamic Render Distance", "Adjust render distance while you play.", state("dynamic"), () -> toggle("dynamic"), editable());
        addRow(left, top + 194, right, "Target FPS", "Adaptive rendering target.", String.valueOf(VoidBoostConfig.get().dynamicTargetFps), this::cycleTargetFps, editable());
    }

    private void buildQuality(int left, int right, int top) {
        addSection(left, top, "Visual Effects", "Change expensive visual effects without changing gameplay.");
        addRow(left, top + 42, right, "Particles", "All, reduced, or minimal particles.", state("particles"), () -> toggle("particles"), editable());
        addRow(left, top + 78, right, "Entity Shadows", "Control shadows beneath entities.", state("shadows"), () -> toggle("shadows"), editable());
        addRow(left, top + 114, right, "Weather Effects", "Control weather and cloud rendering.", state("weather"), () -> toggle("weather"), editable());
        addRow(left, top + 150, right, "Animation Optimization", "Reduce unnecessary view animation work.", state("animations"), () -> toggle("animations"), editable());
        addRow(left, top + 186, right, "Fog Optimization", "Reduce distance fog rendering.", state("fog"), () -> toggle("fog"), editable());
    }

    private void buildPerformance(int left, int right, int top) {
        addSection(left, top, "Rendering", "Tune client-side systems with direct FPS impact.");
        addRow(left, top + 42, right, "Entity Optimization", "Skip distant entity rendering beyond the selected range.", state("entities"), () -> toggle("entities"), editable());
        addRow(left, top + 78, right, "Entity Distance", "Maximum distance for entity optimization.", String.valueOf(VoidBoostConfig.get().maxEntityDistance), this::cycleEntityDistance, editable());
        addRow(left, top + 114, right, "Performance Mode", "Apply low-overhead vanilla rendering settings.", state("performance"), () -> toggle("performance"), editable());
        addRow(left, top + 150, right, "Performance Monitor", "Display live FPS, frame time, RAM and render stats.", state("monitor"), () -> toggle("monitor"), editable());

        addSection(left, top + 204, "Current Profile", "The active configuration mode.");
        addActionButton(modeName(), left, top + 242, 180, () -> {}, false);
    }

    private boolean editable() {
        return !VoidBoostConfig.get().ultimateLocked;
    }

    private void cycleTargetFps() {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.ultimateLocked) return;
        c.dynamicTargetFps = c.dynamicTargetFps >= 240 ? 60 : c.dynamicTargetFps + 30;
        c.markDirty();
        rebuildWidgets();
    }

    private void cycleEntityDistance() {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.ultimateLocked) return;
        c.maxEntityDistance = c.maxEntityDistance >= 128 ? 32 : c.maxEntityDistance + 16;
        c.markDirty();
        rebuildWidgets();
    }

    private void addSection(int x, int y, String title, String subtitle) {
        // Section text is rendered in render().
    }

    private void addPreset(int x, int y, int width, String name, Runnable action) {
        boolean enabled = !VoidBoostConfig.get().ultimateLocked || name.equals("ULTIMATE FPS");
        addActionButton(name, x, y, width, action, enabled);
    }

    private void addRow(int left, int y, int right, String title, String description, String value, Runnable action, boolean enabled) {
        addActionButton(value, right - 104, y, 96, action, enabled);
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
        if (c.ultimateLocked) return;
        switch (key) {
            case "particles" -> {
                if (c.disableParticles) { c.disableParticles = false; c.reducedParticles = true; }
                else if (c.reducedParticles) c.reducedParticles = false;
                else c.disableParticles = true;
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
        int top = 78;

        g.fill(0, 0, width, height, BG);
        g.fill(left, 28, right, height - 28, PANEL);
        g.fill(left, 28, sidebarRight, height - 28, 0xFF0C0F14);
        g.fill(sidebarRight, 28, sidebarRight + 1, height - 28, BORDER);
        g.fill(left, 28, right, 29, ACCENT);

        g.drawString(font, Component.literal("VoidBoost"), left + 14, 43, TEXT, false);
        g.drawString(font, Component.literal("Performance Suite"), left + 14, 57, MUTED, false);
        g.drawString(font, Component.literal("Made by VoidFlame"), right - 96, 43, MUTED, false);

        String title = page == 0 ? "General" : page == 1 ? "Quality" : "Performance";
        String subtitle = page == 0 ? "Profiles and adaptive rendering controls" : page == 1 ? "Visual controls that can reduce rendering cost" : "Rendering, culling and live diagnostics";
        g.drawString(font, Component.literal(title), contentLeft + 14, 43, TEXT, false);
        g.drawString(font, Component.literal(subtitle), contentLeft + 14, 57, MUTED, false);

        if (page == 0) {
            drawSection(g, contentLeft, top, "Optimization Presets", "Start with a profile, then customize anything you need.");
            drawSection(g, contentLeft, top + 118, "Dynamic Rendering", "Automatically adapts chunk distance around your FPS target.");
            drawRow(g, contentLeft, top + 158, right);
            drawRow(g, contentLeft, top + 194, right);
        } else if (page == 1) {
            drawSection(g, contentLeft, top, "Visual Effects", "Change expensive visual effects without changing gameplay.");
            for (int i = 0; i < 5; i++) drawRow(g, contentLeft, top + 42 + i * 36, right);
        } else {
            drawSection(g, contentLeft, top, "Rendering", "Tune client-side systems with direct FPS impact.");
            for (int i = 0; i < 4; i++) drawRow(g, contentLeft, top + 42 + i * 36, right);
            drawSection(g, contentLeft, top + 204, "Current Profile", "The active configuration mode.");
        }

        super.render(g, mouseX, mouseY, delta);
    }

    private void drawSection(GuiGraphics g, int x, int y, String title, String subtitle) {
        g.drawString(font, Component.literal(title), x + 14, y + 4, TEXT, false);
        g.drawString(font, Component.literal(subtitle), x + 14, y + 17, MUTED, false);
    }

    private void drawRow(GuiGraphics g, int x, int y, int right) {
        g.fill(x + 6, y - 5, right - 10, y + 29, PANEL_ALT);
        g.fill(x + 6, y - 5, x + 8, y + 29, ACCENT);
        g.fill(x + 8, y + 28, right - 10, y + 29, BORDER);
        g.fill(right - 110, y - 3, right - 8, y + 28, BORDER);
    }
}
