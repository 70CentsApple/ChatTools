package net.apple70cents.chattools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.util.mod.Mod;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.apple70cents.chattools.ChatTools;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ModClothConfig {
    private static final File file = new File(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().toFile(), "chat_tools.json");
    private static ModClothConfig INSTANCE = new ModClothConfig();


    public boolean modEnabled = true;
    public boolean shouldShowWelcomeMessage = true;

    public static class SoundSettings {
        public boolean soundNotifyEnabled = true;
        public String chatNotifySound = "block.note_block.bit";
        public int chatNotifyVolume = 80;
        public int chatNotifyPitch = 10;
    }

    public SoundSettings soundSettings = new SoundSettings();

    public static class ActionbarSettings {
        public boolean actionbarNotifyEnabled = true;
    }

    public ActionbarSettings actionbarSettings = new ActionbarSettings();

    public static class HighlightSettings {
        public boolean highlightEnabled = true;
        public String highlightPrefix = "&a→ &6";
        public boolean enforceOverwriting = false;
    }

    public HighlightSettings highlightSettings = new HighlightSettings();
    public boolean ignoreSelf = true;
    public boolean matchSelfName = true;
    public boolean ignoreSystemMessage = true;
    public boolean toastNotify = false;
    public List<String> allowList = new ArrayList<>(); // 十分肤色正确的变量名
    public List<String> banList = new ArrayList<>(); // 十分肤色正确的变量名

    public boolean injectorEnabled = false;
    public String injectorString = "I said {text} ~";
    public List<String> injectorBanList = new ArrayList<>() {{
        add("^\\d+$|^[.$/].*|\\ball\\b");
    }};


    public static void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ChatTools.LOGGER.info("[ChatTools] Saving configs.");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(INSTANCE, writer);
        } catch (Exception e) {
            ChatTools.LOGGER.error("[ChatTools] Couldn't save config.");
            e.printStackTrace();
        }
    }

    public static void load() {
        ChatTools.LOGGER.info("[ChatTools] Loading configs...");
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            INSTANCE = gson.fromJson(reader, ModClothConfig.class);
            if (INSTANCE == null) {
                INSTANCE = new ModClothConfig();
            }
        } catch (Exception e) {
            if (file.exists()) {
                ChatTools.LOGGER.warn("[ChatTools] Couldn't understand the config, deleting it.");
                file.delete();
            } else {
                ChatTools.LOGGER.warn("[ChatTools] Couldn't find the config.");
            }
        }
    }

    public static ModClothConfig get() {
        return INSTANCE;
    }

    public static ConfigBuilder getConfigBuilder() {
        ConfigBuilder builder = ConfigBuilder.create().setTitle(new TranslatableText("text.config.chattools.title"));
        builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/oak_planks.png"))
                .setTransparentBackground(true);
        builder.setSavingRunnable(ModClothConfig::save);
        ConfigEntryBuilder eb = builder.entryBuilder();
        final ModClothConfig config = ModClothConfig.get();

        // ========== Main Category ==========
        ConfigCategory mainCategory = builder.getOrCreateCategory(new TranslatableText("key.chattools.category.main"));
        mainCategory.addEntry(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.modEnabled"), config.modEnabled)
                .setDefaultValue(new ModConfigFallback().modEnabled)
                .setTooltip(new TranslatableText("text.config.chattools.option.modEnabled.@Tooltip"))
                .setSaveConsumer(v -> config.modEnabled = v).build());

        // ========== Notifier Category ==========
        ConfigCategory notifierCategory = builder.getOrCreateCategory(new TranslatableText("key.chattools.category.notifier"));
        SubCategoryBuilder soundSettings = eb.startSubCategory(new TranslatableText("text.config.chattools.option.soundSettings"));
        soundSettings.add(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.soundSettings.soundNotifyEnabled"), config.soundSettings.soundNotifyEnabled)
                .setDefaultValue(new ModConfigFallback().soundSettings.soundNotifyEnabled)
                .setSaveConsumer(v -> config.soundSettings.soundNotifyEnabled = v).build());
        soundSettings.add(eb.startStrField(new TranslatableText("text.config.chattools.option.soundSettings.chatNotifySound"), config.soundSettings.chatNotifySound)
                .setDefaultValue(new ModConfigFallback().soundSettings.chatNotifySound)
                .setSaveConsumer(v -> config.soundSettings.chatNotifySound = v).build());
        soundSettings.add(eb.startIntField(new TranslatableText("text.config.chattools.option.soundSettings.chatNotifyVolume"), config.soundSettings.chatNotifyVolume)
                .setDefaultValue(new ModConfigFallback().soundSettings.chatNotifyVolume)
                .setSaveConsumer(v -> config.soundSettings.chatNotifyVolume = v).build());
        soundSettings.add(eb.startIntField(new TranslatableText("text.config.chattools.option.soundSettings.chatNotifyPitch"), config.soundSettings.chatNotifyPitch)
                .setDefaultValue(new ModConfigFallback().soundSettings.chatNotifyPitch)
                .setSaveConsumer(v -> config.soundSettings.chatNotifyPitch = v).build());
        SubCategoryBuilder actionbarSettings = eb.startSubCategory(new TranslatableText("text.config.chattools.option.actionbarSettings"));
        actionbarSettings.add(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.actionbarSettings.actionbarNotifyEnabled"), config.actionbarSettings.actionbarNotifyEnabled)
                .setDefaultValue(new ModConfigFallback().actionbarSettings.actionbarNotifyEnabled)
                .setSaveConsumer(v -> config.actionbarSettings.actionbarNotifyEnabled = v).build());
        SubCategoryBuilder highlightSettings = eb.startSubCategory(new TranslatableText("text.config.chattools.option.highlightSettings"))
                .setTooltip(new TranslatableText("text.config.chattools.option.highlightSettings.@Tooltip"));
        highlightSettings.add(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.highlightSettings.highlightEnabled"), config.highlightSettings.highlightEnabled)
                .setDefaultValue(new ModConfigFallback().highlightSettings.highlightEnabled)
                .setSaveConsumer(v -> config.highlightSettings.highlightEnabled = v).build());
        highlightSettings.add(eb.startStrField(new TranslatableText("text.config.chattools.option.highlightSettings.highlightPrefix"), config.highlightSettings.highlightPrefix)
                .setDefaultValue(new ModConfigFallback().highlightSettings.highlightPrefix)
                .setTooltip(new TranslatableText("text.config.chattools.option.highlightSettings.highlightPrefix.@Tooltip"))
                .setSaveConsumer(v -> config.highlightSettings.highlightPrefix = v).build());
        highlightSettings.add(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.highlightSettings.enforceOverwriting"), config.highlightSettings.enforceOverwriting)
                .setDefaultValue(new ModConfigFallback().highlightSettings.enforceOverwriting)
                .setTooltip(new TranslatableText("text.config.chattools.option.highlightSettings.enforceOverwriting.@Tooltip"))
                .setSaveConsumer(v -> config.highlightSettings.enforceOverwriting = v).build());
        notifierCategory.addEntry(soundSettings.build());
        notifierCategory.addEntry(actionbarSettings.build());
        notifierCategory.addEntry(highlightSettings.build());
        notifierCategory.addEntry(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.ignoreSelf"), config.ignoreSelf)
                .setDefaultValue(new ModConfigFallback().ignoreSelf)
                .setTooltip(new TranslatableText("text.config.chattools.option.ignoreSelf.@Tooltip"))
                .setSaveConsumer(v -> config.ignoreSelf = v).build());
        notifierCategory.addEntry(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.matchSelfName"), config.matchSelfName)
                .setDefaultValue(new ModConfigFallback().matchSelfName)
                .setTooltip(new TranslatableText("text.config.chattools.option.matchSelfName.@Tooltip"))
                .setSaveConsumer(v -> config.matchSelfName = v).build());
        notifierCategory.addEntry(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.ignoreSystemMessage"), config.ignoreSystemMessage)
                .setDefaultValue(new ModConfigFallback().ignoreSystemMessage)
                .setTooltip(new TranslatableText("text.config.chattools.option.ignoreSystemMessage.@Tooltip"))
                .setSaveConsumer(v -> config.ignoreSystemMessage = v).build());
        /*
        notifierCategory.addEntry(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.toastNotify"), config.toastNotify)
                .setDefaultValue(new ModConfigFallback().toastNotify)
                .setTooltip(new TranslatableText("text.config.chattools.option.toastNotify.@Tooltip"))
                .setSaveConsumer(v -> config.toastNotify = v).build());
         */
        notifierCategory.addEntry(eb.startStrList(new TranslatableText("text.config.chattools.option.allowList"), config.allowList)
                .setDefaultValue(new ModConfigFallback().allowList)
                .setTooltip(new TranslatableText("text.config.chattools.option.allowList.@Tooltip"))
                .setSaveConsumer(v -> config.allowList = v).build());
        notifierCategory.addEntry(eb.startStrList(new TranslatableText("text.config.chattools.option.banList"), config.banList)
                .setDefaultValue(new ModConfigFallback().banList)
                .setTooltip(new TranslatableText("text.config.chattools.option.banList.@Tooltip"))
                .setSaveConsumer(v -> config.banList = v).build());

        // ========== Injector Category ==========
        ConfigCategory injectorCategory = builder.getOrCreateCategory(new TranslatableText("key.chattools.category.injector"));
        injectorCategory.addEntry(eb.startBooleanToggle(new TranslatableText("text.config.chattools.option.injectorEnabled"), config.injectorEnabled)
                .setDefaultValue(new ModConfigFallback().injectorEnabled)
                .setTooltip(new TranslatableText("text.config.chattools.option.injectorEnabled.@Tooltip"))
                .setSaveConsumer(v -> config.injectorEnabled = v).build());
        injectorCategory.addEntry(eb.startStrField(new TranslatableText("text.config.chattools.option.injectorString"), config.injectorString)
                .setDefaultValue(new ModConfigFallback().injectorString)
                .setTooltip(new TranslatableText("text.config.chattools.option.injectorString.@Tooltip"))
                .setSaveConsumer(v -> config.injectorString = v).build());
        injectorCategory.addEntry(eb.startStrList(new TranslatableText("text.config.chattools.option.injectorBanList"), config.injectorBanList)
                .setDefaultValue(new ModConfigFallback().injectorBanList)
                .setSaveConsumer(v -> config.injectorBanList = v).build());

        return builder;
    }
}
