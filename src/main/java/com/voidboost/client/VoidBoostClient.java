package com.voidboost.client;

import com.mojang.blaze3d.platform.InputConstants;
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

    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {
        VoidBoostConfig.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.voidboost.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                VOIDBOOST_CATEGORY
        ));

        // If an older VoidBoost installation left the binding unassigned,
        // restore the default O key without overwriting a custom binding.
        if (openMenuKey.isUnbound()) {
            openMenuKey.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_O));
        }

        ClientTickEvents.END_CLIENT_TICK.register(VoidBoostConfig::tick);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new VoidBoostScreen(null));
                }
            }
        });
    }
}
