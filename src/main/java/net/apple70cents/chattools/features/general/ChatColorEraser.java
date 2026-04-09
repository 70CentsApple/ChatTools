package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.*;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChatColorEraser {
    private static final Map<String, Component> cache = new LinkedHashMap<>();
    private static Component text;

    private static final CircuitBreakerExecutor executor = CircuitBreakerExecutor.of(() -> {
        try {
            text = TextUtils.replaceComponentColor(text.copy());
        } catch (Exception e) {
            LoggerUtils.error("[ChatTools] Error occurred on erasing the color of this text: " + text + ", let's show it raw...", e);
        }
    }).setMaxLimitPerSecond(() -> ConfigUtils.getInt("general.CircuitBreaker.OverrideChatColorThreshold"))
    .setFailsafeFunction(() -> {
        ConfigUtils.set("general.OverrideChatColor.Enabled", false);
        int threshold = ConfigUtils.getInt("general.CircuitBreaker.OverrideChatColorThreshold");
        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.CircuitBreaker.exceed.OverrideChatColor", threshold));
        MessageUtils.sendToActionbar(TextUtils.trans("texts.CircuitBreaker.exceed.OverrideChatColor", threshold));
        LoggerUtils.warn("[ChatTools] " + TextUtils.trans("texts.CircuitBreaker.exceed.OverrideChatColor", threshold).getString());
    }).setFailsafeJudgement(() -> ConfigUtils.getBoolean("general.OverrideChatColor.Enabled"));

    public static Component work(Component message) {
        String key = message.toString();

        if (cache.containsKey(key)) {
            return cache.get(key); // get from cache
        }

        text = message;
        executor.run();
        cache.put(key, text);
        return text;
    }
}
