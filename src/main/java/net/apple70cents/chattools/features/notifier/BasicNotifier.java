package net.apple70cents.chattools.features.notifier;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;

//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class BasicNotifier {
    /**
     * Returns the matched NotifierRuleUnit if the message should be processed, or null if not.
     */
    public static SpecialUnits.NotifierRuleUnit shouldWork(Component text) {
        SpecialUnits.NotifierRuleUnit matchedRule = null;
        List<SpecialUnits.NotifierRuleUnit> allowList = SpecialUnits.NotifierRuleUnit.fromList((List) ConfigUtils.get("notifier.AllowList"));
        List<String> denyList = (List<String>) ConfigUtils.get("notifier.DenyList");
        String washedMessage = TextUtils.wash(text.getString());
        String sessionIdentifier = ContextUtils.getSessionIdentifier();

        for (SpecialUnits.NotifierRuleUnit rule : allowList) {
            // check address
            boolean addressMatches = "*".equals(rule.address) || RegExUtils.getOrCompilePattern(rule.address).matcher(sessionIdentifier).matches();
            if (!addressMatches) {
                continue;
            }
            // check pattern
            if (!rule.pattern.isEmpty() && RegExUtils.getOrCompilePattern(rule.pattern, Pattern.MULTILINE).matcher(washedMessage).find()) {
                matchedRule = rule;
                break;
            }
        }
        // if MatchMyNameEnabled and it does have my name
        LocalPlayer player = Minecraft.getInstance().player;
        if ((ConfigUtils.getBoolean(
                "notifier.MatchMyNameEnabled")) && player != null && RegExUtils.getOrCompilePattern(
                Pattern.quote(player.getName().getString()), Pattern.MULTILINE).matcher(washedMessage).find()) {
            // use a default rule with all notifications enabled
            if (matchedRule == null) {
                matchedRule = new SpecialUnits.NotifierRuleUnit();
            }
        }
        // if any of the deny pattern is matched, we should NOT match it
        if (matchedRule != null) {
            for (String denyPattern : denyList) {
                if (RegExUtils.getOrCompilePattern(denyPattern, Pattern.MULTILINE).matcher(washedMessage).find()) {
                    matchedRule = null;
                    break;
                }
            }
        }
        return matchedRule;
    }

    public static Component work(Component text, SpecialUnits.NotifierRuleUnit rule) {
        if (ConfigUtils.getBoolean("notifier.IgnoreMyMessageEnabled") && MessageUtils.hadJustSentMessage()) {
            return text;
        }

        LoggerUtils.info("[ChatTools] Found the latest chat message matches customized RegEx");

        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel world = Minecraft.getInstance().level;

        // Toast
        if (rule.toast && ConfigUtils.getBoolean("notifier.Toast.Enabled") && !Minecraft.getInstance().isWindowActive()) {
            Toast.work(TextUtils.wash(text.getString()));
        }

        // Sound
        if (rule.sound && ConfigUtils.getBoolean("notifier.Sound.Enabled") && player != null && world != null) {
            Minecraft.getInstance().execute(() -> {
                String identifier = ConfigUtils.getString("notifier.Sound.Type");
                int volume = ConfigUtils.getInt("notifier.Sound.Volume");
                int pitch = ConfigUtils.getInt("notifier.Sound.Pitch");

                boolean sendFromCameraPos = ConfigUtils.getBoolean("notifier.Sound.PlaySoundFromCameraPositionEnabled");
//? if >=1.21.9 {
                Entity camera = Minecraft.getInstance().getCameraEntity();
//?} else {
                /*Entity camera = Minecraft.getInstance().cameraEntity;
*///?}
                double x = (sendFromCameraPos && camera != null) ? camera.position().x : player.getX();
                double y = (sendFromCameraPos && camera != null) ? camera.position().y : player.getY();
                double z = (sendFromCameraPos && camera != null) ? camera.position().z : player.getZ();
                world.playLocalSound(x, y, z,
//? if >=1.21.11 {
                        SoundEvent.createVariableRangeEvent(Identifier.parse(identifier))
//?} elif >=1.21 {
                        /*SoundEvent.createVariableRangeEvent(ResourceLocation.parse(identifier))
*///?} elif >=1.19 {
                        /*SoundEvent.createVariableRangeEvent(new ResourceLocation(identifier))
*///?} else {
                        /*new SoundEvent(new ResourceLocation(identifier))
*///?}
                        , SoundSource.PLAYERS, volume * 0.01F, pitch * 0.1F, true);
            });
        }

        // Actionbar notifications
        if (rule.actionbar && ConfigUtils.getBoolean("notifier.Actionbar.Enabled")) {
            Minecraft.getInstance().execute(() -> {
                MessageUtils.sendToActionbar(TextUtils.trans("texts.actionbar.title"));
            });
        }

        // Highlight
        if (rule.highlight && ConfigUtils.getBoolean("notifier.Highlight.Enabled")) {
            String prefix = TextUtils.encodeColorCodes(ConfigUtils.getString("notifier.Highlight.Prefix"));
            if (ConfigUtils.getBoolean("notifier.Highlight.OverwriteEnabled")) {
                return TextUtils.of(prefix + text.getString());
            } else {
                return (TextUtils.SPACER.copy().append(TextUtils.of(prefix))).append(text);
            }
        }
        return text;
    }
}
