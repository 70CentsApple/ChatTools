package net.apple70cents.chattools.features.chatkeybindings;

import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.mixins.ScreenAccessor;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.apple70cents.chattools.utils.McUtils;
import net.minecraft.client.gui.screens.ChatScreen;

public class ReviewLastMessageWithUpArrowOnly {
    private static final String UP = "key.keyboard.up";

    public static void tick() {
        if (!ConfigUtils.REVIEW_LAST_MESSAGE_WITH_UP_ARROW_ONLY_ENABLED) {
            return;
        }
        if (KeyboardUtils.isKeyPressingWithModifier(UP, SpecialUnits.KeyModifiers.NONE,
                SpecialUnits.MacroModes.GREEDY)) {
            // only if no screen is open
            if (McUtils.getScreen() != null) {
                return;
            }
//? if >=1.21.9 {
            ChatScreen chatScreen = new ChatScreen("", false);
//?} else {
            /*ChatScreen chatScreen = new ChatScreen("");
*///?}
            ((ScreenAccessor) chatScreen).invokeInit(
//? if >=1.21.11 {
//?} else {
                    /*net.minecraft.client.Minecraft.getInstance(),
*///?}
                    1, 1);
            McUtils.setScreen(chatScreen);
            chatScreen.moveInHistory(-1);
        }
    }
}
