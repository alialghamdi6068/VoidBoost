package com.voidboost.mixin;

import com.voidboost.client.VoidBoostScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(net.minecraft.network.chat.Component title) {
        super(title);
    }

    @Inject(method="keyPressed", at=@At("HEAD"), cancellable=true)
    private void voidboost$openMenu(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if(keyCode==GLFW.GLFW_KEY_O) {
            ((Screen)(Object)this).getMinecraft().setScreen(new VoidBoostScreen((Screen)(Object)this));
            cir.setReturnValue(true);
        }
    }
}
