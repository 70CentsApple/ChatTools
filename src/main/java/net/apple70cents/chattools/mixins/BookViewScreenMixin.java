package net.apple70cents.chattools.mixins;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import org.spongepowered.asm.mixin.Mixin;

//? if >=1.21.11 {
//?} else {
/*import net.apple70cents.chattools.features.general.ClickEventsPreviewer;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin {
//? if >=1.21.11 {
//?} else {
/*@Inject(method = "getClickedComponentStyleAt", at = @At(value = "RETURN"), cancellable = true)
public void modifyHoverEvent(double x, double y, CallbackInfoReturnable<Style> cir) {
         Style style = cir.getReturnValue();
         if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
             cir.setReturnValue(style);
             return;
         }
         if (!ConfigUtils.PREVIEW_CLICK_EVENTS_ENABLED) {
             cir.setReturnValue(style);
             return;
         }
         cir.setReturnValue(ClickEventsPreviewer.work(style));
}
*///?}
}
