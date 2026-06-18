package net.apple70cents.chattools.features.translator;

import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.apple70cents.chattools.utils.McUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;


public class Translator {
    public static boolean shouldWork() {
        if (!ConfigUtils.getBoolean("translator.Enabled")) {
            return false;
        }
        if (!(McUtils.getScreen() instanceof ChatScreen)) {
            return false;
        }
        return KeyboardUtils.isKeyPressingWithModifier("key.keyboard.tab", SpecialUnits.KeyModifiers.SHIFT, SpecialUnits.MacroModes.LAZY);
    }

    public static void work(EditBox chat) {
        if (chat.getValue().isBlank()) {
            return;
        }
        switch (ConfigUtils.getString("translator.Mode")) {
            case "BUILTIN":
                String api = ConfigUtils.getString("translator.Builtin.API");
                boolean usePost = ConfigUtils.getBoolean("translator.Builtin.PostInstead");
                new BuiltinTranslator(chat, api, usePost).work();
                break;
            case "BAIDU":
                String baiduAppId = ConfigUtils.getString("translator.Baidu.Appid");
                String baiduKey = ConfigUtils.getString("translator.Baidu.Appkey");
                String baiduFrom = ConfigUtils.getString("translator.Baidu.from");
                String baiduTo = ConfigUtils.getString("translator.Baidu.to");
                new BaiduTranslator(chat, baiduAppId, baiduKey, baiduFrom, baiduTo).work();
                break;
            case "MICROSOFT_FREE":
                String microsoftFreeFrom = ConfigUtils.getString("translator.MicrosoftFree.from");
                String microsoftFreeTo = ConfigUtils.getString("translator.MicrosoftFree.to");
                new MicrosoftFreeTranslator(chat, microsoftFreeFrom, microsoftFreeTo).work();
                break;
            case "GOOGLE_FREE":
                String googleFreeSl = ConfigUtils.getString("translator.GoogleFree.sl");
                String googleFreeTl = ConfigUtils.getString("translator.GoogleFree.tl");
                new GoogleFreeTranslator(chat, googleFreeSl, googleFreeTl).work();
                break;
            default:
                return;
        }
    }
}
