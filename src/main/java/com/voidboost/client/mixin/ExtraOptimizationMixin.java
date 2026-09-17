package com.voidboost.client.mixin;

import com.voidboost.client.VoidBoostConfig;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ExtraOptimizationMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void voidboost$disableParticleTick(CallbackInfo ci) {
        if (VoidBoostConfig.get().disableParticles) ci.cancel();
    }
}
