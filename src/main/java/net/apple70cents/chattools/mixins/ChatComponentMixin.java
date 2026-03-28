package net.apple70cents.chattools.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.apple70cents.chattools.features.bubble.BubbleRenderer;
import net.apple70cents.chattools.features.filter.ChatFilter;
import net.apple70cents.chattools.features.general.*;
import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.features.notifier.BasicNotifier;
import net.apple70cents.chattools.features.responder.Responder;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
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

//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.network.chat.MessageSignature;
//?} elif >=1.19 {
/*import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.MessageSignature;
*///?} else {
/*import net.minecraft.client.GuiMessage;
*///?}


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
//? if >=1.20.5 {
            {"addMessageToQueue", "addMessageToDisplayQueue", "addMessage*", "addRecentChat"}
//?} elif >=1.19 {
            /*{"addRecentChat", "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"}
*///?} else {
            /*"addMessage(Lnet/minecraft/network/chat/Component;IIZ)V"
*///?}
            , at = @At(value = "CONSTANT", args = "intValue=100"))
    public int modifyMaxHistorySize(int originalMaxSize) {
        if (ConfigUtils.CHAT_TOOLS_ENABLED) {
            return ((Number) ConfigUtils.get("general.MaxHistoryLength")).intValue();
        } else {
            return 100;
        }
    }

//? if >=26.1 {
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At(value = "HEAD"), cancellable = true)
//?} elif >=1.19 {
    /*@Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "HEAD"), cancellable = true)
*///?} else {
/*@Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At(value = "HEAD"), cancellable = true)
*///?}
    public void onReceivingMessages(
//? if >=26.1 {
            Component message, MessageSignature signature, GuiMessageSource source, GuiMessageTag indicator
//?} elif >=1.19 {
            /*Component message, MessageSignature signature, GuiMessageTag indicator
*///?} else {
            /*Component message, int messageId, int timestamp, boolean refresh
*///?}
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

//? if >=26.1 {
    @ModifyArgs(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"))
//?} elif >=1.20.5 {
    /*@ModifyArgs(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"))
*///?} elif >=1.19 {
    /*@ModifyArgs(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"))
*///?} else {
    /*@ModifyArgs(method = "addMessage(Lnet/minecraft/network/chat/Component;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;IIZ)V"))
*///?}
    public void onReceivingMessages(Args args) {
//? if >=1.20.5 {
        final int MESSAGE_IDX = 1;
//?} else {
        /*final int MESSAGE_IDX = 0;
*///?}
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        Component message = args.get(MESSAGE_IDX);
        // ignores this message if Chat Filter is to work
        if (ChatFilter.shouldFilter(message)) {
            return;
        }
        if (ConfigUtils.BUBBLE_ENABLED) {
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

        int occurrenceCount = 1;
        if ((boolean) ConfigUtils.get("general.ChatCompactor.Enabled")) {
            occurrenceCount = ChatCompactor.calculateOccurrenceCount(message);
            if (occurrenceCount > 1 && !this.allMessages.isEmpty()) {
                try {
                    this.allMessages.remove(0);
                    this.rescaleChat();
                } catch (Exception e) {
                    // if any error (e.g. UnsupportedOperationException), catch it to avoid crashing
                    LoggerUtils.info("[ChatTools] Failed to remove duplicate message for compaction.");
                    e.printStackTrace();
                }
            }
        }

        Component msgWithoutAdditionalAffixes = message;
        String hashcode = TextUtils.generateHashcode(message);

        if ((boolean) ConfigUtils.get("notifier.Highlight.InsertBeforeTimestamps")) {
            if ((boolean) ConfigUtils.get("general.Timestamp.Enabled")) {
                message = Timestamp.work(message, hashcode);
            }
            SpecialUnits.NotifierRuleUnit matchedRule = BasicNotifier.shouldWork(message);
            if (matchedRule != null) {
                message = BasicNotifier.work(message, matchedRule);
            }
        } else {
            SpecialUnits.NotifierRuleUnit matchedRule = BasicNotifier.shouldWork(message);
            if (matchedRule != null) {
                message = BasicNotifier.work(message, matchedRule);
            }
            if ((boolean) ConfigUtils.get("general.Timestamp.Enabled")) {
                message = Timestamp.work(message, hashcode);
            }
        }

        if ((boolean) ConfigUtils.get("general.ChatCompactor.Enabled")) {
            message = ChatCompactor.appendTrailing(message, occurrenceCount);
        }

        if (ConfigUtils.NICK_HIDER_ENABLED) {
            message = NickHider.work(message);
        }

        TextUtils.MessageUnit messageUnit = new TextUtils.MessageUnit(msgWithoutAdditionalAffixes, message, Instant
                .now().getEpochSecond(), occurrenceCount, !MessageUtils.isProcessingServerMessage());
        TextUtils.putMessageMapWithHashcode(hashcode, messageUnit);

        args.set(MESSAGE_IDX, message);
    }

//? if >=1.21.11 {
//?} else {
/*  @Inject(method = "getClickedComponentStyleAt", at = @At(value = "RETURN"), cancellable = true)
    public void modifyHoverEvent(double x, double y, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.network.chat.Style> cir) {
         net.minecraft.network.chat.Style style = cir.getReturnValue();
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
