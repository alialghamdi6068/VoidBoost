package com.voidboost.mixin;

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
    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void voidboost$filterParticles(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz, CallbackInfo ci) {
        VoidBoostConfig c = VoidBoostConfig.get();
        VoidBoostStats.particleAttempt();

        if (c.disableParticles) {
            ci.cancel();
            return;
        }

        if (c.reducedParticles) {
            long hash = Double.doubleToLongBits(x * 31.0 + y * 17.0 + z * 13.0 + vx * 7.0 + vy * 5.0 + vz * 3.0);
            int keep = Math.max(1, c.particleLimitPercent);
            if (Math.floorMod(hash, 100L) >= keep) ci.cancel();
        }
    }
}
