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
    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void voidboost$filterParticles(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz, CallbackInfo ci) {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.disableParticles) {
            ci.cancel();
            return;
        }
        if (c.reducedParticles && c.performanceMode) {
            long hash = Double.doubleToLongBits(x * 31.0 + y * 17.0 + z * 13.0);
            if ((hash & 3L) != 0L) ci.cancel();
        }
    }
}
