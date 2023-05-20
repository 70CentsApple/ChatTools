package net.apple70cents.chattools.mixin;


import net.apple70cents.chattools.config.ModClothConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow
    @Final
    private static Logger LOGGER;
    private MinecraftClient client;
    ModClothConfig config = ModClothConfig.get();

    @Inject(method = "sendMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void sendMessage(String message, CallbackInfo ci) {
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
                LOGGER.info("[ChatTools] Chat Injected.");
                message = config.injectorString.replace("{text}", message);
            }
        }
        this.client.player.sendChatMessage(message);
        ci.cancel();
    }
}