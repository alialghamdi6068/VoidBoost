package com.voidboost.mixin;

import com.voidboost.client.VoidBoostRuntime;
import com.voidboost.client.VoidBoostStats;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Unique
    private static int voidboost$particleCounter;

    /**
     * Intercepts the final particle creation path. This covers normal particles
     * and always-visible/override-limiter particles without duplicating injections
     * on multiple public overloads.
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
        int mode = VoidBoostRuntime.particleMode();
        if (mode == 0) return;

        if (mode == 2) {
            VoidBoostStats.particleAttempt();
            ci.cancel();
            return;
        }

        int keep = VoidBoostRuntime.particleKeepPercent();
        int sample = voidboost$particleCounter++;
        if (voidboost$particleCounter >= 100) {
            voidboost$particleCounter = 0;
        }

        if (sample >= keep) {
            VoidBoostStats.particleAttempt();
            ci.cancel();
        }
    }
}
