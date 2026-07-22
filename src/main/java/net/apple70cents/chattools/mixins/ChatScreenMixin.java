package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.general.ChatHistoryNavigator;
import net.apple70cents.chattools.features.translator.Translator;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.ChatComponentDuck;
import net.apple70cents.chattools.utils.CopyFeatureScreen;
import net.apple70cents.chattools.utils.McUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.Font;
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

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} elif >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
//?}
//? if >=1.20.5 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.chat.Style;
//?}

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

//? if >=1.19 {
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
//?}

    @Inject(method = "keyPressed", at = @At("HEAD"))
//? if >=1.21.9 {
    private void keyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
//?} else {
/*private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
*///?}
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

    // A Ctrl-C that has nothing to copy (no text selected in the chat input) is otherwise a no-op;
    // repurpose it to open the Copy Menu for the message under the mouse cursor. This is an
    // alternative to clicking a timestamp, so it works even when timestamps are disabled.
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
//? if >=1.21.9 {
    private void chatTools$copyHoveredOnCtrlC(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (chatTools$tryOpenCopyMenuForHovered(keyEvent.isCopy())) {
            cir.setReturnValue(true);
        }
    }
//?} else {
/*private void chatTools$copyHoveredOnCtrlC(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (chatTools$tryOpenCopyMenuForHovered(net.minecraft.client.gui.screens.Screen.isCopy(keyCode))) {
            cir.setReturnValue(true);
        }
    }
*///?}

    @Unique
    private boolean chatTools$tryOpenCopyMenuForHovered(boolean isCopy) {
        if (!isCopy) {
            return false;
        }
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return false;
        }
        if (!ConfigUtils.getBoolean("general.CopyMenu.CtrlCEnabled")) {
            return false;
        }
        // Only take over when the copy would be a no-op; a real selection must still copy as usual.
        if (this.input == null || !this.input.getHighlighted().isEmpty()) {
            return false;
        }
        Component hovered = ((ChatComponentDuck) McUtils.getChat()).chatTools$hoveredMessageAtCursor();
        if (hovered == null) {
            return false;
        }
        TextUtils.MessageUnit unit = TextUtils.getMessageUnitByVisualMessage(hovered);
        if (unit == null) {
            return false;
        }
        McUtils.setScreen(new CopyFeatureScreen(unit));
        return true;
    }


//? if >=1.20.5 {
    @Unique
    private boolean chatTools$shouldHideChatHistory() {
        return McUtils.isGuiHidden() &&
                ConfigUtils.CHAT_TOOLS_ENABLED &&
                (McUtils.getScreen() instanceof ChatScreen) &&
                ConfigUtils.getBoolean("general.HideChatHistoryInF1Mode");
    }

//? if >=26.1 {
    @WrapWithCondition(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V"))
    private boolean hideChatHistoryInF1Mode_1(ChatComponent instance, GuiGraphicsExtractor guiGraphics, Font font, int i1, int i2, int i3, ChatComponent.DisplayMode m, boolean b) {
//?} elif >=1.21.11 {
    /*@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIZZ)V"))
    private boolean hideChatHistoryInF1Mode_1(ChatComponent instance, GuiGraphics guiGraphics, Font font, int i1, int i2, int i3, boolean b1, boolean b2) {
*///?} else {
    /*@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V"))
    private boolean hideChatHistoryInF1Mode_1(ChatComponent instance, GuiGraphics context, int i1, int i2, int i3, boolean b) {
*///?}
        // if addition conditions are satisfied, don't make it render
        return !chatTools$shouldHideChatHistory();
    }

//? if >=1.21.11 {
//?} else {
/*@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderComponentHoverEffect(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Style;II)V"))
private boolean hideChatHistoryInF1Mode_2(GuiGraphics instance, Font font, Style style, int i, int j) {
         // if addition conditions are satisfied, don't make it render
         return !chatTools$shouldHideChatHistory();
}

//? if >=1.21.6 {
@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"))
//?} else {
/^@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"))
^///?}
private boolean hideChatHistoryInF1Mode_3(GuiGraphics instance, Font font, java.util.List list, int i, int j) {
         // if addition conditions are satisfied, don't make it render
         return !chatTools$shouldHideChatHistory();
}

@Inject(method = "getComponentStyleAt", at = @At(value = "HEAD"), cancellable = true)
private void hideChatHistoryInF1Mode_4(double x, double y, CallbackInfoReturnable<Style> cir) {
         // if addition conditions are satisfied, don't consume its click
         if (chatTools$shouldHideChatHistory()) {
             cir.setReturnValue(null);
         }
}
*///?}

//? if >=1.21.11 {
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void hideChatHistoryInF1Mode_2(Style style, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        // if addition conditions are satisfied, don't consume its click
        if (chatTools$shouldHideChatHistory()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
//?}

//?}
}
