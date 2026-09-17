package com.voidboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class VoidBoostClient implements ClientModInitializer {
    public static final String MOD_ID = "voidboost";
    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {
        VoidBoostConfig.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.voidboost.open_menu",
                GLFW.GLFW_KEY_O,
                KeyMapping.Category.register("category.voidboost")
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            VoidBoostConfig.tick(client);
            while (openMenuKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new VoidBoostScreen(null));
                }
            }
        });
    }
}
