package com.voidboost.client.mixin;

import com.voidboost.client.VoidBoostConfig;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void voidboost$entityDistance(T entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (VoidBoostConfig.get().maxFpsPreset && entity.distanceToSqr(x, y, z) > 4096.0D) {
            cir.setReturnValue(false);
        }
    }
}
