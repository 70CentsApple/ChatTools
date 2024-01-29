package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * @author 70CentsApple
 */
public class NickHider {
    public static Text work(Text message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            if (TextUtils.wash(message.getString()).contains(player.getName().getString())) {
                return TextUtils.replaceText((MutableText) message, player.getName().getString(),
                        TextUtils.escapeColorCodes((String) ChatTools.CONFIG.get("general.NickHider.Nickname")));
            }
        }
        return message;
    }
}
