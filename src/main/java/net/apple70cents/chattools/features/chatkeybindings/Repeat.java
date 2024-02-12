package net.apple70cents.chattools.features.chatkeybindings;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class Repeat {
    private static boolean keyWasPressed;

    public static void tick() {
        String key = (String) ChatTools.CONFIG.get("chatkeybindings.RepeatKey");
        SpecialUnits.KeyModifiers modifier = SpecialUnits.KeyModifiers.valueOf((String) ChatTools.CONFIG.get("chatkeybindings.RepeatKeyModifier"));
        if (KeyboardUtils.isKeyPressingWithModifier(key, modifier, SpecialUnits.MacroModes.GREEDY) && MinecraftClient.getInstance().currentScreen == null) {
            if (!keyWasPressed) {
                keyWasPressed = true;
                LoggerUtils.info("[ChatTools] Triggered the latest command.");
                MinecraftClient mc = MinecraftClient.getInstance();
                String mcVersion = FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata()
                                               .getVersion().getFriendlyString();
                if ("1.20".equals(mcVersion) || "1.20.1".equals(mcVersion)) {
                    MessageUtils.sendToActionbar(TextUtils.trans("texts.repeat.failure.unsupported"));
                    return;
                }
                // Note:
                // Between versions 1.20.1 ~ 1.20.2, the return value type of `getMessageHistory()` had
                // changed from `List` to `net.minecraft.util.collection.ArrayListDeque` (a.k.a `net/minecraft/class_8623` ),
                // so if this code is compiled on 1.20.2+, there will be a mapping problem here on both 1.20 and 1.20.1,
                // which can be solved by recompiling on either of those versions individually.
                // I don't think it's a big problem, not gonna fix this ^v^
                List<String> history = mc.inGameHud.getChatHud().getMessageHistory();
                if (history.isEmpty()) {
                    MessageUtils.sendToActionbar(TextUtils.trans("texts.repeat.failure.empty"));
                } else {
                    MessageUtils.sendToPublicChat(history.get(history.size() - 1));
                }
            }
        } else {
            keyWasPressed = false;
        }
    }
}
