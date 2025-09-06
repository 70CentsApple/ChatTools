package net.apple70cents.chattools.features.translator;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.gui.components.EditBox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BuiltinTranslator {
    public static void work(EditBox chatField) {
        String originalText = chatField.getValue();
        String method = (boolean) ConfigUtils.get("translator.Translator.Builtin.PostInstead") ? "POST" : "GET";
        if (((String) ConfigUtils.get("translator.Translator.Builtin.API")).isBlank()) {
            chatField.setValue(TextUtils.trans("texts.translator.requireApi").getString());
            return;
        }
        chatField.setValue(TextUtils.trans("texts.translator.await").getString());
        Runnable runnable = () -> {
            try {
                String api = (String) ConfigUtils.get("translator.Translator.Builtin.API");
                if (api.contains("{text}")) {
                    api = api.replace("{text}", URLEncoder.encode(originalText, StandardCharsets.UTF_8));
                } else {
                    api += URLEncoder.encode(originalText, StandardCharsets.UTF_8);
                }
                URL formattedUrl = new URL(api);
                HttpURLConnection connection = (HttpURLConnection) formattedUrl.openConnection();
                connection.setRequestMethod(method);
                LoggerUtils.info("[ChatTools] Visiting \"" + api + "\" with method: " + method);
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("Failed with HTTP Error Code " + responseCode);
                } else {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }
                        chatField.setValue(response.toString());
                        LoggerUtils.info("[ChatTools] Response: " + response);
                        return;
                    }
                }
            } catch (Exception e) {
                LoggerUtils.error("[ChatTools] Error occurred when visiting Translation API");
                chatField.setValue(e.toString());
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable, "ChatTools-Translation-Thread");
        thread.start();
    }
}
