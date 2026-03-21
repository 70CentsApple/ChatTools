package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.general.NickHider;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

//#if MC>=12109
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
@Mixin(NameTagFeatureRenderer.Storage.class)
//#else
//$$ @Mixin(net.minecraft.client.renderer.entity.EntityRenderer.class)
//#endif
public abstract class NameTagFeatureRendererStorageMixin {
//#if MC>=12109
    @ModifyVariable(method = "add", at = @At(value = "HEAD", ordinal = 0), argsOnly = true)
//#else
//$$ @ModifyVariable(method = "renderNameTag", at = @At(value = "HEAD", ordinal = 0), argsOnly = true)
//#endif
    public Component nickHiderChangeLabel(Component text) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return text;
        } else if (!ConfigUtils.NICK_HIDER_ENABLED) {
            return text;
        }
        return NickHider.work(text);
    }
}
