package net.apple70cents.chattools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.quickchat.MacroChat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ModClothConfig {
    private static final File file = new File(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().toFile(), "chat_tools.json");
    private static ModClothConfig INSTANCE = new ModClothConfig();


    public boolean modEnabled = true;
    public boolean displayChatTimeEnabled = true;
    public boolean restoreMessagesEnabled = true;

    public static class NickHiderSettings {
        public boolean nickHiderEnabled = false;
        public String nickHiderText = "&6You&r";
    }

    public NickHiderSettings nickHiderSettings = new NickHiderSettings();
    public int maxHistorySize = 500;
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
        public String highlightPrefix = "&a→ &r";
        public boolean enforceOverwriting = false;
    }

    public HighlightSettings highlightSettings = new HighlightSettings();
    public boolean ignoreSelf = true;
    public boolean matchSelfName = true;
    public boolean ignoreSystemMessage = true;

    public enum ToastMode {
        POWERSHELL, AWT
    }

    public static class ToastNotifySettings {
        public boolean toastNotifyEnabled = false;
        public ToastMode toastNotifyMode = ToastMode.POWERSHELL;
    }

    public ToastNotifySettings toastNotifySettings = new ToastNotifySettings();

    public List<String> allowList = new ArrayList<>(); // 十分肤色正确的变量名
    public List<String> banList = new ArrayList<>(); // 十分肤色正确的变量名

    public boolean injectorEnabled = false;
    public String injectorString = "I said {text} ~";
    public List<String> injectorBanList = new ArrayList<>() {{
        add("^\\d+$|^[.#%$/].*|\\ball\\b");
    }};

    public String quickRepeatKey = InputUtil.UNKNOWN_KEY.getTranslationKey();

    public enum CustomModifier {
        SHIFT, ALT, CTRL, NONE
    }

    public CustomModifier quickRepeatKeyModifier = CustomModifier.NONE;
    public boolean macroChatEnabled = true;
    public List<MacroChat.MacroUnit> macroChatList = new ArrayList<>();

    public boolean chatBubblesEnabled = true;
    public long chatBubblesLifetime = 8;
    public int chatBubblesYOffset = 3;

    /**
     * 保存配置
     */

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

    /**
     * 尝试读取配置
     */
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
                ChatTools.LOGGER.warn("[ChatTools] Couldn't understand the config.");
                e.printStackTrace();
                // file.delete();
            } else {
                ChatTools.LOGGER.warn("[ChatTools] Couldn't find the config.");
            }
        }
    }

    /**
     * 获取配置实例
     *
     * @return 配置实例
     */
    public static ModClothConfig get() {
        return INSTANCE;
    }

    /**
     * 获取配置界面构造器
     *
     * @return 配置界面构造器
     */
    public static ConfigBuilder getConfigBuilder() {
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.translatable("text.config.chattools.title"));
        builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/oak_planks.png")).setTransparentBackground(true);
        builder.setSavingRunnable(ModClothConfig::save);
        ConfigEntryBuilder eb = builder.entryBuilder();
        final ModClothConfig config = ModClothConfig.get();

        // ========== Main Category ==========
        ConfigCategory mainCategory = builder.getOrCreateCategory(Text.translatable("key.chattools.category.main"));
        // 启用ChatTools（总开关）
        mainCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.modEnabled"), config.modEnabled).setDefaultValue(new ModConfigFallback().modEnabled).setTooltip(Text.translatable("text.config.chattools.option.modEnabled.@Tooltip")).setSaveConsumer(v -> config.modEnabled = v).build());
        // 启用显示聊天时间
        mainCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.displayChatTimeEnabled"), config.displayChatTimeEnabled).setDefaultValue(new ModConfigFallback().displayChatTimeEnabled).setSaveConsumer(v -> config.displayChatTimeEnabled = v).build());
        // 启用保留聊天记录
        mainCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.restoreMessagesEnabled"), config.restoreMessagesEnabled).setDefaultValue(new ModConfigFallback().restoreMessagesEnabled).setTooltip(Text.translatable("text.config.chattools.option.restoreMessagesEnabled.@Tooltip")).setSaveConsumer(v -> config.restoreMessagesEnabled = v).build());
        // 隐藏昵称选项
        SubCategoryBuilder nickHiderSettings = eb.startSubCategory(Text.translatable("text.config.chattools.option.nickHiderSettings"));
        // - 启用隐藏昵称
        nickHiderSettings.add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.nickHiderSettings.nickHiderEnabled"), config.nickHiderSettings.nickHiderEnabled).setDefaultValue(new ModConfigFallback().nickHiderSettings.nickHiderEnabled).setSaveConsumer(v -> config.nickHiderSettings.nickHiderEnabled = v).build());
        // - 自定义昵称
        nickHiderSettings.add(eb.startStrField(Text.translatable("text.config.chattools.option.nickHiderSettings.nickHiderText"), config.nickHiderSettings.nickHiderText).setDefaultValue(new ModConfigFallback().nickHiderSettings.nickHiderText).setTooltip(Text.translatable("text.config.chattools.option.nickHiderSettings.nickHiderText.@Tooltip")).setSaveConsumer(v -> config.nickHiderSettings.nickHiderText = v).build());
        mainCategory.addEntry(nickHiderSettings.build());
        // 最大聊天记录数量
        Text label = Text.translatable("text.config.chattools.option.maxHistorySize");
        Text tooltip = Text.translatable("text.config.chattools.option.maxHistorySize.@Tooltip");
        if (FabricLoader.getInstance().isModLoaded("tweakermore")) {
            label = Text.translatable("key.chattools.conflict").append(label);
            tooltip = Text.translatable("key.chattools.conflictTooltip", "TweakerMore").append(tooltip);
        }
        mainCategory.addEntry(eb.startIntSlider(label, config.maxHistorySize, 10, 10000).setDefaultValue(new ModConfigFallback().maxHistorySize).setTooltip(tooltip).setSaveConsumer(v -> config.maxHistorySize = v).build());

        // ========== Notifier Category ==========
        ConfigCategory notifierCategory = builder.getOrCreateCategory(Text.translatable("key.chattools.category.notifier"));
        // 声音选项
        SubCategoryBuilder soundSettings = eb.startSubCategory(Text.translatable("text.config.chattools.option.soundSettings"));
        // - 启用声音提示
        soundSettings.add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.soundSettings.soundNotifyEnabled"), config.soundSettings.soundNotifyEnabled).setDefaultValue(new ModConfigFallback().soundSettings.soundNotifyEnabled).setSaveConsumer(v -> config.soundSettings.soundNotifyEnabled = v).build());
        // - 音效
        soundSettings.add(eb.startStrField(Text.translatable("text.config.chattools.option.soundSettings.chatNotifySound"), config.soundSettings.chatNotifySound).setDefaultValue(new ModConfigFallback().soundSettings.chatNotifySound).setSaveConsumer(v -> config.soundSettings.chatNotifySound = v).build());
        // - 音量
        soundSettings.add(eb.startIntSlider(Text.translatable("text.config.chattools.option.soundSettings.chatNotifyVolume"), config.soundSettings.chatNotifyVolume, 0, 100).setDefaultValue(new ModConfigFallback().soundSettings.chatNotifyVolume).setSaveConsumer(v -> config.soundSettings.chatNotifyVolume = v).build());
        // - 音高
        soundSettings.add(eb.startIntSlider(Text.translatable("text.config.chattools.option.soundSettings.chatNotifyPitch"), config.soundSettings.chatNotifyPitch, 0, 20).setDefaultValue(new ModConfigFallback().soundSettings.chatNotifyPitch).setSaveConsumer(v -> config.soundSettings.chatNotifyPitch = v).build());
        // 动作栏选项
        SubCategoryBuilder actionbarSettings = eb.startSubCategory(Text.translatable("text.config.chattools.option.actionbarSettings"));
        // - 启用动作栏提示
        actionbarSettings.add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.actionbarSettings.actionbarNotifyEnabled"), config.actionbarSettings.actionbarNotifyEnabled).setDefaultValue(new ModConfigFallback().actionbarSettings.actionbarNotifyEnabled).setSaveConsumer(v -> config.actionbarSettings.actionbarNotifyEnabled = v).build());
        // 高亮选项
        SubCategoryBuilder highlightSettings = eb.startSubCategory(Text.translatable("text.config.chattools.option.highlightSettings")).setTooltip(Text.translatable("text.config.chattools.option.highlightSettings.@Tooltip"));
        // - 启用高亮
        highlightSettings.add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.highlightSettings.highlightEnabled"), config.highlightSettings.highlightEnabled).setDefaultValue(new ModConfigFallback().highlightSettings.highlightEnabled).setSaveConsumer(v -> config.highlightSettings.highlightEnabled = v).build());
        // - 高亮前缀
        highlightSettings.add(eb.startStrField(Text.translatable("text.config.chattools.option.highlightSettings.highlightPrefix"), config.highlightSettings.highlightPrefix).setDefaultValue(new ModConfigFallback().highlightSettings.highlightPrefix).setTooltip(Text.translatable("text.config.chattools.option.highlightSettings.highlightPrefix.@Tooltip")).setSaveConsumer(v -> config.highlightSettings.highlightPrefix = v).build());
        // - 启用强制覆盖原聊天
        highlightSettings.add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.highlightSettings.enforceOverwriting"), config.highlightSettings.enforceOverwriting).setDefaultValue(new ModConfigFallback().highlightSettings.enforceOverwriting).setTooltip(Text.translatable("text.config.chattools.option.highlightSettings.enforceOverwriting.@Tooltip")).setSaveConsumer(v -> config.highlightSettings.enforceOverwriting = v).build());
        // 弹窗提示
        SubCategoryBuilder toastNotifySettings = eb.startSubCategory(Text.translatable("text.config.chattools.option.toastNotifySettings")).setTooltip(Text.translatable("text.config.chattools.option.toastNotifySettings.@Tooltip"));
        // - 启用弹窗提示
        toastNotifySettings.add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.toastNotifySettings.toastNotifyEnabled"), config.toastNotifySettings.toastNotifyEnabled).setDefaultValue(new ModConfigFallback().toastNotifySettings.toastNotifyEnabled).setSaveConsumer(v -> config.toastNotifySettings.toastNotifyEnabled = v).build());
        // - 弹窗提醒模式
        toastNotifySettings.add(eb.startEnumSelector(Text.translatable("text.config.chattools.option.toastNotifySettings.toastNotifyMode"), ToastMode.class, config.toastNotifySettings.toastNotifyMode).setDefaultValue(new ModConfigFallback().toastNotifySettings.toastNotifyMode).setTooltip(Text.translatable("text.config.chattools.option.toastNotifySettings.toastNotifyMode.@Tooltip")).setSaveConsumer(v -> config.toastNotifySettings.toastNotifyMode = v).build());
        notifierCategory.addEntry(toastNotifySettings.build());
        notifierCategory.addEntry(soundSettings.build());
        notifierCategory.addEntry(actionbarSettings.build());
        notifierCategory.addEntry(highlightSettings.build());
        // 启用忽略自己发出的消息
        notifierCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.ignoreSelf"), config.ignoreSelf).setDefaultValue(new ModConfigFallback().ignoreSelf).setTooltip(Text.translatable("text.config.chattools.option.ignoreSelf.@Tooltip")).setSaveConsumer(v -> config.ignoreSelf = v).build());
        // 启用匹配玩家自己的昵称
        notifierCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.matchSelfName"), config.matchSelfName).setDefaultValue(new ModConfigFallback().matchSelfName).setTooltip(Text.translatable("text.config.chattools.option.matchSelfName.@Tooltip")).setSaveConsumer(v -> config.matchSelfName = v).build());
        // 启用忽略系统消息
        notifierCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.ignoreSystemMessage"), config.ignoreSystemMessage).setDefaultValue(new ModConfigFallback().ignoreSystemMessage).setTooltip(Text.translatable("text.config.chattools.option.ignoreSystemMessage.@Tooltip")).setSaveConsumer(v -> config.ignoreSystemMessage = v).build());

        // 匹配白名单
        notifierCategory.addEntry(eb.startStrList(Text.translatable("text.config.chattools.option.allowList"), config.allowList).setDefaultValue(new ModConfigFallback().allowList).setTooltip(Text.translatable("text.config.chattools.option.allowList.@Tooltip")).setSaveConsumer(v -> config.allowList = v).build());
        // 匹配黑名单
        notifierCategory.addEntry(eb.startStrList(Text.translatable("text.config.chattools.option.banList"), config.banList).setDefaultValue(new ModConfigFallback().banList).setTooltip(Text.translatable("text.config.chattools.option.banList.@Tooltip")).setSaveConsumer(v -> config.banList = v).build());

        // ========== Injector Category ==========
        ConfigCategory injectorCategory = builder.getOrCreateCategory(Text.translatable("key.chattools.category.injector"));
        // 启用聊天注入
        injectorCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.injectorEnabled"), config.injectorEnabled).setDefaultValue(new ModConfigFallback().injectorEnabled).setTooltip(Text.translatable("text.config.chattools.option.injectorEnabled.@Tooltip")).setSaveConsumer(v -> config.injectorEnabled = v).build());
        // 聊天注入文本
        injectorCategory.addEntry(eb.startStrField(Text.translatable("text.config.chattools.option.injectorString"), config.injectorString).setDefaultValue(new ModConfigFallback().injectorString).setTooltip(Text.translatable("text.config.chattools.option.injectorString.@Tooltip")).setSaveConsumer(v -> config.injectorString = v).build());
        // 注入器匹配黑名单（自动禁用名单）
        injectorCategory.addEntry(eb.startStrList(Text.translatable("text.config.chattools.option.injectorBanList"), config.injectorBanList).setDefaultValue(new ModConfigFallback().injectorBanList).setSaveConsumer(v -> config.injectorBanList = v).build());

        // ========== Quick Chat Category ==========
        ConfigCategory quickChatCategory = builder.getOrCreateCategory(Text.translatable("key.chattools.category.quickchat"));
        // 一键复读（自己的话）按键
        quickChatCategory.addEntry(eb.startKeyCodeField(Text.translatable("text.config.chattools.option.quickRepeat"), InputUtil.fromTranslationKey(config.quickRepeatKey)).setDefaultValue(InputUtil.fromTranslationKey(new ModConfigFallback().quickRepeatKey)).setKeySaveConsumer(key -> config.quickRepeatKey = key.getTranslationKey()).build());
        // 复读修饰符
        quickChatCategory.addEntry(eb.startEnumSelector(Text.translatable("text.config.chattools.option.quickRepeatModifier"), CustomModifier.class, config.quickRepeatKeyModifier).setDefaultValue(new ModConfigFallback().quickRepeatKeyModifier).setSaveConsumer(v -> config.quickRepeatKeyModifier = v).build());
        // 启用宏
        quickChatCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.macroChatEnabled"), config.macroChatEnabled).setDefaultValue(new ModConfigFallback().macroChatEnabled).setSaveConsumer(v -> config.macroChatEnabled = v).build());
        // 宏列表
        quickChatCategory.addEntry(new NestedListListEntry<MacroChat.MacroUnit, MultiElementListEntry<MacroChat.MacroUnit>>(Text.translatable("text.config.chattools.option.macroChatList"), config.macroChatList, true, // 启用默认展开
                Optional::empty, // Tooltip
                v -> config.macroChatList = v, // Save Consumer
                () -> new ModConfigFallback().macroChatList, // 默认值
                eb.getResetButtonKey(), // 重置按钮键值
                true, // 启用删除键
                true, // 在列表前面插入新内容
                (macroUnit, macroUnitMultiElementListEntryNestedListListEntry) -> {
                    // 解决了(macroUnit == null)（或新建宏）宏只能保存为默认宏的问题
                    AtomicReference<MacroChat.MacroUnit> macroUnitRef = new AtomicReference<>(macroUnit);
                    if (macroUnit == null) { // 新建宏
                        Text displayText = Text.translatable("text.config.chattools.option.macroChatNew");
                        MacroChat.MacroUnit defaultMacro = new MacroChat.MacroUnit();
                        macroUnitRef.set(defaultMacro); // 设置初始值
                        return new MultiElementListEntry<>(displayText, defaultMacro, new ArrayList<>() {{
                            add(eb.startKeyCodeField(Text.translatable("text.config.chattools.option.macroChatKey"), InputUtil.fromTranslationKey(defaultMacro.getKey())).setDefaultValue(InputUtil.fromTranslationKey(defaultMacro.getKey())).setKeySaveConsumer(key -> macroUnitRef.get().setKey(key.getTranslationKey())).build());
                            add(eb.startEnumSelector(Text.translatable("text.config.chattools.option.macroChatModifier"), CustomModifier.class, defaultMacro.getModifier()).setDefaultValue(defaultMacro.getModifier()).setSaveConsumer(v -> macroUnitRef.get().setModifier(v)).build());
                            add(eb.startEnumSelector(Text.translatable("text.config.chattools.option.macroChatMode"), MacroChat.MacroMode.class, defaultMacro.getMode()).setDefaultValue(defaultMacro.getMode()).setSaveConsumer(v -> macroUnitRef.get().setMode(v)).setTooltip(Text.translatable("text.config.chattools.option.macroChatMode.@Tooltip")).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.macroChatCommand"), defaultMacro.getCommand()).setDefaultValue(defaultMacro.getCommand()).setSaveConsumer(v -> macroUnitRef.get().setCommand(v)).build());
                        }}, false);
                    } else { // 现有宏
                        Text displayText;
                        if (macroUnit.getKey().equals(InputUtil.UNKNOWN_KEY.getTranslationKey())) {
                            // 显示文字：新建宏
                            displayText = Text.translatable("text.config.chattools.option.macroChatNew");
                        } else if (macroUnit.getModifier() == CustomModifier.NONE) {
                            displayText = Text.translatable("text.config.chattools.option.macroChatDisplay",
                                    // 形如 "[ H ] /home"
                                    "§6" + InputUtil.fromTranslationKey(macroUnit.getKey()).getLocalizedText().getString(), macroUnit.getCommand());
                        } else {
                            displayText = Text.translatable("text.config.chattools.option.macroChatDisplay",
                                    // 形如 "[ Shift + B ] /back"
                                    "§6" + macroUnit.getModifier() + " + " + InputUtil.fromTranslationKey(macroUnit.getKey()).getLocalizedText().getString(), macroUnit.getCommand());
                        }
                        return new MultiElementListEntry<>(displayText, macroUnit, new ArrayList<>() {{
                            add(eb.startKeyCodeField(Text.translatable("text.config.chattools.option.macroChatKey"), InputUtil.fromTranslationKey(macroUnit.getKey())).setDefaultValue(InputUtil.fromTranslationKey(macroUnit.getKey())).setKeySaveConsumer(key -> macroUnit.setKey(key.getTranslationKey())).build());
                            add(eb.startEnumSelector(Text.translatable("text.config.chattools.option.macroChatModifier"), CustomModifier.class, macroUnit.getModifier()).setDefaultValue(macroUnit.getModifier()).setSaveConsumer(macroUnit::setModifier).build());
                            add(eb.startEnumSelector(Text.translatable("text.config.chattools.option.macroChatMode"), MacroChat.MacroMode.class, macroUnit.getMode()).setDefaultValue(macroUnit.getMode()).setSaveConsumer(macroUnit::setMode).setTooltip(Text.translatable("text.config.chattools.option.macroChatMode.@Tooltip")).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.macroChatCommand"), macroUnit.getCommand()).setDefaultValue(macroUnit.getCommand()).setSaveConsumer(macroUnit::setCommand).build());
                        }}, false);
                    }
                }));

        // ========== Chat Bubbles Category ==========
        ConfigCategory chatBubblesCategory = builder.getOrCreateCategory(Text.translatable("key.chattools.category.bubble"));
        // 启用聊天气泡
        chatBubblesCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.chatBubblesEnabled"), config.chatBubblesEnabled).setDefaultValue(new ModConfigFallback().chatBubblesEnabled).setSaveConsumer(v -> config.chatBubblesEnabled = v).build());
        // 气泡持续时间（秒）
        chatBubblesCategory.addEntry(eb.startLongSlider(Text.translatable("text.config.chattools.option.chatBubblesLifetime"), config.chatBubblesLifetime, 1, 60).setDefaultValue(new ModConfigFallback().chatBubblesLifetime).setMin(1L).setMax(60L).setSaveConsumer(v -> config.chatBubblesLifetime = v).build());
        // 气泡Y轴偏移量
        chatBubblesCategory.addEntry(eb.startIntSlider(Text.translatable("text.config.chattools.option.chatBubblesYOffset"), config.chatBubblesYOffset, -20, 20).setDefaultValue(new ModConfigFallback().chatBubblesYOffset).setMin(-20).setMax(20).setSaveConsumer(v -> config.chatBubblesYOffset = v).build());

        return builder;
    }
}
