package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Polished, scrollable client-side configuration screen for VoidBoost. */
public final class VoidBoostScreen extends Screen {
    private static final int BG = 0xFF07090E;
    private static final int SHELL = 0xFF0B0F17;
    private static final int SIDEBAR = 0xFF090C13;
    private static final int CARD = 0xFF101621;
    private static final int CARD_HOVER = 0xFF171F2C;
    private static final int CARD_SELECTED = 0xFF19182A;
    private static final int BORDER = 0xFF222B39;
    private static final int BORDER_HOVER = 0xFF3A4355;
    private static final int ACCENT = 0xFF9B7CFF;
    private static final int ACCENT_SOFT = 0xFF2A2148;
    private static final int TEXT = 0xFFF5F6FA;
    private static final int MUTED = 0xFF7F8A9F;
    private static final int GOOD = 0xFF75E0A4;
    private static final int BAD = 0xFFE07B8B;

    private final Screen parent;
    private int page;
    private double scrollOffset;

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

        int sidebarX = 26;
        int contentX = 202;
        int right = width - 26;
        int top = contentTop();

        addNav(sidebarX + 10, 116, "General", 0);
        addNav(sidebarX + 10, 158, "Quality", 1);
        addNav(sidebarX + 10, 200, "Performance", 2);

        if (page == 0) buildGeneral(contentX, right, top);
        else if (page == 1) buildQuality(contentX, right, top);
        else buildPerformance(contentX, right, top);

        addButton(sidebarX + 10, height - 70, 72, 40, "Reset", "", () -> {
            scrollOffset = 0;
            VoidBoostConfig.applyBalancedPreset();
            rebuildWidgets();
        }, false, true, "Restore the recommended Balanced profile");

