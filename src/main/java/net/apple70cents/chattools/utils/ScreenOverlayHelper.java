package net.apple70cents.chattools.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} elif >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

/**
 * @author 70CentsApple
 */
public abstract class ScreenOverlayHelper extends Overlay {

    private final Overlay oldOverlay;
    private final Minecraft client;
    private final Screen screenToOpen;

    public ScreenOverlayHelper(Minecraft client, Screen screenToOpen) {
        this.client = client;
        this.oldOverlay = client.getOverlay();
        this.screenToOpen = screenToOpen;
    }

//? if >=26.1 {
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
//?} elif >=1.20 {
    /*public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
*///?} else {
    /*public void render(PoseStack context, int mouseX, int mouseY, float delta) {
*///?}
        if (client.screen == null) {
            client.setScreen(screenToOpen);
            client.setOverlay(oldOverlay);
        }
    }
}
