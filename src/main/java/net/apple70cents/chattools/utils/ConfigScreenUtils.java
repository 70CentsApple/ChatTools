package net.apple70cents.chattools.utils;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * @author 70CentsApple
 */
public class ConfigScreenUtils {
    final static boolean SHOULD_EXPAND_ALL_RULES =
            //#if MC>=12104
            true
            //#else
            //$$ false
            //#endif
            ;

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
                                    sb.append("\n    §e" + ele2.substring(0, idx) + "§r§7 = §f" + ele2.substring(
                                            idx + 1) + "§r§7");
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

    public static TooltipListEntry getEntryBuilder(ConfigEntryBuilder eb, String type, String key, String errorSupplier, int... args) {
        // the `args` are only for `min` and `max` value for int sliders (recently)
        // `errorSuppliers` will only apply to `StringList`s
        Component tooltip = "FAQ".equals(type) ? getTooltip(key, type, null) : getTooltip(key, type);
        // display current server (if it can be used)
        final Component SERVER_LABELED_KEY = trans(key, "§f" + ContextUtils.getSessionIdentifier());
        switch (type) {
            case "boolean":
                return eb.startBooleanToggle(trans(key), (boolean) ConfigUtils.get(key))
                        .setDefaultValue((boolean) ConfigUtils.getDefault(key)).setTooltip(tooltip)
                        .setSaveConsumer(v -> ConfigUtils.set(key, v)).build();
            case "String":
                return eb.startStrField(trans(key), (String) ConfigUtils.get(key))
                        .setDefaultValue((String) ConfigUtils.getDefault(key)).setTooltip(tooltip)
                        .setSaveConsumer(v -> ConfigUtils.set(key, v)).build();
            case "intSlider":
                return eb.startIntSlider(trans(key), ((Number) ConfigUtils.get(key)).intValue(), args[0], args[1])
                        .setDefaultValue(((Number) ConfigUtils.getDefault(key)).intValue()).setTooltip(tooltip)
                        .setSaveConsumer(v -> ConfigUtils.set(key, (Number) v)).build();
            case "intField":
                return eb.startIntField(trans(key), ((Number) ConfigUtils.get(key)).intValue())
                        .setDefaultValue(((Number) ConfigUtils.getDefault(key)).intValue()).setTooltip(tooltip)
                        .setSaveConsumer(v -> ConfigUtils.set(key, (Number) v)).build();
            case "doubleField":
                return eb.startDoubleField(trans(key), ((Number) ConfigUtils.get(key)).doubleValue())
                        .setDefaultValue(((Number) ConfigUtils.getDefault(key)).doubleValue()).setTooltip(tooltip)
                        .setSaveConsumer(v -> ConfigUtils.set(key, (Number) v)).build();
            case "keycode":
                return eb.startKeyCodeField(trans(key), InputConstants.getKey((String) ConfigUtils.get(key)))
                        .setDefaultValue(InputConstants.getKey((String) ConfigUtils.getDefault(key)))
                        .setTooltip(tooltip)
                        //#if MC>=11800
                        .setKeySaveConsumer
                        //#elseif MC>=11700
                        // In MC 1.17.X, we use ClothConfig v5, where the discontinued version uses `setSaveConsumer()` method.
                        //$$ .setSaveConsumer
                        //#else
                        //$$ .setKeySaveConsumer
                        //#endif
                                (keybind -> ConfigUtils.set(key, keybind.getName())).build();
            case "StringList":
                StringListBuilder builder = eb.startStrList(trans(key), (List<String>) ConfigUtils.get(key))
                        .setDefaultValue((List<String>) ConfigUtils.getDefault(key)).setTooltip(tooltip)
                        .setSaveConsumer(v -> ConfigUtils.set(key, v));
                switch (errorSupplier) {
                    case "RegExNormal":
                        builder.setErrorSupplier(ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_FOR_LIST);
                        break;
                    case "RegExRequireGroups":
                        builder.setErrorSupplier(ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS_FOR_LIST);
                        break;
                    case "RegExAllowStar":
                        builder.setErrorSupplier(ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR_FOR_LIST);
                        break;
                    case "null":
                    default:
                        break;
                }
                return builder.build();
            case "FAQ":
                return eb.startTextDescription(trans(key).copy().setStyle(TextUtils.WEBSITE_URL_STYLE)).build();
            case "BubbleList":
                return new NestedListListEntry<SpecialUnits.BubbleRuleUnit, MultiElementListEntry<SpecialUnits.BubbleRuleUnit>>(
                        SERVER_LABELED_KEY, SpecialUnits.BubbleRuleUnit.fromList((List) ConfigUtils.get(key)), true,
                        () -> Optional.of(new Component[]{tooltip}), v -> ConfigUtils.set(key, v),
                        () -> SpecialUnits.BubbleRuleUnit.fromList((List) ConfigUtils.getDefault(key)),
                        eb.getResetButtonKey(), true, true, (passedUnit, ignored) -> {
                    SpecialUnits.BubbleRuleUnit unit = (passedUnit == null) ? new SpecialUnits.BubbleRuleUnit() : passedUnit;

                    Component displayText;
                    if (passedUnit == null) {
                        displayText = trans(key + ".@New");
                    } else {
                        boolean isSessionMatch = "*".equals(unit.address) || RegExUtils.getOrCompilePattern(unit.address)
                                .matcher(ContextUtils.getSessionIdentifier()).matches();
                        String colorPrefix = isSessionMatch ? "§a" : "§6";

                        displayText = trans(key + ".@Display", colorPrefix + unit.address,
                                unit.fallback ? "§a✔" : "§c✘", unit.pattern);
                    }

                    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
                    SpecialUnits.BubbleRuleUnit defaultObj = new SpecialUnits.BubbleRuleUnit();

                    entries.add(eb.startStrField(trans(key + ".Address"), unit.address)
                            .setTooltip(getTooltip(key + ".Address", "String", unit.address))
                            .setDefaultValue(defaultObj.address).setSaveConsumer(v -> unit.address = v)
                            .setErrorSupplier(ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());

                    entries.add(eb.startStrField(trans(key + ".Pattern"), unit.pattern)
                            .setTooltip(getTooltip(key + ".Pattern", "String", unit.pattern))
                            .setDefaultValue(defaultObj.pattern).setSaveConsumer(v -> unit.pattern = v)
                            .setErrorSupplier(ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".Fallback"), unit.fallback)
                            .setTooltip(getTooltip(key + ".Fallback", "boolean", unit.fallback))
                            .setDefaultValue(defaultObj.fallback).setSaveConsumer(v -> unit.fallback = v).build());

                    return new MultiElementListEntry<>(displayText, unit, entries, SHOULD_EXPAND_ALL_RULES);
                });
            case "ResponderList":
                return new NestedListListEntry<SpecialUnits.ResponderRuleUnit, MultiElementListEntry<SpecialUnits.ResponderRuleUnit>>(
                        SERVER_LABELED_KEY, SpecialUnits.ResponderRuleUnit.fromList((List) ConfigUtils.get(key)), true,
                        () -> Optional.of(new Component[]{tooltip}), v -> ConfigUtils.set(key, v),
                        () -> SpecialUnits.ResponderRuleUnit.fromList((List) ConfigUtils.getDefault(key)),
                        eb.getResetButtonKey(), true, true, (passedUnit, ignored) -> {
                    SpecialUnits.ResponderRuleUnit unit = (passedUnit == null) ? new SpecialUnits.ResponderRuleUnit() : passedUnit;

                    Component displayText;
                    if (passedUnit == null) {
                        displayText = trans(key + ".@New");
                    } else {
                        boolean isSessionMatch = "*".equals(unit.address) || RegExUtils.getOrCompilePattern(unit.address)
                                .matcher(ContextUtils.getSessionIdentifier()).matches();
                        String colorPrefix = isSessionMatch ? "§a" : "§6";

                        displayText = trans(key + ".@Display", colorPrefix + unit.address,
                                unit.forceDisableFormatter ? "§a✔" : "§c✘", unit.delayInMilliseconds, unit.pattern,
                                unit.message);
                    }

                    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
                    SpecialUnits.ResponderRuleUnit defaultObj = new SpecialUnits.ResponderRuleUnit();

                    entries.add(eb.startStrField(trans(key + ".Address"), unit.address)
                            .setTooltip(getTooltip(key + ".Address", "String", unit.address))
                            .setDefaultValue(defaultObj.address).setSaveConsumer(v -> unit.address = v)
                            .setErrorSupplier(ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());

                    entries.add(eb.startStrField(trans(key + ".Pattern"), unit.pattern)
                            .setTooltip(getTooltip(key + ".Pattern", "String", unit.pattern))
                            .setDefaultValue(defaultObj.pattern).setSaveConsumer(v -> unit.pattern = v)
                            .setErrorSupplier(ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER).build());

                    entries.add(eb.startStrField(trans(key + ".Message"), unit.message)
                            .setTooltip(getTooltip(key + ".Message", "String", unit.message))
                            .setDefaultValue(defaultObj.message).setSaveConsumer(v -> unit.message = v).build());

                    entries.add(eb.startLongField(trans(key + ".DelayInMilliseconds"), unit.delayInMilliseconds)
                            .setTooltip(getTooltip(key + ".DelayInMilliseconds", "longField", unit.delayInMilliseconds))
                            .setDefaultValue(defaultObj.delayInMilliseconds)
                            .setSaveConsumer(v -> unit.delayInMilliseconds = v).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".ForceDisableFormatter"), unit.forceDisableFormatter)
                            .setTooltip(
                                    getTooltip(key + ".ForceDisableFormatter", "boolean", unit.forceDisableFormatter))
                            .setDefaultValue(defaultObj.forceDisableFormatter)
                            .setSaveConsumer(v -> unit.forceDisableFormatter = v).build());

                    return new MultiElementListEntry<>(displayText, unit, entries, SHOULD_EXPAND_ALL_RULES);
                });
            case "MacroList":
                return new NestedListListEntry<SpecialUnits.MacroUnit, MultiElementListEntry<SpecialUnits.MacroUnit>>(
                        trans(key), SpecialUnits.MacroUnit.fromList((List) ConfigUtils.get(key)), true,
                        () -> Optional.of(new Component[]{tooltip}), v -> ConfigUtils.set(key, v),
                        () -> SpecialUnits.MacroUnit.fromList((List) ConfigUtils.getDefault(key)),
                        eb.getResetButtonKey(), true, true, (passedUnit, ignored) -> {
                    SpecialUnits.MacroUnit unit = (passedUnit == null) ? new SpecialUnits.MacroUnit() : passedUnit;

                    Component displayText;
                    if (passedUnit == null || unit.key.equals(InputConstants.UNKNOWN.getName())) {
                        displayText = trans(key + ".@New");
                    } else {
                        if (unit.modifier == SpecialUnits.KeyModifiers.NONE) {
                            // such as "[ H ] /home"
                            displayText = trans(key + ".@Display",
                                    "§6" + InputConstants.getKey(unit.key).getDisplayName().getString(), unit.command);
                        } else {
                            // such as "[ Shift + B ] /back"
                            displayText = trans(key + ".@Display",
                                    "§6" + unit.modifier + " + " + InputConstants.getKey(unit.key).getDisplayName()
                                            .getString(), unit.command);
                        }
                    }

                    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
                    SpecialUnits.MacroUnit defaultObj = new SpecialUnits.MacroUnit();

                    entries.add(eb.startKeyCodeField(trans(key + ".Key"), InputConstants.getKey(unit.key))
                            .setTooltip(getTooltip(key + ".Key", "keycode", InputConstants.getKey(unit.key)))
                            .setDefaultValue(InputConstants.getKey(defaultObj.key))
                            //#if MC>=11800
                            .setKeySaveConsumer
                            //#elseif MC>=11700
                            // In MC 1.17.X, we use ClothConfig v5.
                            // In ClothConfig v5 (discontinued) we use the `setSaveConsumer()` method.
                            //$$ .setSaveConsumer
                            //#else
                            //$$ .setKeySaveConsumer
                            //#endif
                                    (k -> unit.key = k.getName()).build());

                    entries.add(eb.startEnumSelector(trans(key + ".Modifier"), SpecialUnits.KeyModifiers.class,
                                    unit.modifier).setTooltip(getTooltip(key + ".Modifier", "EnumKeyModifiers", unit.modifier))
                            .setDefaultValue(defaultObj.modifier).setSaveConsumer(v -> unit.modifier = v).build());

                    entries.add(eb.startEnumSelector(trans(key + ".Mode"), SpecialUnits.MacroModes.class, unit.mode)
                            .setTooltip(getTooltip(key + ".Mode", "EnumMacroModes", unit.mode))
                            .setDefaultValue(defaultObj.mode).setSaveConsumer(v -> unit.mode = v).build());

                    entries.add(eb.startStrField(trans(key + ".Command"), unit.command)
                            .setTooltip(getTooltip(key + ".Command", "String", unit.command))
                            .setDefaultValue(defaultObj.command).setSaveConsumer(v -> unit.command = v).build());

                    return new MultiElementListEntry<>(displayText, unit, entries, SHOULD_EXPAND_ALL_RULES);
                });
            case "FormatterList":
                return new NestedListListEntry<SpecialUnits.FormatterUnit, MultiElementListEntry<SpecialUnits.FormatterUnit>>(
                        SERVER_LABELED_KEY, SpecialUnits.FormatterUnit.fromList((List) ConfigUtils.get(key)), true,
                        () -> Optional.of(new Component[]{tooltip}), v -> ConfigUtils.set(key, v),
                        () -> SpecialUnits.FormatterUnit.fromList((List) ConfigUtils.getDefault(key)),
                        eb.getResetButtonKey(), true, true, (passedUnit, ignored) -> {
                    SpecialUnits.FormatterUnit unit = (passedUnit == null) ? new SpecialUnits.FormatterUnit() : passedUnit;

                    Component displayText;
                    if (passedUnit == null) {
                        displayText = trans(key + ".@New");
                    } else {
                        boolean isSessionMatch = "*".equals(unit.address) || RegExUtils.getOrCompilePattern(unit.address)
                                .matcher(ContextUtils.getSessionIdentifier()).matches();
                        String colorPrefix = isSessionMatch ? "§a" : "§6";

                        displayText = trans(key + ".@Display", colorPrefix + unit.address, unit.formatter);
                    }

                    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
                    SpecialUnits.FormatterUnit defaultObj = new SpecialUnits.FormatterUnit();

                    entries.add(eb.startStrField(trans(key + ".Address"), unit.address)
                            .setTooltip(getTooltip(key + ".Address", "String", unit.address))
                            .setDefaultValue(defaultObj.address).setSaveConsumer(v -> unit.address = v)
                            .setErrorSupplier(ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());

                    entries.add(eb.startStrField(trans(key + ".Formatter"), unit.formatter)
                            .setTooltip(getTooltip(key + ".Formatter", "String", unit.formatter))
                            .setDefaultValue(defaultObj.formatter).setSaveConsumer(v -> unit.formatter = v).build());

                    return new MultiElementListEntry<>(displayText, unit, entries, SHOULD_EXPAND_ALL_RULES);
                });
            case "CustomJoinMessageList":
                return new NestedListListEntry<SpecialUnits.CustomJoinMessageRuleUnit, MultiElementListEntry<SpecialUnits.CustomJoinMessageRuleUnit>>(
                        SERVER_LABELED_KEY,
                        SpecialUnits.CustomJoinMessageRuleUnit.fromList((List) ConfigUtils.get(key)), true,
                        () -> Optional.of(new Component[]{tooltip}), v -> ConfigUtils.set(key, v),
                        () -> SpecialUnits.CustomJoinMessageRuleUnit.fromList((List) ConfigUtils.getDefault(key)),
                        eb.getResetButtonKey(), true, true, (passedUnit, ignored) -> {
                    SpecialUnits.CustomJoinMessageRuleUnit unit = (passedUnit == null) ? new SpecialUnits.CustomJoinMessageRuleUnit() : passedUnit;

                    Component displayText;
                    if (passedUnit == null) {
                        displayText = trans(key + ".@New");
                    } else {
                        boolean isSessionMatch = "*".equals(unit.address) || RegExUtils.getOrCompilePattern(unit.address)
                                .matcher(ContextUtils.getSessionIdentifier()).matches();
                        String colorPrefix = isSessionMatch ? "§a" : "§6";

                        displayText = trans(key + ".@Display", colorPrefix + unit.address,
                                unit.forceDisableFormatter ? "§a✔" : "§c✘", unit.delayInMilliseconds, unit.message);
                    }

                    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
                    SpecialUnits.CustomJoinMessageRuleUnit defaultObj = new SpecialUnits.CustomJoinMessageRuleUnit();

                    entries.add(eb.startStrField(trans(key + ".Address"), unit.address)
                            .setTooltip(getTooltip(key + ".Address", "String", unit.address))
                            .setDefaultValue(defaultObj.address).setSaveConsumer(v -> unit.address = v)
                            .setErrorSupplier(ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());

                    entries.add(eb.startStrField(trans(key + ".Message"), unit.message)
                            .setTooltip(getTooltip(key + ".Message", "String", unit.message))
                            .setDefaultValue(defaultObj.message).setSaveConsumer(v -> unit.message = v).build());

                    entries.add(eb.startLongField(trans(key + ".DelayInMilliseconds"), unit.delayInMilliseconds)
                            .setTooltip(getTooltip(key + ".DelayInMilliseconds", "longField", unit.delayInMilliseconds))
                            .setDefaultValue(defaultObj.delayInMilliseconds)
                            .setSaveConsumer(v -> unit.delayInMilliseconds = v).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".ForceDisableFormatter"), unit.forceDisableFormatter)
                            .setTooltip(
                                    getTooltip(key + ".ForceDisableFormatter", "boolean", unit.forceDisableFormatter))
                            .setDefaultValue(defaultObj.forceDisableFormatter)
                            .setSaveConsumer(v -> unit.forceDisableFormatter = v).build());

                    return new MultiElementListEntry<>(displayText, unit, entries, SHOULD_EXPAND_ALL_RULES);
                });
            case "EnumKeyModifiers":
                return eb.startEnumSelector(trans(key), SpecialUnits.KeyModifiers.class,
                                SpecialUnits.KeyModifiers.valueOf((String) ConfigUtils.get(key)))
                        .setDefaultValue(SpecialUnits.KeyModifiers.valueOf((String) ConfigUtils.getDefault(key)))
                        .setTooltip(tooltip).setSaveConsumer(v -> ConfigUtils.set(key, v.toString())).build();
            case "EnumToastModes":
                return eb.startEnumSelector(trans(key), SpecialUnits.ToastModes.class,
                                SpecialUnits.ToastModes.valueOf((String) ConfigUtils.get(key)))
                        .setDefaultValue(SpecialUnits.ToastModes.valueOf((String) ConfigUtils.getDefault(key)))
                        .setTooltip(tooltip).setSaveConsumer(v -> ConfigUtils.set(key, v.toString())).build();
            case "EnumTranslators":
                return eb.startEnumSelector(trans(key), SpecialUnits.TranslatorModes.class,
                                SpecialUnits.TranslatorModes.valueOf((String) ConfigUtils.get(key)))
                        .setDefaultValue(SpecialUnits.TranslatorModes.valueOf((String) ConfigUtils.getDefault(key)))
                        .setTooltip(tooltip).setSaveConsumer(v -> ConfigUtils.set(key, v.toString())).build();
            default:
                LoggerUtils.error("[ChatTools] Unknown config type: " + type);
                return null;
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
