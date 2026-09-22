package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public final class VoidBoostScreen extends Screen {
    private static final int BG = 0xFF101016;
    private static final int PANEL = 0xFF171720;
    private static final int PANEL_HOVER = 0xFF1E1E2A;
    private static final int BORDER = 0xFF292938;
    private static final int PURPLE = 0xFF9A7CFF;
    private static final int PURPLE_DARK = 0xFF342B50;
    private static final int TEXT = 0xFFF2F0F7;
    private static final int MUTED = 0xFFA7A3B2;
    private static final int OFF = 0xFF77727F;

    private final Screen parent;
    private int page = 0;

    public VoidBoostScreen(Screen parent) {
        super(Component.literal("VoidBoost Settings"));
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
        int right = width - 30;
        int top = 74;

        String[] pages = {"General", "Performance", "Visuals", "PvP", "HUD", "Advanced"};
        int tabW = Math.max(92, (right - left - 10 * (pages.length - 1)) / pages.length);
        for (int i = 0; i < pages.length; i++) {
            final int target = i;
            addRenderableWidget(new Tab(left + i * (tabW + 10), 40, tabW, 28, pages[i], page == i,
                    () -> { page = target; rebuildWidgets(); }));
        }

        if (page == 0) buildGeneral(left, top, right);
        else if (page == 1) buildPerformance(left, top, right);
        else if (page == 2) buildVisuals(left, top, right);
        else if (page == 3) buildPvp(left, top, right);
        else if (page == 4) buildHud(left, top, right);
        else buildAdvanced(left, top, right);
    }

    private void buildGeneral(int l, int y, int r) {
        heading("General", "Profiles and core VoidBoost behavior.", l, y);
        int cy = y + 58;
        int w = (r - l - 14) / 2;

        addRenderableWidget(new ActionRow(l, cy, w, 54, "Balanced", "Stable everyday performance",
                VoidBoostConfig::applyBalancedPreset));
        addRenderableWidget(new ActionRow(l + w + 14, cy, w, 54, "Competitive", "Low-latency PvP profile",
                VoidBoostConfig::applyCompetitivePreset));
        cy += 66;
        addRenderableWidget(new ActionRow(l, cy, w, 54, "MAX FPS", "Aggressive performance profile",
                VoidBoostConfig::applyMaxFpsPreset));
        addRenderableWidget(new ActionRow(l + w + 14, cy, w, 54, "ULTIMATE FPS", "Maximum performance profile",
                VoidBoostConfig::applyUltimateLockedPreset));
        cy += 78;

        VoidBoostConfig c = VoidBoostConfig.get();
        addToggle(l, cy, w, "FPS Boost", "Enable VoidBoost performance processing.", c.performanceMode,
                () -> c.performanceMode = !c.performanceMode);
        addToggle(l + w + 14, cy, w, "Performance Monitor", "Show live performance information.", c.performanceMonitor,
                () -> c.performanceMonitor = !c.performanceMonitor);
    }

    private void buildPerformance(int l, int y, int r) {
        heading("Performance", "Tune the systems that directly affect frame time.", l, y);
        int cy = y + 58;
        int w = (r - l - 14) / 2;
        VoidBoostConfig c = VoidBoostConfig.get();

        addRenderableWidget(new SliderRow(l, cy, w, 58, "Render Distance",
                "Maximum world view distance.", c.maxRenderDistance, 4, 32,
                v -> c.maxRenderDistance = v));
        addRenderableWidget(new SliderRow(l + w + 14, cy, w, 58, "Simulation Distance",
                "Locked to 4 chunks while Tier 0 is active.", Minecraft.getInstance().options.simulationDistance().get(), 4, 32,
                v -> Minecraft.getInstance().options.simulationDistance().set(v)));
        cy += 70;

        addRenderableWidget(new SliderRow(l, cy, w, 58, "Max Framerate",
                "Target frame-rate limit.", c.targetFps, 60, 260,
                v -> c.targetFps = snapFps(v)));
        addToggle(l + w + 14, cy, w, "VSync", "Synchronize frames to the display.", !c.vsyncOptimization,
                () -> c.vsyncOptimization = !c.vsyncOptimization);
        cy += 70;

        addToggle(l, cy, w, "Entity Shadows", "Keep entity shadow rendering enabled.", c.entityShadows,
                () -> c.entityShadows = !c.entityShadows);
        addToggle(l + w + 14, cy, w, "Entity Optimization", "Cull distant non-critical entities.", c.entityRenderOptimization,
                () -> c.entityRenderOptimization = !c.entityRenderOptimization);
        cy += 70;

        addToggle(l, cy, w, "Particle Culling", "Block particle creation for maximum FPS.", c.disableParticles,
                () -> c.disableParticles = !c.disableParticles);
        addToggle(l + w + 14, cy, w, "Dynamic Distance", "Adapt render distance to current frame time.", c.dynamicRenderDistance,
                () -> c.dynamicRenderDistance = !c.dynamicRenderDistance);
    }

    private void buildVisuals(int l, int y, int r) {
        heading("Visuals", "Optional visual work that can be reduced for performance.", l, y);
        int cy = y + 58;
        int w = (r - l - 14) / 2;
        VoidBoostConfig c = VoidBoostConfig.get();

        addToggle(l, cy, w, "Cloud Optimization", "Reduce cloud rendering work.", c.cloudOptimization,
                () -> c.cloudOptimization = !c.cloudOptimization);
        addToggle(l + w + 14, cy, w, "Vignette Optimization", "Reduce vignette rendering cost.", c.vignetteOptimization,
                () -> c.vignetteOptimization = !c.vignetteOptimization);
        cy += 70;
        addToggle(l, cy, w, "Ambient Occlusion", "Reduce ambient-occlusion work.", c.ambientOcclusionOptimization,
                () -> c.ambientOcclusionOptimization = !c.ambientOcclusionOptimization);
        addToggle(l + w + 14, cy, w, "Mipmap Optimization", "Reduce texture mipmap work.", c.mipmapOptimization,
                () -> c.mipmapOptimization = !c.mipmapOptimization);
        cy += 70;
        addToggle(l, cy, w, "Biome Blend Optimization", "Reduce biome color blending.", c.biomeBlendOptimization,
                () -> c.biomeBlendOptimization = !c.biomeBlendOptimization);
        addToggle(l + w + 14, cy, w, "Animation Optimization", "Reduce unnecessary animation work.", c.animationOptimization,
                () -> c.animationOptimization = !c.animationOptimization);
        cy += 70;
        addToggle(l, cy, w, "Fog Optimization", "Reduce fog processing.", c.fogOptimization,
                () -> c.fogOptimization = !c.fogOptimization);
        addToggle(l + w + 14, cy, w, "Weather Effects", "Keep weather rendering enabled.", c.weatherEffects,
                () -> c.weatherEffects = !c.weatherEffects);
    }

    private void buildPvp(int l, int y, int r) {
        heading("PvP", "Low-latency controls while preserving gameplay-critical entities.", l, y);
        int cy = y + 58;
        int w = (r - l - 14) / 2;
        VoidBoostConfig c = VoidBoostConfig.get();

        addToggle(l, cy, w, "Competitive Mode", "Use the competitive performance profile.", c.competitiveMode,
                () -> c.competitiveMode = !c.competitiveMode);
        addToggle(l + w + 14, cy, w, "Entity Optimization", "Optimize distant entity rendering.", c.entityRenderOptimization,
                () -> c.entityRenderOptimization = !c.entityRenderOptimization);
        cy += 70;
        addRenderableWidget(new SliderRow(l, cy, w, 58, "Entity Distance",
                "Maximum distance for non-critical entities.", c.maxEntityDistance, 24, 128,
                v -> c.maxEntityDistance = v));
        addToggle(l + w + 14, cy, w, "Animation Optimization", "Reduce animation update overhead.", c.animationOptimization,
                () -> c.animationOptimization = !c.animationOptimization);
    }

    private void buildHud(int l, int y, int r) {
        heading("HUD", "Monitoring and adaptive performance controls.", l, y);
        int cy = y + 58;
        int w = (r - l - 14) / 2;
        VoidBoostConfig c = VoidBoostConfig.get();

        addToggle(l, cy, w, "Performance Monitor", "Display FPS, frame time and load.", c.performanceMonitor,
                () -> c.performanceMonitor = !c.performanceMonitor);
        addToggle(l + w + 14, cy, w, "Dynamic Distance", "Adapt view distance to performance.", c.dynamicRenderDistance,
                () -> c.dynamicRenderDistance = !c.dynamicRenderDistance);
        cy += 70;
        addRenderableWidget(new SliderRow(l, cy, w, 58, "Dynamic Target FPS",
                "Target used by adaptive distance.", c.dynamicTargetFps, 60, 240,
                v -> c.dynamicTargetFps = v));
    }

    private void buildAdvanced(int l, int y, int r) {
        heading("Advanced", "Internal performance behavior.", l, y);
        int cy = y + 58;
        int w = (r - l - 14) / 2;
        VoidBoostConfig c = VoidBoostConfig.get();

        addToggle(l, cy, w, "View Bob Optimization", "Reduce view-bobbing work.", c.viewBobOptimization,
                () -> c.viewBobOptimization = !c.viewBobOptimization);
        addToggle(l + w + 14, cy, w, "Particle Reduction", "Use adaptive particle reduction.", c.reducedParticles,
                () -> c.reducedParticles = !c.reducedParticles);
        cy += 70;
        addRenderableWidget(new SliderRow(l, cy, w, 58, "Particle Budget",
                "Percentage of particles allowed through.", c.particleLimitPercent, 1, 100,
                v -> c.particleLimitPercent = v));
        // Tier 0 is permanently locked in the controller; no user-facing
        // toggle is exposed here that could imply a downgrade is possible.
    }

    private void heading(String title, String description, int x, int y) {
        // Layout helper; the title and description are rendered once by render().
    }

    private void addToggle(int x, int y, int w, String title, String desc, boolean value, Runnable change) {
        addRenderableWidget(new ToggleRow(x, y, w, 58, title, desc, value, () -> {
            change.run();
            VoidBoostConfig.get().markDirty();
            rebuildWidgets();
        }));
    }

    private int snapFps(int value) {
        int[] values = {60, 120, 144, 165, 180, 240, 260};
        int best = values[0];
        for (int v : values) if (Math.abs(v - value) < Math.abs(best - value)) best = v;
        return best;
    }

    private String fpsText(int fps) {
        return fps >= 260 ? "Unlimited" : fps + " FPS";
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, BG);

        g.drawString(font, Component.literal("VoidBoost"), 30, 14, TEXT, false);
        g.drawString(font, Component.literal("Performance settings"), 30, 27, MUTED, false);

        String title = switch (page) {
            case 0 -> "General";
            case 1 -> "Performance";
            case 2 -> "Visuals";
            case 3 -> "PvP";
            case 4 -> "HUD";
            default -> "Advanced";
        };
        String description = switch (page) {
            case 0 -> "Profiles and core VoidBoost behavior.";
            case 1 -> "Tune the systems that directly affect frame time.";
            case 2 -> "Optional visual work that can be reduced for performance.";
            case 3 -> "Low-latency controls while preserving gameplay-critical entities.";
            case 4 -> "Monitoring and adaptive performance controls.";
            default -> "Internal performance behavior.";
        };

        g.drawString(font, Component.literal(title), 30, 82, TEXT, false);
        g.drawString(font, Component.literal(description), 30, 96, MUTED, false);
        g.fill(30, 112, width - 30, 113, BORDER);

        super.render(g, mouseX, mouseY, delta);

        int closeX = width - 30;
        g.drawString(font, Component.literal("X"), closeX - 7, 15, MUTED, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.x() >= width - 45 && event.y() <= 40) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private abstract static class Row extends AbstractWidget {
        Row(int x, int y, int w, int h, String name) {
            super(x, y, w, h, Component.literal(name));
        }

        protected void base(GuiGraphics g) {
            g.fill(getX(), getY(), getX() + width, getY() + height, isHovered() ? PANEL_HOVER : PANEL);
            g.fill(getX(), getY(), getX() + width, getY() + 1, BORDER);
            g.fill(getX(), getY() + height - 1, getX() + width, getY() + height, BORDER);
        }

        protected void text(GuiGraphics g, String title, String desc) {
            g.drawString(Minecraft.getInstance().font, Component.literal(title), getX() + 12, getY() + 10, TEXT, false);
            g.drawString(Minecraft.getInstance().font, Component.literal(desc), getX() + 12, getY() + 28, MUTED, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private final class Tab extends Row {
        private final String label;
        private final boolean selected;
        private final Runnable action;

        Tab(int x, int y, int w, int h, String label, boolean selected, Runnable action) {
            super(x, y, w, h, label);
            this.label = label;
            this.selected = selected;
            this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            g.fill(getX(), getY(), getX() + width, getY() + height, selected ? PURPLE_DARK : (isHovered() ? PANEL_HOVER : PANEL));
            g.drawString(Minecraft.getInstance().font, Component.literal(label),
                    getX() + (width - Minecraft.getInstance().font.width(label)) / 2, getY() + 9,
                    selected ? PURPLE : TEXT, false);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) { action.run(); }
    }

    private final class ToggleRow extends Row {
        private final String title, desc;
        private final boolean on;
        private final Runnable action;

        ToggleRow(int x, int y, int w, int h, String title, String desc, boolean on, Runnable action) {
            super(x, y, w, h, title);
            this.title = title; this.desc = desc; this.on = on; this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            base(g); text(g, title, desc);
            String state = on ? "ON" : "OFF";
            int color = on ? PURPLE : OFF;
            g.drawString(Minecraft.getInstance().font, Component.literal(state),
                    getX() + width - 54, getY() + 21, color, false);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) { action.run(); }
    }

    private final class ActionRow extends Row {
        private final String title, desc;
        private final Runnable action;

        ActionRow(int x, int y, int w, int h, String title, String desc, Runnable action) {
            super(x, y, w, h, title); this.title = title; this.desc = desc; this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            base(g); text(g, title, desc);
            g.drawString(Minecraft.getInstance().font, Component.literal("APPLY"),
                    getX() + width - 48, getY() + 21, PURPLE, false);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            action.run();
            Minecraft.getInstance().options.save();
            VoidBoostConfig.get().markDirty();
            rebuildWidgets();
        }
    }

    private final class SliderRow extends Row {
        private final String title, desc;
        private int value, min, max;
        private final IntConsumer setter;
        private boolean dragging;

        SliderRow(int x, int y, int w, int h, String title, String desc, int value, int min, int max, IntConsumer setter) {
            super(x, y, w, h, title);
            this.title = title; this.desc = desc; this.value = Math.max(min, Math.min(max, value));
            this.min = min; this.max = max; this.setter = setter;
        }

        private int valueAt(double mouseX) {
            double t = Math.max(0, Math.min(1, (mouseX - (getX() + 12)) / (double) (width - 24)));
            int raw = (int) Math.round(min + t * (max - min));
            if (title.equals("Max Framerate")) {
                int[] choices = {60, 120, 144, 165, 180, 240, 260};
                int nearest = choices[0];
                for (int choice : choices) {
                    if (Math.abs(choice - raw) < Math.abs(nearest - raw)) nearest = choice;
                }
                return nearest;
            }
            return raw;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            base(g); text(g, title, desc);
            String shown = title.equals("Max Framerate") ? fpsText(value) : Integer.toString(value);
            g.drawString(Minecraft.getInstance().font, Component.literal(shown),
                    getX() + width - Minecraft.getInstance().font.width(shown) - 12, getY() + 10, PURPLE, false);
            int tx = getX() + 12, tw = width - 24, ty = getY() + height - 9;
            g.fill(tx, ty, tx + tw, ty + 3, BORDER);
            int knob = tx + (int) Math.round((value - min) / (double) (max - min) * tw);
            g.fill(tx, ty, knob, ty + 3, PURPLE);
            g.fill(knob - 4, ty - 3, knob + 4, ty + 7, PURPLE);
        }

        @Override
        public void onClick(MouseButtonEvent e, boolean doubleClick) {
            value = valueAt(e.x());
            setter.accept(value);
            dragging = true;
            VoidBoostConfig.get().markDirty();
            Minecraft.getInstance().options.save();
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
            value = valueAt(e.x()); setter.accept(value); return true;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent e) {
            if (dragging) {
                VoidBoostConfig.get().markDirty();
                Minecraft.getInstance().options.save();
                dragging = false;
            }
            return true;
        }
    }
}
