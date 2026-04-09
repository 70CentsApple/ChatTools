package net.apple70cents.chattools.features.translator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.minecraft.client.gui.components.EditBox;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class GoogleFreeTranslator extends AbstractTranslator {
    private static final String ENDPOINT = "https://translate.googleapis.com/translate_a/single";
    private final String sl;
    private final String tl;

    protected GoogleFreeTranslator(EditBox editBox, String sl, String tl) {
        super(editBox);
        this.sl = sl;
        this.tl = tl;
    }

    @Override
    public String translate(String text) throws Exception {
        if (sl.isBlank()) {
            throw new Exception("sl required");
        }
        if (tl.isBlank()) {
            throw new Exception("tl required");
        }
        return getTransResult(text, sl, tl);
    }

    protected static String getTransResult(String query, String sl, String tl) throws Exception {
        Map<String, String> params = buildParams(query, sl, tl);
        String jsonResponse = sendGet(params);
        return parseJsonResponse(jsonResponse);
    }

    // See https://translate.google.com/
    private static Map<String, String> buildParams(String query, String sl, String tl) {
        Map<String, String> params = new HashMap<>();
        params.put("client", "gtx");
        params.put("sl", sl);
        params.put("tl", tl);
        params.put("dt", "t");
        params.put("q", query);
        return params;
    }

    private static String sendGet(Map<String, String> params) throws Exception {
        String queryString = buildQueryString(params);
        String fullUrl = ENDPOINT + "?" + queryString;

        HttpClient client = HttpClient.newBuilder()
                .proxy(ProxySelector.getDefault())
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static String parseJsonResponse(String jsonResponse) throws Exception {
        JsonArray json = new Gson().fromJson(jsonResponse, com.google.gson.JsonArray.class).getAsJsonArray();
        JsonArray results = json.getAsJsonArray().get(0).getAsJsonArray();

        StringBuilder translatedText = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            JsonArray segment = results.get(i).getAsJsonArray();
            if (segment != null && !segment.get(0).isJsonNull()) {
                translatedText.append(segment.get(0).getAsString());
            }
        }

        return translatedText.toString();
    }

}
