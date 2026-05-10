package net.apple70cents.chattools.config.common;

import net.apple70cents.chattools.utils.MigrationUtils;

import java.util.List;

public class ConfigUtils {
    public final static ConfigStorage DEFAULT_CONFIG = new ConfigStorage(true);
    public static ConfigStorage CONFIG;

    // Cached config values for frequently-used keys
    public static boolean CHAT_TOOLS_ENABLED = false;
    public static boolean KFC_ENABLED = false;
    public static boolean DISABLE_TEXT_OBFUSCATION_ENABLED = false;
    public static boolean BUBBLE_ENABLED = false;
    public static boolean BUBBLE_HIDE_IF_INVISIBLE_ENABLED = false;
    public static boolean NICK_HIDER_ENABLED = false;
    public static int NICK_HIDER_CACHE_SIZE = 0x70Ca;
    public static String NICK_HIDER_NICKNAME = "";
    public static boolean EXCLUSIVE_ACTIONBAR_ENABLED = false;
    public static boolean PREVIEW_CLICK_EVENTS_ENABLED = false;
    public static boolean INCREASE_CHAT_FIELD_MAX_LENGTH_ENABLED = false;
    public static boolean MACRO_ENABLED = false;
    public static List<SpecialUnits.MacroUnit> MACRO_LIST = List.of();
    public static boolean REVIEW_LAST_MESSAGE_WITH_UP_ARROW_ONLY_ENABLED = false;
    public static String REPEAT_KEY = "key.keyboard.unknown";
    public static SpecialUnits.KeyModifiers REPEAT_KEY_MODIFIER = SpecialUnits.KeyModifiers.NONE;

    public static void refreshCache() {
        CHAT_TOOLS_ENABLED = getBoolean("general.ChatTools.Enabled");
        KFC_ENABLED = getBoolean("general.kfc");
        DISABLE_TEXT_OBFUSCATION_ENABLED = getBoolean("general.DisableTextObfuscation.Enabled");
        BUBBLE_ENABLED = getBoolean("bubble.Enabled");
        BUBBLE_HIDE_IF_INVISIBLE_ENABLED = getBoolean("bubble.HideIfInvisibleEnabled");
        NICK_HIDER_ENABLED = getBoolean("general.NickHider.Enabled");
        NICK_HIDER_CACHE_SIZE = getInt("general.NickHider.CacheSize");
        NICK_HIDER_NICKNAME = getString("general.NickHider.Nickname");
        EXCLUSIVE_ACTIONBAR_ENABLED = getBoolean("general.ExclusiveActionbar.Enabled");
        PREVIEW_CLICK_EVENTS_ENABLED = getBoolean("general.PreviewClickEvents.Enabled");
        INCREASE_CHAT_FIELD_MAX_LENGTH_ENABLED = getBoolean("general.IncreaseChatFieldMaxLength");
        MACRO_ENABLED = getBoolean("chatkeybindings.Macro.Enabled");
        MACRO_LIST = SpecialUnits.MacroUnit.fromList((List) get("chatkeybindings.Macro.List"));
        REVIEW_LAST_MESSAGE_WITH_UP_ARROW_ONLY_ENABLED = getBoolean("chatkeybindings.ReviewLastMessageWithUpArrowOnly");
        REPEAT_KEY = getString("chatkeybindings.RepeatKey");
        REPEAT_KEY_MODIFIER = SpecialUnits.KeyModifiers.valueOf(getString("chatkeybindings.RepeatKeyModifier"));
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
        CONFIG.set("general.kfc", false);
        refreshCache();
    }

    public static void save() {
        CONFIG.save();
    }

    public static Object get(String var){
        return CONFIG.get(var);
    }

    public static boolean getBoolean(String key) {
        return (boolean) CONFIG.get(key);
    }

    public static int getInt(String key) {
        return ((Number) CONFIG.get(key)).intValue();
    }

    public static long getLong(String key) {
        return ((Number) CONFIG.get(key)).longValue();
    }

    public static float getFloat(String key) {
        return ((Number) CONFIG.get(key)).floatValue();
    }

    public static String getString(String key) {
        return (String) CONFIG.get(key);
    }

    public static Object getDefault(String var){
        return DEFAULT_CONFIG.get(var);
    }

    public static void set(String key, Object value) {
        CONFIG.set(key, value);
        refreshCache();
    }
}
