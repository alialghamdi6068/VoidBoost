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
    private static int voidboost$particleCounter;

    @Inject(
            method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void voidboost$filterParticles(
            ParticleOptions options,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            CallbackInfo ci
    ) {
        // Hot path: one primitive snapshot read; no config/AI work.
        int mode = VoidBoostRuntime.particleMode();
        if (mode == 0) return;

        if (mode == 2 || mode == 4) {
            if (mode == 4) VoidBoostStats.particleAttempt();
            ci.cancel();
            return;
        }

        int keep = VoidBoostRuntime.particleKeepPercent();
        int sample = voidboost$particleCounter++;
        if (voidboost$particleCounter >= 100) {
            voidboost$particleCounter = 0;
        }

        if (sample >= keep) {
            if (mode == 3) VoidBoostStats.particleAttempt();
            ci.cancel();
        }
    }
}
