package net.apple70cents.chattools.features.chatkeybindings;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.minecraft.client.MinecraftClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Macro {
    static Set<SpecialUnits.MacroUnit> keyWasPressed = new HashSet<>();

    public static void tick() {
        if (MinecraftClient.getInstance().currentScreen != null) {
            return;
        }
        for (SpecialUnits.MacroUnit macro : SpecialUnits.MacroUnit.fromList((List) ChatTools.CONFIG.get("chatkeybindings.Macro.List"))) {
            if (KeyboardUtils.isKeyPressedWithModifier(macro.key, macro.modifier, macro.mode)) {
                if (!keyWasPressed.contains(macro)) {
                    keyWasPressed.add(macro);
                    LoggerUtils.info("[ChatTools] Triggered Macro: " + macro.command);
                    MessageUtils.sendToPublicChat(macro.command);
                }
            } else {
                keyWasPressed.remove(macro);
            }
        }
    }
}
