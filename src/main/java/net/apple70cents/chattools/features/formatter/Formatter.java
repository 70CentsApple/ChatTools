package net.apple70cents.chattools.features.formatter;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class Formatter {
    public static String work(String message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        for (String s : (List<String>) ChatTools.CONFIG.get("formatter.DisableOnMatchList")) {
            if (Pattern.compile(s).matcher(message).matches()) {
                return message;
            }
        }
        boolean matched = false;
        String formatter = "{text}";
        for (SpecialUnits.FormatterUnit unit : SpecialUnits.FormatterUnit.fromList((List) ChatTools.CONFIG.get("formatter.List"))) {
            if ("*".equals(unit.address)) {
                matched = true;
                formatter = unit.formatter;
                // we just need the first match result, break instantly.
                break;
            } else if (MinecraftClient.getInstance().getCurrentServerEntry() == null) {
                // It is in a single player world
                continue;
            } else if (Pattern.compile(unit.address)
                              .matcher(MinecraftClient.getInstance().getCurrentServerEntry().address).matches()) {
                matched = true;
                formatter = unit.formatter;
                // we just need the first match result, break instantly.
                break;
            }
        }
        if (matched) {
            LoggerUtils.info("[ChatTools] Chat Formatted.");
            message = formatter.replace("{text}", message);
        }
        if (player != null) {
            message = message.replace("{pos}", String.format("(%d,%d,%d)", (int) player.getX(), (int) player.getY(), (int) player.getZ()));
        }
        return message;
    }
}
