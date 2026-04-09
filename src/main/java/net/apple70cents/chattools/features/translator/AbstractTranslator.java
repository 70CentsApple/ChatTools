package net.apple70cents.chattools.features.translator;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractTranslator {

    /**
     * Builds a URL query string from a map of parameters.
     */
    protected static String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
    protected final EditBox editBox;

    protected AbstractTranslator(EditBox editBox) {
        this.editBox = editBox;
    }

    public void work() {
        String text = editBox.getValue();
        editBox.setValue(TextUtils.trans("texts.translator.await").getString());
        new Thread(() -> {
            Minecraft mc = Minecraft.getInstance();
            try {
                LoggerUtils.info("[ChatTools] Start translating: " + text);
                String result = translate(text).replace("\n","");
                mc.execute(() -> editBox.setValue(result));
                LoggerUtils.info("[ChatTools] Translation Result: " + result);
            } catch (Exception e) {
                LoggerUtils.error("[ChatTools] Error occurred when translating: " + text, e);
                mc.execute(() -> editBox.setValue(TextUtils.trans("texts.translator.error", e).getString()));
            }
        }, "Chat-Tools-Translation").start();
    }

    protected abstract String translate(String text) throws Exception;
}
