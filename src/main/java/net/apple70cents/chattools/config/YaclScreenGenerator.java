package net.apple70cents.chattools.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import dev.isxander.yacl3.gui.controllers.string.number.DoubleFieldController;
import dev.isxander.yacl3.gui.controllers.string.number.IntegerFieldController;
import net.apple70cents.chattools.utils.*;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

import static net.apple70cents.chattools.utils.ConfigScreenUtils.getTooltip;
import static net.apple70cents.chattools.utils.TextUtils.trans;

public class YaclScreenGenerator {
    public static YetAnotherConfigLib.Builder getBuilder() {
        ConfigScreenUtils.initializeConfigGuiMapIfNecessary();
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder().title(trans("gui.title"))
                                                                 .save(ConfigUtils::save);

        for (Object categoryInfo : (List) ConfigScreenUtils.configGuiMap.get("content")) {
            ConfigCategory.Builder category = ConfigCategory.createBuilder()
                                                            .name(trans((String) ((Map) categoryInfo).get("key")));
            for (Object element : (List) ((Map) categoryInfo).get("content")) {
                String type = (String) ((Map) element).get("type");
                String key = (String) ((Map) element).get("key");
                String errorSupplier = (String) ((Map) element).getOrDefault("errorSupplier", "null");
                if ("intSlider".equals(type)) {
                    int min = ((Number) ((Map) element).get("min")).intValue();
                    int max = ((Number) ((Map) element).get("max")).intValue();
                    category.option(getOption(type, key, errorSupplier, min, max));
                } else if ("sub".equals(type)) {
                    OptionGroup.Builder sub = OptionGroup.createBuilder().name(trans(key))
                                                         .description(OptionDescription.of(getTooltip(key, type, null)));
                    for (Object elementInner : (List) ((Map) element).get("content")) {
                        String typeInner = (String) ((Map) elementInner).get("type");
                        String keyInner = (String) ((Map) elementInner).get("key");
                        // we are assuming no sub nested in subs, therefore two layers are enough,
                        // and we are not going to deal with sub in sub
                        if ("intSlider".equals(typeInner)) {
                            int min = ((Number) ((Map) elementInner).get("min")).intValue();
                            int max = ((Number) ((Map) elementInner).get("max")).intValue();
                            sub.option(getOption(typeInner, keyInner, errorSupplier, min, max));
                        } else {
                            sub.option(getOption(typeInner, keyInner, errorSupplier));
                        }
                    }
                    category.group(sub.build());
                } else {
                    category.option(getOption(type, key, errorSupplier));
                }
            }
            builder.category(category.build());
        }
        return builder;
    }

    public static Option getOption(String type, String key, String errorSupplier, int... args) {
        // the `args` are only for `min` and `max` value for int sliders (recently)
        // `errorSuppliers` will only apply to `StringList`s
        Component tooltip = "FAQ".equals(type) ? getTooltip(key, type, null) : getTooltip(key, type);
        // display current server (if it can be used)
        final Component SERVER_LABELED_KEY = trans(key, "§f" + ContextUtils.getSessionIdentifier());
        switch (type) {
            case "boolean":
                return Option.<Boolean>createBuilder().name(trans(key)).description(OptionDescription.of(tooltip))
                             .controller(BooleanControllerBuilder::create)
                             .binding((boolean) ConfigUtils.getDefault(key), () -> (boolean) ConfigUtils.get(key), v -> ConfigUtils.set(key, v))
                             .build();
            case "String":
                return Option.<String>createBuilder().name(trans(key)).description(OptionDescription.of(tooltip))
                             .customController(StringController::new)
                             .binding((String) ConfigUtils.getDefault(key), () -> (String) ConfigUtils.get(key), v -> ConfigUtils.set(key, v))
                             .build();
            case "intSlider":
                return Option.<Integer>createBuilder().name(trans(key)).description(OptionDescription.of(tooltip))
                             .customController(opt -> new IntegerSliderController(opt, args[0], args[1], 1))
                             .binding(((Number) ConfigUtils.getDefault(key)).intValue(), () -> ((Number) ConfigUtils.get(key)).intValue(), v -> ConfigUtils.set(key, (Number) v))
                             .build();
            case "intField":
                return Option.<Integer>createBuilder().name(trans(key)).description(OptionDescription.of(tooltip))
                             .customController(IntegerFieldController::new)
                             .binding(((Number) ConfigUtils.getDefault(key)).intValue(), () -> ((Number) ConfigUtils.get(key)).intValue(), v -> ConfigUtils.set(key, (Number) v))
                             .build();
            case "doubleField":
                return Option.<Double>createBuilder().name(trans(key)).description(OptionDescription.of(tooltip))
                             .customController(DoubleFieldController::new)
                             .binding(((Number) ConfigUtils.getDefault(key)).doubleValue(), () -> ((Number) ConfigUtils.get(key)).doubleValue(), v -> ConfigUtils.set(key, (Number) v))
                             .build();
            case "keycode":
                //TODO keycode
                return LabelOption.create(trans(key));
            case "StringList":
                return ListOption.<String>createBuilder().name(trans(key)).description(OptionDescription.of(tooltip))
                                 .controller(StringControllerBuilder::create)
                                 .binding((List<String>) ConfigUtils.getDefault(key), () -> (List<String>) ConfigUtils.get(key), v -> ConfigUtils.set(key, v))
                                 .initial("").build();
            case "FAQ":
                return LabelOption.create(trans(key).copy().setStyle(TextUtils.WEBSITE_URL_STYLE));
            case "EnumKeyModifiers":
                return Option.<SpecialUnits.KeyModifiers>createBuilder().name(trans(key)).description(OptionDescription.of(tooltip))
                             .customController(opt -> new EnumController<>(opt, SpecialUnits.KeyModifiers.class))
                             .binding(SpecialUnits.KeyModifiers.valueOf((String) ConfigUtils.get(key)),
                                      () -> SpecialUnits.KeyModifiers.valueOf((String) ConfigUtils.getDefault(key)),
                                      v -> ConfigUtils.set(key, v.toString()))
                             .build();
            case "EnumToastModes":
                return Option.<SpecialUnits.ToastModes>createBuilder().name(trans(key)).description(OptionDescription.of(tooltip))
                             .customController(opt -> new EnumController<>(opt, SpecialUnits.ToastModes.class))
                             .binding(SpecialUnits.ToastModes.valueOf((String) ConfigUtils.get(key)),
                                      () -> SpecialUnits.ToastModes.valueOf((String) ConfigUtils.getDefault(key)),
                                      v -> ConfigUtils.set(key, v.toString()))
                             .build();
            case "BubbleList":
            case "ResponderList":
            case "MacroList":
            case "FormatterList":
            case "CustomJoinMessageList":
            default:
                LoggerUtils.error("[ChatTools] Unknown config type: " + type);
                return LabelOption.create(trans(key));
//                return null;
        }
    }
}
