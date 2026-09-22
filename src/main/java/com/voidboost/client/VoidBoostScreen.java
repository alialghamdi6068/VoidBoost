package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/**
 * Compact VoidBoost settings screen.
 * Every setting appears once; presets apply immediately and persist.
 */
public final class VoidBoostScreen extends Screen {
    private static final int OVERLAY = 0x70000000;
    private static final int PANEL = 0xB0080B0F;
    private static final int PANEL_EDGE = 0xFF353C43;
    private static final int PANEL_EDGE_HOVER = 0xFF59656E;
    private static final int GROUP = 0xB20A0D12;
    private static final int GROUP_HOVER = 0xD0141920;
    private static final int TEXT = 0xFFF5F5F5;
    private static final int MUTED = 0xFFB7BEC5;
    private static final int ACCENT = 0xFF75E7E5;
    private static final int ACCENT_DARK = 0xFF25484A;

    private final Screen parent;
    private int page;

    private int renderDistance;
    private int simulationDistance;
    private int fps;

    public VoidBoostScreen(Screen parent) {
        super(Component.literal("VoidBoost"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        VoidBoostConfig c = VoidBoostConfig.get();
        Minecraft mc = Minecraft.getInstance();
        renderDistance = clamp(c.maxRenderDistance, 4, 32);
        simulationDistance = clamp(mc.options.simulationDistance().get(), 4, 32);
        fps = clamp(c.targetFps, 30, 260);
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();

        int left = Math.max(18, width / 2 - 330);
        int top = Math.max(28, height / 2 - 205);
        int totalW = Math.min(660, width - left * 2);
        int tabGap = 5;
        int tabW = (totalW - tabGap * 2) / 3;

        String[] tabs = {"General", "Visuals", "Performance"};
        for (int i = 0; i < tabs.length; i++) {
            addRenderableWidget(new Tab(left + i * (tabW + tabGap), top, tabW, 28, tabs[i], i));
        }

        int contentY = top + 38;
        int columnGap = 8;
        int colW = (totalW - columnGap) / 2;

        if (page == 0) general(left, contentY, colW, left + colW + columnGap);
        else if (page == 1) visuals(left, contentY, colW, left + colW + columnGap);
        else performance(left, contentY, colW, left + colW + columnGap);
    }

    private void general(int x1, int y, int w, int x2) {
        VoidBoostConfig c = VoidBoostConfig.get();
        int h = 32;

        addPreset(x1, y, w, h, "Balanced", () -> VoidBoostConfig.applyBalancedPreset());
        addPreset(x2, y, w, h, "Competitive", () -> VoidBoostConfig.applyCompetitivePreset());
        y += 36;

        addPreset(x1, y, w, h, "MAX FPS", () -> VoidBoostConfig.applyMaxFpsPreset());
        addPreset(x2, y, w, h, "Reset / Vanilla", () -> resetToVanilla());
        y += 36;

        addSlider(x1, y, w, h, "Render Distance", renderDistance, 4, 32, "Chunks", v -> {
            renderDistance = v;
            c.maxRenderDistance = v;
        });
        addSlider(x2, y, w, h, "Simulation Distance", simulationDistance, 4, 32, "Chunks", v -> {
            simulationDistance = v;
            Minecraft.getInstance().options.simulationDistance().set(v);
            Minecraft.getInstance().options.save();
        });
        y += 36;

        addChoice(x1, y, w, h, "Max Framerate", fpsLabel(), this::cycleFpsLimit);
        addToggle(x2, y, w, h, "Disable VSync", c.vsyncOptimization, () -> c.vsyncOptimization = !c.vsyncOptimization);
        y += 36;

        addToggle(x1, y, w, h, "FPS Boost", c.performanceMode, () -> c.performanceMode = !c.performanceMode);
        addToggle(x2, y, w, h, "MAX FPS Mode", c.maxFpsPreset, () -> {
            if (c.maxFpsPreset) {
                VoidBoostConfig.applyBalancedPreset();
            } else {
                VoidBoostConfig.applyMaxFpsPreset();
            }
        });
        y += 36;

        addToggle(x1, y, w, h, "Performance Monitor", c.performanceMonitor, () -> c.performanceMonitor = !c.performanceMonitor);
        addToggle(x2, y, w, h, "Dynamic Render Distance", c.dynamicRenderDistance, () -> c.dynamicRenderDistance = !c.dynamicRenderDistance);
    }

    private void visuals(int x1, int y, int w, int x2) {
        VoidBoostConfig c = VoidBoostConfig.get();
        int h = 32;

        addChoice(x1, y, w, h, "Particles", particles(), this::cycleParticles);
        addSlider(x2, y, w, h, "Particle Limit", c.particleLimitPercent, 1, 100, "%", v -> c.particleLimitPercent = v);
        y += 36;

        addToggle(x1, y, w, h, "Entity Shadows", c.entityShadows, () -> c.entityShadows = !c.entityShadows);
        addToggle(x2, y, w, h, "Weather Effects", c.weatherEffects, () -> c.weatherEffects = !c.weatherEffects);
        y += 36;

        addToggle(x1, y, w, h, "Cloud Optimization", c.cloudOptimization, () -> c.cloudOptimization = !c.cloudOptimization);
        addToggle(x2, y, w, h, "Vignette Optimization", c.vignetteOptimization, () -> c.vignetteOptimization = !c.vignetteOptimization);
        y += 36;

        addToggle(x1, y, w, h, "Ambient Occlusion", c.ambientOcclusionOptimization, () -> c.ambientOcclusionOptimization = !c.ambientOcclusionOptimization);
        addToggle(x2, y, w, h, "Mipmap Optimization", c.mipmapOptimization, () -> c.mipmapOptimization = !c.mipmapOptimization);
        y += 36;

        addToggle(x1, y, w, h, "Biome Blend Optimization", c.biomeBlendOptimization, () -> c.biomeBlendOptimization = !c.biomeBlendOptimization);
        addToggle(x2, y, w, h, "View Bob Optimization", c.viewBobOptimization, () -> c.viewBobOptimization = !c.viewBobOptimization);
    }

    private void performance(int x1, int y, int w, int x2) {
        VoidBoostConfig c = VoidBoostConfig.get();
        int h = 32;

        addToggle(x1, y, w, h, "Animation Optimization", c.animationOptimization, () -> c.animationOptimization = !c.animationOptimization);
        addToggle(x2, y, w, h, "Entity Render Optimization", c.entityRenderOptimization, () -> c.entityRenderOptimization = !c.entityRenderOptimization);
        y += 36;

        addToggle(x1, y, w, h, "Fog Optimization", c.fogOptimization, () -> c.fogOptimization = !c.fogOptimization);
        addToggle(x2, y, w, h, "Competitive Mode", c.competitiveMode, () -> c.competitiveMode = !c.competitiveMode);
        y += 36;

        addSlider(x1, y, w, h, "Entity Distance", c.maxEntityDistance, 32, 128, "Blocks", v -> c.maxEntityDistance = v);
        addSlider(x2, y, w, h, "Dynamic Target FPS", c.dynamicTargetFps, 60, 240, "FPS", v -> c.dynamicTargetFps = v);
        y += 36;

        addLabel(x1, y, w, h, "Adaptive Tier", "Tier 0 • MAX");
        addLabel(x2, y, w, h, "Current Load", Math.round(VoidBoostAI.pressure() * 100) + "%");
    }

    private void addPreset(int x, int y, int w, int h, String name, Runnable action) {
        addRenderableWidget(new ActionButton(x, y, w, h, name, () -> {
            action.run();
            syncFromConfig();
            VoidBoostConfig.get().markDirty();
            Minecraft.getInstance().options.save();
            rebuildWidgets();
        }));
    }

    private void addLabel(int x, int y, int w, int h, String name, String value) {
        addRenderableWidget(new InfoOption(x, y, w, h, name, value));
    }

    private void addToggle(int x, int y, int w, int h, String name, boolean value, Runnable change) {
        addRenderableWidget(new Toggle(x, y, w, h, name, value, () -> {
            change.run();
            VoidBoostConfig.get().markDirty();
            Minecraft.getInstance().options.save();
            rebuildWidgets();
        }));
    }

    private void addChoice(int x, int y, int w, int h, String name, String value, Runnable change) {
        addRenderableWidget(new Choice(x, y, w, h, name, value, () -> {
            change.run();
            VoidBoostConfig.get().markDirty();
            rebuildWidgets();
        }));
    }

    private void addSlider(int x, int y, int w, int h, String name, int value, int min, int max, String unit, IntConsumer change) {
        addRenderableWidget(new Slider(x, y, w, h, name, value, min, max, unit, v -> {
            change.accept(v);
            VoidBoostConfig.get().markDirty();
        }));
    }

    private void syncFromConfig() {
        VoidBoostConfig c = VoidBoostConfig.get();
        renderDistance = clamp(c.maxRenderDistance, 4, 32);
        simulationDistance = clamp(Minecraft.getInstance().options.simulationDistance().get(), 4, 32);
        fps = clamp(c.targetFps, 30, 260);
    }

    private void resetToVanilla() {
        VoidBoostConfig.resetToVanilla();
        syncFromConfig();
    }

    private String fpsLabel() {
        return fps >= 260 ? "Unlimited" : Integer.toString(fps);
    }

    private void cycleFpsLimit() {
        VoidBoostConfig c = VoidBoostConfig.get();
        int[] limits = {60, 120, 144, 165, 180, 240, 260};
        int current = fps >= 260 ? 260 : fps;
        int next = limits[0];
        for (int i = 0; i < limits.length; i++) {
            if (current < limits[i]) {
                next = limits[i];
                break;
            }
            if (current == limits[i]) {
                next = limits[(i + 1) % limits.length];
                break;
            }
        }
        fps = next;
        c.targetFps = next;
        c.maxFpsPreset = false;
        c.ultimateLocked = false;
        c.markDirty();
        Minecraft.getInstance().options.framerateLimit().set(next);
        Minecraft.getInstance().options.save();
        rebuildWidgets();
    }

    private String particles() {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.disableParticles) return "Minimal";
        if (c.reducedParticles) return "Reduced";
        return "All";
    }

    private void cycleParticles() {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.reducedParticles && !c.disableParticles) {
            c.reducedParticles = true;
            c.disableParticles = false;
        } else if (c.reducedParticles) {
            c.reducedParticles = false;
            c.disableParticles = true;
        } else {
            c.disableParticles = false;
            c.reducedParticles = false;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, OVERLAY);

        int left = Math.max(18, width / 2 - 330);
        int top = Math.max(28, height / 2 - 205);
        int totalW = Math.min(660, width - left * 2);
        int panelBottom = Math.min(height - 22, top + 270);

        g.fill(left - 5, top - 6, left + totalW + 5, panelBottom, PANEL);
        border(g, left - 5, top - 6, left + totalW + 5, panelBottom, PANEL_EDGE);

        g.drawCenteredString(font, Component.literal("VoidBoost"), width / 2, Math.max(10, top - 24), TEXT);
        g.drawString(font, Component.literal("Minecraft 1.21.11 • Fabric"), left, Math.max(11, top - 22), MUTED, false);

        super.render(g, mouseX, mouseY, delta);

        g.drawCenteredString(font, Component.literal("ESC  Close"), width / 2, Math.min(height - 12, panelBottom - 12), MUTED);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        if (e.x() >= width / 2 - 330 && e.x() <= width / 2 + 330 && e.y() >= 0) {
            return super.mouseClicked(e, doubleClick);
        }
        return super.mouseClicked(e, doubleClick);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent e) {
        if (e.key() == 256) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(e);
    }

