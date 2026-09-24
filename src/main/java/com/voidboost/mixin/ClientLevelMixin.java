package com.voidboost.mixin;

import com.voidboost.client.VoidBoostConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    /**
     * In aggressive mode, reject particles before Minecraft creates or
     * renders an instance. This intentionally trades visual effects for less
     * particle work and is disabled together with entity culling by the local
     * aggressive_culling setting.
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
        if (VoidBoostConfig.isAggressiveCullingEnabled()) {
            ci.cancel();
        }
    }
}
