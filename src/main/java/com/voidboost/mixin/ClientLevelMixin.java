package com.voidboost.mixin;

import com.voidboost.client.VoidBoostAI;
import com.voidboost.client.VoidBoostConfig;
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
     * In normal mode VoidBoost does not alter particle behavior at all.
     * In emergency mode it rejects only particles that are neither forced
     * nor explicitly allowed to bypass the particle limiter.
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
        if (!VoidBoostAI.emergencyMode() || overrideLimiter || alwaysShow) {
            return;
        }

        if (VoidBoostConfig.isPerformanceMonitorEnabled()) {
            VoidBoostStats.particleAttempt();
        }
        ci.cancel();
    }
}
