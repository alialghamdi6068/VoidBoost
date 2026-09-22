package com.voidboost.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.server.level.ParticleStatus;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VoidBoostConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("voidboost.json");
    private static VoidBoostConfig INSTANCE = new VoidBoostConfig();

    public boolean performanceMode = true;
    public boolean performanceMonitor = false;
    public boolean disableParticles = true;
    public boolean reducedParticles = false;
    public int particleLimitPercent = 1;
    public boolean entityShadows = false;
    public boolean weatherEffects = false;
    public boolean animationOptimization = true;
    public boolean fogOptimization = true;
    public boolean entityRenderOptimization = true;
    public boolean dynamicRenderDistance = true;

    public boolean cloudOptimization = true;
    public boolean vignetteOptimization = true;
    public boolean ambientOcclusionOptimization = true;
    public boolean mipmapOptimization = true;
    public boolean biomeBlendOptimization = true;
    public boolean viewBobOptimization = true;
    public boolean vsyncOptimization = true;

    public boolean competitiveMode = false;
    public boolean maxFpsPreset = false;
    public boolean ultimateLocked = false;
    public int targetFps = 260;
    public int dynamicTargetFps = 240;
    public int maxEntityDistance = 32;
    public int maxRenderDistance = 4;

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
    private static long appliedOptionsSignature = Long.MIN_VALUE;
    private static boolean fogStateCaptured;
    private static boolean fogDisabledByVoidBoost;
    private static int tickCounter;

    public static VoidBoostConfig get() { return INSTANCE; }

    public static void load() {
        try {
            Files.createDirectories(FILE.getParent());
            if (Files.exists(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE)) {
                    VoidBoostConfig loaded = GSON.fromJson(reader, VoidBoostConfig.class);
                    if (loaded != null) INSTANCE = loaded;
                }
            } else save();
        } catch (Exception ignored) {
            INSTANCE = new VoidBoostConfig();
        }
        INSTANCE.sanitize();
        appliedOptionsSignature = Long.MIN_VALUE;
    }

    private void sanitize() {
        particleLimitPercent = Math.max(1, Math.min(100, particleLimitPercent));
        dynamicTargetFps = Math.max(60, Math.min(240, dynamicTargetFps));
        targetFps = Math.max(30, Math.min(260, targetFps));
        maxEntityDistance = Math.max(32, Math.min(128, maxEntityDistance));
        maxRenderDistance = Math.max(4, Math.min(32, maxRenderDistance));
    }

    public static void save() {
        try {
            INSTANCE.sanitize();
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException ignored) {}
    }

    public static void tick(Minecraft client) {
        if (client.level == null) return;
        tickCounter++;
        applyVanillaPerformanceOptions(client);
        if (INSTANCE.dynamicRenderDistance && tickCounter % 20 == 0) updateDynamicRenderDistance(client);
    }

    public static void resetToVanilla() {
        Minecraft client = Minecraft.getInstance();
        if (optionsCaptured) {
            INSTANCE.targetFps = savedMaxFps;
            INSTANCE.maxRenderDistance = Math.max(4, Math.min(32, savedRenderDistance));
        } else {
            INSTANCE.targetFps = Math.max(30, Math.min(260, client.options.framerateLimit().get()));
            INSTANCE.maxRenderDistance = Math.max(4, Math.min(32, client.options.renderDistance().get()));
        }
        INSTANCE.performanceMode = false;
        INSTANCE.performanceMonitor = false;
        INSTANCE.disableParticles = false;
        INSTANCE.reducedParticles = false;
        INSTANCE.particleLimitPercent = 100;
        INSTANCE.entityShadows = true;
        INSTANCE.weatherEffects = true;
        INSTANCE.animationOptimization = false;
        INSTANCE.fogOptimization = false;
        INSTANCE.entityRenderOptimization = false;
        INSTANCE.dynamicRenderDistance = false;
        INSTANCE.cloudOptimization = false;
        INSTANCE.vignetteOptimization = false;
        INSTANCE.ambientOcclusionOptimization = false;
        INSTANCE.mipmapOptimization = false;
        INSTANCE.biomeBlendOptimization = false;
        INSTANCE.viewBobOptimization = false;
        INSTANCE.vsyncOptimization = false;
        INSTANCE.competitiveMode = false;
        INSTANCE.maxFpsPreset = false;
        INSTANCE.ultimateLocked = false;
        INSTANCE.dynamicTargetFps = 120;
        INSTANCE.maxEntityDistance = 64;
        INSTANCE.markDirty();
        applyVanillaPerformanceOptions(client);
        INSTANCE.targetFps = Math.max(30, Math.min(260, client.options.framerateLimit().get()));
        INSTANCE.maxRenderDistance = Math.max(4, Math.min(32, client.options.renderDistance().get()));
        INSTANCE.markDirty();
    }

    public static void applyBalancedPreset() { applyPreset("Balanced"); }
    public static void applyCompetitivePreset() { applyPreset("Competitive"); }
    public static void applyMaxFpsPreset() { applyPreset("MAX FPS"); }

    public static void applyUltimateLockedPreset() {
        INSTANCE.performanceMode = true;
        INSTANCE.performanceMonitor = false;
        INSTANCE.disableParticles = true;
        INSTANCE.reducedParticles = false;
        INSTANCE.entityShadows = false;
        INSTANCE.weatherEffects = false;
        INSTANCE.animationOptimization = true;
        INSTANCE.fogOptimization = true;
        INSTANCE.entityRenderOptimization = true;
        INSTANCE.dynamicRenderDistance = true;
        INSTANCE.cloudOptimization = true;
        INSTANCE.vignetteOptimization = true;
        INSTANCE.ambientOcclusionOptimization = true;
        INSTANCE.mipmapOptimization = true;
        INSTANCE.biomeBlendOptimization = true;
        INSTANCE.viewBobOptimization = true;
        INSTANCE.vsyncOptimization = true;
        INSTANCE.competitiveMode = true;
        INSTANCE.maxFpsPreset = false;
        INSTANCE.ultimateLocked = true;
        INSTANCE.targetFps = 260;
        INSTANCE.dynamicTargetFps = 240;
        INSTANCE.maxEntityDistance = 32;
        INSTANCE.maxRenderDistance = 4;
        setSimulationDistance(4);
        save();
        appliedOptionsSignature = Long.MIN_VALUE;
    }

    public static void applyPreset(String preset) {
        switch (preset) {
            case "Balanced" -> {
                INSTANCE.performanceMode = true; INSTANCE.performanceMonitor = false;
                INSTANCE.disableParticles = false; INSTANCE.reducedParticles = true;
                INSTANCE.entityShadows = false; INSTANCE.weatherEffects = false;
                INSTANCE.animationOptimization = true; INSTANCE.fogOptimization = true;
                INSTANCE.entityRenderOptimization = true; INSTANCE.dynamicRenderDistance = true;
                INSTANCE.cloudOptimization = true; INSTANCE.vignetteOptimization = true;
                INSTANCE.ambientOcclusionOptimization = true; INSTANCE.mipmapOptimization = true;
                INSTANCE.biomeBlendOptimization = true; INSTANCE.viewBobOptimization = false; INSTANCE.vsyncOptimization = true;
                INSTANCE.competitiveMode = false; INSTANCE.maxFpsPreset = false; INSTANCE.ultimateLocked = false;
                INSTANCE.targetFps = 240; INSTANCE.dynamicTargetFps = 240;
                INSTANCE.maxEntityDistance = 56; INSTANCE.maxRenderDistance = 10;
                setSimulationDistance(8);
            }
            case "Competitive" -> {
                INSTANCE.performanceMode = true; INSTANCE.performanceMonitor = false;
                INSTANCE.disableParticles = true; INSTANCE.reducedParticles = false;
                INSTANCE.entityShadows = false; INSTANCE.weatherEffects = false;
                INSTANCE.animationOptimization = true; INSTANCE.fogOptimization = true;
                INSTANCE.entityRenderOptimization = true; INSTANCE.dynamicRenderDistance = true;
                INSTANCE.cloudOptimization = true; INSTANCE.vignetteOptimization = true;
                INSTANCE.ambientOcclusionOptimization = true; INSTANCE.mipmapOptimization = true;
                INSTANCE.biomeBlendOptimization = true; INSTANCE.viewBobOptimization = true; INSTANCE.vsyncOptimization = true;
                INSTANCE.competitiveMode = true; INSTANCE.maxFpsPreset = false; INSTANCE.ultimateLocked = false;
                INSTANCE.targetFps = 240; INSTANCE.dynamicTargetFps = 240;
                INSTANCE.maxEntityDistance = 44; INSTANCE.maxRenderDistance = 8;
                setSimulationDistance(6);
            }
            case "MAX FPS" -> {
                INSTANCE.performanceMode = true; INSTANCE.performanceMonitor = false;
                INSTANCE.disableParticles = true; INSTANCE.reducedParticles = false;
                INSTANCE.entityShadows = false; INSTANCE.weatherEffects = false;
                INSTANCE.animationOptimization = true; INSTANCE.fogOptimization = true;
                INSTANCE.entityRenderOptimization = true; INSTANCE.dynamicRenderDistance = true;
                INSTANCE.cloudOptimization = true; INSTANCE.vignetteOptimization = true;
                INSTANCE.ambientOcclusionOptimization = true; INSTANCE.mipmapOptimization = true;
                INSTANCE.biomeBlendOptimization = true; INSTANCE.viewBobOptimization = true; INSTANCE.vsyncOptimization = true;
                INSTANCE.competitiveMode = false; INSTANCE.maxFpsPreset = true; INSTANCE.ultimateLocked = false;
                INSTANCE.targetFps = 260; INSTANCE.dynamicTargetFps = 240;
                INSTANCE.maxEntityDistance = 32; INSTANCE.maxRenderDistance = 4;
                setSimulationDistance(4);
            }
            case "ULTIMATE FPS" -> applyUltimateLockedPreset();
            default -> { return; }
        }
        save();
        appliedOptionsSignature = Long.MIN_VALUE;
    }

    private static void applyVanillaPerformanceOptions(Minecraft client) {
        long signature = optionsSignature();
        if (signature == appliedOptionsSignature) return;
        try {
            boolean controlVanilla = INSTANCE.performanceMode || INSTANCE.disableParticles || INSTANCE.reducedParticles
                    || !INSTANCE.entityShadows || !INSTANCE.weatherEffects || INSTANCE.animationOptimization || INSTANCE.fogOptimization
                    || INSTANCE.cloudOptimization || INSTANCE.vignetteOptimization || INSTANCE.ambientOcclusionOptimization
                    || INSTANCE.mipmapOptimization || INSTANCE.biomeBlendOptimization || INSTANCE.viewBobOptimization || INSTANCE.vsyncOptimization;
            if (!controlVanilla) {
                restoreVanillaPerformanceOptions(client);
                syncFog(false);
                appliedOptionsSignature = signature;
                return;
            }

            captureVanillaPerformanceOptions(client);

            if (INSTANCE.performanceMode) {
                double entityScale = INSTANCE.maxRenderDistance <= 4 ? 0.25 : INSTANCE.maxRenderDistance <= 6 ? 0.32 : INSTANCE.competitiveMode ? 0.40 : INSTANCE.maxFpsPreset ? 0.36 : 0.55;
                client.options.entityDistanceScaling().set(entityScale);
                client.options.vignette().set(!INSTANCE.vignetteOptimization);
                client.options.ambientOcclusion().set(!INSTANCE.ambientOcclusionOptimization);
                client.options.chunkSectionFadeInTime().set(INSTANCE.animationOptimization ? 0.0 : savedChunkSectionFadeInTime);
                client.options.enableVsync().set(INSTANCE.vsyncOptimization ? false : savedVsync);
                client.options.biomeBlendRadius().set(INSTANCE.biomeBlendOptimization ? 0 : savedBiomeBlendRadius);
                client.options.mipmapLevels().set(INSTANCE.mipmapOptimization ? 0 : savedMipmapLevels);
                client.options.cloudStatus().set(INSTANCE.cloudOptimization ? CloudStatus.OFF : savedCloudStatus);
                client.options.bobView().set(INSTANCE.viewBobOptimization ? false : savedBobView);

                int configuredLimit = Math.max(4, Math.min(32, INSTANCE.maxRenderDistance));
                int adaptiveLimit = Math.max(4, Math.min(configuredLimit, VoidBoostAI.renderDistanceLimit(configuredLimit)));
                if (client.options.renderDistance().get() > adaptiveLimit) client.options.renderDistance().set(adaptiveLimit);
                client.options.framerateLimit().set(Math.max(30, Math.min(260, INSTANCE.targetFps)));
            } else {
                restorePerformanceOnlyOptions(client);
            }

            client.options.entityShadows().set(INSTANCE.entityShadows);
            client.options.weatherRadius().set(INSTANCE.weatherEffects ? 32 : 0);
            if (!INSTANCE.cloudOptimization) client.options.cloudStatus().set(INSTANCE.weatherEffects ? CloudStatus.FANCY : CloudStatus.OFF);
            client.options.particles().set(INSTANCE.disableParticles ? ParticleStatus.MINIMAL : ParticleStatus.ALL);
            if (!INSTANCE.animationOptimization && !INSTANCE.competitiveMode) client.options.bobView().set(savedBobView);
            syncFog(INSTANCE.fogOptimization);
            if (!INSTANCE.dynamicRenderDistance && optionsCaptured) client.options.renderDistance().set(savedRenderDistance);
            appliedOptionsSignature = signature;
        } catch (Exception ignored) {
            appliedOptionsSignature = Long.MIN_VALUE;
        }
    }

    private static long optionsSignature() {
        long result = 17;
        boolean[] flags = {
                INSTANCE.performanceMode, INSTANCE.ultimateLocked, INSTANCE.entityShadows, INSTANCE.competitiveMode,
                INSTANCE.weatherEffects, INSTANCE.disableParticles, INSTANCE.reducedParticles, INSTANCE.animationOptimization,
                INSTANCE.fogOptimization, INSTANCE.dynamicRenderDistance, INSTANCE.cloudOptimization, INSTANCE.vignetteOptimization,
                INSTANCE.ambientOcclusionOptimization, INSTANCE.mipmapOptimization, INSTANCE.biomeBlendOptimization,
                INSTANCE.viewBobOptimization, INSTANCE.vsyncOptimization
        };
        for (boolean flag : flags) result = 31 * result + (flag ? 1 : 0);
        result = 31 * result + INSTANCE.targetFps;
        result = 31 * result + INSTANCE.dynamicTargetFps;
        result = 31 * result + INSTANCE.maxEntityDistance;
        result = 31 * result + INSTANCE.maxRenderDistance;
        result = 31 * result + INSTANCE.particleLimitPercent;
        result = 31 * result + VoidBoostAI.level();
        return result;
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

    private static void restorePerformanceOnlyOptions(Minecraft client) {
        if (!optionsCaptured) return;
        client.options.entityDistanceScaling().set(savedEntityDistanceScaling);
        client.options.vignette().set(savedVignette);
        client.options.ambientOcclusion().set(savedAmbientOcclusion);
        client.options.chunkSectionFadeInTime().set(savedChunkSectionFadeInTime);
        client.options.enableVsync().set(savedVsync);
        client.options.biomeBlendRadius().set(savedBiomeBlendRadius);
        client.options.mipmapLevels().set(savedMipmapLevels);
        client.options.cloudStatus().set(savedCloudStatus);
        client.options.bobView().set(savedBobView);
        client.options.framerateLimit().set(savedMaxFps);
    }

    private static void restoreVanillaPerformanceOptions(Minecraft client) {
        if (!optionsCaptured) return;
        client.options.entityShadows().set(savedEntityShadows);
        client.options.entityDistanceScaling().set(savedEntityDistanceScaling);
        client.options.weatherRadius().set(savedWeatherRadius);
        client.options.cloudStatus().set(savedCloudStatus);
        client.options.particles().set(savedParticleStatus);
        client.options.mipmapLevels().set(savedMipmapLevels);
        client.options.biomeBlendRadius().set(savedBiomeBlendRadius);
        client.options.vignette().set(savedVignette);
        client.options.ambientOcclusion().set(savedAmbientOcclusion);
        client.options.chunkSectionFadeInTime().set(savedChunkSectionFadeInTime);
        client.options.bobView().set(savedBobView);
        client.options.enableVsync().set(savedVsync);
        client.options.framerateLimit().set(savedMaxFps);
        client.options.renderDistance().set(savedRenderDistance);
        client.options.simulationDistance().set(savedSimulationDistance);
        client.options.save();
        optionsCaptured = false;
    }

    private static void syncFog(boolean disable) {
        if (!fogStateCaptured) { fogStateCaptured = true; fogDisabledByVoidBoost = false; }
        if (disable == fogDisabledByVoidBoost) return;
        FogRenderer.toggleFog();
        fogDisabledByVoidBoost = disable;
    }

    private static void updateDynamicRenderDistance(Minecraft client) {
        if (client.level == null) return;
        int current = client.options.renderDistance().get();
        int configuredMax = Math.max(4, Math.min(32, INSTANCE.maxRenderDistance));
        int maxDistance = Math.max(4, Math.min(configuredMax, VoidBoostAI.renderDistanceLimit(configuredMax)));
        int target = Math.max(60, Math.min(240, INSTANCE.dynamicTargetFps));
        int fps = client.getFps();
        int desired = Math.min(current, maxDistance);
        if (current > maxDistance) desired = maxDistance;
        else if (fps > target + 15 && current < maxDistance) desired = Math.min(maxDistance, current + 1);
        else if (fps < target - 15 && current > 4) desired = Math.max(4, current - 1);
        if (desired != current) client.options.renderDistance().set(desired);
    }

    private static void setSimulationDistance(int value) {
        try {
            Minecraft.getInstance().options.simulationDistance().set(Math.max(4, Math.min(32, value)));
            Minecraft.getInstance().options.save();
        } catch (Exception ignored) {}
    }

    public static void markDirty() { appliedOptionsSignature = Long.MIN_VALUE; save(); }
}
