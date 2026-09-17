package com.voidboost.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

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
    public boolean entityRenderOptimization = true;
    public boolean entityShadows = false;
    public boolean weatherEffects = false;
    public boolean animationOptimization = true;
    public boolean fogOptimization = true;
    public boolean performanceMode = false;
    public int maxEntityDistance = 64;
    public int minRenderDistance = 4;
    public int maxRenderDistance = 16;

    private static VoidBoostConfig INSTANCE = new VoidBoostConfig();
    private static long lastSave;
    private static int stableTicks;

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

    public static void applyMaxFpsPreset() {
        INSTANCE.maxFpsPreset = true;
        INSTANCE.performanceMode = true;
        INSTANCE.disableParticles = true;
        INSTANCE.reducedParticles = false;
        INSTANCE.dynamicRenderDistance = true;
        INSTANCE.entityRenderOptimization = true;
        INSTANCE.entityShadows = false;
        INSTANCE.weatherEffects = false;
        INSTANCE.animationOptimization = true;
        INSTANCE.fogOptimization = true;
        INSTANCE.maxEntityDistance = 48;
        INSTANCE.minRenderDistance = 4;
        INSTANCE.maxRenderDistance = 12;
        INSTANCE.dynamicTargetFps = 144;
    }

    public static void applyBalancedPreset() {
        INSTANCE.maxFpsPreset = false;
        INSTANCE.performanceMode = false;
        INSTANCE.disableParticles = false;
        INSTANCE.reducedParticles = true;
        INSTANCE.dynamicRenderDistance = false;
        INSTANCE.entityRenderOptimization = true;
        INSTANCE.entityShadows = true;
        INSTANCE.weatherEffects = true;
        INSTANCE.animationOptimization = false;
        INSTANCE.fogOptimization = false;
        INSTANCE.maxEntityDistance = 96;
        INSTANCE.minRenderDistance = 6;
        INSTANCE.maxRenderDistance = 16;
    }

    public static void tick(Minecraft client) {
        if (client.level == null || !INSTANCE.dynamicRenderDistance) return;
        if (++stableTicks < 20) return;
        stableTicks = 0;

        long now = System.currentTimeMillis();
        if (now - lastSave < 500) return;

        double fps = client.getFps();
        int current = client.options.renderDistance().get();
        int next = current;
        int min = INSTANCE.minRenderDistance;
        int max = INSTANCE.maxRenderDistance;
        int target = INSTANCE.dynamicTargetFps;

        if (fps < target - 25 && current > min) next = current - 1;
        else if (fps > target + 35 && current < max) next = current + 1;

        if (next != current) client.options.renderDistance().set(next);
    }
}
