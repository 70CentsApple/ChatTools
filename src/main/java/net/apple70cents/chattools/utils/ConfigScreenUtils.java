package net.apple70cents.chattools.utils;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.apple70cents.chattools.config.SpecialUnits;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static net.apple70cents.chattools.ChatTools.CONFIG;
import static net.apple70cents.chattools.ChatTools.DEFAULT_CONFIG;
import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * @author 70CentsApple
 */
public class ConfigScreenUtils {
    public static Text getTooltip(String key, String variableType) {
        return getTooltip(key, variableType, DEFAULT_CONFIG.get(key));
    }

    public static Text getTooltip(String key, String variableType, Object defaultVal) {
        boolean isNull = (defaultVal == null || defaultVal.toString().isBlank());
        String defaultValue = isNull ? "NULL" : defaultVal.toString();
        // check if F3+H is on
        if (MinecraftClient.getInstance().options.advancedItemTooltips) {
            try {
                if (variableType.endsWith("List")) {
                    if (!((List<?>) DEFAULT_CONFIG.get(key)).isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("[");
                        for (int i = 0; i < ((List<?>) DEFAULT_CONFIG.get(key)).size(); i++) {
                            String ele = ((List<?>) DEFAULT_CONFIG.get(key)).get(i).toString();
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
            Text defaults = ((MutableText) TextUtils.trans("texts.defaultValue", defaultValue)).formatted(Formatting.GRAY);

            Text keyName = ((MutableText) TextUtils.of(key)).formatted(Formatting.GOLD);
            Text main = ((MutableText) trans(key + ".@Tooltip")).formatted(Formatting.WHITE);
            Text type = ((MutableText) TextUtils.trans("texts.variableType", variableType)).formatted(Formatting.GRAY);
            MutableText tooltip = (MutableText) TextUtils.empty();
            tooltip.append(keyName).append("§r\n").append(main).append("§r\n").append(type).append("§r\n")
                   .append(defaults);
            return tooltip;
        } else {
            return trans(key + ".@Tooltip");
        }
    }

    // the `args` are only for `min` and `max` value for int sliders (recently)
    public static TooltipListEntry getEntryBuilder(ConfigEntryBuilder eb, String type, String key, int... args) {
        Text tooltip = getTooltip(key, type);
        // display current server (if it can be used)
        final Text SERVER_LABELED_KEY = trans(key, "§f" + ((MinecraftClient.getInstance()
                                                                           .getCurrentServerEntry() == null) ? "-" : MinecraftClient
                .getInstance().getCurrentServerEntry().address));
        switch (type) {
            case "boolean":
                return eb.startBooleanToggle(trans(key), (boolean) CONFIG.get(key))
                         .setDefaultValue((boolean) DEFAULT_CONFIG.get(key)).setTooltip(tooltip)
                         .setSaveConsumer(v -> CONFIG.set(key, v)).build();
            case "String":
                return eb.startStrField(trans(key), (String) CONFIG.get(key))
                         .setDefaultValue((String) DEFAULT_CONFIG.get(key)).setTooltip(tooltip)
                         .setSaveConsumer(v -> CONFIG.set(key, v)).build();
            case "intSlider":
                return eb.startIntSlider(trans(key), ((Number) CONFIG.get(key)).intValue(), args[0], args[1])
                         .setDefaultValue(((Number) DEFAULT_CONFIG.get(key)).intValue()).setTooltip(tooltip)
                         .setSaveConsumer(v -> CONFIG.set(key, (Number) v)).build();
            case "keycode":
                return eb.startKeyCodeField(trans(key), InputUtil.fromTranslationKey((String) CONFIG.get(key)))
                         .setDefaultValue(InputUtil.fromTranslationKey((String) DEFAULT_CONFIG.get(key)))
                         .setTooltip(tooltip)
                         //#if MC>=11800
                        .setKeySaveConsumer
                        //#elseif MC>=11700
                        // In MC 1.17.X, we use ClothConfig v5, where the discontinued version uses `setSaveConsumer()` method.
                        //$$ .setSaveConsumer
                        //#else
                        //$$ .setKeySaveConsumer
                        //#endif
                                (keybind -> CONFIG.set(key, keybind.getTranslationKey())).build();
            case "StringList":
                return eb.startStrList(trans(key), (List<String>) CONFIG.get(key))
                         .setDefaultValue((List<String>) DEFAULT_CONFIG.get(key)).setTooltip(tooltip)
                         .setSaveConsumer(v -> CONFIG.set(key, v)).build();
            case "FAQ":
                return eb.startTextDescription(((MutableText) trans("faq")).setStyle(TextUtils.WEBSITE_URL_STYLE))
                         .build();
            // @formatter:off
            case "BubbleList":
                return new NestedListListEntry<SpecialUnits.BubbleRuleUnit, MultiElementListEntry<SpecialUnits.BubbleRuleUnit>>
                    (SERVER_LABELED_KEY,
                    SpecialUnits.BubbleRuleUnit.fromList((List) CONFIG.get(key)),
                    true,
                    () -> Optional.of(new net.minecraft.text.Text[]{tooltip}),
                    v -> CONFIG.set(key, v),
                    () -> SpecialUnits.BubbleRuleUnit.fromList((List) DEFAULT_CONFIG.get(key)),
                    eb.getResetButtonKey(), true, true, (bubbleRuleUnit, ignored) -> {
                        AtomicReference<SpecialUnits.BubbleRuleUnit> bubbleRuleUnitRef = new AtomicReference<>(bubbleRuleUnit);
                        if (bubbleRuleUnit == null) {
                            Text displayText = trans(key + ".@New");
                            SpecialUnits.BubbleRuleUnit defaultRule = new SpecialUnits.BubbleRuleUnit();
                            bubbleRuleUnitRef.set(defaultRule);
                            return new MultiElementListEntry<>(displayText, defaultRule, new ArrayList<AbstractConfigListEntry<?>>() {{
                                add(eb.startStrField(trans(key + ".Address"), defaultRule.address)
                                      .setTooltip(getTooltip(key + ".Address", "String", defaultRule.address))
                                      .setDefaultValue(defaultRule.address)
                                      .setSaveConsumer(v -> bubbleRuleUnitRef.get().address = v)
                                      .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                add(eb.startStrField(trans(key + ".Pattern"), defaultRule.pattern)
                                      .setTooltip(getTooltip(key + ".Pattern", "String", defaultRule.pattern))
                                      .setDefaultValue(defaultRule.pattern)
                                      .setSaveConsumer(v -> bubbleRuleUnitRef.get().pattern = v)
                                      .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());
                                add(eb.startBooleanToggle(trans(key + ".Fallback"), defaultRule.fallback)
                                      .setTooltip(getTooltip(key + ".Fallback", "boolean", defaultRule.fallback))
                                      .setDefaultValue(defaultRule.fallback)
                                      .setSaveConsumer(v -> bubbleRuleUnitRef.get().fallback = v).build());
                            }}, false);
                        } else {
                            String colorPrefix = ("*".equals(bubbleRuleUnit.address) || (MinecraftClient.getInstance()
                                                                                                        .getCurrentServerEntry() != null && Pattern
                                    .compile(bubbleRuleUnit.address)
                                    .matcher(MinecraftClient.getInstance().getCurrentServerEntry().address)
                                    .matches())) ? "§a" : "§6";
                            Text displayText = trans(key + ".@Display", colorPrefix + bubbleRuleUnit.address, bubbleRuleUnit.fallback ? "§a✔" : "§c✘", bubbleRuleUnit.pattern);
                            return new MultiElementListEntry<>(displayText, bubbleRuleUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                add(eb.startStrField(trans(key + ".Address"), bubbleRuleUnit.address)
                                      .setTooltip(getTooltip(key + ".Address", "String", new SpecialUnits.BubbleRuleUnit().address))
                                      .setDefaultValue(new SpecialUnits.BubbleRuleUnit().address)
                                      .setSaveConsumer(v -> bubbleRuleUnit.address = v)
                                      .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                add(eb.startStrField(trans(key + ".Pattern"), bubbleRuleUnit.pattern)
                                      .setTooltip(getTooltip(key + ".Pattern", "String", new SpecialUnits.BubbleRuleUnit().pattern))
                                      .setDefaultValue(new SpecialUnits.BubbleRuleUnit().pattern)
                                      .setSaveConsumer(v -> bubbleRuleUnit.pattern = v)
                                      .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());
                                add(eb.startBooleanToggle(trans(key + ".Fallback"), bubbleRuleUnit.fallback)
                                      .setTooltip(getTooltip(key + ".Fallback", "boolean", new SpecialUnits.BubbleRuleUnit().fallback))
                                      .setDefaultValue(new SpecialUnits.BubbleRuleUnit().fallback)
                                      .setSaveConsumer(v -> bubbleRuleUnit.fallback = v).build());
                            }}, false);
                        }
                    }
                );
            case "ResponserList":
                return new NestedListListEntry<SpecialUnits.ResponserRuleUnit, MultiElementListEntry<SpecialUnits.ResponserRuleUnit>>
                    (SERVER_LABELED_KEY,
                        SpecialUnits.ResponserRuleUnit.fromList((List) CONFIG.get(key)),
                        true,
                        () -> Optional.of(new net.minecraft.text.Text[]{tooltip}),
                        v -> CONFIG.set(key, v),
                        () -> SpecialUnits.ResponserRuleUnit.fromList((List) DEFAULT_CONFIG.get(key)),
                        eb.getResetButtonKey(),
                        true,
                        true, (responserRuleUnit, ignored) -> {
                            AtomicReference<SpecialUnits.ResponserRuleUnit> responserUnitRef = new AtomicReference<>(new SpecialUnits.ResponserRuleUnit());
                            if (responserRuleUnit == null) {
                                Text displayText = trans(key + ".@New");
                                SpecialUnits.ResponserRuleUnit defaultRule = new SpecialUnits.ResponserRuleUnit();
                                responserUnitRef.set(defaultRule);
                                return new MultiElementListEntry<>(displayText, defaultRule, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), defaultRule.address)
                                          .setTooltip(getTooltip(key + ".Address", "String", defaultRule.address))
                                          .setDefaultValue(defaultRule.address)
                                          .setSaveConsumer(v -> responserUnitRef.get().address = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Pattern"), defaultRule.pattern)
                                          .setTooltip(getTooltip(key + ".Pattern", "String", defaultRule.pattern))
                                          .setDefaultValue(defaultRule.pattern)
                                          .setSaveConsumer(v -> responserUnitRef.get().pattern = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER).build());
                                    add(eb.startStrField(trans(key + ".Message"), defaultRule.message)
                                          .setTooltip(getTooltip(key + ".Message", "String", defaultRule.message))
                                          .setDefaultValue(defaultRule.message)
                                          .setSaveConsumer(v -> responserUnitRef.get().message = v).build());
                                    add(eb
                                            .startBooleanToggle(trans(key + ".ForceDisableFormatter"), defaultRule.forceDisableFormatter)
                                            .setTooltip(getTooltip(key + ".ForceDisableFormatter", "boolean", defaultRule.forceDisableFormatter))
                                            .setDefaultValue(defaultRule.forceDisableFormatter)
                                            .setSaveConsumer(v -> responserUnitRef.get().forceDisableFormatter = v).build());
                                }}, false);
                            } else {
                                String colorPrefix = ("*".equals(responserRuleUnit.address) || (MinecraftClient.getInstance()
                                                                                                               .getCurrentServerEntry() != null && Pattern
                                        .compile(responserRuleUnit.address)
                                        .matcher(MinecraftClient.getInstance().getCurrentServerEntry().address)
                                        .matches())) ? "§a" : "§6";
                                Text displayText = trans(key + ".@Display", colorPrefix + responserRuleUnit.address, responserRuleUnit.forceDisableFormatter ? "§a✔" : "§c✘", responserRuleUnit.pattern, responserRuleUnit.message);
                                return new MultiElementListEntry<>(displayText, responserRuleUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), responserRuleUnit.address)
                                          .setTooltip(getTooltip(key + ".Address", "String", new SpecialUnits.ResponserRuleUnit().address))
                                          .setDefaultValue(new SpecialUnits.ResponserRuleUnit().address)
                                          .setSaveConsumer(v -> responserRuleUnit.address = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Pattern"), responserRuleUnit.pattern)
                                          .setTooltip(getTooltip(key + ".Pattern", "String", new SpecialUnits.ResponserRuleUnit().pattern))
                                          .setDefaultValue(new SpecialUnits.ResponserRuleUnit().pattern)
                                          .setSaveConsumer(v -> responserRuleUnit.pattern = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER).build());
                                    add(eb.startStrField(trans(key + ".Message"), responserRuleUnit.message)
                                          .setTooltip(getTooltip(key + ".Message", "String", new SpecialUnits.ResponserRuleUnit().message))
                                          .setDefaultValue(new SpecialUnits.ResponserRuleUnit().message)
                                          .setSaveConsumer(v -> responserRuleUnit.message = v).build());
                                    add(eb
                                            .startBooleanToggle(trans(key + ".ForceDisableFormatter"), responserRuleUnit.forceDisableFormatter)
                                            .setTooltip(getTooltip(key + ".ForceDisableFormatter", "boolean", new SpecialUnits.ResponserRuleUnit().forceDisableFormatter))
                                            .setDefaultValue(new SpecialUnits.ResponserRuleUnit().forceDisableFormatter)
                                            .setSaveConsumer(v -> responserRuleUnit.forceDisableFormatter = v).build());
                                }}, false);
                            }
                        }
                    );
            case "MacroList":
                return new NestedListListEntry<SpecialUnits.MacroUnit, MultiElementListEntry<SpecialUnits.MacroUnit>>
                    (trans(key),
                        SpecialUnits.MacroUnit.fromList((List) CONFIG.get(key)),
                        true,
                        () -> Optional.of(new net.minecraft.text.Text[]{tooltip}),
                        v -> CONFIG.set(key, v),
                        () -> SpecialUnits.MacroUnit.fromList((List) DEFAULT_CONFIG.get(key)),
                        eb.getResetButtonKey(),
                        true,
                        true, (macroUnit, ignored) -> {
                            AtomicReference<SpecialUnits.MacroUnit> macroUnitRef = new AtomicReference<>(macroUnit);
                            if (macroUnit == null) {
                                Text displayText = trans(key + ".@New");
                                SpecialUnits.MacroUnit defaultMacro = new SpecialUnits.MacroUnit();
                                macroUnitRef.set(defaultMacro); // 设置初始值
                                return new MultiElementListEntry<>(displayText, defaultMacro, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb
                                            .startKeyCodeField(trans(key + ".Key"), InputUtil.fromTranslationKey(defaultMacro.key))
                                            .setTooltip(getTooltip(key + ".Key", "keycode", InputUtil.fromTranslationKey(defaultMacro.key)))
                                            .setDefaultValue(InputUtil.fromTranslationKey(defaultMacro.key))
                                            //#if MC>=11800
                                            .setKeySaveConsumer
                                            //#elseif MC>=11700
                                            // In MC 1.17.X, we use ClothConfig v5.
                                            // In ClothConfig v5 (discontinued) we use the `setSaveConsumer()` method.
                                            //$$ .setSaveConsumer
                                            //#else
                                            //$$ .setKeySaveConsumer
                                            //#endif
                                                    (key -> macroUnitRef.get().key = key.getTranslationKey()).build());
                                    add(eb
                                            .startEnumSelector(trans(key + ".Modifier"), SpecialUnits.KeyModifiers.class, defaultMacro.modifier)
                                            .setTooltip(getTooltip(key + ".Modifier", "EnumKeyModifiers", defaultMacro.modifier))
                                            .setDefaultValue(defaultMacro.modifier)
                                            .setSaveConsumer(v -> macroUnitRef.get().modifier = v).build());
                                    add(eb
                                            .startEnumSelector(trans(key + ".Mode"), SpecialUnits.MacroModes.class, defaultMacro.mode)
                                            .setTooltip(getTooltip(key + ".Mode", "EnumMacroModes", defaultMacro.mode))
                                            .setDefaultValue(defaultMacro.mode)
                                            .setSaveConsumer(v -> macroUnitRef.get().mode = v).build());
                                    add(eb.startStrField(trans(key + ".Command"), defaultMacro.command)
                                          .setTooltip(getTooltip(key + ".Command", "String", defaultMacro.command))
                                          .setDefaultValue(defaultMacro.command)
                                          .setSaveConsumer(v -> macroUnitRef.get().command = v).build());
                                }}, false);
                            } else {
                                Text displayText;
                                if (macroUnit.key.equals(InputUtil.UNKNOWN_KEY.getTranslationKey())) {
                                    displayText = trans(key + ".@New");
                                } else if (macroUnit.modifier == SpecialUnits.KeyModifiers.NONE) {
                                    displayText = trans(key + ".@Display",
                                            // such as "[ H ] /home"
                                            "§6" + InputUtil.fromTranslationKey(macroUnit.key).getLocalizedText()
                                                            .getString(), macroUnit.command);
                                } else {
                                    displayText = trans(key + ".@Display",
                                            // such as "[ Shift + B ] /back"
                                            "§6" + macroUnit.modifier + " + " + InputUtil.fromTranslationKey(macroUnit.key)
                                                                                         .getLocalizedText()
                                                                                         .getString(), macroUnit.command);
                                }
                                return new MultiElementListEntry<>(displayText, macroUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startKeyCodeField(trans(key + ".Key"), InputUtil.fromTranslationKey(macroUnit.key))
                                          .setTooltip(getTooltip(key + ".Key", "keycode", InputUtil.fromTranslationKey(new SpecialUnits.MacroUnit().key)))
                                          .setDefaultValue(InputUtil.fromTranslationKey(new SpecialUnits.MacroUnit().key))
                                          //#if MC>=11800
                                            .setKeySaveConsumer
                                            //#elseif MC>=11700
                                            // In MC 1.17.X, we use ClothConfig v5.
                                            // In ClothConfig v5 (discontinued) we use the `setSaveConsumer()` method.
                                            //$$ .setSaveConsumer
                                            //#else
                                            //$$ .setKeySaveConsumer
                                            //#endif
                                                    (key -> macroUnit.key = key.getTranslationKey()).build());
                                    add(eb
                                            .startEnumSelector(trans(key + ".Modifier"), SpecialUnits.KeyModifiers.class, macroUnit.modifier)
                                            .setTooltip(getTooltip(key + ".Modifier", "EnumKeyModifiers", new SpecialUnits.MacroUnit().modifier))
                                            .setDefaultValue(new SpecialUnits.MacroUnit().modifier)
                                            .setSaveConsumer(v -> macroUnit.modifier = v).build());
                                    add(eb
                                            .startEnumSelector(trans(key + ".Mode"), SpecialUnits.MacroModes.class, macroUnit.mode)
                                            .setTooltip(getTooltip(key + ".Mode", "EnumMacroModes", new SpecialUnits.MacroUnit().mode))
                                            .setDefaultValue(new SpecialUnits.MacroUnit().mode).setSaveConsumer(v -> macroUnit.mode = v)
                                            .setSaveConsumer(v -> macroUnit.mode = v).build());
                                    add(eb.startStrField(trans(key + ".Command"), macroUnit.command)
                                            .setTooltip(getTooltip(key + ".Command", "String", new SpecialUnits.MacroUnit().command))
                                            .setDefaultValue(new SpecialUnits.MacroUnit().command)
                                            .setSaveConsumer(v -> macroUnit.command = v).build());
                                }}, false);
                            }
                        }
                    );
            case "FormatterList":
                return new NestedListListEntry<SpecialUnits.FormatterUnit, MultiElementListEntry<SpecialUnits.FormatterUnit>>
                    (SERVER_LABELED_KEY,
                        SpecialUnits.FormatterUnit.fromList((List) CONFIG.get(key)),
                        true,
                        () -> Optional.of(new net.minecraft.text.Text[]{tooltip}),
                        v -> CONFIG.set(key, v),
                        () -> SpecialUnits.FormatterUnit.fromList((List) DEFAULT_CONFIG.get(key)),
                        eb.getResetButtonKey(),
                        true,
                        true,
                        (formatterUnit, ignored) -> {
                            AtomicReference<SpecialUnits.FormatterUnit> formatterUnitRef = new AtomicReference<>(formatterUnit);
                            if (formatterUnit == null) {
                                Text displayText = trans(key + ".@New");
                                SpecialUnits.FormatterUnit defaultFormatterRule = new SpecialUnits.FormatterUnit();
                                formatterUnitRef.set(defaultFormatterRule);
                                return new MultiElementListEntry<>(displayText, defaultFormatterRule, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), defaultFormatterRule.address)
                                          .setTooltip(getTooltip(key + ".Address", "String", defaultFormatterRule.address))
                                          .setDefaultValue(defaultFormatterRule.address)
                                          .setSaveConsumer(v -> formatterUnitRef.get().address = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Formatter"), defaultFormatterRule.formatter)
                                          .setTooltip(getTooltip(key + ".Formatter", "String", defaultFormatterRule.formatter))
                                          .setDefaultValue(defaultFormatterRule.formatter)
                                          .setSaveConsumer(v -> formatterUnitRef.get().formatter = v).build());
                                }}, false);
                            } else {
                                String colorPrefix = ("*".equals(formatterUnit.address) || (MinecraftClient.getInstance()
                                                                                                          .getCurrentServerEntry() != null && Pattern
                                        .compile(formatterUnit.address)
                                        .matcher(MinecraftClient.getInstance().getCurrentServerEntry().address)
                                        .matches())) ? "§a" : "§6";
                                Text displayText = trans(key + ".@Display", colorPrefix + formatterUnit.address, formatterUnit.formatter);
                                return new MultiElementListEntry<>(displayText, formatterUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), formatterUnit.address)
                                          .setTooltip(getTooltip(key + ".Address", "String", new SpecialUnits.FormatterUnit().address))
                                          .setDefaultValue(new SpecialUnits.FormatterUnit().address)
                                          .setSaveConsumer(v -> formatterUnit.address = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Formatter"), formatterUnit.formatter)
                                          .setTooltip(getTooltip(key + ".Formatter", "String", new SpecialUnits.FormatterUnit().formatter))
                                          .setDefaultValue(new SpecialUnits.FormatterUnit().formatter)
                                          .setSaveConsumer(v -> formatterUnit.formatter = v).build());
                                }}, false);
                            }
                        }
                    );
            // @formatter:on
            case "EnumKeyModifiers":
                return eb
                        .startEnumSelector(trans(key), SpecialUnits.KeyModifiers.class, SpecialUnits.KeyModifiers.valueOf((String) CONFIG.get(key)))
                        .setDefaultValue(SpecialUnits.KeyModifiers.valueOf((String) DEFAULT_CONFIG.get(key)))
                        .setTooltip(tooltip).setSaveConsumer(v -> CONFIG.set(key, v.toString())).build();
            case "EnumToastModes":
                return eb
                        .startEnumSelector(trans(key), SpecialUnits.ToastModes.class, SpecialUnits.ToastModes.valueOf((String) CONFIG.get(key)))
                        .setDefaultValue(SpecialUnits.ToastModes.valueOf((String) DEFAULT_CONFIG.get(key)))
                        .setTooltip(tooltip).setSaveConsumer(v -> CONFIG.set(key, v.toString())).build();
            default:
                return null;
        }
    }

    public static final Function<String, Optional<Text>> REGEX_COMPILE_ERROR_SUPPLIER = (v) -> {
        try {
            Pattern.compile(v);
            return Optional.empty();
        } catch (PatternSyntaxException e) {
            return Optional.of(TextUtils.of(e.getDescription()));
        }
    };

    public static final Function<String, Optional<Text>> REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR = (v) -> {
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

    public static final Function<String, Optional<Text>> REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS = (v) -> {
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
}
