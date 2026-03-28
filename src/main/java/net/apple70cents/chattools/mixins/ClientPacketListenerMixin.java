package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.formatter.Formatter;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author 70CentsApple
 */
//? if >=1.19 {
@Mixin(net.minecraft.client.multiplayer.ClientPacketListener.class)
//?} else {
/*@Mixin(net.minecraft.client.player.LocalPlayer.class)
*///?}
public abstract class ClientPacketListenerMixin {
    // it catches the messages that are about to send, to apply Formatter to them
    @ModifyVariable(method =
//? if >=1.19 {
         "sendChat"
//?} else {
        /*"chat"
*///?}
         , at = @At("HEAD"), argsOnly = true)
    public String sendPublicMessage(String message) {
        MessageUtils.updateLastSentMessageTimestamp();
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return message;
        }
        if (!(boolean) ConfigUtils.get("formatter.Enabled")) {
            return message;
        }
        return Formatter.work(message);
    }

}
