package net.apple70cents.chattools;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class ScreenOverlay extends Overlay {

    private final Overlay oldOverlay;
    private final MinecraftClient client;
    private final Screen screenToOpen;
    public ScreenOverlay(MinecraftClient client, Screen screenToOpen) {
        this.client = client;
        this.oldOverlay = client.getOverlay();
        this.screenToOpen = screenToOpen;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (client.currentScreen == null) {
            client.setScreen(screenToOpen);
            client.setOverlay(oldOverlay);
        }
    }
}
