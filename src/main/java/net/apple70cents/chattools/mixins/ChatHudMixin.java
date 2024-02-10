package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.bubble.BubbleRenderer;
import net.apple70cents.chattools.features.general.NickHider;
import net.apple70cents.chattools.features.general.Timestamp;
import net.apple70cents.chattools.features.notifier.BasicNotifier;
import net.apple70cents.chattools.features.notifier.Toast;
import net.apple70cents.chattools.features.responser.Responser;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @ModifyConstant(method =
            //#if MC>=11900
            "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"
            //#else
            //$$"addMessage(Lnet/minecraft/text/Text;IIZ)V"
            //#endif
            , constant = @Constant(intValue = 100), require = 0)
    public int modifyMaxHistorySize(int originalMaxSize) {
        if ((boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return ((Number) ChatTools.CONFIG.get("general.MaxHistoryLength")).intValue();
        } else {
            return 100;
        }
    }

    //#if MC>=11900
    @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"))
    //#else
    //$$@ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;IIZ)V"))
    //#endif
    public void onReceivingMessages(Args args) {
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return;
        }
        Text message = args.get(0);
        if ((boolean) ChatTools.CONFIG.get("bubble.Enabled")) {
            // it must be done before NickHider began to work
            BubbleRenderer.addChatBubble(message);
        }
        if ((boolean) ChatTools.CONFIG.get("responser.Enabled") && !MessageUtils.hadJustSentMessage()) {
            // obviously, we don't respond to our own messages
            Responser.work(message);
        }
        if ((boolean) ChatTools.CONFIG.get("general.NickHider.Enabled")) {
            message = NickHider.work(message);
        }
        if (BasicNotifier.shouldWork(message)) {
            if ((boolean) ChatTools.CONFIG.get("notifier.Toast.Enabled") && !MinecraftClient.getInstance()
                                                                                            .isWindowFocused()) {
                Toast.work(TextUtils.wash(message.getString()));
            }
            message = BasicNotifier.work(message);
        }
        if ((boolean) ChatTools.CONFIG.get("general.Timestamp.Enabled")) {
            message = Timestamp.work(message);
        }
        // we need to reset `justSentMessage` status, since it might be that this message received was sent by us
        MessageUtils.setJustSentMessage(false);
        args.set(0, message);
    }
}
