package net.apple70cents.chattools.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.apple70cents.chattools.features.bubble.BubbleRenderer;
import net.apple70cents.chattools.features.filter.ChatFilter;
import net.apple70cents.chattools.features.general.*;
import net.apple70cents.chattools.features.notifier.BasicNotifier;
import net.apple70cents.chattools.features.responder.Responder;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.time.Instant;
import java.util.List;

//#if MC>=11900
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.MessageSignature;
//#endif

/**
 * @author 70CentsApple
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Shadow
    public abstract void rescaleChat();


    @Shadow
    @Final
    private List<GuiMessage> allMessages;

    @ModifyExpressionValue(method =
            //#if MC>=12005
            {"addMessageToQueue", "addMessageToDisplayQueue", "addMessage*", "addRecentChat"}
            //#elseif MC>=11900
            //$$ {"addRecentChat", "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"}
            //#else
            //$$ "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V"
            //#endif
            , at = @At(value = "CONSTANT", args = "intValue=100"))
    public int modifyMaxHistorySize(int originalMaxSize) {
        if (ConfigUtils.CHAT_TOOLS_ENABLED) {
            return ((Number) ConfigUtils.get("general.MaxHistoryLength")).intValue();
        } else {
            return 100;
        }
    }

    //#if MC>=11900
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "HEAD"), cancellable = true)
    //#else
    //$$ @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At(value = "HEAD"), cancellable = true)
    //#endif
    public void onReceivingMessages(
            //#if MC>=11900
            Component message, MessageSignature signature, GuiMessageTag indicator
            //#else
            //$$ Component message, int messageId, int timestamp, boolean refresh
            //#endif
            , CallbackInfo ci) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (ChatFilter.shouldFilter(message)) {
            LoggerUtils.info("[ChatTools] Filtered message: " + message.getString());
            if (Responder.shouldWork(message)) {
                Responder.work(message);
            }
            ChatFilter.sendPlaceholderIfActive();
            ci.cancel();
        }
    }

    //#if MC>=12005
    @ModifyArgs(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"))
    //#elseif MC>=11900
    //$$ @ModifyArgs(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"))
    //#else
    //$$ @ModifyArgs(method = "addMessage(Lnet/minecraft/network/chat/Component;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;IIZ)V"))
    //#endif
    public void onReceivingMessages(Args args) {
        //#if MC>=12005
        final int MESSAGE_IDX = 1;
        //#else
        //$$ final int MESSAGE_IDX = 0;
        //#endif
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        Component message = args.get(MESSAGE_IDX);
        // ignores this message if Chat Filter is to work
        if (ChatFilter.shouldFilter(message)) {
            return;
        }
        if ((boolean) ConfigUtils.get("bubble.Enabled")) {
            // it must be done before NickHider began to work
            BubbleRenderer.addChatBubble(message);
        }
        // This is not the only attempt that we try to activate the responder.
        // When filtering a message with `responder.respondToFilteredMessages` option on, responder will also try to work.
        if (Responder.shouldWork(message)) {
            Responder.work(message);
        }
        if ((boolean) ConfigUtils.get("general.OverrideChatColor.Enabled")) {
            message = ChatColorEraser.work(message);
        }
        if ((boolean) ConfigUtils.get("general.NickHider.Enabled")) {
            message = NickHider.work(message);
        }
        int occurrenceCount = 1;
        if ((boolean) ConfigUtils.get("general.ChatCompactor.Enabled")) {
            occurrenceCount = ChatCompactor.calculateOccurrenceCount(message);
            if (occurrenceCount > 1 && !this.allMessages.isEmpty()) {
                this.allMessages.remove(0);
                this.rescaleChat();
            }
        }

        Component msgWithoutAdditionalAffixes = message;
        String hashcode = TextUtils.generateHashcode(message);

        if ((boolean) ConfigUtils.get("notifier.Highlight.InsertBeforeTimestamps")) {
            if ((boolean) ConfigUtils.get("general.Timestamp.Enabled")) {
                message = Timestamp.work(message, hashcode);
            }
            if (BasicNotifier.shouldWork(message)) {
                message = BasicNotifier.work(message);
            }
        } else {
            if (BasicNotifier.shouldWork(message)) {
                message = BasicNotifier.work(message);
            }
            if ((boolean) ConfigUtils.get("general.Timestamp.Enabled")) {
                message = Timestamp.work(message, hashcode);
            }
        }

        if ((boolean) ConfigUtils.get("general.ChatCompactor.Enabled")) {
            message = ChatCompactor.appendTrailing(message, occurrenceCount);
        }

        TextUtils.MessageUnit messageUnit = new TextUtils.MessageUnit(msgWithoutAdditionalAffixes, message, Instant
                .now().getEpochSecond(), occurrenceCount, !MessageUtils.isProcessingServerMessage());
        TextUtils.putMessageMapWithHashcode(hashcode, messageUnit);

        args.set(MESSAGE_IDX, message);
    }

    //#if MC>=12111
    //$$ // no-op
    //#else
    //$$ @Inject(method = "getClickedComponentStyleAt", at = @At(value = "RETURN"), cancellable = true)
    //$$ public void modifyHoverEvent(double x, double y, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.network.chat.Style> cir) {
    //$$     net.minecraft.network.chat.Style style = cir.getReturnValue();
    //$$     if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
    //$$         cir.setReturnValue(style);
    //$$         return;
    //$$     }
    //$$     if (!(boolean) ConfigUtils.get("general.PreviewClickEvents.Enabled")) {
    //$$         cir.setReturnValue(style);
    //$$         return;
    //$$     }
    //$$     cir.setReturnValue(ClickEventsPreviewer.work(style));
    //$$ }
    //#endif

}
