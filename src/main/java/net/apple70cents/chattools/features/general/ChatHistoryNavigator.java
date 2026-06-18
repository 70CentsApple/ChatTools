package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.utils.ChatHistoryNavigatorScreen;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.apple70cents.chattools.utils.McUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.gui.screens.ChatScreen;

public class ChatHistoryNavigator {
    public static boolean shouldWork() {
        if (!ConfigUtils.getBoolean("general.ChatHistoryNavigator.Enabled")) {
            return false;
        }
        if (!(McUtils.getScreen() instanceof ChatScreen)) {
            return false;
        }
        return KeyboardUtils.isKeyPressingWithModifier("key.keyboard.f", SpecialUnits.KeyModifiers.CTRL, SpecialUnits.MacroModes.LAZY);
    }

    public static void popupNavigatorScreen() {
        McUtils.setScreen(new ChatHistoryNavigatorScreen(TextUtils.trans("texts.ChatHistoryNavigator.title")));
    }
}
