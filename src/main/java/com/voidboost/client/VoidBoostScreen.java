package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * VoidBoost options UI.
 *
 * Deliberately follows the compact Sodium/Reese-style Minecraft options layout:
 * transparent dark option groups over the world, compact tabs, dense rows,
 * right-aligned values and no oversized dashboard panels.
 */
public final class VoidBoostScreen extends Screen {
    private static final int OVERLAY = 0x70000000;
    private static final int GROUP = 0x9A05070A;
    private static final int GROUP_HOVER = 0xB20B0F14;
    private static final int TEXT = 0xFFF5F5F5;
    private static final int MUTED = 0xFFB7BEC5;
    private static final int ACCENT = 0xFF75E7E5;
    private static final int EDGE = 0x665D666E;

    private final Screen parent;
    private int page = 0;

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
        simulationDistance = clamp(mc.options.simulationDistance().get(), 5, 32);
        fps = clamp(c.targetFps, 30, 1000);
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();

        int left = Math.max(24, width / 2 - 330);
        int top = Math.max(34, height / 2 - 190);
        int totalW = Math.min(660, width - left - 24);
        int tabGap = 4;
        int tabW = (totalW - tabGap * 3) / 4;

        String[] tabs = {"General", "Quality", "Performance", "Advanced"};
        for (int i = 0; i < 4; i++) {
            addRenderableWidget(new Tab(left + i * (tabW + tabGap), top, tabW, 28, tabs[i], i));
        }

        int contentY = top + 38;
        int columnGap = 8;
        int colW = (totalW - columnGap) / 2;

