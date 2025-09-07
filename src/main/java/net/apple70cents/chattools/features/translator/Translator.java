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
        if (chat.getValue().isBlank()) {
            return;
        }
        switch ((String) ConfigUtils.get("translator.Translator.Mode")) {
            case "BUILTIN":
                String api = (String) ConfigUtils.get("translator.Translator.Builtin.API");
                boolean usePost = (boolean) ConfigUtils.get("translator.Translator.Builtin.PostInstead");
                new BuiltinTranslator(chat, api, usePost).work();
                break;
            case "BAIDU":
                String appId = (String) ConfigUtils.get("translator.Translator.Baidu.Appid");
                String key = (String) ConfigUtils.get("translator.Translator.Baidu.Appkey");
                String from = (String) ConfigUtils.get("translator.Translator.Baidu.from");
                String to = (String) ConfigUtils.get("translator.Translator.Baidu.to");
                new BaiduTranslator(chat, appId, key, from, to).work();
                break;
            default:
                return;
        }
    }
}
