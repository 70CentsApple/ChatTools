package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.features.general.ExclusiveActionbarHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 70CentsApple
 */
public class MessageUtils {

    private static boolean justSentMessage = false;

    public static boolean hadJustSentMessage() {
        return justSentMessage;
    }

    public static void setJustSentMessage(boolean bl) {
        justSentMessage = bl;
    }

    public static void sendToActionbar(Component text) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (!(boolean) ConfigUtils.get("general.ExclusiveActionbar.Enabled")) {
            Minecraft.getInstance().player.displayClientMessage(text, true);
        } else {
            ExclusiveActionbarHandler.addToRenderQueue(text, 4000);
        }
    }

    public static void sendToActionbar(Component text, int duration) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (!(boolean) ConfigUtils.get("general.ExclusiveActionbar.Enabled")) {
            LoggerUtils.warn("[ChatTools] Customized actionbar duration is not supported when Exclusive Actionbar is disabled.");
            sendToActionbar(text);
        } else {
            ExclusiveActionbarHandler.addToRenderQueue(text, duration);
        }
    }

    public static void sendToNonPublicChat(Component text) {
        Minecraft.getInstance().gui.getChat().addMessage(text);
    }

    public static void sendToPublicChat(String text, boolean forceDisableFormatter) {
        boolean oldStatus = (boolean) ConfigUtils.get("formatter.Enabled");
        if (forceDisableFormatter) {
            ConfigUtils.set("formatter.Enabled", false);
        }
        sendToPublicChat(text);
        ConfigUtils.set("formatter.Enabled", oldStatus);
    }

    public static void sendToPublicChat(String text) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        setJustSentMessage(true);

        //#if MC>=11900
        String text2 = StringUtils.normalizeSpace(text.trim());
        if (!text2.isEmpty()) {
            Minecraft.getInstance().gui.getChat().addRecentChat(text);
            if (text2.startsWith("/") || text2.startsWith(".")) {
                player.connection.sendCommand(text2.substring(1));
            } else {
                player.connection.sendChat(text2);
            }
        }
        //#else
        //$$ player.chat(text);
        //#endif
    }

    /**
     * finds the most front player nickname (if there is) in the given string
     *
     * @param str the string
     * @return null or the player name
     */
    public static String findTheFirstPlayerName(String str) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        int minIndex = str.length();
        String firstPlayerName = null;
        for (AbstractClientPlayer player : Minecraft.getInstance().level.players()) {
            if (player.getDisplayName() == null) {
                continue;
            }
            String playerName = player.getDisplayName().getString();

            if (str.contains(playerName) && str.indexOf(playerName) < minIndex) {
                minIndex = str.indexOf(playerName);
                firstPlayerName = playerName;
            }
        }
        return firstPlayerName;
    }
}
