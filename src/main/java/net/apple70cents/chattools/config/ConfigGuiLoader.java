package net.apple70cents.chattools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the config_gui.json and parses the key-to-type mappings.
 * Independent of any config library (Cloth Config or YACL).
 *
 * @author 70CentsApple
 */
public class ConfigGuiLoader {
    private static final Gson GSON = new GsonBuilder().create();
    
    public static Map<String, Object> configGuiMap;
    public static boolean configGuiMapInitialized = false;
    private static int GUI_VERSION = -1;
    
    private static final Map<String, String> key2TypeMappings = new HashMap<>();

    public static Map<String, String> getKey2TypeMappings() {
        if (!configGuiMapInitialized || key2TypeMappings.isEmpty()) {
            loadConfigGuiMap(); // Ensure initialized
        }
        return key2TypeMappings;
    }

    @SuppressWarnings("unchecked")
    public static void loadConfigGuiMap() {
        try {
            InputStream inputStream = Minecraft.getInstance().getClass().getClassLoader()
                                                     .getResourceAsStream("assets/chattools/config_gui.json");
            if (inputStream == null) return;
            Reader reader = new InputStreamReader(inputStream);
            configGuiMap = GSON.fromJson(reader, Map.class);
            GUI_VERSION = ((Number) configGuiMap.get("version")).intValue();
            
            // Populate key2TypeMappings
            for (Object categoryInfo : (List<?>) configGuiMap.get("content")) {
                for (Object element : (List<?>) ((Map<?, ?>) categoryInfo).get("content")) {
                    String type = (String) ((Map<?, ?>) element).get("type");
                    String key = (String) ((Map<?, ?>) element).get("key");
                    key2TypeMappings.put(key, type);
                    
                    if ("sub".equals(type)) {
                        for (Object elementInner : (List<?>) ((Map<?, ?>) element).get("content")) {
                            String typeInner = (String) ((Map<?, ?>) elementInner).get("type");
                            String keyInner = (String) ((Map<?, ?>) elementInner).get("key");
                            key2TypeMappings.put(keyInner, typeInner);
                        }
                    }
                }
            }
            
            configGuiMapInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initializeConfigGuiMapIfNecessary() {
        if (!configGuiMapInitialized) {
            loadConfigGuiMap();
        }
    }
}
