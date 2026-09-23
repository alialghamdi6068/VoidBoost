package com.voidboost.mixin;

import com.voidboost.client.VoidBoostAI;
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
    /**
     * Reject ordinary particles before Minecraft allocates the particle instance.
     *
     * During sustained low-FPS pressure, VoidBoost also rejects non-forced
     * visual particles that bypass the normal limiter. Forced/always-visible
     * particles remain protected so important gameplay feedback is preserved.
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
        if (!VoidBoostRuntime.maximumPerformance() || alwaysShow) {
            return;
        }

        if (!overrideLimiter || VoidBoostAI.emergencyMode()) {
            VoidBoostStats.particleAttempt();
            ci.cancel();
        }
    }
}
