package com.voidboost.mixin;

import com.voidboost.client.VoidBoostRuntime;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps expensive block-entity render-state extraction out of the hot path
 * when VoidBoost is actively culling distant renderables.
 *
 * This targets Minecraft's renderer interface rather than Sodium internals,
 * so it remains independent of whether Sodium is installed.
 */
@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRenderMixin {
    @Inject(method = "isInRenderDistance", at = @At("HEAD"), cancellable = true)
    private <T extends BlockEntity> void voidboost$blockEntityDistance(
            T blockEntity,
            Vec3 cameraPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!VoidBoostRuntime.entityCullingEnabled()) return;

        // Keep this hot path allocation-free: block entities can be checked
        // hundreds of times per frame in storage/redstone-heavy areas.
        var pos = blockEntity.getBlockPos();
        double dx = pos.getX() + 0.5D - cameraPos.x;
        double dy = pos.getY() + 0.5D - cameraPos.y;
        double dz = pos.getZ() + 0.5D - cameraPos.z;
        if (dx * dx + dy * dy + dz * dz > VoidBoostRuntime.entityDistanceSquared()) {
            cir.setReturnValue(false);
        }
    }
}
