package net.apple70cents.chattools.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import me.shedaniel.clothconfig2.impl.builders.StringListBuilder;
import net.apple70cents.chattools.config.SpecialUnits;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * @author 70CentsApple
 */
public class ConfigScreenUtils {

    public static Map<String, Object> configGuiMap;
    public static int GUI_VERSION = -1;
    public static boolean configGuiMapInitialized = false;
    private static final Gson GSON = new GsonBuilder().create();

    private static final Map<String, String> key2TypeMappings = new HashMap<>();

    public static Map<String, String> getKey2TypeMappings() {
        if (!configGuiMapInitialized || key2TypeMappings.isEmpty()) {
            initializeConfigGuiMapIfNecessary();
            for (Object categoryInfo : (List) ConfigScreenUtils.configGuiMap.get("content")) {
                for (Object element : (List) ((Map) categoryInfo).get("content")) {
                    String type = (String) ((Map) element).get("type");
                    String key = (String) ((Map) element).get("key");
                    key2TypeMappings.put(key, type);
                    if ("sub".equals(type)) {
                        for (Object elementInner : (List) ((Map) element).get("content")) {
                            String typeInner = (String) ((Map) elementInner).get("type");
                            String keyInner = (String) ((Map) elementInner).get("key");
                            // we are assuming no sub nested in subs, therefore two layers are enough.
                            key2TypeMappings.put(keyInner, typeInner);
                        }
                    }
                }
            }
        }
        return key2TypeMappings;
    }


    public static void loadConfigGuiMap() {
        try {
            InputStream inputStream = Minecraft.getInstance().getClass().getClassLoader()
                                               .getResourceAsStream("assets/chattools/config_gui.json");
            Reader reader = new InputStreamReader(inputStream);
            configGuiMap = GSON.fromJson(reader, Map.class);
            configGuiMapInitialized = true;
            initializeConfigGuiMapIfNecessary();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initializeConfigGuiMapIfNecessary() {
        if (!configGuiMapInitialized) {
            loadConfigGuiMap();
            GUI_VERSION = ((Number) configGuiMap.get("version")).intValue();
        }
    }

    public static Component getTooltip(String key, String variableType) {
        return getTooltip(key, variableType, ConfigUtils.getDefault(key));
    }

    public static Component getTooltip(String key, String variableType, Object defaultVal) {
        boolean isNull = (defaultVal == null || defaultVal.toString().isBlank());
        String defaultValue = isNull ? "NULL" : defaultVal.toString();
        // check if F3+H is on
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            try {
                if (variableType.endsWith("List")) {
                    if (!((List<?>) ConfigUtils.getDefault(key)).isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("[");
                        for (int i = 0; i < ((List<?>) ConfigUtils.getDefault(key)).size(); i++) {
                            String ele = ((List<?>) ConfigUtils.getDefault(key)).get(i).toString();
                            // if this is not the first element, we add a comma to the front
                            if (i != 0) sb.append(",");
                            // check if the list's type is raw string
                            if ("StringList".equals(variableType)) {
                                sb.append("\n  §r§f" + ele + "§r§7");
                            } else {
                                // we need to do pretty-printing further
                                sb.append("\n  {");
                                String[] keyAndValuePairs = ele.substring(1, ele.length() - 1).split(", ");
                                for (int j = 0; j < keyAndValuePairs.length; j++) {
                                    // if (j != 0) sb.append(",");
                                    String ele2 = keyAndValuePairs[j];
                                    int idx = ele2.indexOf("=");
                                    sb.append("\n    §e" + ele2.substring(0, idx) + "§r§7 = §f" + ele2.substring(idx + 1) + "§r§7");
                                }
                                sb.append("\n  }");
                            }
                        }
                        sb.append("\n]");
                        defaultValue = sb.toString();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Component defaults = TextUtils.trans("texts.defaultValue", defaultValue).copy().withStyle(ChatFormatting.GRAY);

            Component keyName = TextUtils.of(key).copy().withStyle(ChatFormatting.GOLD);
            Component main = trans(key + ".@Tooltip").copy().withStyle(ChatFormatting.WHITE);
            Component type = TextUtils.trans("texts.variableType", variableType).copy().withStyle(ChatFormatting.GRAY);
            MutableComponent tooltip = TextUtils.empty().copy();
            tooltip.append(keyName).append("§r\n").append(main).append("§r\n").append(type).append("§r\n")
                   .append(defaults);
            return tooltip;
        } else {
            return trans(key + ".@Tooltip");
        }
    }

    public static class ErrorSuppliers {
        public static final Function<String, Optional<Component>> REGEX_COMPILE_ERROR_SUPPLIER = (v) -> {
            try {
                Pattern.compile(v);
                return Optional.empty();
            } catch (PatternSyntaxException e) {
                return Optional.of(TextUtils.of(e.getDescription()));
            }
        };
        public static final Function<List<String>, Optional<Component>> REGEX_COMPILE_ERROR_SUPPLIER_FOR_LIST = (v) -> {
            try {
                for (String s : v) {
                    Pattern.compile(s);
                }
                return Optional.empty();
            } catch (PatternSyntaxException e) {
                return Optional.of(TextUtils.of(e.getDescription()));
            }
        };

        public static final Function<String, Optional<Component>> REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR = (v) -> {
            if ("*".equals(v)) {
                return Optional.empty();
            }
            try {
                Pattern.compile(v);
                return Optional.empty();
            } catch (PatternSyntaxException e) {
                return Optional.of(TextUtils.of(e.getDescription()));
            }
        };
        public static final Function<List<String>, Optional<Component>> REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR_FOR_LIST = (v) -> {

            try {
                for (String s : v) {
                    if ("*".equals(v)) {
                        continue;
                    }
                    Pattern.compile(s);
                }
                return Optional.empty();
            } catch (PatternSyntaxException e) {
                return Optional.of(TextUtils.of(e.getDescription()));
            }
        };

        public static final Function<String, Optional<Component>> REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS = (v) -> {
            try {
                Pattern.compile(v);
                if (v.contains("<name>") && v.contains("<message>")) {
                    return Optional.empty();
                } else {
                    return Optional.of(TextUtils.literal("Should include both <name> and <message> groups."));
                }
            } catch (PatternSyntaxException e) {
                return Optional.of(TextUtils.of(e.getDescription()));
            }
        };
        public static final Function<List<String>, Optional<Component>> REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS_FOR_LIST = (v) -> {
            try {
                for (String s : v) {
                    Pattern.compile(s);
                    if (s.contains("<name>") && s.contains("<message>")) {
                        continue;
                    } else {
                        return Optional.of(TextUtils.literal("Should include both <name> and <message> groups."));
                    }
                }
                return Optional.empty();
            } catch (PatternSyntaxException e) {
                return Optional.of(TextUtils.of(e.getDescription()));
            }
        };
    }
}
