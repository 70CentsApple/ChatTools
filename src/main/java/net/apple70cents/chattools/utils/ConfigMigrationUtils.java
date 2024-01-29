package net.apple70cents.chattools.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.apple70cents.chattools.config.ConfigStorage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class ConfigMigrationUtils {
    public static final double CURRENT_VERSION = 2.0;
    public static double CONFIG_VERSION = -1.0;

    public static void checkAndMigrate() {
        File configFile = ConfigStorage.FILE;
        if (isConfigOutOfDate(configFile)) {
            LoggerUtils.warn("[ChatTools] The config in version: " + CONFIG_VERSION + " is out of date! Trying to migrate it.");
            migrate(configFile);
        }
    }

    public static boolean isConfigOutOfDate(File configFile) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
            JsonObject json = new Gson().fromJson(content, JsonObject.class);
            if (json.has("config.version")) {
                CONFIG_VERSION = json.get("config.version").getAsDouble();
            } else {
                // if there is no `config.version` key, then we assume it is v1.0.
                CONFIG_VERSION = 1.0;
            }
            return CONFIG_VERSION < CURRENT_VERSION;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void migrate(File configFile) {
        if (CONFIG_VERSION >= 1.0 && CONFIG_VERSION < 2.0) {
            migrateFromV1ToV2(configFile, new File(ConfigMigrationUtils.class.getClassLoader()
                                                                             .getResource("assets/chattools/migration_v1_to_v2.json")
                                                                             .getFile()));
        }
    }

    private static void migrateFromV1ToV2(File file, File migrationMappingsFile) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            // 读取并解析映射文件
            String mappingsContent = new String(Files.readAllBytes(migrationMappingsFile.toPath()));
            JsonObject keyMappings = new Gson().fromJson(mappingsContent, JsonObject.class);

            for (Map.Entry<String, JsonElement> ele : keyMappings.entrySet()) {
                String oldKey = ele.getKey();
                String newKey = ele.getValue().getAsString();
                content = content.replace("\"" + oldKey + "\"", "\"" + newKey + "\"");
            }

            // this flattens the nested settings
            String regex = "(?<clear1>\\\"(nickHiderSettings|soundSettings|actionbarSettings|highlightSettings|toastNotifySettings)\\\"\\s*:\\s*\\{)(?<keep>[^{}]*?)(?<clear2>\\})";
            Matcher matcher = Pattern.compile(regex).matcher(content);
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                String keepContent = matcher.group("keep");
                matcher.appendReplacement(result, Matcher.quoteReplacement(keepContent));
            }
            matcher.appendTail(result);
            Files.write(file.toPath(), result.toString().getBytes());
            LoggerUtils.info("[ChatTools] Migrated it from v1 to v2!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}