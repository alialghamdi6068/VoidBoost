package com.voidboost.client;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Small local configuration.
 *
 * Performance optimizations are enabled by default and do not modify
 * Minecraft/Sodium video settings. The optional monitor is deliberately
 * disabled by default because drawing a HUD every frame costs CPU/GPU time.
 */
public final class VoidBoostConfig {
    private static final String FILE_NAME = "voidboost.properties";
    private static boolean performanceMonitor;
    private static boolean aggressiveCulling = true;

    private VoidBoostConfig() {}

    public static boolean isPerformanceMonitorEnabled() {
        return performanceMonitor;
    }

    public static boolean isAggressiveCullingEnabled() {
        return aggressiveCulling;
    }

    public static void load() {
        performanceMonitor = false;
        aggressiveCulling = true;

        Path path = configPath();
        if (path != null && Files.isRegularFile(path)) {
            try {
                for (String raw : Files.readAllLines(path)) {
                    String line = raw.trim();
                    if (line.startsWith("monitor=")) {
                        performanceMonitor = Boolean.parseBoolean(
                                line.substring("monitor=".length()).trim()
                        );
                    } else if (line.startsWith("aggressive_culling=")) {
                        aggressiveCulling = Boolean.parseBoolean(
                                line.substring("aggressive_culling=".length()).trim()
                        );
                    }
                }
            } catch (IOException ignored) {
                performanceMonitor = false;
                aggressiveCulling = true;
            }
        }
        VoidBoostStats.reset();
    }

    public static void togglePerformanceMonitor() {
        performanceMonitor = !performanceMonitor;
        save();
        VoidBoostStats.reset();
    }

    private static Path configPath() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.gameDirectory == null) return null;
        return client.gameDirectory.toPath().resolve("config").resolve(FILE_NAME);
    }

    private static void save() {
        Path path = configPath();
        if (path == null) return;
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(
                    path,
                    "monitor=" + performanceMonitor + System.lineSeparator()
                            + "aggressive_culling=" + aggressiveCulling + System.lineSeparator()
            );
        } catch (IOException ignored) {
            // A missing preference must never prevent the client from starting.
        }
    }
}
