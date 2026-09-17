package com.voidboost.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VoidBoostConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Path.of("config", "voidboost.json");

    public boolean disableParticles = true;
    public boolean reducedParticles = false;
    public boolean dynamicRenderDistance = false;
    public int dynamicTargetFps = 120;
    public boolean maxFpsPreset = false;
    public boolean competitiveMode = false;
    public boolean ultimateLocked = false;
    public boolean entityRenderOptimization = true;
    public boolean entityShadows = false;
    public boolean weatherEffects = false;
    public boolean animationOptimization = true;
    public boolean fogOptimization = true;
    public boolean performanceMode = false;
    public boolean performanceMonitor = false;
    public int particleLimitPercent = 25;
    public int maxEntityDistance = 64;
    public int minRenderDistance = 4;
    public int maxRenderDistance = 16;

    private static VoidBoostConfig INSTANCE = new VoidBoostConfig();
    private static long lastSave;
    private static int stableTicks;
    private static double smoothedFps = 120.0;

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                VoidBoostConfig loaded = GSON.fromJson(Files.readString(FILE), VoidBoostConfig.class);
                INSTANCE = loaded == null ? new VoidBoostConfig() : loaded;
                INSTANCE.sanitize();
            }
        } catch (Exception ignored) {
            INSTANCE = new VoidBoostConfig();
        }
    }

    private void sanitize() {
        dynamicTargetFps = Math.max(30, Math.min(500, dynamicTargetFps));
        particleLimitPercent = Math.max(1, Math.min(100, particleLimitPercent));
        minRenderDistance = Math.max(2, Math.min(16, minRenderDistance));
        maxRenderDistance = Math.max(minRenderDistance, Math.min(32, maxRenderDistance));
        maxEntityDistance = Math.max(16, Math.min(256, maxEntityDistance));
    }

    public static VoidBoostConfig get() {
        return INSTANCE;
    }

    public static void save() {
        try {
            INSTANCE.sanitize();
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(INSTANCE));
            lastSave = System.currentTimeMillis();
        } catch (IOException ignored) {
        }
    }

    public static void applyUltimateLockedPreset() {
        INSTANCE.maxFpsPreset = true;
        INSTANCE.competitiveMode = true;
        INSTANCE.ultimateLocked = true;
        INSTANCE.performanceMode = true;
        INSTANCE.disableParticles = true;
        INSTANCE.reducedParticles = false;
        INSTANCE.particleLimitPercent = 1;
        INSTANCE.dynamicRenderDistance = true;
        INSTANCE.entityRenderOptimization = true;
        INSTANCE.entityShadows = false;
        INSTANCE.weatherEffects = false;
        INSTANCE.animationOptimization = true;
        INSTANCE.fogOptimization = true;
        INSTANCE.performanceMonitor = true;
        INSTANCE.maxEntityDistance = 32;
        INSTANCE.minRenderDistance = 2;
        INSTANCE.maxRenderDistance = 8;
        INSTANCE.dynamicTargetFps = 120;
    }

    public static void applyMaxFpsPreset() {
        if (INSTANCE.ultimateLocked) return;
        INSTANCE.maxFpsPreset = true;
        INSTANCE.competitiveMode = false;
        INSTANCE.performanceMode = true;
        INSTANCE.disableParticles = true;
        INSTANCE.reducedParticles = false;
        INSTANCE.particleLimitPercent = 1;
        INSTANCE.dynamicRenderDistance = true;
        INSTANCE.entityRenderOptimization = true;
        INSTANCE.entityShadows = false;
        INSTANCE.weatherEffects = false;
        INSTANCE.animationOptimization = true;
        INSTANCE.fogOptimization = true;
        INSTANCE.performanceMonitor = false;
        INSTANCE.maxEntityDistance = 48;
        INSTANCE.minRenderDistance = 4;
        INSTANCE.maxRenderDistance = 12;
        INSTANCE.dynamicTargetFps = 144;
    }

    public static void applyCompetitivePreset() {
        if (INSTANCE.ultimateLocked) return;
        INSTANCE.maxFpsPreset = false;
        INSTANCE.competitiveMode = true;
        INSTANCE.performanceMode = true;
        INSTANCE.disableParticles = false;
        INSTANCE.reducedParticles = true;
        INSTANCE.particleLimitPercent = 20;
        INSTANCE.dynamicRenderDistance = true;
        INSTANCE.entityRenderOptimization = true;
        INSTANCE.entityShadows = false;
        INSTANCE.weatherEffects = false;
        INSTANCE.animationOptimization = true;
        INSTANCE.fogOptimization = true;
        INSTANCE.performanceMonitor = false;
        INSTANCE.maxEntityDistance = 64;
        INSTANCE.minRenderDistance = 6;
        INSTANCE.maxRenderDistance = 12;
        INSTANCE.dynamicTargetFps = 120;
    }

    public static void applyBalancedPreset() {
        if (INSTANCE.ultimateLocked) return;
        INSTANCE.maxFpsPreset = false;
        INSTANCE.competitiveMode = false;
        INSTANCE.performanceMode = true;
        INSTANCE.disableParticles = false;
        INSTANCE.reducedParticles = true;
        INSTANCE.particleLimitPercent = 60;
        INSTANCE.dynamicRenderDistance = false;
        INSTANCE.entityRenderOptimization = true;
        INSTANCE.entityShadows = true;
        INSTANCE.weatherEffects = true;
        INSTANCE.animationOptimization = false;
        INSTANCE.fogOptimization = false;
        INSTANCE.performanceMonitor = false;
        INSTANCE.maxEntityDistance = 96;
        INSTANCE.minRenderDistance = 6;
        INSTANCE.maxRenderDistance = 16;
    }

    public static void applyVanillaPerformanceOptions(Minecraft client) {
        if (client == null) return;
        try {
            if (INSTANCE.performanceMode || INSTANCE.ultimateLocked) {
                client.options.entityShadows().set(INSTANCE.entityShadows);
                client.options.entityDistanceScaling().set(INSTANCE.ultimateLocked ? 0.5 : (INSTANCE.competitiveMode ? 0.65 : 0.75));
                client.options.weatherRadius().set(INSTANCE.weatherEffects ? 32 : 0);
                client.options.cloudStatus().set(INSTANCE.weatherEffects ? CloudStatus.FANCY : CloudStatus.OFF);
                client.options.particles().set(INSTANCE.disableParticles ? ParticleStatus.MINIMAL : ParticleStatus.DECREASED);
                client.options.vignette().set(false);
                client.options.ambientOcclusion().set(false);
                client.options.chunkSectionFadeInTime().set(0.0);
                if (INSTANCE.animationOptimization || INSTANCE.competitiveMode) {
                    client.options.bobView().set(false);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void tick(Minecraft client) {
        if (client == null) return;
        applyVanillaPerformanceOptions(client);
        if (client.level == null || !INSTANCE.dynamicRenderDistance) return;
        if (++stableTicks < 20) return;
        stableTicks = 0;

        long now = System.currentTimeMillis();
        if (now - lastSave < 500) return;

        double fps = client.getFps();
        if (fps > 0) smoothedFps = smoothedFps * 0.82 + fps * 0.18;

        int current = client.options.renderDistance().get();
        int next = current;
        int min = INSTANCE.minRenderDistance;
        int max = INSTANCE.maxRenderDistance;
        int target = INSTANCE.dynamicTargetFps;

        if (smoothedFps < target - 12 && current > min) next = current - 1;
        else if (smoothedFps > target + 22 && current < max) next = current + 1;

        if (next != current) client.options.renderDistance().set(next);
    }
}
