package net.apple70cents.chattools.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;

public class McUtils {
    public static void setScreen(Screen screen) {
//? if >=26.2 {
        Minecraft.getInstance().gui.setScreen(screen);
//? } else {
        /*Minecraft.getInstance().setScreen(screen);
*///? }
    }

    public static void setOverlay(Overlay overlay) {
//? if >=26.2 {
        Minecraft.getInstance().gui.setOverlay(overlay);
//? } else {
        /*Minecraft.getInstance().setOverlay(overlay);
*///? }
    }

    public static Screen getScreen() {
//? if >=26.2 {
        return Minecraft.getInstance().gui.screen();
//? } else {
        /*return Minecraft.getInstance().screen;
*///? }
    }

    public static Overlay getOverlay() {
//? if >=26.2 {
        return Minecraft.getInstance().gui.overlay();
//? } else {
        /*return Minecraft.getInstance().getOverlay();
*///? }
    }

    public static ChatComponent getChat() {
//? if >=26.2 {
        return Minecraft.getInstance().gui.hud.getChat();
//? } else {
        /*return Minecraft.getInstance().gui.getChat();
         *///? }
    }

    public static boolean isGuiHidden() {
//? if >=26.2 {
        return Minecraft.getInstance().gui.hud.isHidden();
//? } else {
        /*return Minecraft.getInstance().options.hideGui;
         *///? }
    }
}
