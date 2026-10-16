package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.*;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChatColorEraser {
    private static final int MAX_CACHE_SIZE = 500;
    private static final Map<String, Component> cache = Collections.synchronizedMap(
            new LinkedHashMap<String, Component>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Component> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            }
    );
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
        if (message == null) {
            return null;
        }
        String key = message.toString();
        Component cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        synchronized (ChatColorEraser.class) {
            cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
            text = message;
            executor.run();
            Component result = text;
            cache.put(key, result);
            return result;
        }
    }
}
