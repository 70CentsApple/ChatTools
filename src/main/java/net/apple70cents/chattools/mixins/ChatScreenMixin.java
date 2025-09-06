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

//#if MC>=12005
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.chat.Style;
import java.util.List;
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
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return;
        }
        if (!(boolean) ConfigUtils.get("general.IncreaseChatFieldMaxLength")) {
            return;
        }
        input.setMaxLength(Integer.MAX_VALUE);
    }

    //#if MC>=11900
    @Inject(method = "normalizeChatMessage", at = @At("HEAD"), cancellable = true)
    private void doNotTruncate(String text, CallbackInfoReturnable<String> cir) {
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return;
        }
        if (!(boolean) ConfigUtils.get("general.IncreaseChatFieldMaxLength")) {
            return;
        }
        cir.setReturnValue(StringUtils.normalizeSpace(text.trim()));
    }
    //#endif

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
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
                (boolean) ConfigUtils.get("general.ChatTools.Enabled") &&
                (Minecraft.getInstance().screen instanceof ChatScreen) &&
                (boolean) ConfigUtils.get("general.HideChatHistoryInF1Mode");
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V"))
    private boolean hideChatHistoryInF1Mode_1(ChatComponent instance, GuiGraphics context, int i1, int i2, int i3, boolean b) {
        // if addition conditions are satisfied, don't make it render
        return !shouldHideChatHistory();
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderComponentHoverEffect(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Style;II)V"))
    private boolean hideChatHistoryInF1Mode_2(GuiGraphics instance, Font font, Style style, int i, int j) {
        // if addition conditions are satisfied, don't make it render
        return !shouldHideChatHistory();
    }

    //#if MC>=12007
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"))
    //#else
    //$$ @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"))
    //#endif
    private boolean hideChatHistoryInF1Mode_3(GuiGraphics instance, Font font, List list, int i, int j) {
        // if addition conditions are satisfied, don't make it render
        return !shouldHideChatHistory();
    }

    @Inject(method = "getComponentStyleAt", at = @At(value = "HEAD"), cancellable = true)
    private void hideChatHistoryInF1Mode_4(double x, double y, CallbackInfoReturnable<Style> cir) {
        // if addition conditions are satisfied, don't consume its click
        if (shouldHideChatHistory()) {
            cir.setReturnValue(null);
        }
    }
    //#endif
}
