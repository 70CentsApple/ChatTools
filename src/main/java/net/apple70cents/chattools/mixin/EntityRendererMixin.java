package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.config.ModClothConfig;
import net.apple70cents.chattools.features.chatbubbles.BubbleRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "render", at = @At(value = "HEAD"))
    private void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if(ModClothConfig.get().chatBubblesEnabled){
            BubbleRenderer.render(entity,matrices,vertexConsumers);
        }
    }
}
