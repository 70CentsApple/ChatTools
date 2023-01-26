package net.apple70cents.chatnotifier.config;

import java.util.ArrayList;
import java.util.List;

public class ModConfigFallback extends ModConfig{
    public boolean modEnabled = true;
    public boolean shouldShowWelcomeMessage = true;

    public static class SoundSettings{
        public boolean soundNotifyEnabled = true;
        public String chatNotifySound = "block.note_block.bit";
        public Float chatNotifyVolume = 1.0F;
        public Float chatNotifyPitch = 1.0F;
    }
    public SoundSettings soundSettings = new SoundSettings();

    public static class ActionbarSettings{
        public boolean actionbarNotifyEnabled = true;
    }
    public ActionbarSettings actionbarSettings = new ActionbarSettings();

    public static class HighlightSettings{
        public boolean highlightEnabled = true;
        public String highlightPrefix = "&a→ &6";
        public boolean enforceOverwriting = false;
    }
    public HighlightSettings highlightSettings = new HighlightSettings();

    public boolean ignoreSelf = true;
    public boolean matchSelfName = true;
    public boolean ignoreSystemMessage = true;
    public boolean toastNotify = false;
    public List<String> allowList = new ArrayList<>(); // 十分肤色正确的变量名
    public List<String> banList = new ArrayList<>(); // 十分肤色正确的变量名
}