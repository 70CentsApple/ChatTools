package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ModClothConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.regex.Pattern;

@Mixin(net.minecraft.client.network.ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandler {
    ModClothConfig config = ModClothConfig.get();

    @ModifyVariable(method = "sendChatMessage", at = @At("HEAD"), argsOnly = true)
    public String sendMessage(String message) {
        if (!config.injectorEnabled) {
        } else {
            boolean shouldMatch = true;
            for (String s : config.injectorBanList) {
                if (Pattern.compile(s).matcher(message).matches()) {
                    shouldMatch = false;
                    break;
                }
            }
            if (!shouldMatch) {
            } else {
                ChatTools.LOGGER.info("[ChatTools] Chat Injected.");
                message = config.injectorString.replace("{text}", message);
            }
        }
        return message;
    }

}
