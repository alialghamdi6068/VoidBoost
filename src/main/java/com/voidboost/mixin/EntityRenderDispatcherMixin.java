package com.voidboost.mixin;

import com.voidboost.client.VoidBoostConfig;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cheap distance culling for entities that do not need to be rendered from
 * long range. Vanilla/Sodium still own the main rendering pipeline.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private <E extends Entity> void voidboost$cullLowValueEntities(
            E entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue() || !VoidBoostConfig.isAggressiveCullingEnabled()) {
            return;
        }

        double distanceSq = x * x + y * y + z * z;

        if (entity instanceof ItemEntity && distanceSq > 48.0D * 48.0D) {
            cir.setReturnValue(false);
            return;
        }

        if (entity instanceof ExperienceOrb && distanceSq > 32.0D * 32.0D) {
            cir.setReturnValue(false);
        }
    }
}
