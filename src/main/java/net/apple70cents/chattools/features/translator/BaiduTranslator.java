package net.apple70cents.chattools.features.translator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.gui.components.EditBox;

import java.util.HashMap;
import java.util.Map;

public class BaiduTranslator {
    private static final String TRANS_API_HOST = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    public static void translate(EditBox chatField) {
        String originalText = chatField.getValue();
        chatField.setValue(TextUtils.trans("texts.translator.await").getString());
        if (((String) ConfigUtils.get("translator.Translator.Baidu.Appid")).isBlank()) {
            chatField.setValue(TextUtils.trans("texts.translator.requireApi").getString());
            return;
        }
        if (((String) ConfigUtils.get("translator.Translator.Baidu.Appkey")).isBlank()) {
            chatField.setValue(TextUtils.trans("texts.translator.requireApi").getString());
            return;
        }
        if (((String) ConfigUtils.get("translator.Translator.Baidu.from")).isBlank()) {
            chatField.setValue(TextUtils.trans("texts.translator.requireApi").getString());
            return;
        }
        if (((String) ConfigUtils.get("translator.Translator.Baidu.to")).isBlank()) {
            chatField.setValue(TextUtils.trans("texts.translator.requireApi").getString());
            return;
        }
        try {
            String translatedText = getTransResult(originalText, (String) ConfigUtils.get("translator.Translator.Baidu.from"), (String) ConfigUtils.get("translator.Translator.Baidu.to"));
            chatField.setValue(translatedText);
        } catch (Exception e) {
            e.printStackTrace();
            chatField.setValue(TextUtils.trans("texts.translator.error").getString());
        }
    }

    private static Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);

        params.put("appid", (String) ConfigUtils.get("translator.Translator.Baidu.Appid"));

        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);
        String src = ConfigUtils.get("translator.Translator.Baidu.Appid") + query + salt + ConfigUtils.get("translator.Translator.Baidu.Appkey"); // 加密前的原文
        params.put("sign", MD5Encoder.md5(src));

        return params;
    }

    public static String getTransResult(String query, String from, String to) {
        Map<String, String> params = buildParams(query, from, to);
        String jsonResponse = HttpGetClient.get(TRANS_API_HOST, params);
        return parseJsonResponse(jsonResponse);
    }

    private static String parseJsonResponse(String jsonResponse) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(jsonResponse, com.google.gson.JsonObject.class);

            if (json.has("trans_result")) {
                JsonArray transResult = json.get("trans_result").getAsJsonArray();
                return transResult.get(0).getAsJsonObject().get("dst").getAsString();
            } else if (json.has("error_code")) {
                return TextUtils.trans("texts.translator.error").getString() + json.get("error_code").getAsString();
            } else {
                return TextUtils.trans("texts.translator.error").getString() + "Unknown";
            }
        } catch (Exception e) {
            return TextUtils.trans("texts.translator.error").getString() + e.getMessage();
        }
    }
}
