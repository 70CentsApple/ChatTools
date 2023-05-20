package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ModClothConfig;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    ModClothConfig config = ModClothConfig.get();

    @ModifyVariable(method = "sendChatMessage",at = @At("HEAD"),argsOnly = true)
    public String sendChatMessage(String message){
        if (!config.injectorEnabled) {
        } else {
            boolean shouldMatch = true;
            for (String s:config.injectorBanList) {
                if(Pattern.compile(s).matcher(message).matches()){
                    shouldMatch = false;
                    break;
                }
            }
            if(!shouldMatch){
            } else {
                ChatTools.LOGGER.info("[ChatTools] Chat Injected.");
                message = config.injectorString.replace("{text}", message);
            }
        }
        return message;
    }

}
