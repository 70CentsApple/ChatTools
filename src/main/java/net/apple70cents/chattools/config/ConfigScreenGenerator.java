package net.apple70cents.chattools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.utils.ConfigScreenUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * @author 70CentsApple
 */
public class ConfigScreenGenerator {
    private static Map<String, Object> configGuiMap;
    private static int GUI_VERSION = -1;
    private static boolean configGuiMapInitialized = false;
    private static final Gson GSON = new GsonBuilder().create();

    private static void loadConfigGuiMap() {
        try {
            InputStream inputStream = MinecraftClient.getInstance().getClass().getClassLoader()
                                                     .getResourceAsStream("assets/chattools/config_gui.json");
            Reader reader = new InputStreamReader(inputStream);
            configGuiMap = GSON.fromJson(reader, Map.class);
            configGuiMapInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ConfigBuilder getConfigBuilder() {
        if (!configGuiMapInitialized) {
            loadConfigGuiMap();
            GUI_VERSION = ((Number) configGuiMap.get("version")).intValue();
        }

        ConfigBuilder builder = ConfigBuilder.create().setTitle(trans("gui.title"))
                                             .setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/oak_planks.png"))
                                             .setTransparentBackground(true).setSavingRunnable(ChatTools.CONFIG::save);
        ConfigEntryBuilder eb = builder.entryBuilder();
        for (Object categoryInfo : (List) configGuiMap.get("content")) {
            ConfigCategory category = builder.getOrCreateCategory(trans((String) ((Map) categoryInfo).get("key")));
            for (Object element : (List) ((Map) categoryInfo).get("content")) {
                String type = (String) ((Map) element).get("type");
                String key = (String) ((Map) element).get("key");
                if ("intSlider".equals(type)) {
                    category.addEntry(ConfigScreenUtils.getEntryBuilder(eb, type, key, ((Number) ((Map) element).get("min")).intValue(), ((Number) ((Map) element).get("max")).intValue()));
                } else if ("sub".equals(type)) {
                    SubCategoryBuilder sub = eb.startSubCategory(trans(key)).setTooltip(trans(key + ".@Tooltip"));
                    for (Object elementInner : (List) ((Map) element).get("content")) {
                        String typeInner = (String) ((Map) elementInner).get("type");
                        String keyInner = (String) ((Map) elementInner).get("key");
                        // assuming no sub nested in subs, therefore two layers are enough
                        // not going to deal with sub in sub
                        if ("intSlider".equals(typeInner)) {
                            sub.add(ConfigScreenUtils.getEntryBuilder(eb, typeInner, keyInner, ((Number) ((Map) elementInner).get("min")).intValue(), ((Number) ((Map) elementInner).get("max")).intValue()));
                        } else {
                            sub.add(ConfigScreenUtils.getEntryBuilder(eb, typeInner, keyInner));
                        }
                    }
                    category.addEntry(sub.build());
                } else {
                    category.addEntry(ConfigScreenUtils.getEntryBuilder(eb, type, key));
                }
            }
        }
        return builder;
    }
}
