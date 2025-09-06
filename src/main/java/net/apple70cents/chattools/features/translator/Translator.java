package net.apple70cents.chattools.features.translator;

import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;


public class Translator {
    public static boolean shouldWork() {
        if (!(boolean) ConfigUtils.get("translator.Translator.Enabled")) {
            return false;
        }
        if (!(Minecraft.getInstance().screen instanceof ChatScreen)) {
            return false;
        }
        return KeyboardUtils.isKeyPressingWithModifier("key.keyboard.tab", SpecialUnits.KeyModifiers.SHIFT, SpecialUnits.MacroModes.LAZY);
    }

    public static void work(EditBox chat) {
        switch ((String) ConfigUtils.get("translator.Translator.Mode")) {
            case "BUILTIN":
                BuiltinTranslator.work(chat);
                break;
            case "BAIDU":
                BaiduTranslator.translate(chat);
                break;
            default:
                return;
        }
    }
}
