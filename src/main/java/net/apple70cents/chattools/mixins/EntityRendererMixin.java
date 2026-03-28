package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.bubble.BubbleRenderer;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?} elif >=1.21.9 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
*///?}

//? if >=1.21.2 {
import net.minecraft.client.renderer.entity.state.EntityRenderState;
//?}

/**
 * @author 70CentsApple
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
//? if >=1.21.2 {
    Entity entity;
    float tickDelta;

    @Inject(method = "extractRenderState", at = @At(value = "HEAD"))
    private void updateEntityAndTickDelta(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        this.entity = entity;
        this.tickDelta = tickDelta;
    }
//?}

//? if >=1.21.9 {
    @Inject(method = "submit", at = @At(value = "HEAD"))
    private void submit(EntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        MultiBufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        if (entityRenderState instanceof AvatarRenderState && Minecraft.getInstance().level != null) {
            entity = Minecraft.getInstance().level.getEntity(((AvatarRenderState) entityRenderState).id);
        }
//?} elif >=1.21.2 {
/*@Inject(method = "render", at = @At(value = "HEAD"))
private void render(EntityRenderState entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
*///?} else {
/*@Inject(method = "render", at = @At(value = "HEAD"))
private void render(Entity entity, float yaw, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
*///?}
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (ConfigUtils.BUBBLE_ENABLED) {
            BubbleRenderer.render(entity, poseStack, multiBufferSource, tickDelta
//? if >=1.21.9 {
                    , submitNodeCollector
//?}
            );
        }
    }
}
