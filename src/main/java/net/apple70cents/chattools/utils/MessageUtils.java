package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.ChatTools;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void sendToActionbar(Text text) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(text, true);
        }
    }

    public static void sendToNonPublicChat(Text text) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
    }

    public static void sendToPublicChat(String text, boolean forceDisableFormatter) {
        boolean oldStatus = (boolean) ChatTools.CONFIG.get("formatter.Enabled");
        if (forceDisableFormatter) {
            ChatTools.CONFIG.set("formatter.Enabled", false);
        }
        sendToPublicChat(text);
        ChatTools.CONFIG.set("formatter.Enabled", oldStatus);
    }

    public static void sendToPublicChat(String text) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        setJustSentMessage(true);

        //#if MC>=11900
        String text2 = StringHelper.truncateChat(StringUtils.normalizeSpace(text.trim()));
        if (!text2.isEmpty()) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addToMessageHistory(text);
            if (text2.startsWith("/")) {
                player.networkHandler.sendChatCommand(text2.substring(1));
            } else {
                player.networkHandler.sendChatMessage(text2);
            }
        }
        //#else
        //$$ player.sendChatMessage(text);
        //#endif
    }

    /**
     * finds the most front player nickname (if there is) in the given string
     *
     * @param str the string
     * @return null or the player name
     */
    public static String findTheFirstPlayerName(String str) {
        if (MinecraftClient.getInstance().world == null) {
            return null;
        }
        int minIndex = str.length();
        String firstPlayerName = null;
        for (AbstractClientPlayerEntity player : MinecraftClient.getInstance().world.getPlayers()) {
            String playerName = player.getDisplayName().getString();
            String regex = "\\b" + Pattern.quote(playerName) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(str);

            if (matcher.find() && matcher.start() < minIndex) {
                minIndex = matcher.start();
                firstPlayerName = matcher.group();
            }
        }
        return firstPlayerName;
    }
}
