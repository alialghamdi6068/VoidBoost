package com.voidboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class VoidBoostClient implements ClientModInitializer {
    public static final String MOD_ID = "voidboost";

    @Override
    public void onInitializeClient() {
        VoidBoostConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> VoidBoostConfig.tick(client));
    }
}
