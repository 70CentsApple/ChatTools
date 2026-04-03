package net.apple70cents.chattools.mixins;

import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//? if >= 1.21.4 {
import me.shedaniel.math.Rectangle;

// fixes https://github.com/shedaniel/cloth-config/issues/300
@Mixin(MultiElementListEntry.CategoryLabelWidget.class)
public abstract class CategoryLabelWidgetMixin {

    @Shadow(remap = false)
    private Rectangle rectangle;

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.rectangle.contains(mouseX, mouseY);
    }
}
//? } else {
/*// don't do anything
@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.Minecraft.class)
public abstract class CategoryLabelWidgetMixin {}
*///? }