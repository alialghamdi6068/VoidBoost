package com.voidboost.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    /**
     * VoidBoost uses the cheapest possible client-side particle path:
     * particle spawning is rejected before Minecraft creates/renders a
     * particle instance. No counters, configuration reads, or allocations
     * are performed here.
     *
     * This targets vanilla ClientLevel only, so it does not replace or patch
     * Sodium's renderer and is intentionally safe to run alongside Sodium.
     */
    @Inject(method = "doAddParticle", at = @At("HEAD"), cancellable = true)
    private void voidboost$disableParticles(
            ParticleOptions particle,
            boolean overrideLimiter,
            boolean alwaysShow,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
