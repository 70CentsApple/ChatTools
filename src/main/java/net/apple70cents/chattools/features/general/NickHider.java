package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class NickHider {
    private static final Map<String, Component> cache = Collections.synchronizedMap(
            new LinkedHashMap<String, Component>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Component> eldest) {
                    return size() > ConfigUtils.NICK_HIDER_CACHE_SIZE;
                }
            }
    );
    private static Component text;
    private static String playerName;
    private static String nickname;

    private static final CircuitBreakerExecutor executor = CircuitBreakerExecutor.of(() -> {
        Component original = text.copy();
        try {
            text = TextUtils.replaceComponentText(text.copy(),
                    RegExUtils.getOrCompilePattern(Pattern.quote(playerName)), nickname);
        } catch (Exception e) {
            LoggerUtils.error("[ChatTools] Error occurred on nick-hiding this text: " + text + ", let's show it raw...", e);
            text = original;
        }
    }).setMaxLimitPerSecond(() -> ConfigUtils.getInt("general.CircuitBreaker.NickHiderThreshold"))
    .setFailsafeFunction(() -> {
        ConfigUtils.set("general.NickHider.Enabled", false);
        int threshold = ConfigUtils.getInt("general.CircuitBreaker.NickHiderThreshold");
        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.CircuitBreaker.exceed.NickHider", threshold));
        MessageUtils.sendToActionbar(TextUtils.trans("texts.CircuitBreaker.exceed.NickHider", threshold));
        LoggerUtils.warn("[ChatTools] " + TextUtils.trans("texts.CircuitBreaker.exceed.NickHider", threshold).getString());
    }).setFailsafeJudgement(() -> ConfigUtils.NICK_HIDER_ENABLED);

    public static Component work(Component message) {
        if (message == null) {
            return null;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return message;
        }

        String curPlayerName = player.getName().getString();
        String plainText = message.getString();
        if (!plainText.contains(curPlayerName)) {
            return message;
        }

        String curNickname = TextUtils.encodeColorCodes(ConfigUtils.NICK_HIDER_NICKNAME);
        String key = curNickname + "|" + curPlayerName + "|" + plainText + "#" + message.hashCode();

        Component cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        synchronized (NickHider.class) {
            cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
            playerName = curPlayerName;
            nickname = curNickname;
            text = message.copy();
            executor.run();
            Component result = text;
            cache.put(key, result);
            return result;
        }
    }
}
