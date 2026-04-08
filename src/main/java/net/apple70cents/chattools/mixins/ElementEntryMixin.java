package net.apple70cents.chattools.mixins;

import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

// A Cloth Config Mixin
@Mixin(DynamicElementListWidget.ElementEntry.class)
public abstract class ElementEntryMixin {

    @Shadow
    @Nullable
    private GuiEventListener focused;

    @Inject(method = "setFocused", at = @At("HEAD"), cancellable = true)
    private void onSetFocused(@Nullable GuiEventListener guiEventListener, CallbackInfo ci) {
        if (this.focused == guiEventListener) {
            ci.cancel();
        }
    }
}