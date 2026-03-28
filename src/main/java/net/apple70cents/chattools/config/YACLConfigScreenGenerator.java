//? if HAS_YACL {
package net.apple70cents.chattools.config;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.apple70cents.chattools.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * YACL-based config screen generator.
 * Reads the same config_gui.json as ConfigScreenGenerator but produces a YACL screen.
 *
 * @author 70CentsApple
 */
public class YACLConfigScreenGenerator {

    @SuppressWarnings("unchecked")
    public static Screen createScreen(Screen parent) {
        ConfigGuiLoader.initializeConfigGuiMapIfNecessary();
        Map<String, Object> guiMap = ConfigGuiLoader.configGuiMap;

        YetAnotherConfigLib.Builder yaclBuilder = YetAnotherConfigLib.createBuilder().title(trans("gui.title"))
                .save(ConfigUtils::save);

        for (Object categoryInfo : (List) guiMap.get("content")) {
            Map catMap = (Map) categoryInfo;
            String catKey = (String) catMap.get("key");
            ConfigCategory.Builder catBuilder = ConfigCategory.createBuilder().name(trans(catKey));

            for (Object element : (List) catMap.get("content")) {
                Map eleMap = (Map) element;
                String type = (String) eleMap.get("type");
                String key = (String) eleMap.get("key");

                if ("sub".equals(type)) {
                    OptionGroup.Builder groupBuilder = OptionGroup.createBuilder().name(trans(key))
                            .description(OptionDescription.of(ConfigScreenTooltipUtils.getTooltip(key, type, null)));
                    for (Object inner : (List) eleMap.get("content")) {
                        Map innerMap = (Map) inner;
                        String innerType = (String) innerMap.get("type");
                        String innerKey = (String) innerMap.get("key");
                        Option<?> opt = buildOption(innerType, innerKey, innerMap);
                        if (opt != null) groupBuilder.option(opt);
                    }
                    catBuilder.group(groupBuilder.build());
                } else if (type.endsWith("List") && !"StringList".equals(type)) {
                    // Complex list types - use ButtonOption to open editing screen
                    catBuilder.option(buildComplexListButton(key, type));
                } else if ("StringList".equals(type)) {
                    catBuilder.group(buildStringListOption(key));
                } else {
                    Option<?> opt = buildOption(type, key, eleMap);
                    if (opt != null) catBuilder.option(opt);
                }
            }
            yaclBuilder.category(catBuilder.build());
        }

        return yaclBuilder.build().generateScreen(parent);
    }

