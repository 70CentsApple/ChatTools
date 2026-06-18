package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.general.NickHider;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

//? if >=26.2 {
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
@Mixin(NameTagFeatureRenderer.Submit.class)
//?} elif >=1.21.9 {
/*import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
@Mixin(NameTagFeatureRenderer.Storage.class)
*///?} else {
/*@Mixin(net.minecraft.client.renderer.entity.EntityRenderer.class)
*///?}
public abstract class NameTagFeatureRendererStorageMixin {
//? if >=26.2 {
    @ModifyVariable(method = "<init>", at = @At(value = "HEAD", ordinal = 0), argsOnly = true)
//?} elif >=1.21.9 {
    /*@ModifyVariable(method = "add", at = @At(value = "HEAD", ordinal = 0), argsOnly = true)
*///?} else {
    /*@ModifyVariable(method = "renderNameTag", at = @At(value = "HEAD", ordinal = 0), argsOnly = true)
*///?}
    private static Component nickHiderChangeLabel(Component text) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return text;
        } else if (!ConfigUtils.NICK_HIDER_ENABLED) {
            return text;
        }
        return NickHider.work(text);
    }
}
