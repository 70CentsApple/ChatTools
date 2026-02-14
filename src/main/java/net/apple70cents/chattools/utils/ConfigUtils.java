package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.config.ConfigStorage;
import net.apple70cents.chattools.config.SpecialUnits;

public class ConfigUtils {
    public final static ConfigStorage DEFAULT_CONFIG = new ConfigStorage(true);
    public static ConfigStorage CONFIG;

    // Cached config values for frequently-used keys
    public static boolean CHAT_TOOLS_ENABLED = false;
    public static boolean DISABLE_TEXT_OBFUSCATION_ENABLED = false;
    public static boolean BUBBLE_ENABLED = false;
    public static boolean NICK_HIDER_ENABLED = false;
    public static boolean EXCLUSIVE_ACTIONBAR_ENABLED = false;
    public static boolean PREVIEW_CLICK_EVENTS_ENABLED = false;
    public static boolean INCREASE_CHAT_FIELD_MAX_LENGTH_ENABLED = false;
    public static boolean MACRO_ENABLED = false;
    public static boolean REVIEW_LAST_MESSAGE_WITH_UP_ARROW_ONLY_ENABLED = false;
    public static String REPEAT_KEY = "key.keyboard.unknown";
    public static SpecialUnits.KeyModifiers REPEAT_KEY_MODIFIER = SpecialUnits.KeyModifiers.NONE;

    public static void refreshCache() {
        CHAT_TOOLS_ENABLED = (boolean) CONFIG.get("general.ChatTools.Enabled");
        DISABLE_TEXT_OBFUSCATION_ENABLED = (boolean) CONFIG.get("general.DisableTextObfuscation.Enabled");
        BUBBLE_ENABLED = (boolean) CONFIG.get("bubble.Enabled");
        NICK_HIDER_ENABLED = (boolean) CONFIG.get("general.NickHider.Enabled");
        EXCLUSIVE_ACTIONBAR_ENABLED = (boolean) CONFIG.get("general.ExclusiveActionbar.Enabled");
        PREVIEW_CLICK_EVENTS_ENABLED = (boolean) CONFIG.get("general.PreviewClickEvents.Enabled");
        INCREASE_CHAT_FIELD_MAX_LENGTH_ENABLED = (boolean) CONFIG.get("general.IncreaseChatFieldMaxLength");
        MACRO_ENABLED = (boolean) CONFIG.get("chatkeybindings.Macro.Enabled");
        REVIEW_LAST_MESSAGE_WITH_UP_ARROW_ONLY_ENABLED = (boolean) CONFIG.get("chatkeybindings.ReviewLastMessageWithUpArrowOnly");
        REPEAT_KEY = (String) CONFIG.get("chatkeybindings.RepeatKey");
        REPEAT_KEY_MODIFIER = SpecialUnits.KeyModifiers.valueOf((String) CONFIG.get("chatkeybindings.RepeatKeyModifier"));
    }

    public static void init(){
        if (!ConfigStorage.configFileExists()) {
            // if the config file doesn't exist, create a new one with the default settings.
            DEFAULT_CONFIG.save();
        }

        CONFIG = new ConfigStorage(false);
        // Run migration before applying defaults (so we can read the old version number)
        MigrationUtils.migrate(CONFIG);
        CONFIG = CONFIG.withDefault(DEFAULT_CONFIG.getHashmap());
        refreshCache();
    }

    public static void save() {
        CONFIG.save();
    }

    public static Object get(String var){
        return CONFIG.get(var);
    }

    public static Object getDefault(String var){
        return DEFAULT_CONFIG.get(var);
    }

    public static void set(String key, Object value) {
        CONFIG.set(key, value);
        refreshCache();
    }
}
