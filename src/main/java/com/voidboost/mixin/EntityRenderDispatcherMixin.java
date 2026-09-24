package com.voidboost.mixin;

import com.voidboost.client.VoidBoostConfig;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cheap distance culling for entities that do not need to be rendered from
 * long range. Vanilla/Sodium still own the main rendering pipeline.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Unique private static final double ITEM_RENDER_DISTANCE_SQR = 48.0D * 48.0D;
    @Unique private static final double EXPERIENCE_ORB_RENDER_DISTANCE_SQR = 32.0D * 32.0D;
    @Unique private static final double OTHER_ENTITY_RENDER_DISTANCE_SQR = 96.0D * 96.0D;

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

        double dx = entity.getX() - x;
        double dy = entity.getY() - y;
        double dz = entity.getZ() - z;
        double distanceSq = dx * dx + dy * dy + dz * dz;

        if (entity instanceof ItemEntity && distanceSq > ITEM_RENDER_DISTANCE_SQR) {
            cir.setReturnValue(false);
            return;
        }

        if (entity instanceof ExperienceOrb && distanceSq > EXPERIENCE_ORB_RENDER_DISTANCE_SQR) {
            cir.setReturnValue(false);
            return;
        }

        if (!(entity instanceof Player) && distanceSq > OTHER_ENTITY_RENDER_DISTANCE_SQR) {
            cir.setReturnValue(false);
        }
    }
}
