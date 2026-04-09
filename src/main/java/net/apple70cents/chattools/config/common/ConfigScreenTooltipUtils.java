package net.apple70cents.chattools.config.common;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * Utility class for generating tooltips and error suppliers.
 *
 * @author 70CentsApple
 */
public class ConfigScreenTooltipUtils {

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
                                sb.append("\n  §r§f").append(ele).append("§r§7");
                            } else {
                                // we need to do pretty-printing further
                                sb.append("\n  {");
                                String[] keyAndValuePairs = ele.substring(1, ele.length() - 1).split(", ");
                                for (int j = 0; j < keyAndValuePairs.length; j++) {
                                    String ele2 = keyAndValuePairs[j];
                                    int idx = ele2.indexOf("=");
                                    sb.append("\n    §e").append(ele2, 0, idx).append("§r§7 = §f").append(ele2.substring(
                                            idx + 1)).append("§r§7");
                                }
                                sb.append("\n  }");
                            }
                        }
                        sb.append("\n]");
                        defaultValue = sb.toString();
                    }
                }
            } catch (Exception e) {
                LoggerUtils.error("[ChatTools] Error getting tooltip default value", e);
            }
            Component defaults = TextUtils.trans("texts.defaultValue", defaultValue).copy()
                    .withStyle(ChatFormatting.GRAY);

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
                    if ("*".equals(s)) {
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