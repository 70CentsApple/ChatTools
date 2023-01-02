package net.apple70cents.chatnotifier.config;

import net.apple70cents.chatnotifier.ChatNotifier;
import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;
import net.kyrptonaught.kyrptconfig.config.screen.ConfigScreen;
import net.kyrptonaught.kyrptconfig.keybinding.CustomKeyBinding;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

public class ConfigOptions implements AbstractConfigFile {
    public boolean modEnabled = true;

    public CustomKeyBinding openConfigScreenKeybind = CustomKeyBinding.configDefault(ChatNotifier.MODID, InputUtil.UNKNOWN_KEY.getTranslationKey());
    public boolean shouldShowWelcomeMessage = true;
    public boolean chatNotifyEnabled = false;
    public boolean soundNotifyEnabled = true;
    public boolean actionbarNotifyEnabled = true;
    public boolean ignoreSelf = true;
    public boolean matchSelfName = true;
    public boolean ignoreSystemMessage = true;
    /*
    // 已弃用（改成白、黑名单）
    public String chatNotifyRegEx = "[Cc]hat[Nn]otifier";
     */
    public String chatNotifySound = "block.note_block.bit";
    public Float chatNotifyVolume = 1.0F;
    public Float chatNotifyPitch = 1.0F;
    public boolean toastNotify = false;
    public boolean highlightEnabled = true;
    public String highlightPrefix = "&a→ &6";
    public boolean enforceOverwriting = false;
    public List<RegexUnit> allowList = new ArrayList<>(); // 十分肤色正确的变量名
    public List<RegexUnit> banList = new ArrayList<>(); // 十分肤色正确的变量名

    public static class RegexUnit {
        public String value;

        // 初始化
        public RegexUnit(){
            this.value = new TranslatableText("key.chatnotifier.regex.placeholder").getString();
        }
    }

    public static Screen buildScreen(Screen screen) {
        ConfigScreen configScreen = new ConfigScreen(screen, new TranslatableText("key.chatnotifier.configScreen"));
        return screen;
    }
}