    private abstract static class Option extends AbstractWidget {
        Option(int x, int y, int w, int h, String name) {
            super(x, y, w, h, Component.literal(name));
        }

        protected void background(GuiGraphics g) {
            int color = isHovered() ? GROUP_HOVER : GROUP;
            g.fill(getX(), getY(), getX() + width, getY() + height, color);
            border(g, getX(), getY(), getX() + width, getY() + height, isHovered() ? PANEL_EDGE_HOVER : PANEL_EDGE);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {}
    }

    private final class Tab extends Option {
        private final String label;
        private final int target;

        Tab(int x, int y, int w, int h, String label, int target) {
            super(x, y, w, h, label);
            this.label = label;
            this.target = target;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            boolean selected = page == target;
            int color = selected ? 0xC5142025 : (isHovered() ? 0xA50E1218 : 0x80080B0F);
            g.fill(getX(), getY(), getX() + width, getY() + height, color);
            border(g, getX(), getY(), getX() + width, getY() + height, selected ? ACCENT : PANEL_EDGE);
            if (selected) g.fill(getX() + 1, getY() + height - 2, getX() + width - 1, getY() + height - 1, ACCENT);
            g.drawCenteredString(font, Component.literal(label), getX() + width / 2, getY() + 9, selected ? TEXT : MUTED);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            page = target;
            rebuildWidgets();
        }
    }

