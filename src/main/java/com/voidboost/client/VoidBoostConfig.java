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

    private static VoidBoostConfig INSTANCE = new VoidBoostConfig();
    private static long lastSave;
    private static int stableTicks;

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                INSTANCE = GSON.fromJson(Files.readString(FILE), VoidBoostConfig.class);
                if (INSTANCE == null) INSTANCE = new VoidBoostConfig();
            }
        } catch (Exception ignored) {
            INSTANCE = new VoidBoostConfig();
        }
    }

    public static VoidBoostConfig get() {
        return INSTANCE;
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(INSTANCE));
            lastSave = System.currentTimeMillis();
        } catch (IOException ignored) {
        }
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
        if (fps < INSTANCE.dynamicTargetFps - 20 && current > 4) next = current - 1;
        else if (fps > INSTANCE.dynamicTargetFps + 30 && current < 20) next = current + 1;

        if (next != current) {
            client.options.renderDistance().set(next);
        }
    }
}
