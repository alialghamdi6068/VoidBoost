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
 * Compact performance-mod settings screen.
 * The layout intentionally follows the familiar Sodium-style Minecraft mod UI:
 * centered panel, compact category tabs, dense option rows and vanilla-like controls.
 */
public final class VoidBoostScreen extends Screen {
    private static final int BG = 0xB8000000;
    private static final int PANEL = 0xF010141B;
    private static final int PANEL_EDGE = 0xFF303943;
    private static final int SECTION = 0xFF1B222B;
    private static final int BUTTON = 0xFF252D36;
    private static final int BUTTON_HOVER = 0xFF303A45;
    private static final int BUTTON_EDGE = 0xFF3B4652;
    private static final int TEXT = 0xFFE8EDF2;
    private static final int MUTED = 0xFF9BA7B2;
    private static final int ACCENT = 0xFF55C7FF;
    private static final int ACCENT_DARK = 0xFF24556B;

    private final Screen parent;
    private int page = 1;

    private int renderDistance;
    private int simulationDistance;
    private int fps;

    public VoidBoostScreen(Screen parent) {
        super(Component.literal("VoidBoost Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();
        VoidBoostConfig c = VoidBoostConfig.get();
        renderDistance = clamp(c.maxRenderDistance, 4, 12);
        simulationDistance = clamp(mc.options.simulationDistance().get(), 5, 32);
        fps = clamp(c.targetFps, 30, 1000);
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();

        int[] b = bounds();
        int x = b[0], y = b[1], w = b[2], h = b[3];

        int tabW = Math.max(118, (w - 40) / 6);
        int tabY = y + 58;
        String[] tabs = {"General", "Performance", "Visuals", "PvP", "HUD", "Advanced"};
        for (int i = 0; i < tabs.length; i++) {
            int tx = x + 20 + i * tabW;
            addRenderableWidget(new TabButton(tx, tabY, tabW - 4, 26, tabs[i], i, i == page));
        }

        int contentX = x + 24;
        int contentY = y + 106;
        int contentW = w - 48;

        if (page == 0) general(contentX, contentY, contentW);
        else if (page == 1) performance(contentX, contentY, contentW);
        else simplePage(contentX, contentY, contentW, page);
    }

    private int[] bounds() {
        int w = Math.min(1040, Math.max(760, width - 80));
        int h = Math.min(720, Math.max(540, height - 90));
        int x = (width - w) / 2;
        int y = Math.max(34, (height - h) / 2);
        return new int[]{x, y, w, h};
    }

    private void performance(int x, int y, int w) {
        int rowH = 48;
        int gap = 7;
        addRenderableWidget(new SliderRow(x, y, w, rowH, "Render Distance",
                "Maximum configured world view distance", renderDistance, 4, 12, "Chunks", 0));
        y += rowH + gap;
        addRenderableWidget(new SliderRow(x, y, w, rowH, "Simulation Distance",
                "Distance at which game mechanics are simulated", simulationDistance, 5, 32, "Chunks", 1));
        y += rowH + gap;
        addRenderableWidget(new SliderRow(x, y, w, rowH, "Max Framerate",
                "Limit FPS to reduce heat and unnecessary rendering", fps, 30, 1000, "FPS", 2));
        y += rowH + gap;
        VoidBoostConfig c = VoidBoostConfig.get();
        addRenderableWidget(new ToggleRow(x, y, w, rowH, "VSync",
                "Synchronize frames with the monitor", !c.vsyncOptimization, () -> {
                    c.vsyncOptimization = !c.vsyncOptimization;
                    c.markDirty();
                    rebuildWidgets();
                }));
        y += rowH + gap;
        addRenderableWidget(new ChoiceRow(x, y, w, rowH, "Particles",
                "Choose how many particle effects are rendered", particles(), this::cycleParticles));
        y += rowH + gap;
        addRenderableWidget(new ToggleRow(x, y, w, rowH, "Entity Shadows",
                "Render shadows beneath entities", c.entityShadows, () -> {
                    c.entityShadows = !c.entityShadows;
                    c.markDirty();
                    rebuildWidgets();
                }));
        y += rowH + gap;
        addRenderableWidget(new ToggleRow(x, y, w, rowH, "FPS Boost",
                "Enable VoidBoost performance optimizations", c.performanceMode, () -> {
                    c.performanceMode = !c.performanceMode;
                    c.markDirty();
                    rebuildWidgets();
                }));
    }

    private void general(int x, int y, int w) {
        int rowH = 48;
        int gap = 7;
        addRenderableWidget(new ProfileRow(x, y, w, rowH, "Balanced",
                "Stable everyday performance", VoidBoostConfig::applyBalancedPreset));
        y += rowH + gap;
        addRenderableWidget(new ProfileRow(x, y, w, rowH, "Competitive",
                "Low-latency PvP profile", VoidBoostConfig::applyCompetitivePreset));
        y += rowH + gap;
        addRenderableWidget(new ProfileRow(x, y, w, rowH, "MAX FPS",
                "Aggressive performance profile", VoidBoostConfig::applyMaxFpsPreset));
        y += rowH + gap;
        addRenderableWidget(new ProfileRow(x, y, w, rowH, "ULTIMATE FPS",
                "Maximum performance profile", VoidBoostConfig::applyUltimateLockedPreset));
    }

    private void simplePage(int x, int y, int w, int p) {
        int rowH = 48;
        int gap = 7;
        VoidBoostConfig c = VoidBoostConfig.get();

        if (p == 2) {
            addToggle(x, y, w, "Weather Effects", "Keep weather rendering enabled", c.weatherEffects, () -> c.weatherEffects = !c.weatherEffects); y += rowH + gap;
            addToggle(x, y, w, "Cloud Optimization", "Reduce cloud rendering work", c.cloudOptimization, () -> c.cloudOptimization = !c.cloudOptimization); y += rowH + gap;
            addToggle(x, y, w, "Vignette Optimization", "Reduce vignette rendering cost", c.vignetteOptimization, () -> c.vignetteOptimization = !c.vignetteOptimization); y += rowH + gap;
            addToggle(x, y, w, "Ambient Occlusion", "Reduce ambient occlusion calculations", c.ambientOcclusionOptimization, () -> c.ambientOcclusionOptimization = !c.ambientOcclusionOptimization); y += rowH + gap;
            addToggle(x, y, w, "Mipmap Optimization", "Reduce texture mipmap work", c.mipmapOptimization, () -> c.mipmapOptimization = !c.mipmapOptimization); y += rowH + gap;
            addToggle(x, y, w, "Biome Blend Optimization", "Reduce biome color blending", c.biomeBlendOptimization, () -> c.biomeBlendOptimization = !c.biomeBlendOptimization); y += rowH + gap;
            addToggle(x, y, w, "View Bob Optimization", "Disable view bobbing overhead", c.viewBobOptimization, () -> c.viewBobOptimization = !c.viewBobOptimization);
        } else if (p == 3) {
            addToggle(x, y, w, "Competitive Mode", "Use the low-latency competitive profile", c.competitiveMode, () -> c.competitiveMode = !c.competitiveMode); y += rowH + gap;
            addToggle(x, y, w, "Entity Render Optimization", "Reduce distant entity rendering", c.entityRenderOptimization, () -> c.entityRenderOptimization = !c.entityRenderOptimization); y += rowH + gap;
            addToggle(x, y, w, "Animation Optimization", "Reduce expensive animation updates", c.animationOptimization, () -> c.animationOptimization = !c.animationOptimization); y += rowH + gap;
            addToggle(x, y, w, "Fog Optimization", "Reduce fog rendering overhead", c.fogOptimization, () -> c.fogOptimization = !c.fogOptimization); y += rowH + gap;
            addRenderableWidget(new IntSliderRow(x, y, w, rowH, "Entity Distance", "Maximum entity processing distance", c.maxEntityDistance, 32, 128, v -> c.maxEntityDistance = v));
        } else if (p == 4) {
            addToggle(x, y, w, "Performance Monitor", "Show the VoidBoost performance monitor", c.performanceMonitor, () -> c.performanceMonitor = !c.performanceMonitor); y += rowH + gap;
            addToggle(x, y, w, "Dynamic Render Distance", "Adapt render distance to current FPS", c.dynamicRenderDistance, () -> c.dynamicRenderDistance = !c.dynamicRenderDistance); y += rowH + gap;
            addRenderableWidget(new IntSliderRow(x, y, w, rowH, "Dynamic Target FPS", "FPS target used by adaptive rendering", c.dynamicTargetFps, 60, 240, v -> c.dynamicTargetFps = v));
        } else {
            addToggle(x, y, w, "Dynamic Render Distance", "Automatically adjust render distance", c.dynamicRenderDistance, () -> c.dynamicRenderDistance = !c.dynamicRenderDistance); y += rowH + gap;
            addToggle(x, y, w, "Animation Optimization", "Reduce animation update overhead", c.animationOptimization, () -> c.animationOptimization = !c.animationOptimization); y += rowH + gap;
            addToggle(x, y, w, "Fog Optimization", "Reduce fog rendering overhead", c.fogOptimization, () -> c.fogOptimization = !c.fogOptimization); y += rowH + gap;
            addRenderableWidget(new IntSliderRow(x, y, w, rowH, "Dynamic Target FPS", "Target used by adaptive render distance", c.dynamicTargetFps, 60, 240, v -> c.dynamicTargetFps = v)); y += rowH + gap;
            addRenderableWidget(new IntSliderRow(x, y, w, rowH, "Max Entity Distance", "Limit distant entity processing", c.maxEntityDistance, 32, 128, v -> c.maxEntityDistance = v));
        }
    }

    private void addToggle(int x, int y, int w, String title, String desc, boolean value, Runnable action) {
        addRenderableWidget(new ToggleRow(x, y, w, 48, title, desc, value, () -> {
            action.run();
            VoidBoostConfig.get().markDirty();
            rebuildWidgets();
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
        c.markDirty();
        rebuildWidgets();
    }

    private String pageTitle() {
        return switch (page) {
            case 0 -> "General";
            case 1 -> "Performance";
            case 2 -> "Visuals";
            case 3 -> "PvP";
            case 4 -> "HUD";
            default -> "Advanced";
        };
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int[] b = bounds();
        int x = b[0], y = b[1], w = b[2], h = b[3];

        g.fill(0, 0, width, height, BG);
        g.fill(x, y, x + w, y + h, PANEL);
        g.fill(x, y, x + w, y + 2, ACCENT);
        g.fill(x, y, x + 1, y + h, PANEL_EDGE);
        g.fill(x + w - 1, y, x + w, y + h, PANEL_EDGE);
        g.fill(x, y + 88, x + w, y + 89, PANEL_EDGE);

        g.drawCenteredString(font, Component.literal("VoidBoost"), width / 2, y + 12, TEXT);
        g.drawCenteredString(font, Component.literal(pageTitle()), width / 2, y + 31, MUTED);

        g.drawString(font, Component.literal("Minecraft 1.21.11 • Fabric"), x + 14, y + 14, MUTED, false);
        g.drawString(font, Component.literal("X"), x + w - 20, y + 14, TEXT, false);

        super.render(g, mouseX, mouseY, delta);

        g.drawCenteredString(font, Component.literal("VoidBoost • Performance settings"), width / 2, y + h - 18, MUTED);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        int[] b = bounds();
        int x = b[0], y = b[1], w = b[2];
        if (e.x() >= x + w - 38 && e.x() <= x + w && e.y() >= y && e.y() <= y + 38) {
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

    private abstract static class Base extends AbstractWidget {
        Base(int x, int y, int w, int h, String name) {
            super(x, y, w, h, Component.literal(name));
        }

        protected void box(GuiGraphics g) {
            int bg = isHovered() ? BUTTON_HOVER : BUTTON;
            g.fill(getX(), getY(), getX() + width, getY() + height, bg);
            g.fill(getX(), getY(), getX() + width, getY() + 1, BUTTON_EDGE);
            g.fill(getX(), getY() + height - 1, getX() + width, getY() + height, BUTTON_EDGE);
            g.fill(getX(), getY(), getX() + 1, getY() + height, BUTTON_EDGE);
            g.fill(getX() + width - 1, getY(), getX() + width, getY() + height, BUTTON_EDGE);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {}
    }

    private final class TabButton extends Base {
        private final String label;
        private final int target;
        private final boolean selected;

        TabButton(int x, int y, int w, int h, String label, int target, boolean selected) {
            super(x, y, w, h, label);
            this.label = label;
            this.target = target;
            this.selected = selected;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            int bg = selected ? ACCENT_DARK : (isHovered() ? BUTTON_HOVER : SECTION);
            g.fill(getX(), getY(), getX() + width, getY() + height, bg);
            g.fill(getX(), getY() + height - 2, getX() + width, getY() + height, selected ? ACCENT : BUTTON_EDGE);
            g.drawCenteredString(font, Component.literal(label), getX() + width / 2, getY() + 8, selected ? TEXT : MUTED);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            page = target;
            rebuildWidgets();
        }
    }

    private final class SliderRow extends Base {
        private final String title, description, unit;
        private final int min, max, kind;
        private int current;
        private boolean dragging;

        SliderRow(int x, int y, int w, int h, String title, String description,
                  int current, int min, int max, String unit, int kind) {
            super(x, y, w, h, title);
            this.title = title; this.description = description; this.current = current;
            this.min = min; this.max = max; this.unit = unit; this.kind = kind;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            box(g);
            g.drawString(font, Component.literal(title), getX() + 12, getY() + 8, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 12, getY() + 25, MUTED, false);

            String value = current + " " + unit;
            g.drawString(font, Component.literal(value), getX() + width - font.width(value) - 12, getY() + 8, ACCENT, false);

            int tx = getX() + 12, tw = width - 24, ty = getY() + height - 9;
            g.fill(tx, ty, tx + tw, ty + 3, 0xFF11171D);
            int knob = tx + (int) Math.round(((current - min) / (double)(max - min)) * tw);
            g.fill(tx, ty, knob, ty + 3, ACCENT);
            g.fill(knob - 3, ty - 3, knob + 4, ty + 7, ACCENT);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            dragging = true;
            setValue(e.x());
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
            if (dragging) setValue(e.x());
            return dragging;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent e) {
            if (dragging) {
                dragging = false;
                VoidBoostConfig.get().markDirty();
                Minecraft.getInstance().options.save();
                return true;
            }
            return false;
        }

        private void setValue(double mx) {
            int tx = getX() + 12, tw = width - 24;
            double t = Math.max(0, Math.min(1, (mx - tx) / (double) tw));
            current = clamp((int)Math.round(min + t * (max - min)), min, max);
            if (kind == 0) { renderDistance = current; VoidBoostConfig.get().maxRenderDistance = current; }
            else if (kind == 1) { simulationDistance = current; Minecraft.getInstance().options.simulationDistance().set(current); }
            else { fps = current; VoidBoostConfig.get().targetFps = current; }
        }
    }

    private final class IntSliderRow extends Base {
        private final String title, description;
        private final int min, max;
        private int current;
        private final IntConsumer change;
        private boolean dragging;

        IntSliderRow(int x, int y, int w, int h, String title, String description,
                     int current, int min, int max, IntConsumer change) {
            super(x, y, w, h, title);
            this.title = title; this.description = description; this.current = current;
            this.min = min; this.max = max; this.change = change;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            box(g);
            g.drawString(font, Component.literal(title), getX() + 12, getY() + 8, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 12, getY() + 25, MUTED, false);
            String value = Integer.toString(current);
            g.drawString(font, Component.literal(value), getX() + width - font.width(value) - 12, getY() + 8, ACCENT, false);
            int tx = getX() + 12, tw = width - 24, ty = getY() + height - 9;
            g.fill(tx, ty, tx + tw, ty + 3, 0xFF11171D);
            int knob = tx + (int)Math.round(((current - min) / (double)(max - min)) * tw);
            g.fill(tx, ty, knob, ty + 3, ACCENT);
            g.fill(knob - 3, ty - 3, knob + 4, ty + 7, ACCENT);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            dragging = true;
            setValue(e.x());
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
            if (dragging) setValue(e.x());
            return dragging;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent e) {
            if (dragging) {
                dragging = false;
                VoidBoostConfig.get().markDirty();
                Minecraft.getInstance().options.save();
                return true;
            }
            return false;
        }

        private void setValue(double mx) {
            int tx = getX() + 12, tw = width - 24;
            double t = Math.max(0, Math.min(1, (mx - tx) / (double)tw));
            current = clamp((int)Math.round(min + t * (max - min)), min, max);
            change.accept(current);
            VoidBoostConfig.get().markDirty();
        }
    }

    private final class ToggleRow extends Base {
        private final String title, description;
        private final boolean on;
        private final Runnable action;

        ToggleRow(int x, int y, int w, int h, String title, String description, boolean on, Runnable action) {
            super(x, y, w, h, title);
            this.title = title; this.description = description; this.on = on; this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            box(g);
            g.drawString(font, Component.literal(title), getX() + 12, getY() + 8, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 12, getY() + 25, MUTED, false);

            String state = on ? "ON" : "OFF";
            int sx = getX() + width - font.width(state) - 42;
            g.drawString(font, Component.literal(state), sx, getY() + 18, on ? ACCENT : MUTED, false);

            int cx = getX() + width - 26, cy = getY() + 15;
            g.fill(cx - 7, cy - 7, cx + 8, cy + 8, on ? ACCENT : BUTTON_EDGE);
            if (on) {
                g.fill(cx - 3, cy - 3, cx + 4, cy + 4, ACCENT_DARK);
                g.fill(cx - 1, cy - 1, cx + 3, cy + 3, ACCENT);
            } else {
                g.fill(cx - 5, cy - 5, cx + 6, cy + 6, 0xFF141A20);
            }
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            action.run();
        }
    }

    private final class ChoiceRow extends Base {
        private final String title, description, value;
        private final Runnable action;

        ChoiceRow(int x, int y, int w, int h, String title, String description, String value, Runnable action) {
            super(x, y, w, h, title);
            this.title = title; this.description = description; this.value = value; this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            box(g);
            g.drawString(font, Component.literal(title), getX() + 12, getY() + 8, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 12, getY() + 25, MUTED, false);
            g.drawString(font, Component.literal(value), getX() + width - font.width(value) - 30, getY() + 17, ACCENT, false);
            g.drawString(font, Component.literal("›"), getX() + width - 16, getY() + 17, MUTED, false);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            action.run();
        }
    }

    private final class ProfileRow extends Base {
        private final String title, description;
        private final Runnable action;

        ProfileRow(int x, int y, int w, int h, String title, String description, Runnable action) {
            super(x, y, w, h, title);
            this.title = title; this.description = description; this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            box(g);
            g.drawString(font, Component.literal(title), getX() + 12, getY() + 8, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 12, getY() + 25, MUTED, false);
            g.drawString(font, Component.literal("APPLY"), getX() + width - font.width("APPLY") - 12, getY() + 17, ACCENT, false);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            action.run();
            Minecraft.getInstance().options.save();
            rebuildWidgets();
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