    private final class ActionButton extends Option {
        private final String name;
        private final Runnable action;

        ActionButton(int x, int y, int w, int h, String name, Runnable action) {
            super(x, y, w, h, name);
            this.name = name;
            this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            background(g);
            int accent = name.equals("MAX FPS") ? ACCENT : MUTED;
            g.drawCenteredString(font, Component.literal(name), getX() + width / 2, getY() + 10, accent);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            action.run();
        }
    }

    private final class InfoOption extends Option {
        private final String name;
        private final String value;

        InfoOption(int x, int y, int w, int h, String name, String value) {
            super(x, y, w, h, name);
            this.name = name;
            this.value = value;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            background(g);
            g.drawString(font, Component.literal(name), getX() + 10, getY() + 10, TEXT, false);
            g.drawString(font, Component.literal(value), getX() + width - font.width(value) - 10, getY() + 10, MUTED, false);
        }
    }

    private final class Slider extends Option {
        private final String name, unit;
        private final int min, max;
        private int value;
        private final IntConsumer change;
        private boolean dragging;

        Slider(int x, int y, int w, int h, String name, int value, int min, int max, String unit, IntConsumer change) {
            super(x, y, w, h, name);
            this.name = name;
            this.value = value;
            this.min = min;
            this.max = max;
            this.unit = unit;
            this.change = change;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            background(g);
            g.drawString(font, Component.literal(name), getX() + 10, getY() + 8, TEXT, false);

            String out = value + (unit.isEmpty() ? "" : " " + unit);
            g.drawString(font, Component.literal(out), getX() + width - font.width(out) - 10, getY() + 8, TEXT, false);

            int tx = getX() + 10;
            int tw = width - 20;
            int ty = getY() + height - 7;
            g.fill(tx, ty, tx + tw, ty + 2, 0xFF59616A);

            int knob = tx + (int) Math.round((value - min) / (double) (max - min) * tw);
            g.fill(tx, ty, knob, ty + 2, ACCENT_DARK);
            g.fill(knob - 2, ty - 3, knob + 3, ty + 6, ACCENT);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            dragging = true;
            set(e.x());
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
            if (dragging) set(e.x());
            return dragging;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent e) {
            if (!dragging) return false;
            dragging = false;
            VoidBoostConfig.get().markDirty();
            Minecraft.getInstance().options.save();
            return true;
        }

