package net.apple70cents.chattools.mixins;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker("init")
//? if >=1.21.11 {
    void invokeInit(int w, int h);
//?} else {
/*void invokeInit(net.minecraft.client.Minecraft minecraft, int w, int h);
*///?}
}
