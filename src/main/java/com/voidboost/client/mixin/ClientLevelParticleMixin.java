package com.voidboost.client.mixin;

import com.voidboost.client.VoidBoostConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelParticleMixin {
    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void voidboost$limitParticles(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<Object> cir) {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (c.disableParticles) {
            cir.setReturnValue(null);
        }
    }
}