        private void set(double mouseX) {
            int tx = getX() + 10;
            int tw = width - 20;
            double t = Math.max(0, Math.min(1, (mouseX - tx) / (double) tw));
            value = clamp((int) Math.round(min + t * (max - min)), min, max);
            change.accept(value);
        }
    }

    private final class Toggle extends Option {
        private final String name;
        private final boolean value;
        private final Runnable change;

        Toggle(int x, int y, int w, int h, String name, boolean value, Runnable change) {
            super(x, y, w, h, name);
            this.name = name;
            this.value = value;
            this.change = change;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            background(g);
            g.drawString(font, Component.literal(name), getX() + 10, getY() + 10, TEXT, false);

            int cx = getX() + width - 16;
            int cy = getY() + 16;
            g.fill(cx - 7, cy - 6, cx + 7, cy + 6, value ? ACCENT_DARK : 0xFF4A5056);
            border(g, cx - 7, cy - 6, cx + 7, cy + 6, value ? ACCENT : PANEL_EDGE);
            g.fill(value ? cx + 2 : cx - 5, cy - 4, value ? cx + 5 : cx - 2, cy + 4, value ? ACCENT : 0xFF9299A0);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            change.run();
        }
    }

    private final class Choice extends Option {
        private final String name, value;
        private final Runnable change;

        Choice(int x, int y, int w, int h, String name, String value, Runnable change) {
            super(x, y, w, h, name);
            this.name = name;
            this.value = value;
            this.change = change;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            background(g);
            g.drawString(font, Component.literal(name), getX() + 10, getY() + 10, TEXT, false);
            String out = value + "  ›";
            g.drawString(font, Component.literal(out), getX() + width - font.width(out) - 10, getY() + 10, TEXT, false);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            change.run();
        }
    }

    private static void border(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}