package net.apple70cents.chattools.features.quickchat;

import com.terraformersmc.modmenu.util.mod.Mod;
import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ModClothConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;

import java.util.List;

import static net.apple70cents.chattools.ChatTools.isKeyPressedOrMouseKeyClicked;


public class QuickRepeat {
    private static boolean keyWasPressed;
    protected static ModClothConfig config = ModClothConfig.get();

    /**
     * 处理一键复读逻辑
     */
    public static void checkQuickRepeat() {
        if (config.quickRepeatKey.equals(InputUtil.UNKNOWN_KEY.getTranslationKey())) {
            return;
        }

        if (isKeyPressedOrMouseKeyClicked(config.quickRepeatKey, config.quickRepeatKeyModifier)) {
            if (!keyWasPressed) {
                keyWasPressed = true;
                ChatTools.LOGGER.info("[ChatTools] Triggered the latest command.");
                MinecraftClient mc = MinecraftClient.getInstance();
                List<String> history = mc.inGameHud.getChatHud().getMessageHistory();
                if (history.isEmpty()) {
                    mc.player.sendMessage(new TranslatableText("text.config.chattools.option.quickRepeatFailure"), true);
                } else {
                    mc.player.sendChatMessage(history.get(history.size() - 1));
                }
            }
        } else {
            keyWasPressed = false;
        }
    }
}