package com.voidboost.mixin;

import com.voidboost.client.VoidBoostScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoOptionsScreenMixin extends Screen {
    protected VideoOptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void voidboost$addButton(CallbackInfo ci) {
        int x = this.width / 2 - 100;
        int y = this.height - 65;
        addRenderableWidget(Button.builder(Component.literal("VoidBoost"), button -> {
            Screen current = this;
            this.minecraft.setScreen(new VoidBoostScreen(current));
        }).bounds(x, y, 200, 20).build());
    }
}
