package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.config.common.ConfigStorage;

import java.util.*;

/**
 * Handles automatic migration of configuration files between versions.
 *
 * @author 70CentsApple
 */
public class MigrationUtils {
    /**
     * Migrates the given config from old versions to the current version.
     * Should be called after loading the config file but before applying defaults.
     *
     * @param config the ConfigStorage to migrate
     */
    public static void migrate(ConfigStorage config) {
        if (!config.hasKey("config.version")) {
            return;
        }
        double version = ((Number) config.get("config.version")).doubleValue();

        if (version <= 2.318) {
            migrateFrom2_31800(config);
        }
        if (version <= 2.319) {
            migrateFrom2_31900(config);
        }
        if (version <= 2.40202) {
            migrateFrom2_40202(config);
        }
    }

    /**
     * Migration from v2.4.2.2 to v2.4.3
     * bubble.List: add boolean fields "partial" and "profile"
     */
    @SuppressWarnings("unchecked")
    private static void migrateFrom2_40202(ConfigStorage config) {
        LoggerUtils.info("[ChatTools] Migrating config from v2.4.2.2 to v2.4.3...");
        if (config.hasKey("bubble.List")) {
            Object listObj = config.get("bubble.List");
            if (listObj instanceof List) {
                List<?> list = (List<?>) listObj;
                for (Object item : list) {
                    if (item instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) item;
                        if (!map.containsKey("partial")) {
                            map.put("partial", false);
                        }
                        if (!map.containsKey("profile")) {
                            map.put("profile", false);
                        }
                    }
                }
            }
        }
        LoggerUtils.info("[ChatTools] Migration from v2.4.2.2 to v2.4.3 completed.");
    }

    /**
     * Migration from v2.3.19 to v2.3.20:
     * responder.List: delayInMilliseconds -> minDelayInMilliseconds and maxDelayInMilliseconds
     */
    @SuppressWarnings("unchecked")
    private static void migrateFrom2_31900(ConfigStorage config) {
        LoggerUtils.info("[ChatTools] Migrating config from v2.3.19 to v2.3.20...");

        if (config.hasKey("responder.List")) {
            Object listObj = config.get("responder.List");
            if (listObj instanceof List) {
                List<?> list = (List<?>) listObj;
                for (Object item : list) {
                    if (item instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) item;
                        if (map.containsKey("delayInMilliseconds")) {
                            Object delayObj = map.get("delayInMilliseconds");
                            long delay = delayObj instanceof Number ? ((Number) delayObj).longValue() : 50;
                            map.put("minDelayInMilliseconds", delay);
                            map.put("maxDelayInMilliseconds", delay);
                            map.remove("delayInMilliseconds");
                        }
                    }
                }
            }
        }

        LoggerUtils.info("[ChatTools] Migration from v2.3.19 to v2.3.20 completed.");
    }

    /**
     * Migration from v2.3.18 to v2.3.19:
     * 1) notifier.AllowList: convert List<String> (regex patterns) to List<NotifierRuleUnit> (maps)
     * 2) notifier.BanList -> notifier.DenyList: rename the key
     * 3) chatkeybindings.Macro.List: command (String) -> commands (List<Map>)
     */
    @SuppressWarnings("unchecked")
    private static void migrateFrom2_31800(ConfigStorage config) {
        LoggerUtils.info("[ChatTools] Migrating config from v2.3.18 to v2.3.19...");

        // 1. Migrate notifier.AllowList: List<String> -> List<Map> (NotifierRuleUnit format)
        if (config.hasKey("notifier.AllowList")) {
            Object allowListObj = config.get("notifier.AllowList");
            if (allowListObj instanceof List) {
                List<?> oldList = (List<?>) allowListObj;
                List<Map<String, Object>> newList = new ArrayList<>();
                for (Object item : oldList) {
                    if (item instanceof String) {
                        // Convert old regex string to NotifierRuleUnit map
                        String pattern = (String) item;
                        if (pattern.isBlank()) {
                            continue;
                        }
                        Map<String, Object> ruleMap = new LinkedHashMap<>();
                        ruleMap.put("address", "*");
                        ruleMap.put("pattern", pattern);
                        ruleMap.put("toast", true);
                        ruleMap.put("sound", true);
                        ruleMap.put("actionbar", true);
                        ruleMap.put("highlight", true);
                        newList.add(ruleMap);
                    }
                    // If it's already a Map (somehow), keep it as-is
                    else if (item instanceof Map) {
                        newList.add((Map<String, Object>) item);
                    }
                }
                config.set("notifier.AllowList", newList);
            }
        }

        // 2. Rename notifier.BanList -> notifier.DenyList
        if (config.hasKey("notifier.BanList")) {
            Object banListData = config.get("notifier.BanList");
            config.set("notifier.DenyList", banListData);
            config.remove("notifier.BanList");
        }

        // 3. Migrate chatkeybindings.Macro.List: command (String) -> commands (List<Map>)
        if (config.hasKey("chatkeybindings.Macro.List")) {
            Object macroListObj = config.get("chatkeybindings.Macro.List");
            if (macroListObj instanceof List) {
                List<?> macroList = (List<?>) macroListObj;
                for (Object item : macroList) {
                    if (item instanceof Map) {
                        Map<String, Object> macroMap = (Map<String, Object>) item;
                        if (macroMap.containsKey("command") && !macroMap.containsKey("commands")) {
                            String oldCommand = (String) macroMap.get("command");
                            Map<String, Object> commandEntry = new LinkedHashMap<>();
                            commandEntry.put("command", oldCommand);
                            commandEntry.put("delayInMilliseconds", 0);
                            commandEntry.put("forceDisableFormatter", false);
                            List<Map<String, Object>> commandsList = new ArrayList<>();
                            commandsList.add(commandEntry);
                            macroMap.put("commands", commandsList);
                            macroMap.remove("command");
                        }
                    }
                }
            }
        }

        LoggerUtils.info("[ChatTools] Migration from v2.3.18 to v2.3.19 completed.");
    }

}
