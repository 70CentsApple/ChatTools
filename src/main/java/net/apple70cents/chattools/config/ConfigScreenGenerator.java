package net.apple70cents.chattools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.apple70cents.chattools.utils.ConfigScreenUtils;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.Minecraft;

//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * @author 70CentsApple
 */
public class ConfigScreenGenerator {
    private static Map<String, Object> configGuiMap;
    private static int GUI_VERSION = -1;
    public static boolean configGuiMapInitialized = false;
    private static final Gson GSON = new GsonBuilder().create();

    private static final Map<String, String> key2TypeMappings = new HashMap<>();

    public static Map<String, String> getKey2TypeMappings() {
        if (!configGuiMapInitialized || key2TypeMappings.isEmpty()) {
            getConfigBuilder(); // let Key2TypeMappings initialize
        }
        return key2TypeMappings;
    }


    public static void loadConfigGuiMap() {
        try {
            InputStream inputStream = Minecraft.getInstance().getClass().getClassLoader()
                                                     .getResourceAsStream("assets/chattools/config_gui.json");
            Reader reader = new InputStreamReader(inputStream);
            configGuiMap = GSON.fromJson(reader, Map.class);
            configGuiMapInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initializeConfigGuiMapIfNecessary() {
        if (!configGuiMapInitialized) {
            loadConfigGuiMap();
            GUI_VERSION = ((Number) configGuiMap.get("version")).intValue();
        }
    }

    public static ConfigBuilder getConfigBuilder() {
        initializeConfigGuiMapIfNecessary();

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
        for (Object categoryInfo : (List) configGuiMap.get("content")) {
            ConfigCategory category = builder.getOrCreateCategory(trans((String) ((Map) categoryInfo).get("key")));
            for (Object element : (List) ((Map) categoryInfo).get("content")) {
                String type = (String) ((Map) element).get("type");
                String key = (String) ((Map) element).get("key");
                key2TypeMappings.put(key, type);
                String errorSupplier = (String) ((Map) element).getOrDefault("errorSupplier", "null");
                if ("intSlider".equals(type)) {
                    category.addEntry(ConfigScreenUtils.getEntryBuilder(eb, type, key, errorSupplier, ((Number) ((Map) element).get("min")).intValue(), ((Number) ((Map) element).get("max")).intValue()));
                } else if ("sub".equals(type)) {
                    SubCategoryBuilder sub = eb.startSubCategory(trans(key))
                                               .setTooltip(ConfigScreenUtils.getTooltip(key, type, null));
                    for (Object elementInner : (List) ((Map) element).get("content")) {
                        String typeInner = (String) ((Map) elementInner).get("type");
                        String keyInner = (String) ((Map) elementInner).get("key");
                        key2TypeMappings.put(keyInner, typeInner);
                        // we are assuming no sub nested in subs, therefore two layers are enough,
                        // and we are not going to deal with sub in sub
                        if ("intSlider".equals(typeInner)) {
                            sub.add(ConfigScreenUtils.getEntryBuilder(eb, typeInner, keyInner, errorSupplier, ((Number) ((Map) elementInner).get("min")).intValue(), ((Number) ((Map) elementInner).get("max")).intValue()));
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
