package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.formatter.Formatter;
import net.apple70cents.chattools.utils.MessageUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author 70CentsApple
 */
//#if MC>=11900
@Mixin(net.minecraft.client.network.ClientPlayNetworkHandler.class)
//#else
//$$ @Mixin(ClientPlayerEntity.class)
//#endif
public abstract class ClientPlayNetworkHandlerMixin {
    // it catches the messages that are about to send, and apply Formatter to them
    @ModifyVariable(method = "sendChatMessage", at = @At("HEAD"), argsOnly = true)
    public String sendPublicMessage(String message) {
        MessageUtils.setJustSentMessage(true);
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return message;
        }
        if (!(boolean) ChatTools.CONFIG.get("formatter.Enabled")) {
            return message;
        }
        return Formatter.work(message);
    }

}
