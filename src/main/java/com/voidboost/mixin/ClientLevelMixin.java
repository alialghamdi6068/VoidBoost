package com.voidboost.mixin;

import com.voidboost.client.VoidBoostRuntime;
import com.voidboost.client.VoidBoostStats;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    /**
     * Reject ordinary particles before Minecraft allocates the particle instance.
     *
     * Forced/always-visible particles are preserved so gameplay-critical visual
     * feedback is not silently removed by VoidBoost.
     */
    @Inject(method = "doAddParticle", at = @At("HEAD"), cancellable = true)
    private void voidboost$filterParticles(
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
        if (!VoidBoostRuntime.maximumPerformance() || overrideLimiter || alwaysShow) {
            return;
        }

        VoidBoostStats.particleAttempt();
        ci.cancel();
    }
}
