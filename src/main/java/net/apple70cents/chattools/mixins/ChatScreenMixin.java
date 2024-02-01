package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.general.Translator;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Inject(at = @At("HEAD"), method = "keyPressed")
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return;
        }
        if (Translator.shouldWork()) {
            Translator.work(chatField);
        }
    }

}
