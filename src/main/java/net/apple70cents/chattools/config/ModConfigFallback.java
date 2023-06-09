package net.apple70cents.chattools.config;

import net.apple70cents.chattools.features.quickchat.MacroChat;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public class ModConfigFallback extends ModClothConfig{
    public boolean modEnabled = true;
    public boolean displayChatTimeEnabled = true;
    public static class NickHiderSettings{
        public boolean nickHiderEnabled = false;
        public String nickHiderText = "&6You&r";
    }
    public NickHiderSettings nickHiderSettings = new NickHiderSettings();
    public boolean shouldShowWelcomeMessage = true;

    public static class SoundSettings{
        public boolean soundNotifyEnabled = true;
        public String chatNotifySound = "block.note_block.bit";
        public int chatNotifyVolume = 80;
        public int chatNotifyPitch = 10;
    }
    public SoundSettings soundSettings = new SoundSettings();

    public static class ActionbarSettings{
        public boolean actionbarNotifyEnabled = true;
    }
    public ActionbarSettings actionbarSettings = new ActionbarSettings();

    public static class HighlightSettings{
        public boolean highlightEnabled = true;
        public String highlightPrefix = "&a→ &r";
        public boolean enforceOverwriting = false;
    }
    public HighlightSettings highlightSettings = new HighlightSettings();

    public boolean ignoreSelf = true;
    public boolean matchSelfName = true;
    public boolean ignoreSystemMessage = true;
    public boolean toastNotify = false;
    public List<String> allowList = new ArrayList<>(); // 十分肤色正确的变量名
    public List<String> banList = new ArrayList<>(); // 十分肤色正确的变量名

    public boolean injectorEnabled = false;
    public String injectorString = "I said {text} ~";
    public List<String> injectorBanList = new ArrayList<>() {{
        add("^\\d+$|^[.#%$/].*|\\ball\\b");
    }};

    public String quickRepeatKey = InputUtil.UNKNOWN_KEY.getTranslationKey();
    public CustomModifier quickRepeatKeyModifier = CustomModifier.NONE;
    public boolean macroChatEnabled = true;
    public List<MacroChat.MacroUnit> macroChatList = new ArrayList<>();

    public boolean chatBubblesEnabled = true;
    public long chatBubblesLifetime = 8;
    public int chatBubblesYOffset = 3;
}