package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Premium VoidBoost configuration screen. */
public final class VoidBoostScreen extends Screen {
    private static final int BG = 0xFF07090D;
    private static final int PANEL = 0xFF0D1118;
    private static final int PANEL_2 = 0xFF111722;
    private static final int PANEL_3 = 0xFF151C28;
    private static final int BORDER = 0xFF283142;
    private static final int ACCENT = 0xFF8A7CFF;
    private static final int ACCENT_SOFT = 0xFF292442;
    private static final int TEXT = 0xFFF5F7FB;
    private static final int MUTED = 0xFF8994A8;
    private static final int GOOD = 0xFF83E0A9;
    private static final int DANGER = 0xFFE58B9B;

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
        int sidebar = 178;
        int content = 198;
        int right = width - 34;
        int top = 92;

        addNavButton(left + 12, 92, "General", 0);
        addNavButton(left + 12, 128, "Quality", 1);
        addNavButton(left + 12, 164, "Performance", 2);

        if (page == 0) buildGeneral(content, right, top);
        else if (page == 1) buildQuality(content, right, top);
        else buildPerformance(content, right, top);

        addPremiumButton("Reset", left + 12, height - 54, 64, () -> {
            VoidBoostConfig.applyBalancedPreset();
            rebuildWidgets();
        }, false);
        addPremiumButton("Done", right - 104, height - 54, 104, () -> {
            VoidBoostConfig.save();
            Minecraft.getInstance().setScreen(parent);
        }, true);
    }

    private void addNavButton(int x, int y, String text, int target) {
        addPremiumButton(text, x, y, 132, () -> {
            page = target;
            rebuildWidgets();
        }, page == target);
    }

    private void buildGeneral(int left, int right, int top) {
        drawOnlyNote(left, top, "Profiles", "Start with a preset, then tune every option yourself.");
        addPresetButton(left, top + 34, 204, 48, "Balanced", "Stable", VoidBoostConfig::applyBalancedPreset);
        addPresetButton(left + 214, top + 34, 204, 48, "Competitive", "PvP", VoidBoostConfig::applyCompetitivePreset);
        addPresetButton(left, top + 88, 204, 48, "MAX FPS", "Maximum", VoidBoostConfig::applyMaxFpsPreset);
        addPresetButton(left + 214, top + 88, 204, 48, "ULTIMATE FPS", "Extreme", VoidBoostConfig::applyUltimateLockedPreset);

        drawOnlyNote(left, top + 154, "Adaptive Rendering", "Keeps the game responsive by adjusting render distance to your target.");
        addSettingButton(left, top + 190, right, "Dynamic Render Distance", state("dynamic"), () -> toggle("dynamic"));
        addSettingButton(left, top + 242, right, "Target FPS", String.valueOf(VoidBoostConfig.get().dynamicTargetFps), this::cycleTargetFps);
    }

    private void buildQuality(int left, int right, int top) {
        drawOnlyNote(left, top, "Visual Quality", "Every control is independent. Nothing is locked by a preset.");
        addSettingButton(left, top + 40, right, "Particles", state("particles"), () -> toggle("particles"));
        addSettingButton(left, top + 92, right, "Entity Shadows", state("shadows"), () -> toggle("shadows"));
        addSettingButton(left, top + 144, right, "Weather Effects", state("weather"), () -> toggle("weather"));
        addSettingButton(left, top + 196, right, "Animation Optimization", state("animations"), () -> toggle("animations"));
        addSettingButton(left, top + 248, right, "Fog Optimization", state("fog"), () -> toggle("fog"));
    }

    private void buildPerformance(int left, int right, int top) {
        drawOnlyNote(left, top, "Performance", "Low-overhead rendering controls focused on real FPS gains.");
        addSettingButton(left, top + 40, right, "Entity Optimization", state("entities"), () -> toggle("entities"));
        addSettingButton(left, top + 92, right, "Entity Distance", String.valueOf(VoidBoostConfig.get().maxEntityDistance), this::cycleEntityDistance);
        addSettingButton(left, top + 144, right, "Performance Mode", state("performance"), () -> toggle("performance"));
        addSettingButton(left, top + 196, right, "Performance Monitor", state("monitor"), () -> toggle("monitor"));
        drawProfileCard(left, top + 266, right);
    }

    private void addPresetButton(int x, int y, int w, int h, String title, String tag, Runnable action) {
        addPremiumButton(title + "  ·  " + tag, x, y, w, () -> {
            action.run();
            rebuildWidgets();
        }, false);
    }

    private void addSettingButton(int left, int y, int right, String title, String value, Runnable action) {
        int buttonWidth = 116;
        int buttonX = right - buttonWidth - 12;
        addPremiumButton(value, buttonX, y + 8, buttonWidth, action, false);
    }

    private void addPremiumButton(String text, int x, int y, int width, Runnable action, boolean selected) {
        addRenderableWidget(new PremiumButton(x, y, width, 32, Component.literal(text), action, selected));
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
        if (c.competitiveMode) return "COMPETITIVE";
        if (c.maxFpsPreset) return "MAX FPS";
        return "CUSTOM";
    }

    private void drawOnlyNote(int x, int y, String title, String subtitle) {
        // Rendered in render(); widgets are intentionally added separately so their hitboxes never overlap the cards.
    }

    private void drawProfileCard(int left, int y, int right) {
        Minecraft client = Minecraft.getInstance();
        int bottom = Math.min(y + 70, height - 70);
        gStatic = null;
    }

    private GuiGraphics gStatic;

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int left = 34;
        int sidebar = 178;
        int content = 198;
        int right = width - 34;
        int top = 92;

        g.fill(0, 0, width, height, BG);
        g.fill(left, 28, right, height - 28, PANEL);
        g.fill(left, 28, sidebar, height - 28, 0xFF090C12);
        g.fill(left, 28, right, 30, ACCENT);
        g.fill(sidebar, 28, sidebar + 1, height - 28, BORDER);

        // Header.
        g.fill(left + 12, 42, left + 44, 74, ACCENT_SOFT);
        g.fill(left + 12, 42, left + 16, 74, ACCENT);
        g.drawString(font, Component.literal("V"), left + 22, 49, TEXT, false);
        g.drawString(font, Component.literal("VoidBoost"), left + 54, 42, TEXT, false);
        g.drawString(font, Component.literal("PERFORMANCE SUITE"), left + 54, 57, MUTED, false);
        g.drawString(font, Component.literal("Made by VoidFlame"), right - 118, 48, MUTED, false);

        String title = page == 0 ? "General" : page == 1 ? "Quality" : "Performance";
        String subtitle = page == 0 ? "Optimization profiles and adaptive rendering" : page == 1 ? "Visual effects with direct performance impact" : "Rendering controls and diagnostics";
        g.drawString(font, Component.literal(title), content + 12, 42, TEXT, false);
        g.drawString(font, Component.literal(subtitle), content + 12, 57, MUTED, false);

        // Sidebar labels and selected states.
        drawNav(g, left + 12, 92, "General", page == 0);
        drawNav(g, left + 12, 128, "Quality", page == 1);
        drawNav(g, left + 12, 164, "Performance", page == 2);

        if (page == 0) drawGeneral(g, content, right, top);
        else if (page == 1) drawQuality(g, content, right, top);
        else drawPerformance(g, content, right, top);

        // Footer.
        g.drawString(font, Component.literal("CLIENT-SIDE  •  1.21.11"), left + 12, height - 43, MUTED, false);
        super.render(g, mouseX, mouseY, delta);
    }

    private void drawNav(GuiGraphics g, int x, int y, String text, boolean selected) {
        if (selected) {
            g.fill(x, y, x + 132, y + 32, ACCENT_SOFT);
            g.fill(x, y, x + 3, y + 32, ACCENT);
        }
        g.drawString(font, Component.literal(text), x + 12, y + 10, selected ? TEXT : MUTED, false);
    }

    private void drawGeneral(GuiGraphics g, int left, int right, int top) {
        drawSection(g, left, top, "Profiles", "Start with a preset, then tune every option yourself.");
        drawCard(g, left, top + 34, 204, 48, "Balanced", "Stable", isPreset("balanced"));
        drawCard(g, left + 214, top + 34, 204, 48, "Competitive", "PvP", isPreset("competitive"));
        drawCard(g, left, top + 88, 204, 48, "MAX FPS", "Maximum", isPreset("max"));
        drawCard(g, left + 214, top + 88, 204, 48, "ULTIMATE FPS", "Extreme", isPreset("ultimate"));
        drawSection(g, left, top + 154, "Adaptive Rendering", "Automatically adjusts render distance around your FPS target.");
        drawSetting(g, left, top + 190, right, "Dynamic Render Distance", "Keeps FPS stable by changing chunk distance.");
        drawSetting(g, left, top + 242, right, "Target FPS", "Used only by dynamic render distance.");
    }

    private void drawQuality(GuiGraphics g, int left, int right, int top) {
        drawSection(g, left, top, "Visual Quality", "Every control is independent. Nothing is locked by a preset.");
        drawSetting(g, left, top + 40, right, "Particles", "All, reduced, or minimal particles.");
        drawSetting(g, left, top + 92, right, "Entity Shadows", "Disable expensive entity shadow rendering.");
        drawSetting(g, left, top + 144, right, "Weather Effects", "Control clouds and weather rendering.");
        drawSetting(g, left, top + 196, right, "Animation Optimization", "Reduce view and animation work for PvP.");
        drawSetting(g, left, top + 248, right, "Fog Optimization", "Reduce client fog rendering work.");
    }

    private void drawPerformance(GuiGraphics g, int left, int right, int top) {
        drawSection(g, left, top, "Performance", "Low-overhead rendering controls focused on real FPS gains.");
        drawSetting(g, left, top + 40, right, "Entity Optimization", "Skip entities outside the selected render distance.");
        drawSetting(g, left, top + 92, right, "Entity Distance", "Maximum distance used by entity optimization.");
        drawSetting(g, left, top + 144, right, "Performance Mode", "Apply low-cost vanilla rendering settings.");
        drawSetting(g, left, top + 196, right, "Performance Monitor", "Optional live FPS and frame-time diagnostics.");
        drawSection(g, left, top + 250, "Current Profile", "Your active preset state.");
        g.drawString(font, Component.literal(modeName()), left + 10, top + 274, GOOD, false);
    }

    private void drawSection(GuiGraphics g, int x, int y, String title, String subtitle) {
        g.drawString(font, Component.literal(title), x + 10, y, TEXT, false);
        g.drawString(font, Component.literal(subtitle), x + 10, y + 15, MUTED, false);
    }

    private void drawCard(GuiGraphics g, int x, int y, int w, int h, String title, String tag, boolean selected) {
        g.fill(x, y, x + w, y + h, selected ? ACCENT_SOFT : PANEL_3);
        g.fill(x, y, x + 3, y + h, selected ? ACCENT : BORDER);
        g.drawString(font, Component.literal(title), x + 12, y + 9, TEXT, false);
        g.drawString(font, Component.literal(tag), x + 12, y + 25, selected ? ACCENT : MUTED, false);
        if (selected) g.fill(x + w - 10, y + 8, x + w - 6, y + 12, GOOD);
    }

    private void drawSetting(GuiGraphics g, int x, int y, int right, String title, String description) {
        g.fill(x, y, right - 4, y + 44, PANEL_2);
        g.fill(x, y, x + 3, y + 44, ACCENT);
        g.drawString(font, Component.literal(title), x + 12, y + 7, TEXT, false);
        g.drawString(font, Component.literal(description), x + 12, y + 23, MUTED, false);
    }

    private boolean isPreset(String name) {
        VoidBoostConfig c = VoidBoostConfig.get();
        return switch (name) {
            case "competitive" -> c.competitiveMode;
            case "max" -> c.maxFpsPreset;
            case "ultimate" -> c.competitiveMode && c.maxRenderDistance <= 6;
            default -> !c.competitiveMode && !c.maxFpsPreset;
        };
    }

    private static final class PremiumButton extends AbstractWidget {
        private final Runnable action;
        private final boolean selected;

        private PremiumButton(int x, int y, int width, int height, Component message, Runnable action, boolean selected) {
            super(x, y, width, height, message);
            this.action = action;
            this.selected = selected;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            boolean hover = isHovered();
            int fill = selected ? ACCENT_SOFT : (hover ? 0xFF202938 : PANEL_3);
            int border = selected ? ACCENT : (hover ? ACCENT : BORDER);
            g.fill(getX(), getY(), getX() + width, getY() + height, fill);
            g.fill(getX(), getY(), getX() + 2, getY() + height, border);
            int textWidth = Minecraft.getInstance().font.width(getMessage());
            int textX = getX() + (width - textWidth) / 2;
            int textY = getY() + (height - 8) / 2;
            g.drawString(Minecraft.getInstance().font, getMessage(), textX, textY, TEXT, false);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            action.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
