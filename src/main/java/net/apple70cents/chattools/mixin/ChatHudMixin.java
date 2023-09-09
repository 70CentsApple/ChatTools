package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.features.chatnotifier.ChatNotifier;
import net.apple70cents.chattools.features.chatresponser.ChatResponser;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static net.apple70cents.chattools.ChatTools.config;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @ModifyConstant(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", constant = @Constant(intValue = 100), require = 0)
    public int modifyMaxHistorySize(int originalMaxSize) {
        return config.maxHistorySize;
//        if (FabricLoader.getInstance().isModLoaded("tweakermore")) {
//            return originalMaxSize;
//        } else {
//            return config.maxHistorySize;
//        }
    }

    @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"))
    public void dealWithMessage(Args args) {
        if (!config.modEnabled) {
            return;
        }
        Text message = args.get(0);
        MessageIndicator indicator = args.get(3);
        if(config.chatResponserEnabled && !ChatNotifier.isJustSentMessage()){
            ChatResponser.work(message.getString());
        }
        args.set(0, ChatNotifier.deal(message, indicator));
    }
}