package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author 70CentsApple
 */

//#if MC>=11700
@Mixin(Gui.class)
//#else
//$$ import net.minecraft.client.gui.components.ChatComponent;
//$$ @Mixin(ChatComponent.class)
//#endif
public abstract class GuiMixinForRestoreMessages {
//#if MC>=11700
    @Inject(method = "onDisconnected", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;clearMessages(Z)V"), cancellable = true)
    public void restoreMessages(CallbackInfo ci) {
//#else
//$$ boolean theFirstVisit = true;
//$$ @Inject(at = @At("HEAD"), method = "clearMessages", cancellable = true)
//$$ public void restoreMessages(boolean clearHistory, CallbackInfo ci) {
//#endif
        if (!(ConfigUtils.CHAT_TOOLS_ENABLED)) {
            return;
        }
        if (!((boolean) ConfigUtils.get("general.RestoreMessages.Enabled"))) {
            return;
        }
//#if MC<11700
//$$    if(theFirstVisit){ theFirstVisit = false; } else if (clearHistory) {
//#endif
        if ((boolean) ConfigUtils.get("general.RestoreMessages.SplitLineEnabled")) {
            MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.RestoreMessagesSplitLine"));
        }
        // this cancels the clear function, in other words, restores the message.
        ci.cancel();
//#if MC<11700
//$$    }
//#endif
    }
}
