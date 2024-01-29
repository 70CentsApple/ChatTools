package net.apple70cents.chattools.features.notifier;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.regex.Pattern;

public class BasicNotifier {
    public static boolean shouldWork(Text text) {
        boolean shouldMatch = false;
        List<String> allowList = (List<String>) ChatTools.CONFIG.get("notifier.AllowList");
        List<String> banList = (List<String>) ChatTools.CONFIG.get("notifier.BanList");
        String washedMessage = TextUtils.wash(text.getString());
        for (String allowPattern : allowList) {
            if (Pattern.compile(allowPattern, Pattern.MULTILINE).matcher(washedMessage).find()) {
                shouldMatch = true;
                break;
            }
        }
        // if MatchMyNameEnabled and it does have my name
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (((boolean) ChatTools.CONFIG.get("notifier.MatchMyNameEnabled")) && player != null && Pattern
                .compile(player.getName().getString(), Pattern.MULTILINE).matcher(washedMessage).find()) {
            shouldMatch = true;
        }
        // if any of the ban pattern is matched, we should NOT match it
        for (String banPattern : banList) {
            if (Pattern.compile(banPattern, Pattern.MULTILINE).matcher(washedMessage).find()) {
                shouldMatch = false;
                break;
            }
        }
        return shouldMatch;
    }

    public static Text work(Text text) {
        if ((boolean) ChatTools.CONFIG.get("notifier.IgnoreMyMessageEnabled") && MessageUtils.hadJustSentMessage()) {
            // my message SHOULD BE and ALREADY BEEN ignored
            MessageUtils.setJustSentMessage(false);
            return text;
        }

        LoggerUtils.info("[ChatTools] Found the latest chat message matches customized RegEx");
        MessageUtils.setJustSentMessage(false);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        // Sound
        if ((boolean) ChatTools.CONFIG.get("notifier.Sound.Enabled") && player != null) {
            String identifier = (String) ChatTools.CONFIG.get("notifier.Sound.Type");
            int volume = ((Number) ChatTools.CONFIG.get("notifier.Sound.Volume")).intValue();
            int pitch = ((Number) ChatTools.CONFIG.get("notifier.Sound.Pitch")).intValue();
            player.playSound(
                    //#if MC>=11900
                    SoundEvent.of(new Identifier(identifier))
                    //#else
                    //$$ new SoundEvent(new Identifier(identifier))
                    //#endif
                    , SoundCategory.PLAYERS, volume * 0.01F, pitch * 0.1F);
        }

        // Actionbar notifications
        if ((boolean) ChatTools.CONFIG.get("notifier.Actionbar.Enabled")) {
            MessageUtils.sendToActionbar(TextUtils.trans("texts.actionbar.title"));
        }

        // Highlight
        if ((boolean) ChatTools.CONFIG.get("notifier.Highlight.Enabled")) {
            String prefix = TextUtils.escapeColorCodes((String) ChatTools.CONFIG.get("notifier.Highlight.Prefix"));
            if ((boolean) ChatTools.CONFIG.get("notifier.Highlight.OverwriteEnabled")) {
                return TextUtils.of(prefix + text.getString());
            } else {
                return ((MutableText) TextUtils.of(prefix)).append(text);
            }
        }
        return text;
    }
}
