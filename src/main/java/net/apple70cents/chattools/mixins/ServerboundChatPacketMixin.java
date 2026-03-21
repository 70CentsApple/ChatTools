package net.apple70cents.chattools.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerboundChatPacket.class)
public abstract class ServerboundChatPacketMixin {
    @ModifyExpressionValue(
//#if MC>=11900
        method = "write"
//#else
//$$    method = "<init>(Ljava/lang/String;)V"
//#endif
        , at = @At(value = "CONSTANT", args = "intValue=256")
    )
    private int increaseMaxLength(int endIndex) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return 256;
        }
        if (!ConfigUtils.INCREASE_CHAT_FIELD_MAX_LENGTH_ENABLED) {
            return 256;
        }
        return Integer.MAX_VALUE;
    }
}
