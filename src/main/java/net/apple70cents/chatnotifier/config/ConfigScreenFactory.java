package net.apple70cents.chatnotifier.config;

import net.apple70cents.chatnotifier.ChatNotifier;
import net.kyrptonaught.kyrptconfig.config.screen.ConfigScreen;
import net.kyrptonaught.kyrptconfig.config.screen.ConfigSection;
import net.kyrptonaught.kyrptconfig.config.screen.items.*;
import net.kyrptonaught.kyrptconfig.config.screen.items.number.FloatItem;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigScreenFactory {
    public static Text parseName(String key) {
        String prefix = "key.chatnotifier.settings.%s";
        Text text = new TranslatableText(String.format(prefix, key));
        return text;
    }

//    public static Text parseTooltip(String key) {
//        String prefix = "key.chatnotifier.settings.%s";
//        Text text = new TranslatableText(String.format(prefix, key));
//        return text;
//    }

    public static void setTooltipArrayed(ConfigItem<?> item, List<Text> arr) {
        try {
            Field field = item.getClass().getSuperclass().getDeclaredField("toolTipText");
            field.setAccessible(true);
            field.set(item, arr);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static List<Text> parseTooltip(String key) {
        String prefix = "key.chatnotifier.settings.%s";
        String text = new TranslatableText(String.format(prefix, key)).getString();
        String[] arr = text.split("<br>");
        List<Text> arr2 = new ArrayList<Text>();
        for (String str : arr) {
            arr2.add(Text.of(str));
        }
        return arr2;
    }

    public static Screen buildScreen(Screen screen) {
        ConfigOptions options = ChatNotifier.getConfig();

        ConfigScreen configScreen = new ConfigScreen(screen, new TranslatableText("key.chatnotifier.modname"));
        configScreen.setSavingEvent(() -> {
            ChatNotifier.config.save();
        });
//        setTooltipArrayed(mainSection.addConfigItem(new BooleanItem(parseName("REPLACEME"), options.REPLACEME, true)
//                        .setSaveConsumer(b -> options.REPLACEME = b))
//                ,parseKey("REPLACEMEDescription"));

        ConfigSection mainSection = new ConfigSection(configScreen, new TranslatableText("key.chatnotifier.category"));
        setTooltipArrayed(mainSection.addConfigItem(new BooleanItem(parseName("enableMod"), options.modEnabled, true)
                        .setSaveConsumer(b -> options.modEnabled = b))
                , parseTooltip("enableModDescription"));

        setTooltipArrayed(mainSection.addConfigItem(new TextItem(parseName("regex"), options.chatNotifyRegEx, "[Cc]hat[Nn]otifier")
                        .setSaveConsumer(t -> options.chatNotifyRegEx = t))
                , parseTooltip("regexDescription"));

        setTooltipArrayed(mainSection.addConfigItem(new BooleanItem(parseName("ignoreSelf"), options.ignoreSelf, true)
                        .setSaveConsumer(b -> options.ignoreSelf = b))
                ,parseTooltip("ignoreSelfDescription"));

        setTooltipArrayed(mainSection.addConfigItem(new BooleanItem(parseName("matchSelfName"), options.matchSelfName, true)
                        .setSaveConsumer(b -> options.matchSelfName = b))
                ,parseTooltip("matchSelfNameDescription"));

        setTooltipArrayed(mainSection.addConfigItem(new BooleanItem(parseName("ignoreSystemMessage"), options.ignoreSystemMessage, true)
                        .setSaveConsumer(b -> options.ignoreSystemMessage = b))
                ,parseTooltip("ignoreSystemMessageDescription"));

        setTooltipArrayed(mainSection.addConfigItem(new BooleanItem(parseName("toastNotify"), options.toastNotify, false)
                        .setSaveConsumer(b -> options.toastNotify = b))
                ,parseTooltip("toastNotifyDescription"));

        setTooltipArrayed(mainSection.addConfigItem(unfoldHighlightAdvance()), parseTooltip("highlightDescription"));
        mainSection.addConfigItem(unfoldSoundAdvance());
        mainSection.addConfigItem(unfoldActionBarAdvance());
        return configScreen;
    }

    private static SubItem<?> unfoldHighlightAdvance() {
        ConfigOptions options = ChatNotifier.getConfig();
        SubItem<?> sub = (SubItem<?>) new SubItem<>(new TranslatableText("key.chatnotifier.settings.highlightSettings"));
        sub.addConfigItem(new BooleanItem(parseName("enableHighlight"), options.highlightEnabled, true).setSaveConsumer(b -> options.highlightEnabled = b));
        sub.addConfigItem(new TextItem(parseName("highlightPrefix"), options.highlightPrefix, "&a→ &6").setSaveConsumer(t -> options.highlightPrefix = t)).setToolTip(new TranslatableText("key.chatnotifier.settings.highlightPrefixDescription"));
        sub.addConfigItem(new BooleanItem(parseName("enforceOverwriting"), options.enforceOverwriting, false).setSaveConsumer(b -> options.enforceOverwriting = b));
        return sub;
    }

    private static SubItem<?> unfoldSoundAdvance() {
        ConfigOptions options = ChatNotifier.getConfig();
        SubItem<?> sub = (SubItem<?>) new SubItem<>(new TranslatableText("key.chatnotifier.settings.soundSettings"));
        sub.addConfigItem(new BooleanItem(parseName("enableSound"), options.soundNotifyEnabled, true).setSaveConsumer(b -> options.soundNotifyEnabled = b));
        sub.addConfigItem(new TextItem(parseName("soundID"), options.chatNotifySound, "block.note_block.bit").setSaveConsumer(t -> options.chatNotifySound = t));
        sub.addConfigItem(new FloatItem(parseName("volume"), options.chatNotifyVolume, 1.0F).setSaveConsumer(f -> options.chatNotifyVolume = f));
        sub.addConfigItem(new FloatItem(parseName("pitch"), options.chatNotifyPitch, 1.0F).setSaveConsumer(f -> options.chatNotifyPitch = f));
        return sub;
    }

    private static SubItem<?> unfoldActionBarAdvance() {
        ConfigOptions options = ChatNotifier.getConfig();
        SubItem<?> sub = (SubItem<?>) new SubItem<>(new TranslatableText("key.chatnotifier.settings.actionbarSettings"));
        sub.addConfigItem(new BooleanItem(parseName("enableActionbar"), options.actionbarNotifyEnabled, true).setSaveConsumer(b -> options.actionbarNotifyEnabled = b));
        return sub;
    }
}
