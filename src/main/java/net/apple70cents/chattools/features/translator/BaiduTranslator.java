package net.apple70cents.chattools.features.translator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.components.EditBox;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BaiduTranslator extends AbstractTranslator {
    private static final String ENDPOINT = "https://fanyi-api.baidu.com/api/trans/vip/translate";
    private final String appId;
    private final String key;
    private final String from;
    private final String to;

    protected BaiduTranslator(EditBox editBox, String appId, String key, String from, String to) {
        super(editBox);
        this.appId = appId;
        this.key = key;
        this.from = from;
        this.to = to;
    }

    public String translate(String text) throws Exception {
        if (appId.isBlank()) {
            throw new Exception("appId required");
        }
        if (key.isBlank()) {
            throw new Exception("key required");
        }
        if (from.isBlank()) {
            throw new Exception("from language required");
        }
        if (to.isBlank()) {
            throw new Exception("to language required");
        }
        return getTransResult(text, appId, key, from, to);
    }

    public static String getTransResult(String query, String appId, String key, String from, String to) throws Exception {
        Map<String, String> params = buildParams(query, appId, key, from, to);
        String jsonResponse = sendPost(params);
        return parseJsonResponse(jsonResponse);
    }

    // See https://api.fanyi.baidu.com/doc/21
    private static Map<String, String> buildParams(String query, String appId, String key, String from, String to) {
        String salt = String.valueOf(System.currentTimeMillis());
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);
        params.put("appid", appId);
        params.put("salt", salt);
        params.put("sign", md5(appId + query + salt + key));
        return params;
    }

    private static String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String sendPost(Map<String, String> params) throws Exception{
        String form = params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static String parseJsonResponse(String jsonResponse) throws Exception {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(jsonResponse, com.google.gson.JsonObject.class);

        if (json.has("trans_result")) {
            JsonArray transResult = json.get("trans_result").getAsJsonArray();
            return transResult.get(0).getAsJsonObject().get("dst").getAsString();
        } else if (json.has("error_code")) {
            throw new Exception("Code " + json.get("error_code").getAsString());
        } else {
            throw new Exception("Unexpected response format");
        }
    }
}
