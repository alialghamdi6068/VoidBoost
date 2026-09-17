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
    public boolean disableParticles = false;
    public boolean reducedParticles = true;
    public int particleLimitPercent = 50;
    public boolean entityShadows = false;
    public boolean weatherEffects = false;
    public boolean animationOptimization = true;
    public boolean fogOptimization = true;
    public boolean entityRenderOptimization = true;
    public boolean dynamicRenderDistance = true;
    public boolean competitiveMode = false;
    public boolean maxFpsPreset = false;
    public boolean ultimateLocked = false;
    public int targetFps = 240;
    public int dynamicTargetFps = 120;
    public int maxEntityDistance = 64;
    public int maxRenderDistance = 12;

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
    }

    public static void save() {
        try {
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
        INSTANCE.competitiveMode = true;
        INSTANCE.maxFpsPreset = false;
        INSTANCE.ultimateLocked = false;
        INSTANCE.targetFps = 240;
        INSTANCE.dynamicTargetFps = 240;
        INSTANCE.maxEntityDistance = 40;
        INSTANCE.maxRenderDistance = 6;
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
                INSTANCE.competitiveMode = false; INSTANCE.maxFpsPreset = false; INSTANCE.ultimateLocked = false;
                INSTANCE.targetFps = 240; INSTANCE.dynamicTargetFps = 120;
                INSTANCE.maxEntityDistance = 64; INSTANCE.maxRenderDistance = 12;
            }
            case "Competitive" -> {
                INSTANCE.performanceMode = true; INSTANCE.performanceMonitor = false;
                INSTANCE.disableParticles = true; INSTANCE.reducedParticles = false;
                INSTANCE.entityShadows = false; INSTANCE.weatherEffects = false;
                INSTANCE.animationOptimization = true; INSTANCE.fogOptimization = true;
                INSTANCE.entityRenderOptimization = true; INSTANCE.dynamicRenderDistance = true;
                INSTANCE.competitiveMode = true; INSTANCE.maxFpsPreset = false; INSTANCE.ultimateLocked = false;
                INSTANCE.targetFps = 240; INSTANCE.dynamicTargetFps = 180;
                INSTANCE.maxEntityDistance = 52; INSTANCE.maxRenderDistance = 10;
            }
            case "MAX FPS" -> {
                INSTANCE.performanceMode = true; INSTANCE.performanceMonitor = false;
                INSTANCE.disableParticles = true; INSTANCE.reducedParticles = false;
                INSTANCE.entityShadows = false; INSTANCE.weatherEffects = false;
                INSTANCE.animationOptimization = true; INSTANCE.fogOptimization = true;
                INSTANCE.entityRenderOptimization = true; INSTANCE.dynamicRenderDistance = true;
                INSTANCE.competitiveMode = false; INSTANCE.maxFpsPreset = true; INSTANCE.ultimateLocked = false;
                INSTANCE.targetFps = 240; INSTANCE.dynamicTargetFps = 240;
                INSTANCE.maxEntityDistance = 44; INSTANCE.maxRenderDistance = 8;
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
                    || !INSTANCE.entityShadows || !INSTANCE.weatherEffects || INSTANCE.animationOptimization || INSTANCE.fogOptimization;
            if (!controlVanilla) {
                restoreVanillaPerformanceOptions(client);
                syncFog(false);
                appliedOptionsSignature = signature;
                return;
            }

            captureVanillaPerformanceOptions(client);
            if (INSTANCE.performanceMode) {
                double entityScale = INSTANCE.maxRenderDistance <= 6 ? 0.45 : INSTANCE.competitiveMode ? 0.52 : INSTANCE.maxFpsPreset ? 0.48 : 0.62;
                client.options.entityDistanceScaling().set(entityScale);
                client.options.vignette().set(false);
                client.options.ambientOcclusion().set(false);
                client.options.chunkSectionFadeInTime().set(0.0);
                client.options.enableVsync().set(false);
                client.options.biomeBlendRadius().set(0);
                client.options.mipmapLevels().set(0);

                int hardLimit = Math.max(4, Math.min(12, INSTANCE.maxRenderDistance));
                if (client.options.renderDistance().get() > hardLimit) {
                    client.options.renderDistance().set(hardLimit);
                }
            }
            client.options.entityShadows().set(INSTANCE.entityShadows);
            client.options.weatherRadius().set(INSTANCE.weatherEffects ? 32 : 0);
            client.options.cloudStatus().set(INSTANCE.weatherEffects ? CloudStatus.FANCY : CloudStatus.OFF);
            client.options.particles().set(INSTANCE.disableParticles ? ParticleStatus.MINIMAL : (INSTANCE.reducedParticles ? ParticleStatus.DECREASED : ParticleStatus.ALL));
            client.options.bobView().set(!INSTANCE.animationOptimization && !INSTANCE.competitiveMode);
            syncFog(INSTANCE.fogOptimization);
            if (!INSTANCE.dynamicRenderDistance) client.options.renderDistance().set(savedRenderDistance);
            appliedOptionsSignature = signature;
        } catch (Exception ignored) {
            appliedOptionsSignature = Long.MIN_VALUE;
        }
    }

    private static long optionsSignature() {
        long result = 17;
        result = 31 * result + (INSTANCE.performanceMode ? 1 : 0);
        result = 31 * result + (INSTANCE.ultimateLocked ? 1 : 0);
        result = 31 * result + (INSTANCE.entityShadows ? 1 : 0);
        result = 31 * result + (INSTANCE.competitiveMode ? 1 : 0);
        result = 31 * result + (INSTANCE.weatherEffects ? 1 : 0);
        result = 31 * result + (INSTANCE.disableParticles ? 1 : 0);
        result = 31 * result + (INSTANCE.reducedParticles ? 1 : 0);
        result = 31 * result + (INSTANCE.animationOptimization ? 1 : 0);
        result = 31 * result + (INSTANCE.fogOptimization ? 1 : 0);
        result = 31 * result + (INSTANCE.dynamicRenderDistance ? 1 : 0);
        result = 31 * result + INSTANCE.targetFps;
        result = 31 * result + INSTANCE.dynamicTargetFps;
        result = 31 * result + INSTANCE.maxEntityDistance;
        result = 31 * result + INSTANCE.maxRenderDistance;
        result = 31 * result + INSTANCE.particleLimitPercent;
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
        optionsCaptured = true;
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
        int target = Math.max(60, Math.min(240, INSTANCE.dynamicTargetFps));
        int maxDistance = Math.max(4, INSTANCE.maxRenderDistance);
        int fps = client.getFps();
        int desired = Math.min(current, maxDistance);
        if (current > maxDistance) desired = maxDistance;
        else if (fps > target + 15 && current < maxDistance) desired = Math.min(maxDistance, current + 1);
        else if (fps < target - 15 && current > 4) desired = Math.max(4, current - 1);
        if (desired != current) client.options.renderDistance().set(desired);
    }

    public static void markDirty() { appliedOptionsSignature = Long.MIN_VALUE; save(); }
}
