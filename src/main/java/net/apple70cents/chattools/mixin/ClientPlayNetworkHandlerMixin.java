package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ModClothConfig;
import net.apple70cents.chattools.features.chatnotifier.ChatNotifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.regex.Pattern;

@Mixin(net.minecraft.client.network.ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    ModClothConfig config = ModClothConfig.get();

    @ModifyVariable(method = "sendChatMessage", at = @At("HEAD"), argsOnly = true)
    public String sendMessage(String message) {
        ChatNotifier.setJustSentMessage(true);
        if (config.injectorEnabled) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            boolean shouldMatch = true;
            for (String s : config.injectorBanList) {
                if (Pattern.compile(s).matcher(message).matches()) {
                    shouldMatch = false;
                    break;
                }
            }
            if (shouldMatch) {
                ChatTools.LOGGER.info("[ChatTools] Chat Formatted.");
                message = config.injectorString.replace("{text}", message);
            }
            if (player != null) {
                message = message.replace("{pos}", String.format("(%d,%d,%d)", (int) player.getX(), (int) player.getY(), (int) player.getZ()));
            }
        }
        return message;
    }

}
