package net.apple70cents.chattools.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.apple70cents.chattools.utils.ConfigScreenTooltipUtils;
import net.apple70cents.chattools.utils.ConfigScreenUtils;
import net.apple70cents.chattools.utils.ConfigUtils;

//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}

import java.util.List;
import java.util.Map;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * Generates the Cloth Config screen.
 * Used as a fallback if YACL is not available.
 * 
 * @author 70CentsApple
 */
public class ClothConfigScreenGenerator {

    @SuppressWarnings("unchecked")
    public static ConfigBuilder getConfigBuilder() {
        ConfigGuiLoader.initializeConfigGuiMapIfNecessary();
        Map<String, Object> configGuiMap = ConfigGuiLoader.configGuiMap;

//? if >=1.21.11 {
        Identifier backgroundTexture = Identifier.parse("minecraft:textures/block/oak_planks.png");
//?} elif >=1.21 {
        /*ResourceLocation backgroundTexture = ResourceLocation.parse("minecraft:textures/block/oak_planks.png");
*///?} else {
        /*ResourceLocation backgroundTexture = new ResourceLocation("minecraft:textures/block/oak_planks.png");
*///?}
        ConfigBuilder builder = ConfigBuilder.create().setTitle(trans("gui.title"))
                                             .setDefaultBackgroundTexture(backgroundTexture)
                                             .setTransparentBackground(true).setSavingRunnable(ConfigUtils::save);
        ConfigEntryBuilder eb = builder.entryBuilder();
        for (Object categoryInfo : (List<?>) configGuiMap.get("content")) {
            ConfigCategory category = builder.getOrCreateCategory(trans((String) ((Map<?, ?>) categoryInfo).get("key")));
            for (Object element : (List<?>) ((Map<?, ?>) categoryInfo).get("content")) {
                String type = (String) ((Map<?, ?>) element).get("type");
                String key = (String) ((Map<?, ?>) element).get("key");
                String errorSupplier = (String) ((Map) element).getOrDefault("errorSupplier", "null");
                if ("intSlider".equals(type)) {
                    category.addEntry(ConfigScreenUtils.getEntryBuilder(eb, type, key, errorSupplier, ((Number) ((Map<?, ?>) element).get("min")).intValue(), ((Number) ((Map<?, ?>) element).get("max")).intValue()));
                } else if ("sub".equals(type)) {
                    SubCategoryBuilder sub = eb.startSubCategory(trans(key))
                                               .setTooltip(ConfigScreenTooltipUtils.getTooltip(key, type, null));
                    for (Object elementInner : (List<?>) ((Map<?, ?>) element).get("content")) {
                        String typeInner = (String) ((Map<?, ?>) elementInner).get("type");
                        String keyInner = (String) ((Map<?, ?>) elementInner).get("key");
                        // we are assuming no sub nested in subs, therefore two layers are enough,
                        // and we are not going to deal with sub in sub
                        if ("intSlider".equals(typeInner)) {
                            sub.add(ConfigScreenUtils.getEntryBuilder(eb, typeInner, keyInner, errorSupplier, ((Number) ((Map<?, ?>) elementInner).get("min")).intValue(), ((Number) ((Map<?, ?>) elementInner).get("max")).intValue()));
                        } else {
                            sub.add(ConfigScreenUtils.getEntryBuilder(eb, typeInner, keyInner, errorSupplier));
                        }
                    }
                    category.addEntry(sub.build());
                } else {
                    category.addEntry(ConfigScreenUtils.getEntryBuilder(eb, type, key, errorSupplier));
                }
            }
        }
        return builder;
    }
}
