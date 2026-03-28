package net.apple70cents.chattools.features.chatkeybindings;

import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.minecraft.client.Minecraft;

import java.util.HashSet;
import java.util.Set;


public class Macro {
    static Set<SpecialUnits.MacroUnit> keyWasPressed = new HashSet<>();

    public static void tick() {
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        for (SpecialUnits.MacroUnit macro : ConfigUtils.MACRO_LIST) {
            if (KeyboardUtils.isKeyPressingWithModifier(macro.key, macro.modifier, macro.mode)) {
                if (!keyWasPressed.contains(macro)) {
                    keyWasPressed.add(macro);
                    LoggerUtils.info("[ChatTools] Triggered Macro: " + macro.commands);
                    for (SpecialUnits.MacroCommandEntry entry : macro.commands) {
                        MessageUtils.sendToPublicChatScheduled(entry.command, entry.forceDisableFormatter,
                                entry.delayInMilliseconds);
                    }
                }
            } else {
                keyWasPressed.remove(macro);
            }
        }
    }
}
