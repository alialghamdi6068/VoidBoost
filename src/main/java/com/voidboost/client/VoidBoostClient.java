package com.voidboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class VoidBoostClient implements ClientModInitializer {
    public static final String MOD_ID = "voidboost";

    private static final KeyMapping.Category VOIDBOOST_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "keybindings")
    );

    private KeyMapping toggleMonitorKey;

    @Override
    public void onInitializeClient() {
        VoidBoostConfig.load();

        toggleMonitorKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.voidboost.toggle_monitor",
                GLFW.GLFW_KEY_O,
                VOIDBOOST_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(VoidBoostAI::tick);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleMonitorKey.consumeClick()) {
                VoidBoostConfig.togglePerformanceMonitor();
            }
        });
    }
}
