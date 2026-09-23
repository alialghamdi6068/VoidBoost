package com.voidboost.mixin;

import com.voidboost.client.VoidBoostRuntime;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds no fixed entity-distance policy.
 *
 * Sodium/Minecraft remain responsible for entity render distance and culling.
 * VoidBoost must not override a player's renderer settings.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void voidboost$earlyReject(
            T entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // Intentionally empty. Entity visibility is owned by Minecraft/Sodium.
        // The mixin remains as a compatibility placeholder for older mappings.
    }
}
