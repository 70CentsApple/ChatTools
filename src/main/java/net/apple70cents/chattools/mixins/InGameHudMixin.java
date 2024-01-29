package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author 70CentsApple
 */

//#if MC>=11700
@Mixin(InGameHud.class)
//#else
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.gui.hud.ChatHud;
//$$ @Mixin(ChatHud.class)
//#endif
public abstract class InGameHudMixin {
    //#if MC>=11700
    @Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"), cancellable = true)
    public void restoreMessages(CallbackInfo ci) {
    //#else
    //$$ boolean theFirstVisit = true;
    //$$ @Inject(at = @At("HEAD"), method = "clear", cancellable = true)
    //$$ public void restoreMessages(boolean clearHistory, CallbackInfo ci) {
    //#endif
        if (!((boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled"))) {
            return;
        }
        if (!((boolean) ChatTools.CONFIG.get("general.RestoreMessages.Enabled"))) {
            return;
        }
        //#if MC<11700
        //$$ if(theFirstVisit){ theFirstVisit = false; } else if (clearHistory) {
        //#endif
        if ((boolean) ChatTools.CONFIG.get("general.RestoreMessages.SplitLineEnabled")) {
            MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.RestoreMessagesSplitLine"));
        }
        // this cancels the clear function, in other words, restores the message.
        ci.cancel();
        //#if MC<11700
        //$$ }
        //#endif
    }
}
