package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.BooleanSupplier;

//#if MC>=11900
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.MessageSignature;
//#else
//$$ import net.minecraft.client.gui.chat.StandardChatListener;
//$$ import net.minecraft.network.chat.ChatType;
//#endif

/**
 * @author 70CentsApple
 */
//#if MC>=11900
@Mixin(ChatListener.class)
//#else
//$$ @Mixin(StandardChatListener.class)
//#endif
public abstract class ChatListenerMixin {
//#if MC>=11900
    @Inject(method = "handleMessage", at = @At("HEAD"))
    private void beforeHandleChat(MessageSignature messageSignature, BooleanSupplier booleanSupplier, CallbackInfo ci) {
//#else
//$$ @Inject(method = "handle", at = @At("HEAD"))
//$$ private void beforeHandleChat(ChatType chatType, Component message, UUID sender, CallbackInfo ci) {
//#endif
        MessageUtils.setProcessingServerMessageState();
    }

//#if MC>=11900
    @Inject(method = "handleMessage", at = @At("RETURN"))
    private void afterHandleChat(MessageSignature messageSignature, BooleanSupplier booleanSupplier, CallbackInfo ci) {
//#else
//$$ @Inject(method = "handle", at = @At("RETURN"))
//$$ private void afterHandleChat(ChatType chatType, Component message, UUID sender, CallbackInfo ci) {
//#endif
        MessageUtils.resetProcessingServerMessageState();
    }

//#if MC>=11900
    @Inject(method = "handleSystemMessage", at = @At("HEAD"))
    private void beforeHandleSystemChat(Component component, boolean bl, CallbackInfo ci) {
        MessageUtils.setProcessingServerMessageState();
    }

    @Inject(method = "handleSystemMessage", at = @At("RETURN"))
    private void afterHandleSystemChat(Component component, boolean bl, CallbackInfo ci) {
        MessageUtils.resetProcessingServerMessageState();
    }
//#endif
}
