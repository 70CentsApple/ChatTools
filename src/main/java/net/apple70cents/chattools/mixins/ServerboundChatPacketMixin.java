package net.apple70cents.chattools.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.3 {
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
//?}

@Mixin(ServerboundChatPacket.class)
public abstract class ServerboundChatPacketMixin {
//? if >=26.3 {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/ByteBufCodecs;stringUtf8(I)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<ByteBuf, String> changeMessageCodec(StreamCodec<ByteBuf, String> original) {
        return new StreamCodec<ByteBuf, String>() {
            @Override
            public String decode(ByteBuf input) {
                return original.decode(input);
            }

            @Override
            public void encode(ByteBuf output, String value) {
                if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
                    original.encode(output, value);
                } else if (!ConfigUtils.INCREASE_CHAT_FIELD_MAX_LENGTH_ENABLED) {
                    original.encode(output, value);
                } else {
                    Utf8String.write(output, value, Short.MAX_VALUE);
                }
            }
        };
    }
//?} else {
    /*@ModifyExpressionValue(
//? if >=1.19 {
        method = "write"
//?} else {
        /^method = "<init>(Ljava/lang/String;)V"
^///?}
        , at = @At(value = "CONSTANT", args = "intValue=256")
    )
    private int increaseMaxLength(int endIndex) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return endIndex;
        }
        if (!ConfigUtils.INCREASE_CHAT_FIELD_MAX_LENGTH_ENABLED) {
            return endIndex;
        }
        return Short.MAX_VALUE;
    }
*///?}
}
