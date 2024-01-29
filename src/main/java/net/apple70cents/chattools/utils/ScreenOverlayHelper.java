package net.apple70cents.chattools.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

/**
 * @author 70CentsApple
 */
public abstract class ScreenOverlayHelper extends Overlay {

    private final Overlay oldOverlay;
    private final MinecraftClient client;
    private final Screen screenToOpen;

    public ScreenOverlayHelper(MinecraftClient client, Screen screenToOpen) {
        this.client = client;
        this.oldOverlay = client.getOverlay();
        this.screenToOpen = screenToOpen;
    }

    public void render(MatrixStack context, int mouseX, int mouseY, float delta) {
        if (client.currentScreen == null) {
            client.setScreen(screenToOpen);
            client.setOverlay(oldOverlay);
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (client.currentScreen == null) {
            client.setScreen(screenToOpen);
            client.setOverlay(oldOverlay);
        }
    }
}
