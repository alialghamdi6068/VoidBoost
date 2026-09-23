package com.voidboost.mixin;

import com.voidboost.client.VoidBoostAI;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S> {
    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void voidboost$adaptiveClutterCull(
            T entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue()) {
            return;
        }

        if (VoidBoostAI.shouldCullEntity(entity)) {
            cir.setReturnValue(false);
        }
    }
}
