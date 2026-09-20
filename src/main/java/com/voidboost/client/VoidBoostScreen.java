package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * VoidBoost custom settings UI.
 *
 * Layout is intentionally based on the supplied VoidBoost reference artwork:
 * floating header, left navigation rail, large performance panel, cyan accents,
 * compact settings rows and no dependency on vanilla Video Settings.
 */
public final class VoidBoostScreen extends Screen {
    private static final int BACKDROP = 0xB8040A12;
    private static final int SIDE = 0xE60A111B;
    private static final int PANEL = 0xE90B1420;
    private static final int ROW = 0xD9141E2A;
    private static final int ROW_HOVER = 0xE71C2935;
    private static final int BORDER = 0x663B5368;
    private static final int CYAN = 0xFF31D8FF;
    private static final int CYAN_DIM = 0x6631D8FF;
    private static final int TEXT = 0xFFF2F8FC;
    private static final int MUTED = 0xFF91A8B8;

    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath("voidboost", "textures/gui/voidboost_logo.png");

    private final Screen parent;
    private int page = 1;

    private boolean draggingRender;
    private boolean draggingSimulation;
    private boolean draggingFps;

    private int renderDistance = 12;
    private int simulationDistance = 8;
    private int fps = 240;

    public VoidBoostScreen(Screen parent) {
        super(Component.literal("VoidBoost"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Minecraft client = Minecraft.getInstance();
        VoidBoostConfig c = VoidBoostConfig.get();

        renderDistance = clamp(c.maxRenderDistance, 4, 12);
        simulationDistance = clamp(client.options.simulationDistance().get(), 5, 32);
        fps = clamp(c.targetFps, 30, 1000);

        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();

        int[] l = layout();
        int sx = l[0], py = l[1], sw = l[2], mainX = l[3], mainW = l[4], ph = l[5];

        int navY = py + 100;
        nav(sx + 12, navY, sw - 24, "General", 0);
        nav(sx + 12, navY + 46, sw - 24, "Performance", 1);
        nav(sx + 12, navY + 92, sw - 24, "Visuals", 2);
        nav(sx + 12, navY + 138, sw - 24, "PvP", 3);
        nav(sx + 12, navY + 184, sw - 24, "HUD", 4);
        nav(sx + 12, navY + 230, sw - 24, "Advanced", 5);

        if (page == 1) {
            performance(mainX, py, mainW, ph);
        } else if (page == 0) {
            general(mainX, py, mainW, ph);
        } else {
            simplePage(mainX, py, mainW, ph, page);
        }
    }

    /**
     * Reference canvas is 1536x1024 with the main panels starting around y=178
     * and ending around y=946. Keep that vertical composition on large displays,
     * while shrinking gracefully on smaller screens.
     */
    private int[] layout() {
        int shellW = Math.min(width - 104, 1432);
        int sideW = Math.min(306, Math.max(270, shellW / 5));
        int mainW = shellW - sideW - 20;

        int panelH = Math.min(768, Math.max(560, height - 256));
        int panelY = Math.min(178, Math.max(72, height - panelH - 78));
        int shellX = (width - shellW) / 2;
        int mainX = shellX + sideW + 20;

        return new int[]{shellX, panelY, sideW, mainX, mainW, panelH};
    }

    private void nav(int x, int y, int w, String label, int targetPage) {
        addRenderableWidget(new NavButton(
                x, y, w, label, targetPage == page,
                () -> {
                    page = targetPage;
                    rebuildWidgets();
                }
        ));
    }

    private void performance(int x, int y, int w, int h) {
        int rowY = y + 154;
        int rowH = 58;
        int gap = 14;

        addRenderableWidget(new SliderRow(
                x + 22, rowY, w - 44, rowH,
                "Render Distance",
                "How far you can see in the world.",
                renderDistance + " Chunks",
                4, 12, renderDistance, 0
        ));
        rowY += rowH + gap;

        addRenderableWidget(new SliderRow(
                x + 22, rowY, w - 44, rowH,
                "Simulation Distance",
                "How far game mechanics are simulated.",
                simulationDistance + " Chunks",
                5, 32, simulationDistance, 1
        ));
        rowY += rowH + gap;

        addRenderableWidget(new SliderRow(
                x + 22, rowY, w - 44, rowH,
                "Max Framerate",
                "Limits the FPS to reduce heat and lag.",
                fps + " FPS",
                30, 1000, fps, 2
        ));
        rowY += rowH + gap;

        VoidBoostConfig c = VoidBoostConfig.get();

        addRenderableWidget(new ToggleRow(
                x + 22, rowY, w - 44, rowH,
                "VSync",
                "Synchronize frames with your monitor.",
                !c.vsyncOptimization,
                () -> {
                    c.vsyncOptimization = !c.vsyncOptimization;
                    c.markDirty();
                    rebuildWidgets();
                }
        ));
        rowY += rowH + gap;

        addRenderableWidget(new ChoiceRow(
                x + 22, rowY, w - 44, rowH,
                "Particles",
                "Reduce particle effects for better performance.",
                particles(),
                this::cycleParticles
        ));
        rowY += rowH + gap;

        addRenderableWidget(new ToggleRow(
                x + 22, rowY, w - 44, rowH,
                "Entity Shadows",
                "Disable entity shadows to save resources.",
                c.entityShadows,
                () -> {
                    c.entityShadows = !c.entityShadows;
                    c.markDirty();
                    rebuildWidgets();
                }
        ));
        rowY += rowH + gap;

        addRenderableWidget(new ToggleRow(
                x + 22, rowY, w - 44, rowH,
                "FPS Boost",
                "Applies additional optimizations (experimental).",
                c.performanceMode,
                () -> {
                    c.performanceMode = !c.performanceMode;
                    c.markDirty();
                    rebuildWidgets();
                }
        ));
    }

    private void general(int x, int y, int w, int h) {
        int rowY = y + 154;
        int rowH = 58;
        int gap = 14;

        addRenderableWidget(new ProfileRow(
                x + 22, rowY, w - 44, rowH,
                "Balanced", "Stable everyday performance",
                VoidBoostConfig::applyBalancedPreset
        ));
        rowY += rowH + gap;

        addRenderableWidget(new ProfileRow(
                x + 22, rowY, w - 44, rowH,
                "Competitive", "Low-latency PvP profile",
                VoidBoostConfig::applyCompetitivePreset
        ));
        rowY += rowH + gap;

        addRenderableWidget(new ProfileRow(
                x + 22, rowY, w - 44, rowH,
                "MAX FPS", "Aggressive performance profile",
                VoidBoostConfig::applyMaxFpsPreset
        ));
        rowY += rowH + gap;

        addRenderableWidget(new ProfileRow(
                x + 22, rowY, w - 44, rowH,
                "ULTIMATE FPS", "Maximum performance profile",
                VoidBoostConfig::applyUltimateLockedPreset
        ));
    }

    private void simplePage(int x, int y, int w, int h, int p) {
        int rowY = y + 154;
        int rowH = 58;
        int gap = 14;
        int rw = w - 44;
        VoidBoostConfig c = VoidBoostConfig.get();

        if (p == 2) {
            addToggle(x, rowY, rw, "Weather Effects", "Keep weather rendering enabled.", c.weatherEffects,
                    () -> { c.weatherEffects = !c.weatherEffects; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Cloud Optimization", "Disable clouds to reduce render work.", c.cloudOptimization,
                    () -> { c.cloudOptimization = !c.cloudOptimization; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Vignette Optimization", "Reduce the vignette rendering cost.", c.vignetteOptimization,
                    () -> { c.vignetteOptimization = !c.vignetteOptimization; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Ambient Occlusion", "Reduce ambient occlusion calculations.", c.ambientOcclusionOptimization,
                    () -> { c.ambientOcclusionOptimization = !c.ambientOcclusionOptimization; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Mipmap Optimization", "Lower mipmap work for better performance.", c.mipmapOptimization,
                    () -> { c.mipmapOptimization = !c.mipmapOptimization; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Biome Blend Optimization", "Reduce biome color blending work.", c.biomeBlendOptimization,
                    () -> { c.biomeBlendOptimization = !c.biomeBlendOptimization; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "View Bob Optimization", "Disable view bobbing for lower visual overhead.", c.viewBobOptimization,
                    () -> { c.viewBobOptimization = !c.viewBobOptimization; });
        } else if (p == 3) {
            addToggle(x, rowY, rw, "Competitive Mode", "Use the low-latency competitive profile.", c.competitiveMode,
                    () -> { c.competitiveMode = !c.competitiveMode; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Entity Render Optimization", "Reduce distant entity rendering work.", c.entityRenderOptimization,
                    () -> { c.entityRenderOptimization = !c.entityRenderOptimization; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Animation Optimization", "Reduce expensive animation updates.", c.animationOptimization,
                    () -> { c.animationOptimization = !c.animationOptimization; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Fog Optimization", "Reduce fog rendering overhead.", c.fogOptimization,
                    () -> { c.fogOptimization = !c.fogOptimization; });
            rowY += rowH + gap;
            addRenderableWidget(new IntSliderRow(x + 22, rowY, rw, rowH,
                    "Entity Distance", "Maximum distance for entity processing.",
                    c.maxEntityDistance, 32, 128, v -> c.maxEntityDistance = v));
        } else if (p == 4) {
            addToggle(x, rowY, rw, "Performance Monitor", "Show the VoidBoost performance monitor.", c.performanceMonitor,
                    () -> { c.performanceMonitor = !c.performanceMonitor; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Dynamic Render Distance", "Adapt render distance to current FPS.", c.dynamicRenderDistance,
                    () -> { c.dynamicRenderDistance = !c.dynamicRenderDistance; });
            rowY += rowH + gap;
            addRenderableWidget(new IntSliderRow(x + 22, rowY, rw, rowH,
                    "Dynamic Target FPS", "FPS target used by adaptive rendering.",
                    c.dynamicTargetFps, 60, 240, v -> c.dynamicTargetFps = v));
        } else {
            addToggle(x, rowY, rw, "Dynamic Render Distance", "Automatically adjust render distance.", c.dynamicRenderDistance,
                    () -> { c.dynamicRenderDistance = !c.dynamicRenderDistance; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Animation Optimization", "Reduce animation update overhead.", c.animationOptimization,
                    () -> { c.animationOptimization = !c.animationOptimization; });
            rowY += rowH + gap;
            addToggle(x, rowY, rw, "Fog Optimization", "Reduce fog rendering overhead.", c.fogOptimization,
                    () -> { c.fogOptimization = !c.fogOptimization; });
            rowY += rowH + gap;
            addRenderableWidget(new IntSliderRow(x + 22, rowY, rw, rowH,
                    "Dynamic Target FPS", "Target used by adaptive render distance.",
                    c.dynamicTargetFps, 60, 240, v -> c.dynamicTargetFps = v));
            rowY += rowH + gap;
            addRenderableWidget(new IntSliderRow(x + 22, rowY, rw, rowH,
                    "Max Entity Distance", "Limit distant entity processing.",
                    c.maxEntityDistance, 32, 128, v -> c.maxEntityDistance = v));
        }
    }

    private void addToggle(int x, int y, int w, String title, String desc, boolean value, Runnable action) {
        addRenderableWidget(new ToggleRow(x + 22, y, w, 58, title, desc, value, () -> {
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

        if (!c.reducedParticles && !c.disableParticles) {
            c.reducedParticles = true;
        } else if (c.reducedParticles) {
            c.reducedParticles = false;
            c.disableParticles = true;
        } else {
            c.disableParticles = false;
        }

        c.markDirty();
        rebuildWidgets();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        int closeX = width - 74;
        if (e.x() >= closeX && e.y() <= 82) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.mouseClicked(e, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (draggingRender || draggingSimulation || draggingFps) {
            int[] l = layout();
            int panelX = l[3];
            int panelW = l[4];
            int trackX = panelX + 22 + 16;
            int trackW = panelW - 44 - 32;

            if (draggingRender) {
                renderDistance = sliderValue(e.x(), trackX, trackW, 4, 12);
                VoidBoostConfig.get().maxRenderDistance = renderDistance;
            } else if (draggingSimulation) {
                simulationDistance = sliderValue(e.x(), trackX, trackW, 5, 32);
                Minecraft.getInstance().options.simulationDistance().set(simulationDistance);
            } else {
                fps = sliderValue(e.x(), trackX, trackW, 30, 1000);
                VoidBoostConfig.get().targetFps = fps;
            }

            return true;
        }

        return super.mouseDragged(e, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        if (draggingRender || draggingSimulation || draggingFps) {
            VoidBoostConfig.get().markDirty();
            Minecraft.getInstance().options.save();
        }

        draggingRender = false;
        draggingSimulation = false;
        draggingFps = false;
        return super.mouseReleased(e);
    }

    private int sliderValue(double mouseX, int x, int w, int min, int max) {
        double t = (mouseX - x) / (double) w;
        t = Math.max(0.0, Math.min(1.0, t));
        return clamp((int) Math.round(min + t * (max - min)), min, max);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int[] l = layout();
        int sx = l[0], py = l[1], sw = l[2], mainX = l[3], mainW = l[4], ph = l[5];
        int bottom = py + ph;

        // World remains visible through the dark VoidBoost overlay.
        g.fill(0, 0, width, height, BACKDROP);

        // Branding/header area.
        g.blit(LOGO, sx, 48, 0, 0, 64, 64, 64, 64);
        g.drawString(font, Component.literal("VOIDBOOST"), sx + 76, 51, TEXT, false);
        g.drawString(font, Component.literal("More FPS • Smoother • Better"), sx + 76, 69, MUTED, false);

        g.drawString(
                font,
                Component.literal("Minecraft 1.21.11 | Fabric"),
                mainX + mainW - 188,
                58,
                MUTED,
                false
        );

        // Close X, drawn rather than using a text glyph.
        drawClose(g, width - 55, 58);

        // Main floating panels.
        g.fill(sx, py, sx + sw, bottom, SIDE);
        g.fill(mainX, py, mainX + mainW, bottom, PANEL);

        // Thin cyan top edges exactly where the reference establishes hierarchy.
        g.fill(sx, py, sx + sw, py + 2, CYAN);
        g.fill(mainX, py, mainX + mainW, py + 2, CYAN);

        // Sidebar title and footer.
        g.drawString(font, Component.literal("VOIDBOOST"), sx + 20, py + 24, TEXT, false);
        g.drawString(font, Component.literal("Boost your game"), sx + 20, py + 42, MUTED, false);

        g.drawString(font, Component.literal("VOIDBOOST"), sx + 20, bottom - 50, MUTED, false);
        g.drawString(font, Component.literal("Boost your game"), sx + 20, bottom - 33, MUTED, false);

        // Main page heading.
        String title = switch (page) {
            case 0 -> "General";
            case 1 -> "Performance";
            case 2 -> "Visuals";
            case 3 -> "PvP";
            case 4 -> "HUD";
            default -> "Advanced";
        };

        String subtitle = page == 1
                ? "Optimize your game for higher FPS and smoother gameplay."
                : "VoidBoost client-side controls.";

        drawGaugeIcon(g, mainX + 24, py + 24);
        g.drawString(font, Component.literal(title), mainX + 50, py + 24, TEXT, false);
        g.drawString(font, Component.literal(subtitle), mainX + 50, py + 43, MUTED, false);

        g.fill(mainX + 22, py + 72, mainX + mainW - 22, py + 73, BORDER);

        // Subtle right-side scroll rail, matching the reference panel treatment.
        g.fill(mainX + mainW - 13, py + 88, mainX + mainW - 9, bottom - 18, 0x44314A5A);

        super.render(g, mouseX, mouseY, delta);

        // Fade the area below the floating panels.
        g.fill(0, bottom, width, height, 0x55040A12);
    }

    private static void drawClose(GuiGraphics g, int cx, int cy) {
        g.fill(cx - 6, cy - 7, cx - 4, cy - 2, TEXT);
        g.fill(cx + 4, cy - 7, cx + 6, cy - 2, TEXT);
        g.fill(cx - 4, cy - 3, cx + 4, cy + 3, TEXT);
        g.fill(cx - 6, cy + 2, cx - 4, cy + 7, TEXT);
        g.fill(cx + 4, cy + 2, cx + 6, cy + 7, TEXT);
    }

    private static void drawGaugeIcon(GuiGraphics g, int x, int y) {
        g.fill(x, y + 8, x + 22, y + 10, CYAN);
        g.fill(x + 2, y + 5, x + 4, y + 8, CYAN);
        g.fill(x + 18, y + 5, x + 20, y + 8, CYAN);
        g.fill(x + 10, y + 2, x + 12, y + 9, CYAN);
        g.fill(x + 10, y + 8, x + 14, y + 10, CYAN);
    }

    private abstract static class Base extends AbstractWidget {
        Base(int x, int y, int w, int h, String narration) {
            super(x, y, w, h, Component.literal(narration));
        }

        protected void drawRow(GuiGraphics g) {
            int color = isHovered() ? ROW_HOVER : ROW;
            g.fill(getX(), getY(), getX() + width, getY() + height, color);
            g.fill(
                    getX(),
                    getY(),
                    getX() + 1,
                    getY() + height,
                    isHovered() ? CYAN_DIM : BORDER
            );
            g.fill(
                    getX(),
                    getY() + height - 1,
                    getX() + width,
                    getY() + height,
                    BORDER
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
        }
    }

    private final class NavButton extends Base {
        private final String label;
        private final boolean selected;
        private final Runnable action;

        NavButton(int x, int y, int w, String label, boolean selected, Runnable action) {
            super(x, y, w, 38, label);
            this.label = label;
            this.selected = selected;
            this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            int bg = selected ? 0xCC132A35 : (isHovered() ? ROW_HOVER : SIDE);
            g.fill(getX(), getY(), getX() + width, getY() + height, bg);

            if (selected) {
                g.fill(getX(), getY(), getX() + 3, getY() + height, CYAN);
            }

            drawIcon(g, getX() + 16, getY() + 11, label, selected ? CYAN : MUTED);
            g.drawString(
                    font,
                    Component.literal(label),
                    getX() + 46,
                    getY() + 13,
                    selected ? TEXT : MUTED,
                    false
            );
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            action.run();
        }
    }

    private final class SliderRow extends Base {
        private final String title;
        private final String description;
        private final int min;
        private final int max;
        private int current;
        private final int kind;

        SliderRow(int x, int y, int w, int h, String title, String description,
                  String ignoredValue, int min, int max, int current, int kind) {
            super(x, y, w, h, title);
            this.title = title;
            this.description = description;
            this.min = min;
            this.max = max;
            this.current = current;
            this.kind = kind;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            drawRow(g);

            String value = switch (kind) {
                case 0 -> renderDistance + " Chunks";
                case 1 -> simulationDistance + " Chunks";
                default -> fps + " FPS";
            };

            g.drawString(font, Component.literal(title), getX() + 16, getY() + 10, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 16, getY() + 28, MUTED, false);
            g.drawString(font, Component.literal(value), getX() + width - 104, getY() + 10, CYAN, false);

            int tx = getX() + 16;
            int tw = width - 32;
            int ty = getY() + height - 10;
            g.fill(tx, ty, tx + tw, ty + 3, 0xFF263A49);

            double ratio = (current - min) / (double) (max - min);
            int knob = tx + (int) Math.round(ratio * tw);

            g.fill(tx, ty, knob, ty + 3, CYAN);
            g.fill(knob - 4, ty - 4, knob + 5, ty + 9, CYAN);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            current = sliderValue(event.x(), getX() + 16, width - 32, min, max);

            if (kind == 0) {
                renderDistance = current;
                VoidBoostConfig.get().maxRenderDistance = current;
                draggingRender = true;
            } else if (kind == 1) {
                simulationDistance = current;
                Minecraft.getInstance().options.simulationDistance().set(current);
                draggingSimulation = true;
            } else {
                fps = current;
                VoidBoostConfig.get().targetFps = current;
                draggingFps = true;
            }
        }
    }

    private final class ToggleRow extends Base {
        private final String title;
        private final String description;
        private final boolean on;
        private final Runnable action;

        ToggleRow(int x, int y, int w, int h, String title, String description,
                  boolean on, Runnable action) {
            super(x, y, w, h, title);
            this.title = title;
            this.description = description;
            this.on = on;
            this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            drawRow(g);
            g.drawString(font, Component.literal(title), getX() + 16, getY() + 10, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 16, getY() + 28, MUTED, false);

            String state = on ? "ON" : "OFF";
            g.drawString(font, Component.literal(state), getX() + width - 104, getY() + 21,
                    on ? CYAN : MUTED, false);
            drawToggle(g, getX() + width - 58, getY() + 17, on);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            action.run();
        }
    }

    private final class ChoiceRow extends Base {
        private final String title;
        private final String description;
        private final String value;
        private final Runnable action;

        ChoiceRow(int x, int y, int w, int h, String title, String description,
                  String value, Runnable action) {
            super(x, y, w, h, title);
            this.title = title;
            this.description = description;
            this.value = value;
            this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            drawRow(g);
            g.drawString(font, Component.literal(title), getX() + 16, getY() + 10, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 16, getY() + 28, MUTED, false);
            g.drawString(font, Component.literal(value), getX() + width - 104, getY() + 21, CYAN, false);
            g.drawString(font, Component.literal("›"), getX() + width - 20, getY() + 20, MUTED, false);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            action.run();
        }
    }

    private final class ProfileRow extends Base {
        private final String title;
        private final String description;
        private final Runnable action;

        ProfileRow(int x, int y, int w, int h, String title, String description, Runnable action) {
            super(x, y, w, h, title);
            this.title = title;
            this.description = description;
            this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            drawRow(g);
            g.drawString(font, Component.literal(title), getX() + 16, getY() + 10, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 16, getY() + 28, MUTED, false);
            g.drawString(font, Component.literal("APPLY"), getX() + width - 58, getY() + 21, CYAN, false);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            action.run();
            Minecraft.getInstance().options.save();
            rebuildWidgets();
        }
    }

    private final class InfoRow extends Base {
        private final String title;
        private final String description;

        InfoRow(int x, int y, int w, int h, String title, String description) {
            super(x, y, w, h, title);
            this.title = title;
            this.description = description;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            drawRow(g);
            g.drawString(font, Component.literal(title), getX() + 16, getY() + 10, TEXT, false);
            g.drawString(font, Component.literal(description), getX() + 16, getY() + 28, MUTED, false);
        }
    }

    private static void drawToggle(GuiGraphics g, int x, int y, boolean on) {
        g.fill(x, y, x + 42, y + 20, on ? CYAN_DIM : 0xFF1A2631);
        g.fill(
                x + (on ? 23 : 2),
                y + 2,
                x + (on ? 39 : 18),
                y + 18,
                on ? CYAN : 0xFF71818B
        );
    }

    private static void drawIcon(GuiGraphics g, int x, int y, String label, int color) {
        if (label.equals("General")) {
            g.fill(x + 6, y, x + 8, y + 14, color);
            g.fill(x + 2, y + 4, x + 12, y + 6, color);
            g.fill(x + 3, y + 9, x + 11, y + 11, color);
        } else if (label.equals("Performance")) {
            g.fill(x, y + 9, x + 14, y + 11, color);
            g.fill(x + 2, y + 6, x + 4, y + 9, color);
            g.fill(x + 10, y + 4, x + 12, y + 9, color);
            g.fill(x + 6, y + 3, x + 8, y + 9, color);
        } else if (label.equals("Visuals")) {
            g.fill(x + 1, y + 5, x + 13, y + 9, color);
            g.fill(x + 5, y + 3, x + 9, y + 11, color);
            g.fill(x + 6, y + 6, x + 8, y + 8, 0xFF0A111B);
        } else if (label.equals("PvP")) {
            g.fill(x + 2, y + 2, x + 4, y + 13, color);
            g.fill(x + 10, y + 2, x + 12, y + 13, color);
            g.fill(x + 3, y + 4, x + 11, y + 6, color);
            g.fill(x + 3, y + 10, x + 11, y + 12, color);
        } else if (label.equals("HUD")) {
            g.fill(x + 2, y + 2, x + 12, y + 10, color);
            g.fill(x + 5, y + 11, x + 9, y + 13, color);
        } else {
            g.fill(x + 1, y + 2, x + 13, y + 4, color);
            g.fill(x + 1, y + 6, x + 13, y + 8, color);
            g.fill(x + 1, y + 10, x + 13, y + 12, color);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
