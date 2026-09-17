package com.voidboost.mixin;

import com.voidboost.client.VoidBoostConfig;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void voidboost$entityDistance(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        VoidBoostConfig c = VoidBoostConfig.get();
        if (!c.entityRenderOptimization) return;

        double maxDistance = c.maxEntityDistance;
        if (entity.distanceToSqr(x, y, z) > maxDistance * maxDistance) {
            cir.setReturnValue(false);
        }
    }
}
