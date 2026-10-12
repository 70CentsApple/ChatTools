package net.apple70cents.chattools.config.common;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Map;
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
                if (variableType.endsWith("List") && defaultVal instanceof List<?>) {
                    defaultValue = prettyPrintDefaultValue(defaultVal);
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

    /**
     * Format the actual value hierarchy rather than parsing Object.toString().
     * Map/List string representations do not quote string values, so splitting
     * them on commas or brackets can corrupt regular expressions and commands.
     */
    private static String prettyPrintDefaultValue(Object value) {
        StringBuilder sb = new StringBuilder();
        appendPrettyValue(sb, value, 0);
        return sb.toString();
    }

    private static void appendPrettyValue(StringBuilder sb, Object value, int depth) {
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            sb.append("§r§7{");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                appendIndentedLine(sb, depth + 1);
                sb.append("§r§e").append(entry.getKey()).append("§r§7 = ");
                appendPrettyValue(sb, entry.getValue(), depth + 1);
            }
            if (!map.isEmpty()) {
                appendIndentedLine(sb, depth);
            }
            sb.append("§r§7}");
        } else if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            sb.append("§r§7[");
            boolean first = true;
            for (Object element : list) {
                if (!first) {
                    sb.append("§r§7,");
                }
                first = false;
                appendIndentedLine(sb, depth + 1);
                appendPrettyValue(sb, element, depth + 1);
            }
            if (!list.isEmpty()) {
                appendIndentedLine(sb, depth);
            }
            sb.append("§r§7]");
        } else {
            // Strings (including StringList elements) and unknown types are leaves.
            // Never guess a structure from their text; String.valueOf handles null.
            sb.append("§r§f").append(value).append("§r§7");
        }
    }

    private static void appendIndentedLine(StringBuilder sb, int depth) {
        sb.append('\n');
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
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