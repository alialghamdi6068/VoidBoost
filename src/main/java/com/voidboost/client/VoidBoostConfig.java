package com.voidboost.client;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.server.level.ParticleStatus;

/**
 * Runtime state for VoidBoost's permanent Tier 0 profile.
 *
 * The performance profile is deliberately not user-configurable. The only
 * exposed state is the optional diagnostic monitor toggled by the keybind.
 */
public final class VoidBoostConfig {
    private static final VoidBoostConfig INSTANCE = new VoidBoostConfig();

    public boolean performanceMonitor;

    private static boolean optionsCaptured;
    private static boolean savedEntityShadows;
    private static double savedEntityDistanceScaling;
    private static int savedWeatherRadius;
    private static CloudStatus savedCloudStatus;
    private static ParticleStatus savedParticleStatus;
    private static int savedMipmapLevels;
    private static int savedBiomeBlendRadius;
    private static boolean savedVignette;
    private static boolean savedAmbientOcclusion;
    private static double savedChunkSectionFadeInTime;
    private static boolean savedBobView;
    private static boolean savedVsync;
    private static int savedMaxFps;
    private static int savedRenderDistance;
    private static int savedSimulationDistance;
    private static boolean optionsDirty = true;
    private static boolean fogStateCaptured;
    private static boolean fogDisabledByVoidBoost;

    private VoidBoostConfig() {}

    public static VoidBoostConfig get() {
        return INSTANCE;
    }

    public static void load() {
        INSTANCE.performanceMonitor = false;
        optionsDirty = true;
    }

    public static void tick(Minecraft client) {
        if (client.level == null) return;
        if (optionsDirty) {
            applyMaximumPerformanceOptions(client);
        }
    }

    private static void applyMaximumPerformanceOptions(Minecraft client) {
        if (!optionsDirty) return;

        try {
            captureVanillaPerformanceOptions(client);

            // Do not overwrite Sodium's own render-distance/quality controls.
            // VoidBoost only enforces its independent vanilla performance hooks.
            client.options.entityDistanceScaling().set(0.25D);
            client.options.entityShadows().set(false);
            client.options.weatherRadius().set(0);
            client.options.cloudStatus().set(CloudStatus.OFF);
            client.options.particles().set(ParticleStatus.MINIMAL);
            // Sodium owns these renderer quality settings when installed.
            // Leave them untouched so changing Sodium options always wins.
            client.options.chunkSectionFadeInTime().set(0.0D);
            client.options.bobView().set(false);
            // Respect Minecraft/Sodium's own FPS, render-distance and VSync controls.
            // VoidBoost must never fight the user's Sodium settings.

            syncFog(true);
            optionsDirty = false;
        } catch (RuntimeException ignored) {
            // Keep the client alive if a Minecraft point release changes an option.
        }
    }

    private static void captureVanillaPerformanceOptions(Minecraft client) {
        if (optionsCaptured) return;

        savedEntityShadows = client.options.entityShadows().get();
        savedEntityDistanceScaling = client.options.entityDistanceScaling().get();
        savedWeatherRadius = client.options.weatherRadius().get();
        savedCloudStatus = client.options.cloudStatus().get();
        savedParticleStatus = client.options.particles().get();
        savedMipmapLevels = client.options.mipmapLevels().get();
        savedBiomeBlendRadius = client.options.biomeBlendRadius().get();
        savedVignette = client.options.vignette().get();
        savedAmbientOcclusion = client.options.ambientOcclusion().get();
        savedChunkSectionFadeInTime = client.options.chunkSectionFadeInTime().get();
        savedBobView = client.options.bobView().get();
        savedVsync = client.options.enableVsync().get();
        savedMaxFps = client.options.framerateLimit().get();
        savedRenderDistance = client.options.renderDistance().get();
        savedSimulationDistance = client.options.simulationDistance().get();
        optionsCaptured = true;
    }

    private static void syncFog(boolean disable) {
        if (!fogStateCaptured) {
            fogStateCaptured = true;
            fogDisabledByVoidBoost = false;
        }

        if (disable == fogDisabledByVoidBoost) return;

        FogRenderer.toggleFog();
        fogDisabledByVoidBoost = disable;
    }

    public static void togglePerformanceMonitor() {
        INSTANCE.performanceMonitor = !INSTANCE.performanceMonitor;
    }

    public static void markDirty() {
        optionsDirty = true;
    }
}
