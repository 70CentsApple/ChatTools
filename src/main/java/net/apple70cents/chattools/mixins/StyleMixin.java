package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public abstract class StyleMixin {
    @Inject(method = "isObfuscated", at = @At("HEAD"), cancellable = true)
    public void disableTextObfuscation(CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (!ConfigUtils.DISABLE_TEXT_OBFUSCATION_ENABLED) {
            return;
        }
        cir.setReturnValue(false);
    }
}
