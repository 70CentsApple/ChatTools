package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.general.ChatHistoryNavigator;
import net.apple70cents.chattools.features.translator.Translator;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC>=12109
import net.minecraft.client.input.KeyEvent;
//#endif
//#if MC>=12005
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.chat.Style;
//#endif

/**
 * @author 70CentsApple
 */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow
    protected EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    private void increaseChatFieldMaxLength(CallbackInfo ci) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (!ConfigUtils.INCREASE_CHAT_FIELD_MAX_LENGTH_ENABLED) {
            return;
        }
        input.setMaxLength(Integer.MAX_VALUE);
    }

//#if MC>=11900
    @Inject(method = "normalizeChatMessage", at = @At("HEAD"), cancellable = true)
    private void doNotTruncate(String text, CallbackInfoReturnable<String> cir) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (!ConfigUtils.INCREASE_CHAT_FIELD_MAX_LENGTH_ENABLED) {
            return;
        }
        cir.setReturnValue(StringUtils.normalizeSpace(text.trim()));
    }
//#endif

    @Inject(method = "keyPressed", at = @At("HEAD"))
//#if MC>=12109
    private void keyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
//#else
//$$ private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
//#endif
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (Translator.shouldWork()) {
            Translator.work(input);
        }
        if (ChatHistoryNavigator.shouldWork()) {
            ChatHistoryNavigator.popupNavigatorScreen();
        }
    }


//#if MC>=12005
    @Unique
    private boolean shouldHideChatHistory() {
        return Minecraft.getInstance().options.hideGui &&
                ConfigUtils.CHAT_TOOLS_ENABLED &&
                (Minecraft.getInstance().screen instanceof ChatScreen) &&
                (boolean) ConfigUtils.get("general.HideChatHistoryInF1Mode");
    }

//#if MC>=12111
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIZZ)V"))
    private boolean hideChatHistoryInF1Mode_1(ChatComponent instance, GuiGraphics guiGraphics, Font font, int i1, int i2, int i3, boolean b1, boolean b2) {
//#else
//$$ @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V"))
//$$ private boolean hideChatHistoryInF1Mode_1(ChatComponent instance, GuiGraphics context, int i1, int i2, int i3, boolean b) {
//#endif
        // if addition conditions are satisfied, don't make it render
        return !shouldHideChatHistory();
    }

//#if MC>=12111
//$$ // no-op
//#else
//$$ @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderComponentHoverEffect(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Style;II)V"))
//$$ private boolean hideChatHistoryInF1Mode_2(GuiGraphics instance, Font font, Style style, int i, int j) {
//$$     // if addition conditions are satisfied, don't make it render
//$$     return !shouldHideChatHistory();
//$$ }
//$$
//$$ //#if MC>=12007
//$$ @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"))
//$$ //#else
//$$ //$$ @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"))
//$$ //#endif
//$$ private boolean hideChatHistoryInF1Mode_3(GuiGraphics instance, Font font, java.util.List list, int i, int j) {
//$$     // if addition conditions are satisfied, don't make it render
//$$     return !shouldHideChatHistory();
//$$ }
//$$
//$$ @Inject(method = "getComponentStyleAt", at = @At(value = "HEAD"), cancellable = true)
//$$ private void hideChatHistoryInF1Mode_4(double x, double y, CallbackInfoReturnable<Style> cir) {
//$$     // if addition conditions are satisfied, don't consume its click
//$$     if (shouldHideChatHistory()) {
//$$         cir.setReturnValue(null);
//$$     }
//$$ }
//#endif

//#if MC>=12111
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void hideChatHistoryInF1Mode_2(Style style, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        // if addition conditions are satisfied, don't consume its click
        if (shouldHideChatHistory()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
//#endif

//#endif
}
