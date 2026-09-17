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
    private static int voidboost$particleCounter;

    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void voidboost$filterParticles(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz, CallbackInfo ci) {
        VoidBoostConfig c = VoidBoostConfig.get();

        if (c.disableParticles) {
            VoidBoostStats.particleAttempt();
            ci.cancel();
            return;
        }

        if (!c.reducedParticles) return;

        // Cheap deterministic sampling; the adaptive controller can tighten the budget under load.
        int configured = Math.max(1, Math.min(100, c.particleLimitPercent));
        int keep = VoidBoostAI.particleBudget(configured);
        int sample = voidboost$particleCounter++ % 100;
        if (sample >= keep) {
            VoidBoostStats.particleAttempt();
            ci.cancel();
        }
    }
}
