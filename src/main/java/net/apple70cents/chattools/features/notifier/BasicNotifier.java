package net.apple70cents.chattools.features.notifier;

import net.apple70cents.chattools.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;

//#if MC>=12111
import net.minecraft.resources.Identifier;
//#else
//$$ import net.minecraft.resources.ResourceLocation;
//#endif

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class BasicNotifier {
    public static boolean shouldWork(Component text) {
        boolean shouldMatch = false;
        List<String> allowList = (List<String>) ConfigUtils.get("notifier.AllowList");
        List<String> banList = (List<String>) ConfigUtils.get("notifier.BanList");
        String washedMessage = TextUtils.wash(text.getString());
        for (String allowPattern : allowList) {
            if (RegExUtils.getOrCompilePattern(allowPattern, Pattern.MULTILINE).matcher(washedMessage).find()) {
                shouldMatch = true;
                break;
            }
        }
        // if MatchMyNameEnabled and it does have my name
        LocalPlayer player = Minecraft.getInstance().player;
        if (((boolean) ConfigUtils.get(
                "notifier.MatchMyNameEnabled")) && player != null && RegExUtils.getOrCompilePattern(
                Pattern.quote(player.getName().getString()), Pattern.MULTILINE).matcher(washedMessage).find()) {
            shouldMatch = true;
        }
        // if any of the ban pattern is matched, we should NOT match it
        for (String banPattern : banList) {
            if (RegExUtils.getOrCompilePattern(banPattern, Pattern.MULTILINE).matcher(washedMessage).find()) {
                shouldMatch = false;
                break;
            }
        }
        return shouldMatch;
    }

    public static Component work(Component text) {
        if ((boolean) ConfigUtils.get("notifier.IgnoreMyMessageEnabled") && MessageUtils.hadJustSentMessage()) {
            return text;
        }

        LoggerUtils.info("[ChatTools] Found the latest chat message matches customized RegEx");

        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel world = Minecraft.getInstance().level;

        // Toast
        if ((boolean) ConfigUtils.get("notifier.Toast.Enabled") && !Minecraft.getInstance().isWindowActive()) {
            Toast.work(TextUtils.wash(text.getString()));
        }

        // Sound
        if ((boolean) ConfigUtils.get("notifier.Sound.Enabled") && player != null && world != null) {
            Minecraft.getInstance().execute(() -> {
                String identifier = (String) ConfigUtils.get("notifier.Sound.Type");
                int volume = ((Number) ConfigUtils.get("notifier.Sound.Volume")).intValue();
                int pitch = ((Number) ConfigUtils.get("notifier.Sound.Pitch")).intValue();

                boolean sendFromCameraPos = (boolean) ConfigUtils.get("notifier.Sound.PlaySoundFromCameraPositionEnabled");
                //#if MC>=12109
                Entity camera = Minecraft.getInstance().getCameraEntity();
                //#else
                //$$ Entity camera = Minecraft.getInstance().cameraEntity;
                //#endif
                double x = (sendFromCameraPos && camera != null) ? camera.position().x : player.getX();
                double y = (sendFromCameraPos && camera != null) ? camera.position().y : player.getY();
                double z = (sendFromCameraPos && camera != null) ? camera.position().z : player.getZ();
                world.playLocalSound(x, y, z,
                        //#if MC>=12111
                        SoundEvent.createVariableRangeEvent(Identifier.parse(identifier))
                        //#elseif MC>=12100
                        //$$ SoundEvent.createVariableRangeEvent(ResourceLocation.parse(identifier))
                        //#elseif MC>=11900
                        //$$ SoundEvent.createVariableRangeEvent(new ResourceLocation(identifier))
                        //#else
                        //$$ new SoundEvent(new ResourceLocation(identifier))
                        //#endif
                        , SoundSource.PLAYERS, volume * 0.01F, pitch * 0.1F, true);
            });
        }

        // Actionbar notifications
        if ((boolean) ConfigUtils.get("notifier.Actionbar.Enabled")) {
            Minecraft.getInstance().execute(() -> {
                MessageUtils.sendToActionbar(TextUtils.trans("texts.actionbar.title"));
            });
        }

        // Highlight
        if ((boolean) ConfigUtils.get("notifier.Highlight.Enabled")) {
            String prefix = TextUtils.encodeColorCodes((String) ConfigUtils.get("notifier.Highlight.Prefix"));
            if ((boolean) ConfigUtils.get("notifier.Highlight.OverwriteEnabled")) {
                return TextUtils.of(prefix + text.getString());
            } else {
                return (TextUtils.SPACER.copy().append(TextUtils.of(prefix))).append(text);
            }
        }
        return text;
    }
}
