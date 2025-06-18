package net.apple70cents.chattools.config;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import me.shedaniel.clothconfig2.impl.builders.StringListBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.apple70cents.chattools.utils.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static net.apple70cents.chattools.utils.ConfigScreenUtils.getTooltip;
import static net.apple70cents.chattools.utils.TextUtils.trans;

public class ClothConfigScreenGenerator {
    final static boolean SHOULD_EXPAND_ALL_RULES =
            //#if MC>=12104
            true
            //#else
            //$$ false
            //#endif
            ;

    public static ConfigBuilder getConfigBuilder() {
        ConfigScreenUtils.initializeConfigGuiMapIfNecessary();
        //#if MC>=12100
        ResourceLocation backgroundTexture = ResourceLocation.parse("minecraft:textures/block/oak_planks.png");
        //#else
        //$$ ResourceLocation backgroundTexture = new ResourceLocation("minecraft:textures/block/oak_planks.png");
        //#endif
        ConfigBuilder builder = ConfigBuilder.create().setTitle(trans("gui.title"))
                                             .setDefaultBackgroundTexture(backgroundTexture)
                                             .setTransparentBackground(true).setSavingRunnable(ConfigUtils::save);
        ConfigEntryBuilder eb = builder.entryBuilder();
        for (Object categoryInfo : (List) ConfigScreenUtils.configGuiMap.get("content")) {
            ConfigCategory category = builder.getOrCreateCategory(trans((String) ((Map) categoryInfo).get("key")));
            for (Object element : (List) ((Map) categoryInfo).get("content")) {
                String type = (String) ((Map) element).get("type");
                String key = (String) ((Map) element).get("key");
                String errorSupplier = (String) ((Map) element).getOrDefault("errorSupplier", "null");
                if ("intSlider".equals(type)) {
                    category.addEntry(getEntryBuilder(eb, type, key, errorSupplier, ((Number) ((Map) element).get("min")).intValue(), ((Number) ((Map) element).get("max")).intValue()));
                } else if ("sub".equals(type)) {
                    SubCategoryBuilder sub = eb.startSubCategory(trans(key))
                                               .setTooltip(getTooltip(key, type, null));
                    for (Object elementInner : (List) ((Map) element).get("content")) {
                        String typeInner = (String) ((Map) elementInner).get("type");
                        String keyInner = (String) ((Map) elementInner).get("key");
                        // we are assuming no sub nested in subs, therefore two layers are enough,
                        // and we are not going to deal with sub in sub
                        if ("intSlider".equals(typeInner)) {
                            sub.add(getEntryBuilder(eb, typeInner, keyInner, errorSupplier, ((Number) ((Map) elementInner).get("min")).intValue(), ((Number) ((Map) elementInner).get("max")).intValue()));
                        } else {
                            sub.add(getEntryBuilder(eb, typeInner, keyInner, errorSupplier));
                        }
                    }
                    category.addEntry(sub.build());
                } else {
                    category.addEntry(getEntryBuilder(eb, type, key, errorSupplier));
                }
            }
        }
        return builder;
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
                                              .setDefaultValue((List<String>) ConfigUtils.getDefault(key))
                                              .setTooltip(tooltip).setSaveConsumer(v -> ConfigUtils.set(key, v));
                switch (errorSupplier) {
                    case "RegExNormal":
                        builder.setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_FOR_LIST);
                        break;
                    case "RegExRequireGroups":
                        builder.setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS_FOR_LIST);
                        break;
                    case "RegExAllowStar":
                        builder.setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR_FOR_LIST);
                        break;
                    case "null":
                    default:
                        break;
                }
                return builder.build();
            case "FAQ":
                return eb.startTextDescription(trans(key).copy().setStyle(TextUtils.WEBSITE_URL_STYLE))
                         .build();
            case "EnumKeyModifiers":
                return eb
                        .startEnumSelector(trans(key), SpecialUnits.KeyModifiers.class, SpecialUnits.KeyModifiers.valueOf((String) ConfigUtils.get(key)))
                        .setDefaultValue(SpecialUnits.KeyModifiers.valueOf((String) ConfigUtils.getDefault(key)))
                        .setTooltip(tooltip).setSaveConsumer(v -> ConfigUtils.set(key, v.toString())).build();
            case "EnumToastModes":
                return eb
                        .startEnumSelector(trans(key), SpecialUnits.ToastModes.class, SpecialUnits.ToastModes.valueOf((String) ConfigUtils.get(key)))
                        .setDefaultValue(SpecialUnits.ToastModes.valueOf((String) ConfigUtils.getDefault(key)))
                        .setTooltip(tooltip).setSaveConsumer(v -> ConfigUtils.set(key, v.toString())).build();
            // @formatter:off
            case "BubbleList":
                return new NestedListListEntry<SpecialUnits.BubbleRuleUnit, MultiElementListEntry<SpecialUnits.BubbleRuleUnit>>
                        (SERVER_LABELED_KEY,
                                SpecialUnits.BubbleRuleUnit.fromList((List) ConfigUtils.get(key)),
                                true,
                                () -> Optional.of(new Component[]{tooltip}),
                                v -> ConfigUtils.set(key, v),
                                () -> SpecialUnits.BubbleRuleUnit.fromList((List) ConfigUtils.getDefault(key)),
                                eb.getResetButtonKey(), true, true, (bubbleRuleUnit, ignored) -> {
                            AtomicReference<SpecialUnits.BubbleRuleUnit> bubbleRuleUnitRef = new AtomicReference<>(bubbleRuleUnit);
                            if (bubbleRuleUnit == null) {
                                Component displayText = trans(key + ".@New");
                                SpecialUnits.BubbleRuleUnit defaultRule = new SpecialUnits.BubbleRuleUnit();
                                bubbleRuleUnitRef.set(defaultRule);
                                return new MultiElementListEntry<>(displayText, defaultRule, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), defaultRule.address)
                                          .setTooltip(getTooltip(key + ".Address", "String", defaultRule.address))
                                          .setDefaultValue(defaultRule.address)
                                          .setSaveConsumer(v -> bubbleRuleUnitRef.get().address = v)
                                          .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Pattern"), defaultRule.pattern)
                                          .setTooltip(getTooltip(key + ".Pattern", "String", defaultRule.pattern))
                                          .setDefaultValue(defaultRule.pattern)
                                          .setSaveConsumer(v -> bubbleRuleUnitRef.get().pattern = v)
                                          .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());
                                    add(eb.startBooleanToggle(trans(key + ".Fallback"), defaultRule.fallback)
                                          .setTooltip(getTooltip(key + ".Fallback", "boolean", defaultRule.fallback))
                                          .setDefaultValue(defaultRule.fallback)
                                          .setSaveConsumer(v -> bubbleRuleUnitRef.get().fallback = v).build());
                                }}, SHOULD_EXPAND_ALL_RULES);
                            } else {
                                String colorPrefix = ("*".equals(bubbleRuleUnit.address) ||
                                        Pattern.compile(bubbleRuleUnit.address)
                                               .matcher(ContextUtils.getSessionIdentifier())
                                               .matches()) ? "§a" : "§6";
                                Component displayText = trans(key + ".@Display", colorPrefix + bubbleRuleUnit.address, bubbleRuleUnit.fallback ? "§a✔" : "§c✘", bubbleRuleUnit.pattern);
                                return new MultiElementListEntry<>(displayText, bubbleRuleUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), bubbleRuleUnit.address)
                                          .setTooltip(getTooltip(key + ".Address", "String", new SpecialUnits.BubbleRuleUnit().address))
                                          .setDefaultValue(new SpecialUnits.BubbleRuleUnit().address)
                                          .setSaveConsumer(v -> bubbleRuleUnit.address = v)
                                          .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Pattern"), bubbleRuleUnit.pattern)
                                          .setTooltip(getTooltip(key + ".Pattern", "String", new SpecialUnits.BubbleRuleUnit().pattern))
                                          .setDefaultValue(new SpecialUnits.BubbleRuleUnit().pattern)
                                          .setSaveConsumer(v -> bubbleRuleUnit.pattern = v)
                                          .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());
                                    add(eb.startBooleanToggle(trans(key + ".Fallback"), bubbleRuleUnit.fallback)
                                          .setTooltip(getTooltip(key + ".Fallback", "boolean", new SpecialUnits.BubbleRuleUnit().fallback))
                                          .setDefaultValue(new SpecialUnits.BubbleRuleUnit().fallback)
                                          .setSaveConsumer(v -> bubbleRuleUnit.fallback = v).build());
                                }}, SHOULD_EXPAND_ALL_RULES);
                            }
                        }
                        );
            case "ResponderList":
                return new NestedListListEntry<SpecialUnits.ResponderRuleUnit, MultiElementListEntry<SpecialUnits.ResponderRuleUnit>>
                        (SERVER_LABELED_KEY,
                                SpecialUnits.ResponderRuleUnit.fromList((List) ConfigUtils.get(key)),
                                true,
                                () -> Optional.of(new Component[]{tooltip}),
                                v -> ConfigUtils.set(key, v),
                                () -> SpecialUnits.ResponderRuleUnit.fromList((List) ConfigUtils.getDefault(key)),
                                eb.getResetButtonKey(),
                                true,
                                true, (responderRuleUnit, ignored) -> {
                            AtomicReference<SpecialUnits.ResponderRuleUnit> responderUnitRef = new AtomicReference<>(new SpecialUnits.ResponderRuleUnit());
                            if (responderRuleUnit == null) {
                                Component displayText = trans(key + ".@New");
                                SpecialUnits.ResponderRuleUnit defaultRule = new SpecialUnits.ResponderRuleUnit();
                                responderUnitRef.set(defaultRule);
                                return new MultiElementListEntry<>(displayText, defaultRule, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), defaultRule.address)
                                          .setTooltip(getTooltip(key + ".Address", "String", defaultRule.address))
                                          .setDefaultValue(defaultRule.address)
                                          .setSaveConsumer(v -> responderUnitRef.get().address = v)
                                          .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Pattern"), defaultRule.pattern)
                                          .setTooltip(getTooltip(key + ".Pattern", "String", defaultRule.pattern))
                                          .setDefaultValue(defaultRule.pattern)
                                          .setSaveConsumer(v -> responderUnitRef.get().pattern = v)
                                          .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER).build());
                                    add(eb.startStrField(trans(key + ".Message"), defaultRule.message)
                                          .setTooltip(getTooltip(key + ".Message", "String", defaultRule.message))
                                          .setDefaultValue(defaultRule.message)
                                          .setSaveConsumer(v -> responderUnitRef.get().message = v).build());
                                    add(eb.startLongField(trans(key + ".DelayInMilliseconds"), defaultRule.delayInMilliseconds)
                                          .setTooltip(getTooltip(key + ".DelayInMilliseconds", "longField", defaultRule.delayInMilliseconds))
                                          .setDefaultValue(defaultRule.delayInMilliseconds)
                                          .setSaveConsumer(v -> responderUnitRef.get().delayInMilliseconds = v).build());
                                    add(eb.startBooleanToggle(trans(key + ".ForceDisableFormatter"), defaultRule.forceDisableFormatter)
                                          .setTooltip(getTooltip(key + ".ForceDisableFormatter", "boolean", defaultRule.forceDisableFormatter))
                                          .setDefaultValue(defaultRule.forceDisableFormatter)
                                          .setSaveConsumer(v -> responderUnitRef.get().forceDisableFormatter = v).build());
                                }}, SHOULD_EXPAND_ALL_RULES);
                            } else {
                                String colorPrefix = ("*".equals(responderRuleUnit.address) ||
                                        Pattern.compile(responderRuleUnit.address)
                                               .matcher(ContextUtils.getSessionIdentifier())
                                               .matches()) ? "§a" : "§6";
                                Component displayText = trans(key + ".@Display", colorPrefix + responderRuleUnit.address, responderRuleUnit.forceDisableFormatter ? "§a✔" : "§c✘",
                                        responderRuleUnit.delayInMilliseconds, responderRuleUnit.pattern, responderRuleUnit.message);
                                return new MultiElementListEntry<>(displayText, responderRuleUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startStrField(trans(key + ".Address"), responderRuleUnit.address)
                                          .setTooltip(getTooltip(key + ".Address", "String", new SpecialUnits.ResponderRuleUnit().address))
                                          .setDefaultValue(new SpecialUnits.ResponderRuleUnit().address)
                                          .setSaveConsumer(v -> responderRuleUnit.address = v)
                                          .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                    add(eb.startStrField(trans(key + ".Pattern"), responderRuleUnit.pattern)
                                          .setTooltip(getTooltip(key + ".Pattern", "String", new SpecialUnits.ResponderRuleUnit().pattern))
                                          .setDefaultValue(new SpecialUnits.ResponderRuleUnit().pattern)
                                          .setSaveConsumer(v -> responderRuleUnit.pattern = v)
                                          .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER).build());
                                    add(eb.startStrField(trans(key + ".Message"), responderRuleUnit.message)
                                          .setTooltip(getTooltip(key + ".Message", "String", new SpecialUnits.ResponderRuleUnit().message))
                                          .setDefaultValue(new SpecialUnits.ResponderRuleUnit().message)
                                          .setSaveConsumer(v -> responderRuleUnit.message = v).build());
                                    add(eb.startLongField(trans(key + ".DelayInMilliseconds"), responderRuleUnit.delayInMilliseconds)
                                          .setTooltip(getTooltip(key + ".DelayInMilliseconds", "longField", new SpecialUnits.ResponderRuleUnit().delayInMilliseconds))
                                          .setDefaultValue(new SpecialUnits.ResponderRuleUnit().delayInMilliseconds)
                                          .setSaveConsumer(v -> responderRuleUnit.delayInMilliseconds = v).build());
                                    add(eb.startBooleanToggle(trans(key + ".ForceDisableFormatter"), responderRuleUnit.forceDisableFormatter)
                                          .setTooltip(getTooltip(key + ".ForceDisableFormatter", "boolean", new SpecialUnits.ResponderRuleUnit().forceDisableFormatter))
                                          .setDefaultValue(new SpecialUnits.ResponderRuleUnit().forceDisableFormatter)
                                          .setSaveConsumer(v -> responderRuleUnit.forceDisableFormatter = v).build());
                                }}, SHOULD_EXPAND_ALL_RULES);
                            }
                        }
                        );
            case "MacroList":
                return new NestedListListEntry<SpecialUnits.MacroUnit, MultiElementListEntry<SpecialUnits.MacroUnit>>
                        (trans(key),
                                SpecialUnits.MacroUnit.fromList((List) ConfigUtils.get(key)),
                                true,
                                () -> Optional.of(new Component[]{tooltip}),
                                v -> ConfigUtils.set(key, v),
                                () -> SpecialUnits.MacroUnit.fromList((List) ConfigUtils.getDefault(key)),
                                eb.getResetButtonKey(),
                                true,
                                true, (macroUnit, ignored) -> {
                            AtomicReference<SpecialUnits.MacroUnit> macroUnitRef = new AtomicReference<>(macroUnit);
                            if (macroUnit == null) {
                                Component displayText = trans(key + ".@New");
                                SpecialUnits.MacroUnit defaultMacro = new SpecialUnits.MacroUnit();
                                macroUnitRef.set(defaultMacro); // 设置初始值
                                return new MultiElementListEntry<>(displayText, defaultMacro, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb
                                            .startKeyCodeField(trans(key + ".Key"), InputConstants.getKey(defaultMacro.key))
                                            .setTooltip(getTooltip(key + ".Key", "keycode", InputConstants.getKey(defaultMacro.key)))
                                            .setDefaultValue(InputConstants.getKey(defaultMacro.key))
                                            //#if MC>=11800
                                            .setKeySaveConsumer
                                            //#elseif MC>=11700
                                            // In MC 1.17.X, we use ClothConfig v5.
                                            // In ClothConfig v5 (discontinued) we use the `setSaveConsumer()` method.
                                            //$$ .setSaveConsumer
                                            //#else
                                            //$$ .setKeySaveConsumer
                                            //#endif
                                                    (key -> macroUnitRef.get().key = key.getName()).build());
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
                                }}, SHOULD_EXPAND_ALL_RULES);
                            } else {
                                Component displayText;
                                if (macroUnit.key.equals(InputConstants.UNKNOWN.getName())) {
                                    displayText = trans(key + ".@New");
                                } else if (macroUnit.modifier == SpecialUnits.KeyModifiers.NONE) {
                                    displayText = trans(key + ".@Display",
                                            // such as "[ H ] /home"
                                            "§6" + InputConstants.getKey(macroUnit.key).getDisplayName()
                                                                 .getString(), macroUnit.command);
                                } else {
                                    displayText = trans(key + ".@Display",
                                            // such as "[ Shift + B ] /back"
                                            "§6" + macroUnit.modifier + " + " + InputConstants.getKey(macroUnit.key)
                                                                                              .getDisplayName()
                                                                                              .getString(), macroUnit.command);
                                }
                                return new MultiElementListEntry<>(displayText, macroUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                    add(eb.startKeyCodeField(trans(key + ".Key"), InputConstants.getKey(macroUnit.key))
                                          .setTooltip(getTooltip(key + ".Key", "keycode", InputConstants.getKey(new SpecialUnits.MacroUnit().key)))
                                          .setDefaultValue(InputConstants.getKey(new SpecialUnits.MacroUnit().key))
                                          //#if MC>=11800
                                            .setKeySaveConsumer
                                            //#elseif MC>=11700
                                            // In MC 1.17.X, we use ClothConfig v5.
                                            // In ClothConfig v5 (discontinued) we use the `setSaveConsumer()` method.
                                            //$$ .setSaveConsumer
                                            //#else
                                            //$$ .setKeySaveConsumer
                                            //#endif
                                                    (key -> macroUnit.key = key.getName()).build());
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
                                }}, SHOULD_EXPAND_ALL_RULES);
                            }
                        }
                        );
            case "FormatterList":
                return new NestedListListEntry<SpecialUnits.FormatterUnit, MultiElementListEntry<SpecialUnits.FormatterUnit>>
                        (SERVER_LABELED_KEY,
                                SpecialUnits.FormatterUnit.fromList((List) ConfigUtils.get(key)),
                                true,
                                () -> Optional.of(new Component[]{tooltip}),
                                v -> ConfigUtils.set(key, v),
                                () -> SpecialUnits.FormatterUnit.fromList((List) ConfigUtils.getDefault(key)),
                                eb.getResetButtonKey(),
                                true,
                                true,
                                (formatterUnit, ignored) -> {
                                    AtomicReference<SpecialUnits.FormatterUnit> formatterUnitRef = new AtomicReference<>(formatterUnit);
                                    if (formatterUnit == null) {
                                        Component displayText = trans(key + ".@New");
                                        SpecialUnits.FormatterUnit defaultFormatterRule = new SpecialUnits.FormatterUnit();
                                        formatterUnitRef.set(defaultFormatterRule);
                                        return new MultiElementListEntry<>(displayText, defaultFormatterRule, new ArrayList<AbstractConfigListEntry<?>>() {{
                                            add(eb.startStrField(trans(key + ".Address"), defaultFormatterRule.address)
                                                  .setTooltip(getTooltip(key + ".Address", "String", defaultFormatterRule.address))
                                                  .setDefaultValue(defaultFormatterRule.address)
                                                  .setSaveConsumer(v -> formatterUnitRef.get().address = v)
                                                  .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                            add(eb.startStrField(trans(key + ".Formatter"), defaultFormatterRule.formatter)
                                                  .setTooltip(getTooltip(key + ".Formatter", "String", defaultFormatterRule.formatter))
                                                  .setDefaultValue(defaultFormatterRule.formatter)
                                                  .setSaveConsumer(v -> formatterUnitRef.get().formatter = v).build());
                                        }}, SHOULD_EXPAND_ALL_RULES);
                                    } else {
                                        String colorPrefix = ("*".equals(formatterUnit.address) ||
                                                Pattern.compile(formatterUnit.address)
                                                       .matcher(ContextUtils.getSessionIdentifier())
                                                       .matches()) ? "§a" : "§6";
                                        Component displayText = trans(key + ".@Display", colorPrefix + formatterUnit.address, formatterUnit.formatter);
                                        return new MultiElementListEntry<>(displayText, formatterUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                            add(eb.startStrField(trans(key + ".Address"), formatterUnit.address)
                                                  .setTooltip(getTooltip(key + ".Address", "String", new SpecialUnits.FormatterUnit().address))
                                                  .setDefaultValue(new SpecialUnits.FormatterUnit().address)
                                                  .setSaveConsumer(v -> formatterUnit.address = v)
                                                  .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                            add(eb.startStrField(trans(key + ".Formatter"), formatterUnit.formatter)
                                                  .setTooltip(getTooltip(key + ".Formatter", "String", new SpecialUnits.FormatterUnit().formatter))
                                                  .setDefaultValue(new SpecialUnits.FormatterUnit().formatter)
                                                  .setSaveConsumer(v -> formatterUnit.formatter = v).build());
                                        }}, SHOULD_EXPAND_ALL_RULES);
                                    }
                                }
                        );
            case "CustomJoinMessageList":
                return new NestedListListEntry<SpecialUnits.CustomJoinMessageRuleUnit, MultiElementListEntry<SpecialUnits.CustomJoinMessageRuleUnit>>
                        (SERVER_LABELED_KEY,
                                SpecialUnits.CustomJoinMessageRuleUnit.fromList((List) ConfigUtils.get(key)),
                                true,
                                () -> Optional.of(new Component[]{tooltip}),
                                v -> ConfigUtils.set(key, v),
                                () -> SpecialUnits.CustomJoinMessageRuleUnit.fromList((List) ConfigUtils.getDefault(key)),
                                eb.getResetButtonKey(),
                                true,
                                true,
                                (customJoinMessageRuleUnit, ignored) -> {
                                    AtomicReference<SpecialUnits.CustomJoinMessageRuleUnit> customJoinMessageRuleUnitRef = new AtomicReference<>(customJoinMessageRuleUnit);
                                    if (customJoinMessageRuleUnit == null) {
                                        Component displayText = trans(key + ".@New");
                                        SpecialUnits.CustomJoinMessageRuleUnit defaultCustomJoinMessageRuleUnit = new SpecialUnits.CustomJoinMessageRuleUnit();
                                        customJoinMessageRuleUnitRef.set(defaultCustomJoinMessageRuleUnit);
                                        return new MultiElementListEntry<>(displayText, defaultCustomJoinMessageRuleUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                            add(eb.startStrField(trans(key + ".Address"), defaultCustomJoinMessageRuleUnit.address)
                                                  .setTooltip(getTooltip(key + ".Address", "String", defaultCustomJoinMessageRuleUnit.address))
                                                  .setDefaultValue(defaultCustomJoinMessageRuleUnit.address)
                                                  .setSaveConsumer(v -> customJoinMessageRuleUnitRef.get().address = v)
                                                  .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                            add(eb.startStrField(trans(key + ".Message"), defaultCustomJoinMessageRuleUnit.message)
                                                  .setTooltip(getTooltip(key + ".Message", "String", defaultCustomJoinMessageRuleUnit.message))
                                                  .setDefaultValue(defaultCustomJoinMessageRuleUnit.message)
                                                  .setSaveConsumer(v -> customJoinMessageRuleUnitRef.get().message = v).build());
                                            add(eb.startLongField(trans(key + ".DelayInMilliseconds"), defaultCustomJoinMessageRuleUnit.delayInMilliseconds)
                                                  .setTooltip(getTooltip(key + ".DelayInMilliseconds", "longField", defaultCustomJoinMessageRuleUnit.delayInMilliseconds))
                                                  .setDefaultValue(defaultCustomJoinMessageRuleUnit.delayInMilliseconds)
                                                  .setSaveConsumer(v -> customJoinMessageRuleUnitRef.get().delayInMilliseconds = v).build());
                                            add(eb.startBooleanToggle(trans(key + ".ForceDisableFormatter"), defaultCustomJoinMessageRuleUnit.forceDisableFormatter)
                                                  .setTooltip(getTooltip(key + ".ForceDisableFormatter", "boolean", defaultCustomJoinMessageRuleUnit.forceDisableFormatter))
                                                  .setDefaultValue(defaultCustomJoinMessageRuleUnit.forceDisableFormatter)
                                                  .setSaveConsumer(v -> customJoinMessageRuleUnitRef.get().forceDisableFormatter = v).build());
                                        }},SHOULD_EXPAND_ALL_RULES);
                                    } else {
                                        String colorPrefix = ("*".equals(customJoinMessageRuleUnit.address) ||
                                                Pattern.compile(customJoinMessageRuleUnit.address)
                                                       .matcher(ContextUtils.getSessionIdentifier())
                                                       .matches()) ? "§a" : "§6";
                                        Component displayText = trans(key + ".@Display", colorPrefix + customJoinMessageRuleUnit.address, customJoinMessageRuleUnit.forceDisableFormatter ? "§a✔" : "§c✘",
                                                customJoinMessageRuleUnit.delayInMilliseconds, customJoinMessageRuleUnit.message);
                                        return new MultiElementListEntry<>(displayText, customJoinMessageRuleUnit, new ArrayList<AbstractConfigListEntry<?>>() {{
                                            add(eb.startStrField(trans(key + ".Address"), customJoinMessageRuleUnit.address)
                                                  .setTooltip(getTooltip(key + ".Address", "String", new SpecialUnits.CustomJoinMessageRuleUnit().address))
                                                  .setDefaultValue(new SpecialUnits.CustomJoinMessageRuleUnit().address)
                                                  .setSaveConsumer(v -> customJoinMessageRuleUnit.address = v)
                                                  .setErrorSupplier(ConfigScreenUtils.ErrorSuppliers.REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                                            add(eb.startStrField(trans(key + ".Message"), customJoinMessageRuleUnit.message)
                                                  .setTooltip(getTooltip(key + ".Message", "String", new SpecialUnits.CustomJoinMessageRuleUnit().message))
                                                  .setDefaultValue(new SpecialUnits.CustomJoinMessageRuleUnit().message)
                                                  .setSaveConsumer(v -> customJoinMessageRuleUnit.message = v).build());
                                            add(eb.startLongField(trans(key + ".DelayInMilliseconds"), customJoinMessageRuleUnit.delayInMilliseconds)
                                                  .setTooltip(getTooltip(key + ".DelayInMilliseconds", "longField", new SpecialUnits.CustomJoinMessageRuleUnit().delayInMilliseconds))
                                                  .setDefaultValue(new SpecialUnits.CustomJoinMessageRuleUnit().delayInMilliseconds)
                                                  .setSaveConsumer(v -> customJoinMessageRuleUnit.delayInMilliseconds = v).build());
                                            add(eb.startBooleanToggle(trans(key + ".ForceDisableFormatter"), customJoinMessageRuleUnit.forceDisableFormatter)
                                                  .setTooltip(getTooltip(key + ".ForceDisableFormatter", "boolean", new SpecialUnits.CustomJoinMessageRuleUnit().forceDisableFormatter))
                                                  .setDefaultValue(new SpecialUnits.CustomJoinMessageRuleUnit().forceDisableFormatter)
                                                  .setSaveConsumer(v -> customJoinMessageRuleUnit.forceDisableFormatter = v).build());
                                        }}, SHOULD_EXPAND_ALL_RULES);
                                    }
                                }
                        );
            // @formatter:on
            default:
                LoggerUtils.error("[ChatTools] Unknown config type: " + type);
                return null;
        }
    }

}