        addButton(right - 112, height - 70, 112, 40, "Done", "SAVE", () -> {
            VoidBoostConfig.save();
            Minecraft.getInstance().setScreen(parent);
        }, true, true, "Save and close VoidBoost");
    }

    private int contentTop() {
        return 122 - (int) Math.round(scrollOffset);
    }

    private int contentBottom() {
        return height - 78;
    }

    private int maxScroll() {
        return switch (page) {
            case 0 -> 250;
            case 1 -> 130;
            default -> 170;
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < 190 || mouseY < 82 || mouseY > contentBottom()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        double amount = scrollY * 42.0;
        double old = scrollOffset;
        scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset - amount));
        if (old != scrollOffset) rebuildWidgets();
        return true;
    }

    private void addNav(int x, int y, String name, int target) {
        addButton(x, y, 134, 34, name, target == page ? "●" : "", () -> {
            page = target;
            scrollOffset = 0;
            rebuildWidgets();
        }, target == page, true, "");
    }

    private void buildGeneral(int left, int right, int y) {
        int cardW = (right - left - 14) / 2;
        addButton(left, y + 18, cardW, 82, "Balanced", "STABLE", VoidBoostConfig::applyBalancedPreset, isPreset("balanced"), false, "Smooth visuals • reliable FPS");
        addButton(left + cardW + 14, y + 18, cardW, 82, "Competitive", "PVP", VoidBoostConfig::applyCompetitivePreset, isPreset("competitive"), false, "Low latency • competitive rendering");
        addButton(left, y + 114, cardW, 82, "MAX FPS", "FAST", VoidBoostConfig::applyMaxFpsPreset, isPreset("max"), false, "Aggressive optimization • high FPS");
        addButton(left + cardW + 14, y + 114, cardW, 82, "ULTIMATE FPS", "EXTREME", VoidBoostConfig::applyUltimateLockedPreset, isPreset("ultimate"), false, "Maximum cuts • lowest render load");
        addButton(left, y + 210, right - left, 62, "Custom", "MANUAL", this::activateCustom, isPreset("custom"), false, "Keep your current settings and control every option");
        addSetting(left, y + 290, right, "Dynamic Render Distance", state("dynamic"), "Adaptive chunk distance", () -> toggle("dynamic"));
        addSetting(left, y + 342, right, "Target FPS", VoidBoostConfig.get().dynamicTargetFps + " FPS", "Adaptive rendering target", this::cycleTargetFps);
        addSetting(left, y + 394, right, "Performance Mode", state("performance"), "Apply optimized vanilla settings", () -> toggle("performance"));
    }

    private void buildQuality(int left, int right, int y) {
        addSetting(left, y + 26, right, "Particles", state("particles"), "ALL / REDUCED / MINIMAL", () -> toggle("particles"));
        addSetting(left, y + 78, right, "Entity Shadows", state("shadows"), "Shadow pass for entities", () -> toggle("shadows"));
        addSetting(left, y + 130, right, "Weather Effects", state("weather"), "Clouds and weather distance", () -> toggle("weather"));
        addSetting(left, y + 182, right, "Animation Optimization", state("animations"), "Reduce view animation work", () -> toggle("animations"));
        addSetting(left, y + 234, right, "Fog Optimization", state("fog"), "Reduce fog rendering", () -> toggle("fog"));
        addSetting(left, y + 286, right, "Entity Optimization", state("entities"), "Cull distant entities", () -> toggle("entities"));
    }

    private void buildPerformance(int left, int right, int y) {
        addSetting(left, y + 26, right, "Entity Optimization", state("entities"), "Cull distant entities", () -> toggle("entities"));
        addSetting(left, y + 78, right, "Entity Distance", VoidBoostConfig.get().maxEntityDistance + " BLOCKS", "Maximum entity render distance", this::cycleEntityDistance);
        addSetting(left, y + 130, right, "Performance Mode", state("performance"), "Apply optimized vanilla settings", () -> toggle("performance"));
        addSetting(left, y + 182, right, "Performance Monitor", state("monitor"), "Live FPS and frame diagnostics", () -> toggle("monitor"));
        addSetting(left, y + 234, right, "Dynamic Render Distance", state("dynamic"), "Adaptive chunk distance", () -> toggle("dynamic"));
        addInfo(left, y + 304, right, modeName());
    }

    private void addSetting(int left, int y, int right, String title, String value, String description, Runnable action) {
        addButton(left, y, right - left, 44, title, value, action, false, false, description);
    }

    private void addInfo(int left, int y, int right, String text) {
        addButton(left, y, right - left, 40, text, "ACTIVE", () -> {}, true, false, "");
    }

    private void addButton(int x, int y, int width, int height, String title, String value, Runnable action, boolean selected, boolean compact, String description) {
        addRenderableWidget(new PremiumButton(x, y, width, height, title, value, description, action, selected, compact));
    }

    private void activateCustom() {
        VoidBoostConfig c = VoidBoostConfig.get();
        c.maxFpsPreset = false;
        c.competitiveMode = false;
        c.ultimateLocked = false;
        c.markDirty();
        rebuildWidgets();
    }

    private void toggle(String key) {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.ultimateLocked && !key.equals("performance")) return;
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
            case "performance" -> {
                c.performanceMode = !c.performanceMode;
                if (!c.performanceMode) c.ultimateLocked = false;
            }
            case "monitor" -> c.performanceMonitor = !c.performanceMonitor;
            default -> { return; }
        }
        c.maxFpsPreset = false;
        c.competitiveMode = false;
        if (!key.equals("performance")) c.ultimateLocked = false;
        c.markDirty();
        rebuildWidgets();
    }

    private void cycleTargetFps() {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.ultimateLocked) return;
        c.dynamicTargetFps = c.dynamicTargetFps >= 240 ? 60 : c.dynamicTargetFps + 30;
        c.maxFpsPreset = false;
        c.competitiveMode = false;
        c.ultimateLocked = false;
        c.markDirty();
        rebuildWidgets();
    }

    private void cycleEntityDistance() {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.ultimateLocked) return;
        c.maxEntityDistance = c.maxEntityDistance >= 128 ? 32 : c.maxEntityDistance + 16;
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
        if (isPresetStatic("ultimate")) return "ULTIMATE FPS";
        if (isPresetStatic("competitive")) return "COMPETITIVE";
        if (isPresetStatic("max")) return "MAX FPS";
        if (isPresetStatic("balanced")) return "BALANCED";
        return "CUSTOM";
    }

    private static boolean isPresetStatic(String name) {
        VoidBoostConfig c = VoidBoostConfig.get();
        return switch (name) {
            case "ultimate" -> c.ultimateLocked;
            case "competitive" -> c.competitiveMode && !c.maxFpsPreset && !c.ultimateLocked;
            case "max" -> c.maxFpsPreset && !c.competitiveMode && !c.ultimateLocked;
            case "balanced" -> !c.competitiveMode && !c.maxFpsPreset && !c.ultimateLocked
                    && c.performanceMode && !c.disableParticles && c.reducedParticles
                    && !c.entityShadows && !c.weatherEffects && c.animationOptimization
                    && c.fogOptimization && c.entityRenderOptimization && c.dynamicRenderDistance
                    && c.dynamicTargetFps == 120 && c.maxEntityDistance == 56 && c.maxRenderDistance == 10;
            case "custom" -> !isPresetStatic("balanced") && !c.competitiveMode && !c.maxFpsPreset && !c.ultimateLocked;
            default -> false;
        };
    }

    private boolean isPreset(String name) {
        return isPresetStatic(name);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int shellX = 26;
        int shellTop = 24;
        int shellRight = width - 26;
        int shellBottom = height - 24;
        int sidebarRight = 180;
        int content = 202;
        int clipTop = 82;
        int clipBottom = shellBottom - 48;

        g.fill(0, 0, width, height, BG);
        g.fill(shellX, shellTop, shellRight, shellBottom, SHELL);
        g.fill(shellX, shellTop, sidebarRight, shellBottom, SIDEBAR);
        g.fill(shellX, shellTop, shellRight, shellTop + 2, ACCENT);
        g.fill(sidebarRight, shellTop + 2, sidebarRight + 1, shellBottom, BORDER);

        g.fill(shellX + 14, 40, shellX + 48, 74, ACCENT_SOFT);
        g.fill(shellX + 14, 40, shellX + 17, 74, ACCENT);
        g.drawString(font, Component.literal("V"), shellX + 25, 48, TEXT, false);
        g.drawString(font, Component.literal("VoidBoost"), shellX + 60, 41, TEXT, false);
        g.drawString(font, Component.literal("PERFORMANCE SUITE"), shellX + 60, 56, MUTED, false);
        g.drawString(font, Component.literal("CONTROL CENTER"), shellX + 12, 94, MUTED, false);
        g.fill(shellX + 12, 103, sidebarRight - 12, 104, BORDER);
        g.drawString(font, Component.literal("Made by VoidFlame"), shellX + 12, shellBottom - 38, MUTED, false);
        g.drawString(font, Component.literal("CLIENT OPTIMIZER"), shellX + 12, shellBottom - 25, MUTED, false);

        String title = page == 0 ? "General" : page == 1 ? "Quality" : "Performance";
        String subtitle = page == 0 ? "Profiles & adaptive rendering" : page == 1 ? "Visual workload controls" : "Frame-time optimization";
        g.drawString(font, Component.literal(title), content, 42, TEXT, false);
        g.drawString(font, Component.literal(subtitle), content, 57, MUTED, false);

        boolean perf = VoidBoostConfig.get().performanceMode;
        int badgeX = shellRight - 112;
        g.fill(badgeX, 40, shellRight - 12, 64, perf ? ACCENT_SOFT : CARD);
        g.fill(badgeX, 40, badgeX + 2, 64, perf ? ACCENT : BORDER);
        g.drawString(font, Component.literal(perf ? "OPTIMIZED" : "STANDARD"), badgeX + 12, 48, perf ? ACCENT : MUTED, false);

        int sectionY = contentTop();
        g.enableScissor(0, clipTop, width, clipBottom);
        if (page == 0) {
            g.drawString(font, Component.literal("PERFORMANCE PROFILES"), content, sectionY + 1, MUTED, false);
            g.drawString(font, Component.literal("ADAPTIVE CONTROLS"), content, sectionY + 282, MUTED, false);
        } else if (page == 1) {
            g.drawString(font, Component.literal("VISUAL LOAD"), content, sectionY + 8, MUTED, false);
        } else {
            g.drawString(font, Component.literal("RENDER ENGINE"), content, sectionY + 8, MUTED, false);
        }
        super.render(g, mouseX, mouseY, delta);
        g.disableScissor();

        if (maxScroll() > 0) {
            int scrollBottom = shellBottom - 50;
            g.drawString(font, Component.literal("SCROLL"), shellRight - 64, scrollBottom + 13, MUTED, false);
            g.fill(shellRight - 26, clipTop, shellRight - 23, scrollBottom, BORDER);
            int track = Math.max(1, scrollBottom - clipTop);
            int thumbH = Math.max(28, track * track / (track + maxScroll()));
            int thumbY = clipTop + (int) ((track - thumbH) * (scrollOffset / Math.max(1, maxScroll())));
            g.fill(shellRight - 26, thumbY, shellRight - 23, thumbY + thumbH, ACCENT);
        }

        g.fill(content, shellBottom - 49, shellRight - 12, shellBottom - 48, BORDER);
        g.drawString(font, Component.literal("VOIDBOOST  •  FABRIC  •  MINECRAFT 1.21.11"), content, shellBottom - 25, MUTED, false);
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
            int edge = selected ? ACCENT : (hover ? BORDER_HOVER : BORDER);
            g.fill(getX(), getY(), getX() + width, getY() + height, fill);
            g.fill(getX(), getY(), getX() + 3, getY() + height, edge);
            if (selected) g.fill(getX() + 3, getY(), getX() + width, getY() + 2, ACCENT);
            int titleY = getY() + (compact ? 11 : 12);
            int titleColor = hover || selected ? TEXT : 0xFFE7EAF0;
            g.drawString(Minecraft.getInstance().font, Component.literal(title), getX() + 16, titleY, titleColor, false);
            if (!description.isEmpty() && !compact) {
                g.drawString(Minecraft.getInstance().font, Component.literal(description), getX() + 16, titleY + 17, MUTED, false);
            }
            if (!value.isEmpty()) {
                int valueWidth = Minecraft.getInstance().font.width(value);
                int valueX = getX() + width - valueWidth - 16;
                int color = selected ? ACCENT : (value.equals("OFF") ? BAD : GOOD);
                g.drawString(Minecraft.getInstance().font, Component.literal(value), valueX, getY() + 13, color, false);
            }
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
            action.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
