package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.config.ModClothConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    ModClothConfig config = ModClothConfig.get();

    @ModifyArgs(method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    public void nickHiderChangeLabel(Args args) {
        AbstractClientPlayerEntity player = args.get(0);
        if (player != MinecraftClient.getInstance().player) {
            return;
        } else if (!config.nickHiderSettings.nickHiderEnabled) {
            return;
        }
        args.set(1, Text.translatable(config.nickHiderSettings.nickHiderText.replace('&', '§').replace("\\§", "&")));
    }
}
