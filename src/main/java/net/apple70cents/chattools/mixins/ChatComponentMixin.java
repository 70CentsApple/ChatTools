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

    @Shadow
    @Final
    private List<
//? if >=1.19 {
            GuiMessage.Line
//?} else {
            /*GuiMessage<net.minecraft.util.FormattedCharSequence>
*///?}
            > trimmedMessages;

//? if >=1.20.5 {
    @Inject(method = "refreshTrimmedMessages", at = @At("HEAD"))
    public void chatTools$onRefreshHead(CallbackInfo ci) {
        ChatAnimator.pushReplaying();
    }

    @Inject(method = "refreshTrimmedMessages", at = @At("RETURN"))
    public void chatTools$onRefreshReturn(CallbackInfo ci) {
        for (Object line : this.trimmedMessages) {
            ChatAnimator.onNewLine(line);
        }
        ChatAnimator.popReplaying();
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At("RETURN"))
    public void chatTools$onAddToDisplayQueue(GuiMessage message, CallbackInfo ci) {
        chatTools$markFreshLines();
    }
//?} elif >=1.19 {
    /*// Pre-1.20.5: addition + refresh both go through the same private overload
    // addMessage(Component, MessageSignature, int addedTime, GuiMessageTag tag, boolean onlyTrim).
    // The boolean tells us whether this is a refresh replay.
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", at = @At("HEAD"))
    public void chatTools$onAddHead(Component msg, MessageSignature sig, int addedTime, GuiMessageTag tag, boolean onlyTrim, CallbackInfo ci) {
        if (onlyTrim) ChatAnimator.pushReplaying();
    }
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", at = @At("RETURN"))
    public void chatTools$onAddReturn(Component msg, MessageSignature sig, int addedTime, GuiMessageTag tag, boolean onlyTrim, CallbackInfo ci) {
        try {
            chatTools$markFreshLines();
        } finally {
            if (onlyTrim) ChatAnimator.popReplaying();
        }
    }
*///?} else {
    /*// 1.16.5 - 1.18.2: addMessage(Component, int chatLineId, int addedTime, boolean refresh).
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At("HEAD"))
    public void chatTools$onAddHead(Component msg, int chatLineId, int addedTime, boolean refresh, CallbackInfo ci) {
        if (refresh) ChatAnimator.pushReplaying();
    }
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At("RETURN"))
    public void chatTools$onAddReturn(Component msg, int chatLineId, int addedTime, boolean refresh, CallbackInfo ci) {
        try {
            chatTools$markFreshLines();
        } finally {
            if (refresh) ChatAnimator.popReplaying();
        }
    }
*///?}

    private void chatTools$markFreshLines() {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            ChatAnimator.clearNextLineNoPush();
            return;
        }
        // The SEEN set must be populated even when the animation is off or replaying,
        // so the first real activation does not retroactively flag pre-existing lines.
        for (int i = 0; i < this.trimmedMessages.size(); i++) {
            Object line = this.trimmedMessages.get(i);
            if (ChatAnimator.isTracked(line)) break;
            ChatAnimator.onNewLine(line);
        }
        // Consume the one-shot "do not push older lines" flag set by the Chat Compactor
        // path so it does not accidentally bleed into the next, unrelated insertion.
        ChatAnimator.clearNextLineNoPush();
    }

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
            return ConfigUtils.getInt("general.MaxHistoryLength");
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
        if (ConfigUtils.getBoolean("general.OverrideChatColor.Enabled")) {
            message = ChatColorEraser.work(message);
        }

        int occurrenceCount = 1;
        if (ConfigUtils.getBoolean("general.ChatCompactor.Enabled")) {
            occurrenceCount = ChatCompactor.calculateOccurrenceCount(message);
            if (occurrenceCount > 1 && !this.allMessages.isEmpty()) {
                try {
                    this.allMessages.remove(0);
                    this.rescaleChat();
                    // Net stack height does not change for a compactor replacement
                    // (we just removed one and will add one). Tell the animator not
                    // to push older lines upward when this next line gets registered.
                    ChatAnimator.markNextLineNoPush();
                } catch (Exception e) {
                    // if any error (e.g. UnsupportedOperationException), catch it to avoid crashing
                    LoggerUtils.info("[ChatTools] Failed to remove duplicate message for compaction.");
                    LoggerUtils.error("[ChatTools] Compaction error", e);
                }
            }
        }

        Component msgWithoutAdditionalAffixes = message;
        String hashcode = TextUtils.generateHashcode(message);

        if (ConfigUtils.getBoolean("notifier.Highlight.InsertBeforeTimestamps")) {
            if (ConfigUtils.getBoolean("general.Timestamp.Enabled")) {
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
            if (ConfigUtils.getBoolean("general.Timestamp.Enabled")) {
                message = Timestamp.work(message, hashcode);
            }
        }

        if (ConfigUtils.getBoolean("general.ChatCompactor.Enabled")) {
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
  /*@Inject(method = "getClickedComponentStyleAt", at = @At(value = "RETURN"), cancellable = true)
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
