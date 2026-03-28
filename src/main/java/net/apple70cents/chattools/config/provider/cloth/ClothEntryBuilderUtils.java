package net.apple70cents.chattools.config.provider.cloth;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import me.shedaniel.clothconfig2.impl.builders.StringListBuilder;
import net.apple70cents.chattools.config.common.ConfigScreenTooltipUtils;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.config.common.SpecialUnits;
import com.mojang.blaze3d.platform.InputConstants;
import net.apple70cents.chattools.utils.*;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * @author 70CentsApple
 */
public class ClothEntryBuilderUtils {
    final static boolean SHOULD_EXPAND_ALL_RULES =
//? if >=1.21.4 {
            true
//?} else {
            /*false
*///?}
            ;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TooltipListEntry getEntryBuilder(ConfigEntryBuilder eb, String type, String key, String errorSupplier, int... args) {
        // the `args` are only for `min` and `max` value for int sliders (recently)
        // `errorSuppliers` will only apply to `StringList`s
        Component tooltip = "FAQ".equals(type) ? ConfigScreenTooltipUtils.getTooltip(key, type, null) : ConfigScreenTooltipUtils.getTooltip(key, type);
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
//? if >=1.18 {
                        .setKeySaveConsumer
//?} elif >=1.17 {
                        /*// In MC 1.17.X, we use ClothConfig v5, where the discontinued version uses `setSaveConsumer()` method.
                        .setSaveConsumer
*///?} else {
                        /*.setKeySaveConsumer
                         *///?}
                                (keybind -> ConfigUtils.set(key, keybind.getName())).build();
            case "StringList":
                StringListBuilder builder = eb.startStrList(trans(key), (List<String>) ConfigUtils.get(key))
                        .setDefaultValue((List<String>) ConfigUtils.getDefault(key)).setTooltip(tooltip)
                        .setSaveConsumer(v -> ConfigUtils.set(key, v));
                switch (errorSupplier) {
                    case "RegExNormal":
                        builder.setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_FOR_LIST);
                        break;
                    case "RegExRequireGroups":
                        builder.setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS_FOR_LIST);
                        break;
                    case "RegExAllowStar":
                        builder.setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR_FOR_LIST);
                        break;
                    case "null":
                    default:
                        break;
                }
                return builder.build();
            case "FAQ":
                return eb.startTextDescription(trans(key).copy().setStyle(TextUtils.WEBSITE_URL_STYLE)).build();
            case "NotifierList":
                return new NestedListListEntry<SpecialUnits.NotifierRuleUnit, MultiElementListEntry<SpecialUnits.NotifierRuleUnit>>(
                        SERVER_LABELED_KEY, SpecialUnits.NotifierRuleUnit.fromList((List) ConfigUtils.get(key)), true,
                        () -> Optional.of(new Component[]{tooltip}), v -> ConfigUtils.set(key, v),
                        () -> SpecialUnits.NotifierRuleUnit.fromList((List) ConfigUtils.getDefault(key)),
                        eb.getResetButtonKey(), true, true, (passedUnit, ignored) -> {
                    SpecialUnits.NotifierRuleUnit unit = (passedUnit == null) ? new SpecialUnits.NotifierRuleUnit() : passedUnit;

                    Component displayText;
                    if (passedUnit == null) {
                        displayText = trans(key + ".@New");
                    } else {
                        boolean isSessionMatch = "*".equals(unit.address) || RegExUtils.getOrCompilePattern(unit.address)
                                .matcher(ContextUtils.getSessionIdentifier()).matches();
                        String colorPrefix = isSessionMatch ? "§a" : "§6";

                        displayText = trans(key + ".@Display", colorPrefix + unit.address, unit.pattern);
                    }

                    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
                    SpecialUnits.NotifierRuleUnit defaultObj = new SpecialUnits.NotifierRuleUnit();

                    entries.add(eb.startStrField(trans(key + ".Address"), unit.address)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Address", "String", defaultObj.address))
                            .setDefaultValue(defaultObj.address).setSaveConsumer(v -> unit.address = v)
                            .setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());

                    entries.add(eb.startStrField(trans(key + ".Pattern"), unit.pattern)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Pattern", "String", defaultObj.pattern))
                            .setDefaultValue(defaultObj.pattern).setSaveConsumer(v -> unit.pattern = v)
                            .setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".Toast"), unit.toast)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Toast", "boolean", defaultObj.toast))
                            .setDefaultValue(defaultObj.toast).setSaveConsumer(v -> unit.toast = v).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".Sound"), unit.sound)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Sound", "boolean", defaultObj.sound))
                            .setDefaultValue(defaultObj.sound).setSaveConsumer(v -> unit.sound = v).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".Actionbar"), unit.actionbar)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Actionbar", "boolean", defaultObj.actionbar))
                            .setDefaultValue(defaultObj.actionbar).setSaveConsumer(v -> unit.actionbar = v).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".Highlight"), unit.highlight)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Highlight", "boolean", defaultObj.highlight))
                            .setDefaultValue(defaultObj.highlight).setSaveConsumer(v -> unit.highlight = v).build());

                    return new MultiElementListEntry<>(displayText, unit, entries, SHOULD_EXPAND_ALL_RULES);
                });
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
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Address", "String", defaultObj.address))
                            .setDefaultValue(defaultObj.address).setSaveConsumer(v -> unit.address = v)
                            .setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());

                    entries.add(eb.startStrField(trans(key + ".Pattern"), unit.pattern)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Pattern", "String", defaultObj.pattern))
                            .setDefaultValue(defaultObj.pattern).setSaveConsumer(v -> unit.pattern = v)
                            .setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".Fallback"), unit.fallback)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Fallback", "boolean", defaultObj.fallback))
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

                        String delayInMillisecondsText = unit.maxDelayInMilliseconds > unit.minDelayInMilliseconds ?
                                unit.minDelayInMilliseconds + "~" + unit.maxDelayInMilliseconds :
                                String.valueOf(unit.minDelayInMilliseconds);

                        displayText = trans(key + ".@Display", colorPrefix + unit.address,
                                unit.forceDisableFormatter ? "§a✔" : "§c✘", delayInMillisecondsText, unit.pattern,
                                unit.message);
                    }

                    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
                    SpecialUnits.ResponderRuleUnit defaultObj = new SpecialUnits.ResponderRuleUnit();

                    entries.add(eb.startStrField(trans(key + ".Address"), unit.address)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Address", "String", defaultObj.address))
                            .setDefaultValue(defaultObj.address).setSaveConsumer(v -> unit.address = v)
                            .setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());

                    entries.add(eb.startStrField(trans(key + ".Pattern"), unit.pattern)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Pattern", "String", defaultObj.pattern))
                            .setDefaultValue(defaultObj.pattern).setSaveConsumer(v -> unit.pattern = v)
                            .setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER).build());

                    entries.add(eb.startStrField(trans(key + ".Message"), unit.message)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Message", "String", defaultObj.message))
                            .setDefaultValue(defaultObj.message).setSaveConsumer(v -> unit.message = v).build());

                    entries.add(eb.startLongField(trans(key + ".MinDelayInMilliseconds"), unit.minDelayInMilliseconds)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".MinDelayInMilliseconds", "longField", defaultObj.minDelayInMilliseconds))
                            .setDefaultValue(defaultObj.minDelayInMilliseconds)
                            .setSaveConsumer(v -> unit.minDelayInMilliseconds = v).build());

                    entries.add(eb.startLongField(trans(key + ".MaxDelayInMilliseconds"), unit.maxDelayInMilliseconds)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".MaxDelayInMilliseconds", "longField", defaultObj.maxDelayInMilliseconds))
                            .setDefaultValue(defaultObj.maxDelayInMilliseconds)
                            .setSaveConsumer(v -> unit.maxDelayInMilliseconds = v).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".ForceDisableFormatter"), unit.forceDisableFormatter)
                            .setTooltip(
                                    ConfigScreenTooltipUtils.getTooltip(key + ".ForceDisableFormatter", "boolean", defaultObj.forceDisableFormatter))
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
                        String firstCommand = unit.commands.isEmpty() ? "" : unit.commands.get(0).command;
                        if (unit.modifier == SpecialUnits.KeyModifiers.NONE) {
                            displayText = trans(key + ".@Display",
                                    "§6" + InputConstants.getKey(unit.key).getDisplayName().getString(), firstCommand);
                        } else {
                            displayText = trans(key + ".@Display",
                                    "§6" + unit.modifier + " + " + InputConstants.getKey(unit.key).getDisplayName()
                                            .getString(), firstCommand);
                        }
                    }

                    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
                    SpecialUnits.MacroUnit defaultObj = new SpecialUnits.MacroUnit();

                    entries.add(eb.startKeyCodeField(trans(key + ".Key"), InputConstants.getKey(unit.key))
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Key", "keycode", InputConstants.getKey(defaultObj.key)))
                            .setDefaultValue(InputConstants.getKey(defaultObj.key))
//? if >=1.18 {
                            .setKeySaveConsumer
//?} elif >=1.17 {
                            /*// In MC 1.17.X, we use ClothConfig v5.
                            // In ClothConfig v5 (discontinued) we use the `setSaveConsumer()` method.
                            .setSaveConsumer
*///?} else {
                            /*.setKeySaveConsumer
                             *///?}
                                    (k -> unit.key = k.getName()).build());

                    entries.add(eb.startEnumSelector(trans(key + ".Modifier"), SpecialUnits.KeyModifiers.class,
                                    unit.modifier).setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Modifier", "EnumKeyModifiers", defaultObj.modifier))
                            .setDefaultValue(defaultObj.modifier).setSaveConsumer(v -> unit.modifier = v).build());

                    entries.add(eb.startEnumSelector(trans(key + ".Mode"), SpecialUnits.MacroModes.class, unit.mode)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Mode", "EnumMacroModes", defaultObj.mode))
                            .setDefaultValue(defaultObj.mode).setSaveConsumer(v -> unit.mode = v).build());

                    String commandsKey = key + ".Commands";
                    entries.add(new NestedListListEntry<SpecialUnits.MacroCommandEntry, MultiElementListEntry<SpecialUnits.MacroCommandEntry>>(
                            trans(commandsKey), unit.commands, false,
                            () -> Optional.of(new Component[]{trans(commandsKey + ".@Tooltip")}),
                            v -> { unit.commands.clear(); unit.commands.addAll(v); },
                            () -> defaultObj.commands,
                            eb.getResetButtonKey(), true, false, (passedCmd, ignored2) -> {
                        SpecialUnits.MacroCommandEntry cmd = (passedCmd == null) ? new SpecialUnits.MacroCommandEntry() : passedCmd;

                        Component cmdDisplayText;
                        if (passedCmd == null || cmd.command.isEmpty()) {
                            cmdDisplayText = trans(commandsKey + ".@New");
                        } else {
                            cmdDisplayText = trans(commandsKey + ".@Display", cmd.command);
                        }

                        List<AbstractConfigListEntry<?>> cmdEntries = new ArrayList<>();
                        SpecialUnits.MacroCommandEntry cmdDefault = new SpecialUnits.MacroCommandEntry();

                        cmdEntries.add(eb.startStrField(trans(commandsKey + ".Command"), cmd.command)
                                .setTooltip(ConfigScreenTooltipUtils.getTooltip(commandsKey + ".Command", "String", cmdDefault.command))
                                .setDefaultValue(cmdDefault.command).setSaveConsumer(v -> cmd.command = v).build());

                        cmdEntries.add(eb.startLongField(trans(commandsKey + ".DelayInMilliseconds"), cmd.delayInMilliseconds)
                                .setTooltip(ConfigScreenTooltipUtils.getTooltip(commandsKey + ".DelayInMilliseconds", "longField", cmdDefault.delayInMilliseconds))
                                .setDefaultValue(cmdDefault.delayInMilliseconds)
                                .setSaveConsumer(v -> cmd.delayInMilliseconds = v).build());

                        cmdEntries.add(eb.startBooleanToggle(trans(commandsKey + ".ForceDisableFormatter"), cmd.forceDisableFormatter)
                                .setTooltip(ConfigScreenTooltipUtils.getTooltip(commandsKey + ".ForceDisableFormatter", "boolean", cmdDefault.forceDisableFormatter))
                                .setDefaultValue(cmdDefault.forceDisableFormatter)
                                .setSaveConsumer(v -> cmd.forceDisableFormatter = v).build());

                        return new MultiElementListEntry<>(cmdDisplayText, cmd, cmdEntries, SHOULD_EXPAND_ALL_RULES);
                    }));

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
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Address", "String", defaultObj.address))
                            .setDefaultValue(defaultObj.address).setSaveConsumer(v -> unit.address = v)
                            .setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());

                    entries.add(eb.startStrField(trans(key + ".Formatter"), unit.formatter)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Formatter", "String", defaultObj.formatter))
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
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Address", "String", defaultObj.address))
                            .setDefaultValue(defaultObj.address).setSaveConsumer(v -> unit.address = v)
                            .setErrorSupplier(ConfigScreenTooltipUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());

                    entries.add(eb.startStrField(trans(key + ".Message"), unit.message)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".Message", "String", defaultObj.message))
                            .setDefaultValue(defaultObj.message).setSaveConsumer(v -> unit.message = v).build());

                    entries.add(eb.startLongField(trans(key + ".DelayInMilliseconds"), unit.delayInMilliseconds)
                            .setTooltip(ConfigScreenTooltipUtils.getTooltip(key + ".DelayInMilliseconds", "longField", defaultObj.delayInMilliseconds))
                            .setDefaultValue(defaultObj.delayInMilliseconds)
                            .setSaveConsumer(v -> unit.delayInMilliseconds = v).build());

                    entries.add(eb.startBooleanToggle(trans(key + ".ForceDisableFormatter"), unit.forceDisableFormatter)
                            .setTooltip(
                                    ConfigScreenTooltipUtils.getTooltip(key + ".ForceDisableFormatter", "boolean", defaultObj.forceDisableFormatter))
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
}