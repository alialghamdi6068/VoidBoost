package com.voidboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class VoidBoostClient implements ClientModInitializer {
    public static final String MOD_ID = "voidboost";

    @Override
    public void onInitializeClient() {
        // VoidBoost is intentionally UI-free: the maximum-performance profile
        // is applied automatically and remains active for the whole session.
        VoidBoostConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(VoidBoostConfig::tick);
        ClientTickEvents.END_CLIENT_TICK.register(VoidBoostAI::tick);
    }
}
