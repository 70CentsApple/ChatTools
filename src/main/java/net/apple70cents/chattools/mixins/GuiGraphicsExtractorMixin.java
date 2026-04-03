package net.apple70cents.chattools.mixins;

//? if >=1.21.11 {

import net.apple70cents.chattools.features.general.ClickEventsPreviewer;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
@Mixin(GuiGraphicsExtractor.class)
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
@Mixin(GuiGraphics.class)
*///?}
public abstract class GuiGraphicsExtractorMixin {
    @Shadow
    Style hoveredTextStyle;
    @Shadow
    Style clickableTextStyle;

//? if >=26.1 {
    @Inject(method = "extractDeferredElements", at = @At("HEAD"))
//?} else {
    /*@Inject(method = "renderDeferredElements", at = @At("HEAD"))
*///?}
    public void modifyHoverEvent(CallbackInfo ci) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (!ConfigUtils.PREVIEW_CLICK_EVENTS_ENABLED) {
            return;
        }
        Style style = clickableTextStyle != null ? clickableTextStyle : hoveredTextStyle;
        hoveredTextStyle = ClickEventsPreviewer.work(style);
    }
}
//?} else {
/*// don't do anything
@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.Minecraft.class)
public abstract class GuiGraphicsExtractorMixin {}
*///?}
