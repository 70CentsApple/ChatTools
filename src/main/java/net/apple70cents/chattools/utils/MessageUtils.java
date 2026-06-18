package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.features.general.ExclusiveActionbarHandler;
import net.apple70cents.chattools.mixins.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Objects;
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
//? if >=26.1 {
        Minecraft.getInstance().player.sendOverlayMessage(text);
//?} else {
        /*Minecraft.getInstance().player.displayClientMessage(text, true);
*///?}
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
//? if >=26.1 {
        McUtils.getChat().addClientSystemMessage(text);
//?} else {
        /*McUtils.getChat().addMessage(text);
*///?}
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

        boolean oldStatus = ConfigUtils.getBoolean("formatter.Enabled");

        if (forceDisableFormatter) {
            ConfigUtils.set("formatter.Enabled", false);
        }

//? if >=1.19 {
        Minecraft.getInstance().execute(() -> {
            if (ConfigUtils.getBoolean("general.UseSendPacketsForSendingMessages")) {
                ChatScreen tempChatScreen = new ChatScreen(text
//? if >=1.21.9 {
                        , false
//?}
                );
                ((ScreenAccessor) tempChatScreen).invokeInit(
//? if >=1.21.11 {
//?} else {
                        /*Minecraft.getInstance(),
*///?}
                        1, 1);
                tempChatScreen.handleChatInput(text, false);
            } else {
                String text2 = StringUtils.normalizeSpace(text.trim());
                if (!text2.isEmpty()) {
                    McUtils.getChat().addRecentChat(text);
                    if (text2.startsWith("/")) {
                        player.connection.sendCommand(text2.substring(1));
                    } else {
                        player.connection.sendChat(text2);
                    }
                }
            }
        });
//?} else {
        /*player.chat(text);
*///?}
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
    public static String findTheFirstPlayerName(String str, boolean useProfile) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        int minIndex = str.length();
        String firstPlayerName = null;
        for (AbstractClientPlayer player : Minecraft.getInstance().level.players()) {
            String playerName = null;
            if (useProfile) {
                playerName = player.getGameProfile()
//? if >=1.21.10 {
                        .name();
//?} else {
                        /*.getName();
*///?}
            } else {
                if (player.getDisplayName() != null) {
                    playerName = player.getDisplayName().getString();
                }
            }
            if (playerName == null) {
                continue;
            }

            int index = str.indexOf(playerName);
            if (index != -1) {
                // select the longer one
                if (index < minIndex || (index == minIndex && playerName.length() > firstPlayerName.length())) {
                    minIndex = index;
                    firstPlayerName = playerName;
                }
            }
        }
        return firstPlayerName;
    }
}
