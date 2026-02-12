package net.apple70cents.chattools.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;

/**
 * @author 70CentsApple
 */
public class ContextUtils {
    public static String getSessionIdentifier() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            return server.getWorldData().getLevelName();
        }

        ServerData currentServer = Minecraft.getInstance().getCurrentServer();
        if (currentServer != null) {
            return currentServer.ip;
        }

        return "-";
    }

}
