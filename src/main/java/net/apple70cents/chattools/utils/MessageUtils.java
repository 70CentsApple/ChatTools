package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.features.general.ExclusiveActionbarHandler;
import net.apple70cents.chattools.mixins.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 70CentsApple
 */
public class MessageUtils {
    private static long lastSentMessageTimestamp = -1L;
    private static final long JUST_SENT_MESSAGE_AWAIT_TIME_IN_MILLISECONDS = 500L;

    private static boolean processingServerMessage = false;

    public static boolean isProcessingServerMessage() {
        return processingServerMessage;
    }

    public static void setProcessingServerMessageState() {
        MessageUtils.processingServerMessage = true;
    }

    public static void resetProcessingServerMessageState() {
        MessageUtils.processingServerMessage = false;
    }


    public static boolean hadJustSentMessage() {
        return System.currentTimeMillis() - lastSentMessageTimestamp < JUST_SENT_MESSAGE_AWAIT_TIME_IN_MILLISECONDS;
    }

    public static void updateLastSentMessageTimestamp() {
        lastSentMessageTimestamp = System.currentTimeMillis();
    }

    public static void sendToOriginalActionbar(Component text) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        Minecraft.getInstance().player.displayClientMessage(text, true);
    }

    public static void sendToActionbar(Component text) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (!ConfigUtils.EXCLUSIVE_ACTIONBAR_ENABLED) {
            sendToOriginalActionbar(text);
        } else {
            ExclusiveActionbarHandler.addToRenderQueue(text, 4000);
        }
    }

    public static void sendToActionbar(Component text, int duration) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (!ConfigUtils.EXCLUSIVE_ACTIONBAR_ENABLED) {
            LoggerUtils.warn(
                    "[ChatTools] Customized actionbar duration is not supported when Exclusive Actionbar is disabled.");
            sendToActionbar(text);
        } else {
            ExclusiveActionbarHandler.addToRenderQueue(text, duration);
        }
    }

    public static void sendToNonPublicChat(Component text) {
        Minecraft.getInstance().gui.getChat().addMessage(text);
    }

    public static void sendToPublicChat(String text) {
        sendToPublicChat(text, false);
    }

    public static void sendToPublicChat(String text, boolean forceDisableFormatter) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        updateLastSentMessageTimestamp();

        boolean oldStatus = (boolean) ConfigUtils.get("formatter.Enabled");

        if (forceDisableFormatter) {
            ConfigUtils.set("formatter.Enabled", false);
        }

        //#if MC>=11900
        Minecraft.getInstance().execute(() -> {
            if ((boolean) ConfigUtils.get("general.UseSendPacketsForSendingMessages")) {
                ChatScreen tempChatScreen = new ChatScreen(text
                        //#if MC>=12109
                        , false
                        //#endif
                );
                ((ScreenAccessor) tempChatScreen).invokeInit(
                        //#if MC>=12111
                        //$$ // no-op
                        //#else
                        //$$ Minecraft.getInstance(),
                        //#endif
                        1, 1);
                tempChatScreen.handleChatInput(text, false);
            } else {
                String text2 = StringUtils.normalizeSpace(text.trim());
                if (!text2.isEmpty()) {
                    Minecraft.getInstance().gui.getChat().addRecentChat(text);
                    if (text2.startsWith("/")) {
                        player.connection.sendCommand(text2.substring(1));
                    } else {
                        player.connection.sendChat(text2);
                    }
                }
            }
        });
        //#else
        //$$ player.chat(text);
        //#endif
        ConfigUtils.set("formatter.Enabled", oldStatus);
    }

    public static void sendToPublicChatScheduled(String text, long delayInMilliseconds) {
        sendToPublicChatScheduled(text, false, delayInMilliseconds);
    }
    
    public static void sendToPublicChatScheduled(String text, boolean forceDisableFormatter, long delayInMilliseconds) {
        CompletableFuture.runAsync(() -> {
            if (delayInMilliseconds > 0) {
                try {
                    Thread.sleep(delayInMilliseconds);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            Minecraft.getInstance().execute(() -> MessageUtils.sendToPublicChat(text, forceDisableFormatter));
        });
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