    @SuppressWarnings("unchecked")
    private static Option<?> buildOption(String type, String key, Map eleMap) {
        OptionDescription desc = OptionDescription.of(
                "FAQ".equals(type) ? ConfigScreenTooltipUtils.getTooltip(key, type, null) : ConfigScreenTooltipUtils.getTooltip(key,
                        type));
        switch (type) {
            case "boolean":
                return Option.<Boolean>createBuilder().name(trans(key)).description(desc)
                        .binding((boolean) ConfigUtils.getDefault(key), () -> (boolean) ConfigUtils.get(key),
                                v -> ConfigUtils.set(key, v)).controller(TickBoxControllerBuilder::create).build();
            case "String":
                return Option.<String>createBuilder().name(trans(key)).description(desc)
                        .binding((String) ConfigUtils.getDefault(key), () -> (String) ConfigUtils.get(key),
                                v -> ConfigUtils.set(key, v)).controller(StringControllerBuilder::create).build();
            case "intSlider": {
                int min = ((Number) eleMap.get("min")).intValue();
                int max = ((Number) eleMap.get("max")).intValue();
                return Option.<Integer>createBuilder().name(trans(key)).description(desc)
                        .binding(((Number) ConfigUtils.getDefault(key)).intValue(),
                                () -> ((Number) ConfigUtils.get(key)).intValue(), v -> ConfigUtils.set(key, (Number) v))
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1)).build();
            }
            case "intField":
                return Option.<Integer>createBuilder().name(trans(key)).description(desc)
                        .binding(((Number) ConfigUtils.getDefault(key)).intValue(),
                                () -> ((Number) ConfigUtils.get(key)).intValue(), v -> ConfigUtils.set(key, (Number) v))
                        .controller(IntegerFieldControllerBuilder::create).build();
            case "doubleField":
                return Option.<Double>createBuilder().name(trans(key)).description(desc)
                        .binding(((Number) ConfigUtils.getDefault(key)).doubleValue(),
                                () -> ((Number) ConfigUtils.get(key)).doubleValue(),
                                v -> ConfigUtils.set(key, (Number) v)).controller(DoubleFieldControllerBuilder::create)
                        .build();
            case "keycode":
                Component btnText = TextUtils.literal(
                        InputConstants.getKey((String) ConfigUtils.get(key)).getDisplayName().getString());
                 return ButtonOption.createBuilder().name(trans(key)).description(desc).text(btnText)
                        .action((screen, opt) -> Minecraft.getInstance().setScreen(new KeyCaptureScreen(screen, key)))
                        .build();
            case "EnumKeyModifiers":
                return Option.<SpecialUnits.KeyModifiers>createBuilder().name(trans(key)).description(desc)
                        .binding(SpecialUnits.KeyModifiers.valueOf((String) ConfigUtils.getDefault(key)),
                                () -> SpecialUnits.KeyModifiers.valueOf((String) ConfigUtils.get(key)),
                                v -> ConfigUtils.set(key, v.toString()))
                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(SpecialUnits.KeyModifiers.class))
                        .build();
            case "EnumToastModes":
                return Option.<SpecialUnits.ToastModes>createBuilder().name(trans(key)).description(desc)
                        .binding(SpecialUnits.ToastModes.valueOf((String) ConfigUtils.getDefault(key)),
                                () -> SpecialUnits.ToastModes.valueOf((String) ConfigUtils.get(key)),
                                v -> ConfigUtils.set(key, v.toString()))
                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(SpecialUnits.ToastModes.class))
                        .build();
            case "EnumTranslators":
                return Option.<SpecialUnits.TranslatorModes>createBuilder().name(trans(key)).description(desc)
                        .binding(SpecialUnits.TranslatorModes.valueOf((String) ConfigUtils.getDefault(key)),
                                () -> SpecialUnits.TranslatorModes.valueOf((String) ConfigUtils.get(key)),
                                v -> ConfigUtils.set(key, v.toString())).controller(
                                opt -> EnumControllerBuilder.create(opt).enumClass(SpecialUnits.TranslatorModes.class))
                        .build();
            case "FAQ":
                return ButtonOption.createBuilder().name(trans(key).copy().setStyle(TextUtils.WEBSITE_URL_STYLE))
                        .description(desc)
                        .action((screen, opt) -> {
//? if >=1.21.11 {
                            net.minecraft.util.Util.getPlatform()
//?} else {
                            /*net.minecraft.Util.getPlatform()
*///?}
                                    .openUri(URI.create("https://70centsapple.top/blogs/#/chat-tools-faq"));
                        }).build();
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static ListOption<String> buildStringListOption(String key) {
        return ListOption.<String>createBuilder().name(trans(key))
                .description(OptionDescription.of(ConfigScreenTooltipUtils.getTooltip(key, "StringList")))
                .binding((List<String>) ConfigUtils.getDefault(key), () -> (List<String>) ConfigUtils.get(key),
                        v -> ConfigUtils.set(key, v)).controller(StringControllerBuilder::create).initial("").build();
    }

    @SuppressWarnings("unchecked")
    private static ButtonOption buildComplexListButton(String key, String type) {
        final Component SERVER_LABELED_KEY = trans(key, "§f" + ContextUtils.getSessionIdentifier());
        return ButtonOption.createBuilder().name(SERVER_LABELED_KEY)
                .description(OptionDescription.of(ConfigScreenTooltipUtils.getTooltip(key, type)))
                .text(TextUtils.literal("§e✎ " + ((List) ConfigUtils.get(key)).size() + " ▸"))
                .action((screen, opt) -> Minecraft.getInstance().setScreen(new RuleListScreen(screen, key, type)))
                .build();
    }
}
//?}