        if (page == 0) general(left, contentY, colW, left + colW + columnGap);
        else if (page == 1) quality(left, contentY, colW, left + colW + columnGap);
        else if (page == 2) performance(left, contentY, colW, left + colW + columnGap);
        else advanced(left, contentY, colW, left + colW + columnGap);
    }

    private void general(int x1, int y, int w, int x2) {
        VoidBoostConfig c = VoidBoostConfig.get();
        int h = 32;

        addSlider(x1, y, w, h, "Render Distance", renderDistance, 4, 32, "Chunks", v -> {
            renderDistance = v;
            c.maxRenderDistance = v;
        });
        addSlider(x2, y, w, h, "Simulation Distance", simulationDistance, 5, 32, "Chunks", v -> {
            simulationDistance = v;
            Minecraft.getInstance().options.simulationDistance().set(v);
        });
        y += 36;

        addToggle(x1, y, w, h, "VSync", !c.vsyncOptimization, () -> c.vsyncOptimization = !c.vsyncOptimization);
        addSlider(x2, y, w, h, "Max Framerate", fps, 30, 1000, "FPS", v -> {
            fps = v;
            c.targetFps = v;
        });
        y += 36;

        addToggle(x1, y, w, h, "Entity Shadows", c.entityShadows, () -> c.entityShadows = !c.entityShadows);
        addToggle(x2, y, w, h, "FPS Boost", c.performanceMode, () -> c.performanceMode = !c.performanceMode);
        y += 36;

        addToggle(x1, y, w, h, "Performance Monitor", c.performanceMonitor, () -> c.performanceMonitor = !c.performanceMonitor);
        addToggle(x2, y, w, h, "Dynamic Render Distance", c.dynamicRenderDistance, () -> c.dynamicRenderDistance = !c.dynamicRenderDistance);
    }

    private void quality(int x1, int y, int w, int x2) {
        VoidBoostConfig c = VoidBoostConfig.get();
        int h = 32;

        addChoice(x1, y, w, h, "Particles", particles(), this::cycleParticles);
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

        addSlider(x1, y, w, h, "Entity Distance", c.maxEntityDistance, 32, 128, "", v -> c.maxEntityDistance = v);
        addSlider(x2, y, w, h, "Dynamic Target FPS", c.dynamicTargetFps, 60, 240, "FPS", v -> c.dynamicTargetFps = v);
        y += 36;

        addToggle(x1, y, w, h, "Dynamic Render Distance", c.dynamicRenderDistance, () -> c.dynamicRenderDistance = !c.dynamicRenderDistance);
    }

    private void advanced(int x1, int y, int w, int x2) {
        VoidBoostConfig c = VoidBoostConfig.get();
        int h = 32;

        addToggle(x1, y, w, h, "Dynamic Render Distance", c.dynamicRenderDistance, () -> c.dynamicRenderDistance = !c.dynamicRenderDistance);
        addToggle(x2, y, w, h, "Animation Optimization", c.animationOptimization, () -> c.animationOptimization = !c.animationOptimization);
        y += 36;

        addToggle(x1, y, w, h, "Fog Optimization", c.fogOptimization, () -> c.fogOptimization = !c.fogOptimization);
        addToggle(x2, y, w, h, "Performance Monitor", c.performanceMonitor, () -> c.performanceMonitor = !c.performanceMonitor);
        y += 36;

        addSlider(x1, y, w, h, "Dynamic Target FPS", c.dynamicTargetFps, 60, 240, "FPS", v -> c.dynamicTargetFps = v);
        addSlider(x2, y, w, h, "Max Entity Distance", c.maxEntityDistance, 32, 128, "", v -> c.maxEntityDistance = v);
    }

    private void addToggle(int x, int y, int w, int h, String name, boolean value, Runnable change) {
        addRenderableWidget(new Toggle(x, y, w, h, name, value, () -> {
            change.run();
            VoidBoostConfig.get().markDirty();
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

    private String particles() {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.disableParticles) return "Minimal";
        if (c.reducedParticles) return "Reduced";
        return "All";
    }

    private void cycleParticles() {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.reducedParticles && !c.disableParticles) c.reducedParticles = true;
        else if (c.reducedParticles) { c.reducedParticles = false; c.disableParticles = true; }
        else c.disableParticles = false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, OVERLAY);

        int left = Math.max(24, width / 2 - 330);
        int top = Math.max(34, height / 2 - 190);
        int totalW = Math.min(660, width - left - 24);

        g.drawCenteredString(font, Component.literal("VoidBoost"), width / 2, Math.max(14, top - 22), TEXT);
        g.drawString(font, Component.literal("Minecraft 1.21.11 • Fabric"), left, Math.max(15, top - 20), MUTED, false);
        g.drawString(font, Component.literal("×"), left + totalW - 8, Math.max(12, top - 24), TEXT, false);

        super.render(g, mouseX, mouseY, delta);

        g.drawCenteredString(font, Component.literal("Done"), width / 2, Math.min(height - 18, top + 190), TEXT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        int left = Math.max(24, width / 2 - 330);
        int top = Math.max(34, height / 2 - 190);
        int totalW = Math.min(660, width - left - 24);
        if (e.x() >= left + totalW - 30 && e.x() <= left + totalW + 10 && e.y() >= top - 32 && e.y() <= top + 2) {
            Minecraft.getInstance().setScreen(parent);
            return true;
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
            g.fill(getX(), getY() + height - 1, getX() + width, getY() + height, EDGE);
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
            int color = selected ? 0xB5142025 : (isHovered() ? 0xA50A0E13 : 0x7A05070A);
            g.fill(getX(), getY(), getX() + width, getY() + height, color);
            if (selected) g.fill(getX(), getY() + height - 2, getX() + width, getY() + height, ACCENT);
            g.drawCenteredString(font, Component.literal(label), getX() + width / 2, getY() + 9, selected ? TEXT : MUTED);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            page = target;
            rebuildWidgets();
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
            this.name = name; this.value = value; this.min = min; this.max = max; this.unit = unit; this.change = change;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            background(g);
            g.drawString(font, Component.literal(name), getX() + 10, getY() + 10, TEXT, false);
            String out = value + (unit.isEmpty() ? "" : " " + unit);
            g.drawString(font, Component.literal(out), getX() + width - font.width(out) - 10, getY() + 10, TEXT, false);

            int tx = getX() + 10, tw = width - 20, ty = getY() + height - 6;
            g.fill(tx, ty, tx + tw, ty + 2, 0xFF59616A);
            int knob = tx + (int)Math.round((value - min) / (double)(max - min) * tw);
            g.fill(tx, ty, knob, ty + 2, ACCENT);
            g.fill(knob - 2, ty - 3, knob + 3, ty + 6, ACCENT);
        }

        @Override public void onClick(MouseButtonEvent e, boolean doubleClick) { dragging = true; set(e.x()); }
        @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { if (dragging) set(e.x()); return dragging; }
        @Override public boolean mouseReleased(MouseButtonEvent e) {
            if (!dragging) return false;
            dragging = false;
            VoidBoostConfig.get().markDirty();
            Minecraft.getInstance().options.save();
            return true;
        }

        private void set(double mouseX) {
            int tx = getX() + 10, tw = width - 20;
            double t = Math.max(0, Math.min(1, (mouseX - tx) / (double)tw));
            value = clamp((int)Math.round(min + t * (max - min)), min, max);
            change.accept(value);
        }
    }

    private final class Toggle extends Option {
        private final String name;
        private final boolean value;
        private final Runnable change;

        Toggle(int x, int y, int w, int h, String name, boolean value, Runnable change) {
            super(x, y, w, h, name);
            this.name = name; this.value = value; this.change = change;
        }

        @Override protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            background(g);
            g.drawString(font, Component.literal(name), getX() + 10, getY() + 10, TEXT, false);
            int cx = getX() + width - 13, cy = getY() + 16;
            g.fill(cx - 6, cy - 6, cx + 6, cy + 6, value ? ACCENT : 0xFF777E85);
            if (!value) g.fill(cx - 3, cy - 3, cx + 3, cy + 3, 0xFF20252A);
        }

        @Override public void onClick(MouseButtonEvent e, boolean doubleClick) { change.run(); }
    }

    private final class Choice extends Option {
        private final String name, value;
        private final Runnable change;

        Choice(int x, int y, int w, int h, String name, String value, Runnable change) {
            super(x, y, w, h, name);
            this.name = name; this.value = value; this.change = change;
        }

        @Override protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            background(g);
            g.drawString(font, Component.literal(name), getX() + 10, getY() + 10, TEXT, false);
            g.drawString(font, Component.literal(value + "  ›"), getX() + width - font.width(value + "  ›") - 10, getY() + 10, TEXT, false);
        }

        @Override public void onClick(MouseButtonEvent e, boolean doubleClick) { change.run(); }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
