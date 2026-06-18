package net.apple70cents.chattools.features.chatkeybindings;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.utils.*;
import net.minecraft.client.Minecraft;

import java.util.List;

public class Repeat {
    private static boolean keyWasPressed;

    public static void tick() {
        String key = ConfigUtils.REPEAT_KEY;
        SpecialUnits.KeyModifiers modifier = ConfigUtils.REPEAT_KEY_MODIFIER;
        if (KeyboardUtils.isKeyPressingWithModifier(key, modifier, SpecialUnits.MacroModes.GREEDY) &&
                McUtils.getScreen() == null) {
            if (!keyWasPressed) {
                keyWasPressed = true;
                LoggerUtils.info("[ChatTools] Triggered the latest command.");
                Minecraft mc = Minecraft.getInstance();
                List<String> history = McUtils.getChat().getRecentChat();
                if (history.isEmpty()) {
                    MessageUtils.sendToActionbar(TextUtils.trans("texts.repeat.failure.empty"));
                } else {
                    MessageUtils.sendToPublicChat(history.get(history.size() - 1));
                }
            }
        } else {
            keyWasPressed = false;
        }
    }
}
