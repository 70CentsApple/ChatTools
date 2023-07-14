package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ModClothConfig;
import net.apple70cents.chattools.features.chatnotifier.ChatNotifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.regex.Pattern;

@Mixin(net.minecraft.client.network.ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    ModClothConfig config = ModClothConfig.get();

    @ModifyVariable(method = "sendChatMessage", at = @At("HEAD"), argsOnly = true)
    public String sendMessage(String message) {
        if (!config.modEnabled) {
            return message;
        }
        ChatNotifier.setJustSentMessage(true);
        if (config.injectorEnabled) {
            String formatter = "{text}";
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            boolean shouldMatch = true; // 假设应该匹配
            for (String s : config.injectorBanList) {
                if (Pattern.compile(s).matcher(message).matches()) {
                    shouldMatch = false; // 在黑名单里 所以不能匹配
                    break;
                }
            }
            boolean serverAddressPass = false;
            for (ModClothConfig.InjectorUnit unit : config.injectorList) {
                if ("*".equals(unit.getAddress())) {
                    serverAddressPass = true;
                    formatter = unit.getFormatter();
                    break;
                } else if (client.getCurrentServerEntry() == null) { // 单人世界
                    // Do Nothing
                } else if (Pattern.compile(unit.getAddress()).matcher(client.getCurrentServerEntry().address).matches()) {
                    serverAddressPass = true;
                    formatter = unit.getFormatter();
                    break;
                }
            }
            shouldMatch = shouldMatch & serverAddressPass;
            if (shouldMatch) {
                ChatTools.LOGGER.info("[ChatTools] Chat Formatted.");
                message = formatter.replace("{text}", message);
            }
            if (player != null) {
                message = message.replace("{pos}", String.format("(%d,%d,%d)", (int) player.getX(), (int) player.getY(), (int) player.getZ()));
            }
        }
        return message;
    }

}
