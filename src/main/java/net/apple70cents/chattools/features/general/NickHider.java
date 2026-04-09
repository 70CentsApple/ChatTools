package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class NickHider {
    private static final Map<String, Component> cache = new LinkedHashMap<>();
    private static Component text;
    private static String playerName;
    private static String nickname;

    private static final CircuitBreakerExecutor executor = CircuitBreakerExecutor.of(() -> {
        Component original = text.copy();
        try {
            Component result = TextUtils.replaceComponentText(text.copy(),
                    RegExUtils.getOrCompilePattern(Pattern.quote(playerName)), nickname);
            if (result != null && !"ERROR".equals(result.getString())) {
                text = result;
            } else {
                LoggerUtils.error("[ChatTools] ERROR was returned on nick-hiding this text: " + text + ", let's show it raw...");
                text = original;
            }
        } catch (Exception e) {
            LoggerUtils.error("[ChatTools] Error occurred on nick-hiding this text: " + text + ", let's show it raw...");
            e.printStackTrace();
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
        while (cache.size() > ConfigUtils.NICK_HIDER_CACHE_SIZE) {
            cache.remove(cache.keySet().iterator().next());
        }
        LocalPlayer player = Minecraft.getInstance().player;
        nickname = TextUtils.encodeColorCodes(ConfigUtils.NICK_HIDER_NICKNAME);
        if (player != null) {
            playerName = player.getName().getString();
            String key = nickname + "|" + playerName + "|" + message.toString();
            return cache.computeIfAbsent(key, k -> {
                text = message.copy();
                executor.run();
                return text;
            });
        }
        return message;
    }
}
