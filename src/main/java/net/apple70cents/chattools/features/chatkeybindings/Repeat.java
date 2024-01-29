package net.apple70cents.chattools.features.chatkeybindings;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class Repeat {
    private static boolean keyWasPressed;

    public static void tick() {
        String key = (String) ChatTools.CONFIG.get("chatkeybindings.RepeatKey");
        SpecialUnits.KeyModifiers modifier = SpecialUnits.KeyModifiers.valueOf((String) ChatTools.CONFIG.get("chatkeybindings.RepeatKeyModifier"));
        if (KeyboardUtils.isKeyPressedWithModifier(key, modifier, SpecialUnits.MacroModes.GREEDY) && MinecraftClient.getInstance().currentScreen == null) {
            if (!keyWasPressed) {
                keyWasPressed = true;
                LoggerUtils.info("[ChatTools] Triggered the latest command.");
                MinecraftClient mc = MinecraftClient.getInstance();
                List<String> history = mc.inGameHud.getChatHud().getMessageHistory();
                if (history.isEmpty()) {
                    MessageUtils.sendToActionbar(TextUtils.trans("texts.repeat.failure"));
                } else {
                    MessageUtils.sendToPublicChat(history.get(history.size() - 1));
                }
            }
        } else {
            keyWasPressed = false;
        }
    }
}
