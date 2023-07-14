package net.apple70cents.chattools.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.apple70cents.chattools.ChatTools.config;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"), cancellable = true)
    public void restoreMessages(CallbackInfo ci) {
        if (!config.restoreMessagesEnabled) {
            return;
        }
        if (config.shouldShowRestoreMessagesText) {
            if (MinecraftClient.getInstance().player == null) {
                return;
            }
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("key.chattools.restoredMessages").formatted(Formatting.GRAY));
        }
        ci.cancel(); // 取消清除聊天记录
    }
}
