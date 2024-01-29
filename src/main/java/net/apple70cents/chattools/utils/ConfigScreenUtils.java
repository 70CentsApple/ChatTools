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
    public static TooltipListEntry getEntryBuilder(ConfigEntryBuilder eb, String type, String key, int... args) {
        final Text TOOLTIP = trans(key + ".@Tooltip");
        // display current server (if it can be used)
        final Text SERVER_LABELED_KEY = trans(key, "§f" + ((MinecraftClient.getInstance()
                                                                           .getCurrentServerEntry() == null) ? "-" : MinecraftClient
                .getInstance().getCurrentServerEntry().address));
        switch (type) {
            case "boolean":
                return eb.startBooleanToggle(trans(key), (boolean) CONFIG.get(key))
                         .setDefaultValue((boolean) DEFAULT_CONFIG.get(key)).setTooltip(TOOLTIP)
                         .setSaveConsumer(v -> CONFIG.set(key, v)).build();
            case "String":
                return eb.startStrField(trans(key), (String) CONFIG.get(key))
                         .setDefaultValue((String) DEFAULT_CONFIG.get(key)).setTooltip(TOOLTIP)
                         .setSaveConsumer(v -> CONFIG.set(key, v)).build();
            case "intSlider":
                return eb.startIntSlider(trans(key), ((Number) CONFIG.get(key)).intValue(), args[0], args[1])
                         .setDefaultValue(((Number) DEFAULT_CONFIG.get(key)).intValue()).setTooltip(TOOLTIP)
                         .setSaveConsumer(v -> CONFIG.set(key, (Number) v)).build();
            case "keycode":
                return eb.startKeyCodeField(trans(key), InputUtil.fromTranslationKey((String) CONFIG.get(key)))
                         .setDefaultValue(InputUtil.fromTranslationKey((String) DEFAULT_CONFIG.get(key)))
                         .setTooltip(TOOLTIP)
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
                         .setDefaultValue((List<String>) DEFAULT_CONFIG.get(key)).setTooltip(TOOLTIP)
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
                    () -> Optional.of(new net.minecraft.text.Text[]{TOOLTIP}),
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
                                      .setTooltip(trans(key + ".Address.@Tooltip")).setDefaultValue(defaultRule.address)
                                      .setSaveConsumer(v -> bubbleRuleUnitRef.get().address = v)
                                      .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                add(eb.startStrField(trans(key + ".Pattern"), defaultRule.pattern)
                                      .setTooltip(trans(key + ".Pattern.@Tooltip")).setDefaultValue(defaultRule.pattern)
                                      .setSaveConsumer(v -> bubbleRuleUnitRef.get().pattern = v)
                                      .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());
                                add(eb.startBooleanToggle(trans(key + ".Fallback"), defaultRule.fallback)
                                      .setTooltip(trans(key + ".Fallback.@Tooltip")).setDefaultValue(defaultRule.fallback)
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
                                      .setTooltip(trans(key + ".Address.@Tooltip"))
                                      .setDefaultValue(new SpecialUnits.BubbleRuleUnit().address)
                                      .setSaveConsumer(v -> bubbleRuleUnit.address = v)
                                      .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                add(eb.startStrField(trans(key + ".Pattern"), bubbleRuleUnit.pattern)
                                      .setTooltip(trans(key + ".Pattern.@Tooltip"))
                                      .setDefaultValue(new SpecialUnits.BubbleRuleUnit().pattern)
                                      .setSaveConsumer(v -> bubbleRuleUnit.pattern = v)
                                      .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());
                                add(eb.startBooleanToggle(trans(key + ".Fallback"), bubbleRuleUnit.fallback)
                                      .setTooltip(trans(key + ".Fallback.@Tooltip"))
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
                        () -> Optional.of(new net.minecraft.text.Text[]{TOOLTIP}),
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
                                          .setTooltip(trans(key + ".Address.@Tooltip")).setDefaultValue(defaultRule.address)
                                          .setSaveConsumer(v -> responserUnitRef.get().address = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Pattern"), defaultRule.pattern)
                                          .setTooltip(trans(key + ".Pattern.@Tooltip")).setDefaultValue(defaultRule.pattern)
                                          .setSaveConsumer(v -> responserUnitRef.get().pattern = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER).build());
                                    add(eb.startStrField(trans(key + ".Message"), defaultRule.message)
                                          .setTooltip(trans(key + ".Message.@Tooltip")).setDefaultValue(defaultRule.message)
                                          .setSaveConsumer(v -> responserUnitRef.get().message = v).build());
                                    add(eb
                                            .startBooleanToggle(trans(key + ".ForceDisableFormatter"), defaultRule.forceDisableFormatter)
                                            .setTooltip(trans(key + ".ForceDisableFormatter.@Tooltip"))
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
                                          .setTooltip(trans(key + ".Address.@Tooltip"))
                                          .setDefaultValue(new SpecialUnits.ResponserRuleUnit().address)
                                          .setSaveConsumer(v -> responserRuleUnit.address = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Pattern"), responserRuleUnit.pattern)
                                          .setTooltip(trans(key + ".Pattern.@Tooltip"))
                                          .setDefaultValue(new SpecialUnits.ResponserRuleUnit().pattern)
                                          .setSaveConsumer(v -> responserRuleUnit.pattern = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER).build());
                                    add(eb.startStrField(trans(key + ".Message"), responserRuleUnit.message)
                                          .setTooltip(trans(key + ".Message.@Tooltip"))
                                          .setDefaultValue(new SpecialUnits.ResponserRuleUnit().message)
                                          .setSaveConsumer(v -> responserRuleUnit.message = v).build());
                                    add(eb
                                            .startBooleanToggle(trans(key + ".ForceDisableFormatter"), responserRuleUnit.forceDisableFormatter)
                                            .setTooltip(trans(key + ".ForceDisableFormatter.@Tooltip"))
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
                        () -> Optional.of(new net.minecraft.text.Text[]{TOOLTIP}),
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
                                            .setTooltip(trans(key + ".Key.@Tooltip"))
                                            .setDefaultValue(InputUtil.fromTranslationKey(defaultMacro.key))
                                            //#if MC>=11800
                                            .setKeySaveConsumer
                                            //#elseif MC>=11700
                                            // In MC 1.17.X, we use ClothConfig v5, the discontinued version that uses `setSaveConsumer()` method.
                                            //$$ .setSaveConsumer
                                            //#else
                                            //$$ .setKeySaveConsumer
                                            //#endif
                                                    (key -> macroUnitRef.get().key = key.getTranslationKey()).build());
                                    add(eb
                                            .startEnumSelector(trans(key + ".Modifier"), SpecialUnits.KeyModifiers.class, defaultMacro.modifier)
                                            .setTooltip(trans(key + ".Modifier.@Tooltip"))
                                            .setDefaultValue(defaultMacro.modifier)
                                            .setSaveConsumer(v -> macroUnitRef.get().modifier = v).build());
                                    add(eb
                                            .startEnumSelector(trans(key + ".Mode"), SpecialUnits.MacroModes.class, defaultMacro.mode)
                                            .setDefaultValue(defaultMacro.mode)
                                            .setSaveConsumer(v -> macroUnitRef.get().mode = v)
                                            .setTooltip(trans(key + ".Mode.@Tooltip")).build());
                                    add(eb.startStrField(trans(key + ".Command"), defaultMacro.command)
                                          .setDefaultValue(defaultMacro.command).setTooltip(trans(key + ".Command.@Tooltip"))
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
                                          .setTooltip(trans(key + ".Key.@Tooltip"))
                                          .setDefaultValue(InputUtil.fromTranslationKey(macroUnit.key))
                                          //#if MC>=11800
                                            .setKeySaveConsumer
                                            //#elseif MC>=11700
                                            // In MC 1.17.X, we use ClothConfig v5, where the discontinued version uses `setSaveConsumer()` method.
                                            //$$ .setSaveConsumer
                                            //#else
                                            //$$ .setKeySaveConsumer
                                            //#endif
                                                    (key -> macroUnit.key = key.getTranslationKey()).build());
                                    add(eb
                                            .startEnumSelector(trans(key + ".Modifier"), SpecialUnits.KeyModifiers.class, macroUnit.modifier)
                                            .setTooltip(trans(key + ".Modifier.@Tooltip")).setDefaultValue(macroUnit.modifier)
                                            .setSaveConsumer(v -> macroUnit.modifier = v).build());
                                    add(eb
                                            .startEnumSelector(trans(key + ".Mode"), SpecialUnits.MacroModes.class, macroUnit.mode)
                                            .setDefaultValue(macroUnit.mode).setSaveConsumer(v -> macroUnit.mode = v)
                                            .setTooltip(trans(key + ".Mode.@Tooltip")).build());
                                    add(eb.startStrField(trans(key + ".Command"), macroUnit.command)
                                          .setDefaultValue(macroUnit.command).setTooltip(trans(key + ".Command.@Tooltip"))
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
                        () -> Optional.of(new net.minecraft.text.Text[]{TOOLTIP}),
                        v -> CONFIG.set(key, v),
                        () -> SpecialUnits.FormatterUnit.fromList((List) DEFAULT_CONFIG.get(key)),
                        eb.getResetButtonKey(),
                        true,
                        true,
                        (injectorUnit, ignored) -> {
                            AtomicReference<SpecialUnits.FormatterUnit> injectorUnitRef = new AtomicReference<>(injectorUnit);
                            if (injectorUnit == null) {
                                Text displayText = trans(key + ".@New");
                                SpecialUnits.FormatterUnit defaultInjectorRule = new SpecialUnits.FormatterUnit();
                                injectorUnitRef.set(defaultInjectorRule);
                                return new MultiElementListEntry<>(displayText, defaultInjectorRule, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), defaultInjectorRule.address)
                                          .setTooltip(trans(key + ".Address.@Tooltip"))
                                          .setDefaultValue(defaultInjectorRule.address)
                                          .setSaveConsumer(v -> injectorUnitRef.get().address = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Formatter"), defaultInjectorRule.formatter)
                                          .setTooltip(trans(key + ".Formatter.@Tooltip"))
                                          .setDefaultValue(defaultInjectorRule.formatter)
                                          .setSaveConsumer(v -> injectorUnitRef.get().formatter = v).build());
                                }}, false);
                            } else { // 现有
                                String colorPrefix = ("*".equals(injectorUnit.address) || (MinecraftClient.getInstance()
                                                                                                          .getCurrentServerEntry() != null && Pattern
                                        .compile(injectorUnit.address)
                                        .matcher(MinecraftClient.getInstance().getCurrentServerEntry().address)
                                        .matches())) ? "§a" : "§6";
                                Text displayText = trans(key + ".@Display", colorPrefix + injectorUnit.address, injectorUnit.formatter);
                                return new MultiElementListEntry<>(displayText, injectorUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), injectorUnit.address)
                                          .setTooltip(trans(key + ".Address.@Tooltip"))
                                          .setDefaultValue(new SpecialUnits.FormatterUnit().address)
                                          .setSaveConsumer(v -> injectorUnit.address = v)
                                          .setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Formatter"), injectorUnit.formatter)
                                          .setTooltip(trans(key + ".Formatter.@Tooltip"))
                                          .setDefaultValue(new SpecialUnits.FormatterUnit().formatter)
                                          .setSaveConsumer(v -> injectorUnit.formatter = v).build());
                                }}, false);
                            }
                        }
                    );
            // @formatter:on
            case "EnumKeyModifiers":
                return eb
                        .startEnumSelector(trans(key), SpecialUnits.KeyModifiers.class, SpecialUnits.KeyModifiers.valueOf((String) CONFIG.get(key)))
                        .setDefaultValue(SpecialUnits.KeyModifiers.valueOf((String) DEFAULT_CONFIG.get(key)))
                        .setTooltip(TOOLTIP).setSaveConsumer(v -> CONFIG.set(key, v.toString())).build();
            case "EnumToastModes":
                return eb
                        .startEnumSelector(trans(key), SpecialUnits.ToastModes.class, SpecialUnits.ToastModes.valueOf((String) CONFIG.get(key)))
                        .setDefaultValue(SpecialUnits.ToastModes.valueOf((String) DEFAULT_CONFIG.get(key)))
                        .setTooltip(TOOLTIP).setSaveConsumer(v -> CONFIG.set(key, v.toString())).build();
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
