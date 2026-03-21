package net.apple70cents.chattools.features.chatkeybindings;

import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.mixins.ScreenAccessor;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

public class ReviewLastMessageWithUpArrowOnly {
    private static final String UP = "key.keyboard.up";

    public static void tick() {
        if (!ConfigUtils.REVIEW_LAST_MESSAGE_WITH_UP_ARROW_ONLY_ENABLED) {
            return;
        }
        if (KeyboardUtils.isKeyPressingWithModifier(UP, SpecialUnits.KeyModifiers.NONE,
                SpecialUnits.MacroModes.GREEDY)) {
            Minecraft mc = Minecraft.getInstance();
            // only if no screen is open
            if (mc.screen != null) {
                return;
            }
//#if MC>=12109
            ChatScreen chatScreen = new ChatScreen("", false);
//#else
//$$        ChatScreen chatScreen = new ChatScreen("");
//#endif
            ((ScreenAccessor) chatScreen).invokeInit(
//#if MC>=12111
//$$                // no-op
//#else
//$$                mc,
//#endif
                    1, 1);
            mc.setScreen(chatScreen);
            chatScreen.moveInHistory(-1);
        }
    }
}
