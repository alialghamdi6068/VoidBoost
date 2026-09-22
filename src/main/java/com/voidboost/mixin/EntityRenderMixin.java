package com.voidboost.mixin;

import com.voidboost.client.VoidBoostRuntime;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void voidboost$entityDistance(
            T entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // Hot path: only a primitive volatile read before vanilla rendering work.
        if (!VoidBoostRuntime.entityCullingEnabled()) return;

        // Players and projectiles are gameplay-critical for PvP and remain visible.
        if (entity instanceof Player || entity instanceof Projectile) return;

        if (entity.distanceToSqr(x, y, z) > VoidBoostRuntime.entityDistanceSquared()) {
            cir.setReturnValue(false);
        }
    }
}
