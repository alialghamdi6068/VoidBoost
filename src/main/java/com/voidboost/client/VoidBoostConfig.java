package com.voidboost.client;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Small local configuration. Only the optional monitor preference is persisted.
 * VoidBoost never modifies Minecraft or Sodium video settings.
 */
public final class VoidBoostConfig {
    private static final String FILE_NAME = "voidboost.properties";
    private static boolean performanceMonitor;

    private VoidBoostConfig() {}

    public static boolean isPerformanceMonitorEnabled() {
        return performanceMonitor;
    }

    public static void load() {
        performanceMonitor = false;
        Path path = configPath();
        if (path != null && Files.isRegularFile(path)) {
            try {
                for (String line : Files.readAllLines(path)) {
                    if (line.startsWith("monitor=")) {
                        performanceMonitor = Boolean.parseBoolean(line.substring("monitor=".length()).trim());
                        break;
                    }
                }
            } catch (IOException ignored) {
                performanceMonitor = false;
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
            Files.writeString(path, "monitor=" + performanceMonitor + System.lineSeparator());
        } catch (IOException ignored) {
            // A missing preference must never prevent the client from starting.
        }
    }
}
