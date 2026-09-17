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
        if (VoidBoostConfig.get().disableParticles) {
            ci.cancel();
        }
    }
}
