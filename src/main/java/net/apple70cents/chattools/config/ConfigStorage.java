package net.apple70cents.chattools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.LoggerUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 70CentsApple
 */
public class ConfigStorage {
    public static final File FILE = new File(
//? if FABRIC {
            /*net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()
*///?} elif NEOFORGE {
            net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get()
//?}
                    .toFile(), "chat_tools.json");

    private Map<String, Object> configMap;

    public static boolean configFileExists() {
        return FILE.exists();
    }

    public ConfigStorage(boolean useDefault) {
        readConfigFile(useDefault);
    }

    public Map getHashmap() {
        return Collections.unmodifiableMap(configMap);
    }

    // combine two hashmaps
    public ConfigStorage withDefault(Map<String, Object> defaultMap) {
        for (Map.Entry<String, Object> entry : defaultMap.entrySet()) {
            // If there is a key conflict, the value of `configMap` takes precedence;
            // if there is no conflict, the key is added to `configMap`, and the value comes from `defaultMap`.
            configMap.putIfAbsent(entry.getKey(), entry.getValue());
        }
        configMap.put("config.version", defaultMap.get("config.version"));
        return this;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public void readConfigFile(boolean loadDefault) {
        try {
            Reader reader;
            if (loadDefault) {
                reader = new InputStreamReader(
                        this.getClass().getClassLoader().getResourceAsStream("assets/chattools/default_config.json"));
            } else {
                reader = new InputStreamReader(new FileInputStream(FILE), StandardCharsets.UTF_8);
            }
            configMap = GSON.fromJson(reader, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object get(String key) {
        if (this.hasKey(key)) {
            return configMap.get(key);
        } else if (ConfigUtils.DEFAULT_CONFIG.hasKey(key)) {
            return ConfigUtils.DEFAULT_CONFIG.get(key);
        } else {
            LoggerUtils.error("[ChatTools] Error occurred when getting variable \"" + key + "\", no such key!");
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public boolean hasKey(String key) {
        return this.configMap.containsKey(key);
    }

    public void set(String variableName, Object value) {
        configMap.put(variableName, value);
    }

    public void remove(String key) {
        configMap.remove(key);
    }

    public void save() {
        ((List<String>) get("notifier.DenyList")).removeIf(String::isBlank);
        ((List<String>) get("formatter.DisableOnMatchList")).removeIf(String::isBlank);
        ((List<String>) get("filter.List")).removeIf(String::isBlank);
        LoggerUtils.info("[ChatTools] Saving configs.");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8)) {
            GSON.toJson(configMap, writer);
        } catch (Exception e) {
            LoggerUtils.error("[ChatTools] Couldn't save config.");
            e.printStackTrace();
        }
    }
}
