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
     * Reject ordinary particles before Minecraft allocates the particle
     * instance. Forced/always-visible particles remain untouched.
     *
     * Normal mode removes the particles Minecraft's limiter would reject
     * anyway before their client-side instance is created. Emergency mode
     * additionally rejects non-forced particles that bypass that limiter.
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
        if (alwaysShow) {
            return;
        }

        if (!overrideLimiter || VoidBoostAI.emergencyMode()) {
            if (VoidBoostConfig.isPerformanceMonitorEnabled()) {
                VoidBoostStats.particleAttempt();
            }
            ci.cancel();
        }
    }
}
