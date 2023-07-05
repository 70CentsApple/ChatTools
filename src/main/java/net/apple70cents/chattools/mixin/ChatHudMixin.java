package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.features.chatnotifier.ChatNotifier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static net.apple70cents.chattools.ChatTools.config;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Shadow
    public abstract void addMessage(Text message);

    @Inject(method = "clear", at = @At("HEAD"), cancellable = true)
    public void restoreMessages(boolean clearHistory, CallbackInfo ci) {
        if (config.restoreMessagesEnabled) {
            this.addMessage(Text.translatable("text.config.chattools.option.restoredMessages").formatted(Formatting.GRAY));
            ci.cancel(); // 取消清除聊天记录
        }
    }

    @ModifyConstant(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", constant = @Constant(intValue = 100), require = 0)
    public int modifyMaxHistorySize(int originalMaxSize) {
        if (FabricLoader.getInstance().isModLoaded("tweakermore")) {
            return originalMaxSize;
        } else {
            return config.maxHistorySize;
        }
    }

    @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"))
    // TODO 兼容Compact Chat
    public void dealWithMessage(Args args) {
        Text message = args.get(0);
        MessageIndicator indicator = args.get(3);
        args.set(0, ChatNotifier.deal(message, indicator));
    }
}