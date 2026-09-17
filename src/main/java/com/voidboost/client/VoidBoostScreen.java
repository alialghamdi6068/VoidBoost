package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Premium VoidBoost configuration screen. */
public final class VoidBoostScreen extends Screen {
    private static final int BG = 0xFF06080C;
    private static final int PANEL = 0xFF0B0F16;
    private static final int CARD = 0xFF111722;
    private static final int CARD_HOVER = 0xFF171F2D;
    private static final int CARD_SELECTED = 0xFF211C3A;
    private static final int BORDER = 0xFF252E3C;
    private static final int ACCENT = 0xFF9B82FF;
    private static final int ACCENT_DARK = 0xFF352A59;
    private static final int TEXT = 0xFFF4F5FA;
    private static final int MUTED = 0xFF8490A5;
    private static final int GOOD = 0xFF7EE2A8;

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

        int left = 34;
        int sidebarRight = 184;
        int content = 204;
        int right = width - 34;

        addNav(left + 12, 92, "General", 0);
        addNav(left + 12, 134, "Quality", 1);
        addNav(left + 12, 176, "Performance", 2);

        if (page == 0) buildGeneral(content, right);
        else if (page == 1) buildQuality(content, right);
        else buildPerformance(content, right);

        addButton(left + 12, height - 62, 78, 40, "Reset", "Defaults", () -> {
            VoidBoostConfig.applyBalancedPreset();
            rebuildWidgets();
        }, false, false);
        addButton(right - 116, height - 62, 116, 40, "Done", "Save", () -> {
            VoidBoostConfig.save();
            Minecraft.getInstance().setScreen(parent);
        }, true, false);
    }

    private void addNav(int x, int y, String name, int target) {
        addButton(x, y, 138, 34, name, target == page ? "ACTIVE" : "", () -> {
            page = target;
            rebuildWidgets();
        }, target == page, true);
    }

    private void buildGeneral(int left, int right) {
        int y = 108;
        addButton(left, y, 222, 62, "Balanced", "Stable profile", VoidBoostConfig::applyBalancedPreset, isPreset("balanced"), false);
        addButton(left + 234, y, 222, 62, "Competitive", "PvP profile", VoidBoostConfig::applyCompetitivePreset, isPreset("competitive"), false);
        addButton(left, y + 74, 222, 62, "MAX FPS", "High performance", VoidBoostConfig::applyMaxFpsPreset, isPreset("max"), false);
        addButton(left + 234, y + 74, 222, 62, "ULTIMATE FPS", "Extreme performance", VoidBoostConfig::applyUltimateLockedPreset, isPreset("ultimate"), false);

        drawSectionTitle(left, 262, "Adaptive Rendering", "Automatic chunk-distance control based on your FPS target.");
        addSetting(left, 292, right, "Dynamic Render Distance", state("dynamic"), "Automatically adjusts chunks", () -> toggle("dynamic"));
        addSetting(left, 344, right, "Target FPS", String.valueOf(VoidBoostConfig.get().dynamicTargetFps) + " FPS", "Target used by adaptive rendering", this::cycleTargetFps);
    }

    private void buildQuality(int left, int right) {
        drawSectionTitle(left, 108, "Visual Quality", "Direct controls for expensive visual effects.");
        addSetting(left, 140, right, "Particles", state("particles"), "ALL / REDUCED / MINIMAL", () -> toggle("particles"));
        addSetting(left, 192, right, "Entity Shadows", state("shadows"), "Disable entity shadow rendering", () -> toggle("shadows"));
        addSetting(left, 244, right, "Weather Effects", state("weather"), "Clouds and weather rendering", () -> toggle("weather"));
        addSetting(left, 296, right, "Animation Optimization", state("animations"), "Reduce view animation work", () -> toggle("animations"));
        addSetting(left, 348, right, "Fog Optimization", state("fog"), "Reduce fog rendering work", () -> toggle("fog"));
    }

    private void buildPerformance(int left, int right) {
        drawSectionTitle(left, 108, "Performance", "Low-overhead controls designed for real frame-time reduction.");
        addSetting(left, 140, right, "Entity Optimization", state("entities"), "Skip distant entity rendering", () -> toggle("entities"));
        addSetting(left, 192, right, "Entity Distance", String.valueOf(VoidBoostConfig.get().maxEntityDistance) + " blocks", "Maximum entity render distance", this::cycleEntityDistance);
        addSetting(left, 244, right, "Performance Mode", state("performance"), "Apply aggressive vanilla settings", () -> toggle("performance"));
        addSetting(left, 296, right, "Performance Monitor", state("monitor"), "Optional live diagnostics", () -> toggle("monitor"));

        drawSectionTitle(left, 358, "Current Profile", "Active optimization profile.");
        addInfo(left, 388, right, modeName());
    }

    private void addSetting(int left, int y, int right, String title, String value, String description, Runnable action) {
        addButton(left, y, right - left - 4, 44, title, value, action, false, false, description);
    }

    private void addInfo(int left, int y, int right, String text) {
        addButton(left, y, right - left - 4, 38, text, "ACTIVE PROFILE", () -> {}, true, false);
    }

    private void addButton(int x, int y, int width, int height, String title, String value, Runnable action, boolean selected, boolean compact) {
        addButton(x, y, width, height, title, value, action, selected, compact, "");
    }

    private void addButton(int x, int y, int width, int height, String title, String value, Runnable action, boolean selected, boolean compact, String description) {
        addRenderableWidget(new PremiumButton(x, y, width, height, title, value, description, action, selected, compact));
    }

    private void toggle(String key) {
        VoidBoostConfig c = VoidBoostConfig.get();
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
        c.ultimateLocked = false;
        c.markDirty();
        rebuildWidgets();
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
        if (c.competitiveMode && c.maxRenderDistance <= 6) return "ULTIMATE FPS";
        if (c.competitiveMode) return "COMPETITIVE";
        if (c.maxFpsPreset) return "MAX FPS";
        return "CUSTOM";
    }

    private boolean isPreset(String name) {
        VoidBoostConfig c = VoidBoostConfig.get();
        return switch (name) {
            case "competitive" -> c.competitiveMode && c.maxRenderDistance > 6;
            case "max" -> c.maxFpsPreset;
            case "ultimate" -> c.competitiveMode && c.maxRenderDistance <= 6;
            default -> !c.competitiveMode && !c.maxFpsPreset;
        };
    }

    private void drawSectionTitle(int x, int y, String title, String subtitle) {
        // Section labels are drawn in render so widgets stay fully clickable.
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int left = 34;
        int sidebarRight = 184;
        int content = 204;
        int right = width - 34;

        g.fill(0, 0, width, height, BG);
        g.fill(left, 28, right, height - 28, PANEL);
        g.fill(left, 28, sidebarRight, height - 28, 0xFF080B11);
        g.fill(left, 28, right, 31, ACCENT);
        g.fill(sidebarRight, 31, sidebarRight + 1, height - 28, BORDER);

        // Brand.
        g.fill(left + 14, 43, left + 48, 77, ACCENT_DARK);
        g.fill(left + 14, 43, left + 18, 77, ACCENT);
        g.drawString(font, Component.literal("V"), left + 25, 50, TEXT, false);
        g.drawString(font, Component.literal("VoidBoost"), left + 60, 43, TEXT, false);
        g.drawString(font, Component.literal("PERFORMANCE SUITE"), left + 60, 58, MUTED, false);
        g.drawString(font, Component.literal("Made by VoidFlame"), right - 118, 49, MUTED, false);

        String title = page == 0 ? "General" : page == 1 ? "Quality" : "Performance";
        String subtitle = page == 0 ? "Profiles & adaptive rendering" : page == 1 ? "Visual performance controls" : "Frame-time optimization";
        g.drawString(font, Component.literal(title), content + 10, 43, TEXT, false);
        g.drawString(font, Component.literal(subtitle), content + 10, 58, MUTED, false);

        if (page == 0) {
            drawSection(g, content, 96, "Optimization Profiles", "Choose a profile. Every option can still be tuned.");
            drawSection(g, content, 250, "Adaptive Rendering", "Automatic chunk-distance control based on your FPS target.");
        } else if (page == 1) {
            drawSection(g, content, 96, "Visual Quality", "Direct controls for expensive visual effects.");
        } else {
            drawSection(g, content, 96, "Performance", "Low-overhead controls designed for real frame-time reduction.");
            drawSection(g, content, 350, "Current Profile", "Active optimization profile.");
        }

        g.drawString(font, Component.literal("CLIENT-SIDE  •  MINECRAFT 1.21.11"), left + 12, height - 45, MUTED, false);
        super.render(g, mouseX, mouseY, delta);
    }

    private void drawSection(GuiGraphics g, int x, int y, String title, String subtitle) {
        g.drawString(font, Component.literal(title), x + 10, y, TEXT, false);
        g.drawString(font, Component.literal(subtitle), x + 10, y + 15, MUTED, false);
    }

    private static final class PremiumButton extends AbstractWidget {
        private final String title;
        private final String value;
        private final String description;
        private final Runnable action;
        private final boolean selected;
        private final boolean compact;

        private PremiumButton(int x, int y, int width, int height, String title, String value, String description, Runnable action, boolean selected, boolean compact) {
            super(x, y, width, height, Component.literal(title));
            this.title = title;
            this.value = value;
            this.description = description;
            this.action = action;
            this.selected = selected;
            this.compact = compact;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            boolean hover = isHovered();
            int fill = selected ? CARD_SELECTED : (hover ? CARD_HOVER : CARD);
            int edge = selected ? ACCENT : (hover ? ACCENT : BORDER);
            g.fill(getX(), getY(), getX() + width, getY() + height, fill);
            g.fill(getX(), getY(), getX() + 3, getY() + height, edge);

            int titleY = getY() + (compact ? 9 : 8);
            g.drawString(Minecraft.getInstance().font, Component.literal(title), getX() + 14, titleY, TEXT, false);

            if (!description.isEmpty() && !compact) {
                g.drawString(Minecraft.getInstance().font, Component.literal(description), getX() + 14, titleY + 16, MUTED, false);
            }

            if (!value.isEmpty()) {
                int valueWidth = Minecraft.getInstance().font.width(value);
                int valueX = getX() + width - valueWidth - 14;
                int valueY = getY() + (height - 8) / 2;
                g.drawString(Minecraft.getInstance().font, Component.literal(value), valueX, valueY, selected ? ACCENT : GOOD, false);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            action.run();
        }
    }
}
