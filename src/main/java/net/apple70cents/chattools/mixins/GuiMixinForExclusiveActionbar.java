package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.general.ExclusiveActionbarHandler;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.McUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
//?} elif >=1.21 {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
*///?} elif >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

/**
 * @author 70CentsApple
 */

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
@Mixin(Hud.class)
//?} else {
/*import net.minecraft.client.gui.Gui;
@Mixin(Gui.class)
*///?}
public abstract class GuiMixinForExclusiveActionbar {
//? if >=26.1 {
    @Inject(method = "extractOverlayMessage", at = @At(value = "HEAD"))
    public void renderExclusiveActionbar(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
//?} elif >=1.21 {
    /*@Inject(method = "renderOverlayMessage", at = @At(value = "HEAD"))
    public void renderExclusiveActionbar(GuiGraphics context, DeltaTracker deltaTracker, CallbackInfo ci) {
*///?} elif >=1.20 {
    /*@Inject(method = "render", at = @At(value = "HEAD"))
    public void renderExclusiveActionbar(GuiGraphics context, float tickDelta, CallbackInfo ci) {
*///?} else {
    /*@Inject(method = "render", at = @At(value = "HEAD"))
    public void renderExclusiveActionbar(PoseStack context, float tickDelta, CallbackInfo ci) {
*///?}
        if (McUtils.isGuiHidden()) {
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
