package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.general.ExclusiveActionbarHandler;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=12100
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
//#elseif MC>=12000
//$$ import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif

/**
 * @author 70CentsApple
 */

@Mixin(Gui.class)
public abstract class GuiMixinForExclusiveActionbar {
    //#if MC>=12100
    @Inject(method = "renderOverlayMessage", at = @At(value = "HEAD"))
    public void renderExclusiveActionbar(GuiGraphics context, DeltaTracker deltaTracker, CallbackInfo ci) {
    //#elseif MC>=12000
    //$$ @Inject(method = "render", at = @At(value = "HEAD"))
    //$$ public void renderExclusiveActionbar(GuiGraphics context, float tickDelta, CallbackInfo ci) {
    //#else
    //$$ @Inject(method = "render", at = @At(value = "HEAD"))
    //$$ public void renderExclusiveActionbar(PoseStack context, float tickDelta, CallbackInfo ci) {
    //#endif
        if (Minecraft.getInstance().options.hideGui) {
            return;
        }
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (!ConfigUtils.EXCLUSIVE_ACTIONBAR_ENABLED) {
            return;
        }
        ExclusiveActionbarHandler.tick();
        ExclusiveActionbarHandler.render(context);
    }
}
