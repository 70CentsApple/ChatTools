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
import net.apple70cents.chattools.features.chatbubbles.BubbleRenderer;
import net.apple70cents.chattools.features.chatresponser.ChatResponser;
import net.apple70cents.chattools.features.quickchat.MacroChat;
import net.minecraft.client.MinecraftClient;
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
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ModClothConfig {
    private static final File file = new File(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().toFile(), "chat_tools.json");

    public static File getFile() {
        return file;
    }

    private static ModClothConfig INSTANCE = new ModClothConfig();
    public boolean modEnabled = true;
    public boolean displayChatTimeEnabled = true;
    public String displayChatTimeFormatter = "&e[{hour}:{minute}:{second}] &r";
    public boolean restoreMessagesEnabled = true;
    public boolean shouldShowRestoreMessagesText = true;

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

    public enum ToastMode {
        ADDON, POWERSHELL, AWT
    }

    public static class ToastNotifySettings {
        public boolean toastNotifyEnabled = false;
        public ToastMode toastNotifyMode = ToastMode.ADDON;
    }

    public ToastNotifySettings toastNotifySettings = new ToastNotifySettings();

    public List<String> allowList = new ArrayList<>(); // 十分肤色正确的变量名
    public List<String> banList = new ArrayList<>(); // 十分肤色正确的变量名

    public boolean injectorEnabled = false;

    public static class InjectorUnit {
        private String address;
        private String formatter;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getFormatter() {
            return formatter;
        }

        public void setFormatter(String formatter) {
            this.formatter = formatter;
        }


        public InjectorUnit() {
            this.address = "*";
            this.formatter = "{text}";
        }
    }

    public List<InjectorUnit> injectorList = new ArrayList<>();
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
    public List<BubbleRenderer.BubbleRuleUnit> bubbleRuleList = new ArrayList<>() {{
        add(new BubbleRenderer.BubbleRuleUnit(".*\\.hypixel\\.net", "(?<name>\\S+): (?<message>.*)", false));
        add(new BubbleRenderer.BubbleRuleUnit());
    }};
    public boolean chatResponserEnabled = false;
    public List<ChatResponser.ResponserRuleUnit> responserRuleList = new ArrayList<>() {{
        add(new ChatResponser.ResponserRuleUnit());
    }};

    /**
     * 保存配置
     */

    public static void save() {
        INSTANCE.allowList.removeIf(String::isBlank);
        INSTANCE.banList.removeIf(String::isBlank);
        INSTANCE.injectorBanList.removeIf(String::isBlank);
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
            if (INSTANCE.toastNotifySettings.toastNotifyMode == null) {
                INSTANCE.toastNotifySettings.toastNotifyMode = ToastMode.ADDON;
            }
            if (INSTANCE == null) {
                ChatTools.LOGGER.warn("[ChatTools] Using default configs.");
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
        final Function<String, Optional<Text>> REGEX_COMPILE_ERROR_SUPPLIER = (v) -> {
            try {
                Pattern.compile(v);
                return Optional.empty();
            } catch (PatternSyntaxException e) {
                return Optional.of(Text.of(e.getDescription()));
            }
        };
        final Function<String, Optional<Text>> REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR = (v) -> {
            if ("*".equals(v)) return Optional.empty();
            try {
                Pattern.compile(v);
                return Optional.empty();
            } catch (PatternSyntaxException e) {
                return Optional.of(Text.of(e.getDescription()));
            }
        };
        final Function<String, Optional<Text>> REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS = (v) -> {
            try {
                Pattern.compile(v);
                if (v.contains("<name>") && v.contains("<message>")) {
                    return Optional.empty();
                } else {
                    return Optional.of(Text.literal("Should include both <name> and <message> groups."));
                }
            } catch (PatternSyntaxException e) {
                return Optional.of(Text.of(e.getDescription()));
            }
        };

        // ========== Main Category ==========
        ConfigCategory mainCategory = builder.getOrCreateCategory(Text.translatable("key.chattools.category.main"));
        // 启用ChatTools（总开关）
        mainCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.modEnabled"), config.modEnabled).setDefaultValue(new ModConfigFallback().modEnabled).setTooltip(Text.translatable("text.config.chattools.option.modEnabled.@Tooltip")).setSaveConsumer(v -> config.modEnabled = v).build());
        // 启用显示聊天时间
        mainCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.displayChatTimeEnabled"), config.displayChatTimeEnabled).setDefaultValue(new ModConfigFallback().displayChatTimeEnabled).setSaveConsumer(v -> config.displayChatTimeEnabled = v).build());
        // 聊天时间格式
        mainCategory.addEntry(eb.startStrField(Text.translatable("text.config.chattools.option.displayChatTimeFormatter"), config.displayChatTimeFormatter).setDefaultValue(new ModConfigFallback().displayChatTimeFormatter).setTooltip(Text.translatable("text.config.chattools.option.displayChatTimeFormatter.@Tooltip")).setSaveConsumer(v -> config.displayChatTimeFormatter = v).build());
        // 保留聊天记录选项
        SubCategoryBuilder restoreMessageSettings = eb.startSubCategory(Text.translatable("text.config.chattools.option.restoreMessagesSettings")).setTooltip(Text.translatable("text.config.chattools.option.restoreMessagesSettings.@Tooltip"));
        // - 启用保留聊天记录
        restoreMessageSettings.add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.restoreMessagesEnabled"), config.restoreMessagesEnabled).setDefaultValue(new ModConfigFallback().restoreMessagesEnabled).setSaveConsumer(v -> config.restoreMessagesEnabled = v).build());
        // - 保留聊天记录文字显示
        restoreMessageSettings.add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.showRestoreMessagesTextEnabled"), config.shouldShowRestoreMessagesText).setDefaultValue(new ModConfigFallback().shouldShowRestoreMessagesText).setTooltip(Text.translatable("text.config.chattools.option.showRestoreMessagesTextEnabled.@Tooltip")).setSaveConsumer(v -> config.shouldShowRestoreMessagesText = v).build());
        mainCategory.addEntry(restoreMessageSettings.build());
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
//        if (FabricLoader.getInstance().isModLoaded("tweakermore")) {
//            label = Text.translatable("key.chattools.conflict").append(label);
//            tooltip = Text.translatable("key.chattools.conflictTooltip", "TweakerMore").append(tooltip);
//        }
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
        // notifierCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.ignoreSystemMessage"), config.ignoreSystemMessage).setDefaultValue(new ModConfigFallback().ignoreSystemMessage).setTooltip(Text.translatable("text.config.chattools.option.ignoreSystemMessage.@Tooltip")).setSaveConsumer(v -> config.ignoreSystemMessage = v).build());

        // 匹配白名单
        notifierCategory.addEntry(eb.startStrList(Text.translatable("text.config.chattools.option.allowList"), config.allowList).setDefaultValue(new ModConfigFallback().allowList).setTooltip(Text.translatable("text.config.chattools.option.allowList.@Tooltip")).setSaveConsumer(v -> config.allowList = v).build());
        // 匹配黑名单
        notifierCategory.addEntry(eb.startStrList(Text.translatable("text.config.chattools.option.banList"), config.banList).setDefaultValue(new ModConfigFallback().banList).setTooltip(Text.translatable("text.config.chattools.option.banList.@Tooltip")).setSaveConsumer(v -> config.banList = v).build());

        // ========== Injector Category ==========
        ConfigCategory injectorCategory = builder.getOrCreateCategory(Text.translatable("key.chattools.category.injector"));
        // 启用聊天注入
        injectorCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.injectorEnabled"), config.injectorEnabled).setDefaultValue(new ModConfigFallback().injectorEnabled).setTooltip(Text.translatable("text.config.chattools.option.injectorEnabled.@Tooltip")).setSaveConsumer(v -> config.injectorEnabled = v).build());
        // 聊天注入规则列表
        if (MinecraftClient.getInstance().getCurrentServerEntry() == null) {
            label = Text.translatable("text.config.chattools.option.injectorList", "§f-");
        } else {
            label = Text.translatable("text.config.chattools.option.injectorList", "§f" + MinecraftClient.getInstance().getCurrentServerEntry().address);
        }
        injectorCategory.addEntry(new NestedListListEntry<InjectorUnit, MultiElementListEntry<InjectorUnit>>(label, config.injectorList, true, // 启用默认展开
                () -> {
                    return Optional.of(new net.minecraft.text.MutableText[]{Text.translatable("text.config.chattools.option.injectorList.@Tooltip")});
                }, // Tooltip
                v -> config.injectorList = v, // Save Consumer
                () -> new ModConfigFallback().injectorList, // 默认值
                eb.getResetButtonKey(), // 重置按钮键值
                true, // 启用删除键
                true, // 在列表前面插入新内容
                (injectorUnit, __) -> {
                    AtomicReference<InjectorUnit> injectorUnitRef = new AtomicReference<>(injectorUnit);
                    if (injectorUnit == null) { // 新建
                        Text displayText = Text.translatable("text.config.chattools.option.injectorNew");
                        InjectorUnit defaultInjectorRule = new InjectorUnit();
                        injectorUnitRef.set(defaultInjectorRule); // 设置初始值
                        return new MultiElementListEntry<>(displayText, defaultInjectorRule, new ArrayList<>() {{
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.injectorAddress"), defaultInjectorRule.address).setTooltip(Text.translatable("text.config.chattools.option.injectorAddress.@Tooltip")).setDefaultValue(defaultInjectorRule.address).setSaveConsumer(v -> injectorUnitRef.get().address = v).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.injectorString"), defaultInjectorRule.formatter).setTooltip(Text.translatable("text.config.chattools.option.injectorString.@Tooltip")).setDefaultValue(defaultInjectorRule.formatter).setSaveConsumer(v -> injectorUnitRef.get().formatter = v).build());
                        }}, false);
                    } else { // 现有
                        String colorPrefix = ("*".equals(injectorUnit.address) || (MinecraftClient.getInstance().getCurrentServerEntry() != null && Pattern.compile(injectorUnit.getAddress()).matcher(MinecraftClient.getInstance().getCurrentServerEntry().address).matches())) ? "§a" : "§6";
                        Text displayText = Text.translatable("text.config.chattools.option.injectorDisplay", colorPrefix + injectorUnit.address, injectorUnit.formatter);
                        return new MultiElementListEntry<>(displayText, injectorUnit, new ArrayList<>() {{
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.injectorAddress"), injectorUnit.address).setTooltip(Text.translatable("text.config.chattools.option.injectorAddress.@Tooltip")).setDefaultValue(new InjectorUnit().address).setSaveConsumer(injectorUnit::setAddress).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.injectorString"), injectorUnit.formatter).setTooltip(Text.translatable("text.config.chattools.option.injectorString.@Tooltip")).setDefaultValue(new InjectorUnit().formatter).setSaveConsumer(injectorUnit::setFormatter).build());
                        }}, false);
                    }
                }));
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
        // 气泡规则
        if (MinecraftClient.getInstance().getCurrentServerEntry() == null) {
            label = Text.translatable("text.config.chattools.option.chatBubblesRulesList", "§f-");
        } else {
            label = Text.translatable("text.config.chattools.option.chatBubblesRulesList", "§f" + MinecraftClient.getInstance().getCurrentServerEntry().address);
        }
        chatBubblesCategory.addEntry(new NestedListListEntry<BubbleRenderer.BubbleRuleUnit, MultiElementListEntry<BubbleRenderer.BubbleRuleUnit>>(label, config.bubbleRuleList, true, // 启用默认展开
                () -> Optional.of(new net.minecraft.text.MutableText[]{Text.translatable("text.config.chattools.option.chatBubblesRulesList.@Tooltip")}), // Tooltip
                v -> config.bubbleRuleList = v, // Save Consumer
                () -> new ModConfigFallback().bubbleRuleList, // 默认值
                eb.getResetButtonKey(), // 重置按钮键值
                true, // 启用删除键
                true, // 在列表前面插入新内容
                (bubbleRuleUnit, __) -> {
                    AtomicReference<BubbleRenderer.BubbleRuleUnit> bubbleRuleUnitRef = new AtomicReference<>(bubbleRuleUnit);
                    if (bubbleRuleUnit == null) { // 新建
                        Text displayText = Text.translatable("text.config.chattools.option.chatBubblesNew");
                        BubbleRenderer.BubbleRuleUnit defaultRule = new BubbleRenderer.BubbleRuleUnit();
                        bubbleRuleUnitRef.set(defaultRule); // 设置初始值
                        return new MultiElementListEntry<>(displayText, defaultRule, new ArrayList<>() {{
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.chatBubblesAddress"), defaultRule.getAddress()).setTooltip(Text.translatable("text.config.chattools.option.chatBubblesAddress.@Tooltip")).setDefaultValue(defaultRule.getAddress()).setSaveConsumer(bubbleRuleUnitRef.get()::setAddress).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.chatBubblesPattern"), defaultRule.getPattern()).setTooltip(Text.translatable("text.config.chattools.option.chatBubblesPattern.@Tooltip")).setDefaultValue(defaultRule.getPattern()).setSaveConsumer(bubbleRuleUnitRef.get()::setPattern).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());
                            add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.chatBubblesFallback"), defaultRule.isFallback()).setTooltip(Text.translatable("text.config.chattools.option.chatBubblesFallback.@Tooltip")).setDefaultValue(defaultRule.isFallback()).setSaveConsumer(bubbleRuleUnitRef.get()::setFallback).build());
                        }}, false);
                    } else { // 现有
                        String colorPrefix = ("*".equals(bubbleRuleUnit.getAddress()) || (MinecraftClient.getInstance().getCurrentServerEntry() != null && Pattern.compile(bubbleRuleUnit.getAddress()).matcher(MinecraftClient.getInstance().getCurrentServerEntry().address).matches())) ? "§a" : "§6";
                        Text displayText = Text.translatable("text.config.chattools.option.chatBubblesDisplay", colorPrefix + bubbleRuleUnit.getAddress(), bubbleRuleUnit.isFallback() ? "§a✔" : "§c✘", bubbleRuleUnit.getPattern());
                        return new MultiElementListEntry<>(displayText, bubbleRuleUnit, new ArrayList<>() {{
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.chatBubblesAddress"), bubbleRuleUnit.getAddress()).setTooltip(Text.translatable("text.config.chattools.option.chatBubblesAddress.@Tooltip")).setDefaultValue(new BubbleRenderer.BubbleRuleUnit().getAddress()).setSaveConsumer(bubbleRuleUnit::setAddress).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.chatBubblesPattern"), bubbleRuleUnit.getPattern()).setTooltip(Text.translatable("text.config.chattools.option.chatBubblesPattern.@Tooltip")).setDefaultValue(new BubbleRenderer.BubbleRuleUnit().getPattern()).setSaveConsumer(bubbleRuleUnit::setPattern).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_REQUIRE_GROUPS).build());
                            add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.chatBubblesFallback"), bubbleRuleUnit.isFallback()).setTooltip(Text.translatable("text.config.chattools.option.chatBubblesFallback.@Tooltip")).setDefaultValue(new BubbleRenderer.BubbleRuleUnit().isFallback()).setSaveConsumer(bubbleRuleUnit::setFallback).build());
                        }}, false);
                    }
                }));

        // ========== Chat Responser Category ==========
        ConfigCategory chatResponserCategory = builder.getOrCreateCategory(Text.translatable("key.chattools.category.responser"));
        // 启用聊天回应
        chatResponserCategory.addEntry(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.responserEnabled"), config.chatResponserEnabled).setDefaultValue(new ModConfigFallback().chatResponserEnabled).setSaveConsumer(v -> config.chatResponserEnabled = v).build());
        // 回应规则
        if (MinecraftClient.getInstance().getCurrentServerEntry() == null) {
            label = Text.translatable("text.config.chattools.option.responserRulesList", "§f-");
        } else {
            label = Text.translatable("text.config.chattools.option.responserRulesList", "§f" + MinecraftClient.getInstance().getCurrentServerEntry().address);
        }
        chatResponserCategory.addEntry(new NestedListListEntry<ChatResponser.ResponserRuleUnit, MultiElementListEntry<ChatResponser.ResponserRuleUnit>>(label, config.responserRuleList, true, // 启用默认展开
                () -> Optional.of(new net.minecraft.text.MutableText[]{Text.translatable("text.config.chattools.option.responserRulesList.@Tooltip")}), // Tooltip
                v -> config.responserRuleList = v, // Save Consumer
                () -> new ModConfigFallback().responserRuleList, // 默认值
                eb.getResetButtonKey(), // 重置按钮键值
                true, // 启用删除键
                true, // 在列表前面插入新内容
                (responserRuleUnit, __) -> {
                    AtomicReference<ChatResponser.ResponserRuleUnit> responserUnitRef = new AtomicReference<>(new ChatResponser.ResponserRuleUnit());
                    if (responserRuleUnit == null) { // 新建
                        Text displayText = Text.translatable("text.config.chattools.option.responserNew");
                        ChatResponser.ResponserRuleUnit defaultRule = new ChatResponser.ResponserRuleUnit();
                        responserUnitRef.set(defaultRule); // 设置初始值
                        return new MultiElementListEntry<>(displayText, defaultRule, new ArrayList<>() {{
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.responserAddress"), defaultRule.getAddress()).setTooltip(Text.translatable("text.config.chattools.option.responserAddress.@Tooltip")).setDefaultValue(defaultRule.getAddress()).setSaveConsumer(responserUnitRef.get()::setAddress).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.responserMatchPattern"), defaultRule.getPattern()).setTooltip(Text.translatable("text.config.chattools.option.responserMatchPattern.@Tooltip")).setDefaultValue(defaultRule.getPattern()).setSaveConsumer(responserUnitRef.get()::setPattern).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.responserString"), defaultRule.getMessage()).setTooltip(Text.translatable("text.config.chattools.option.responserString.@Tooltip")).setDefaultValue(defaultRule.getMessage()).setSaveConsumer(responserUnitRef.get()::setMessage).build());
                            add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.responserForceDisableInjector"), defaultRule.isForceDisableInjector()).setTooltip(Text.translatable("text.config.chattools.option.responserForceDisableInjector.@Tooltip")).setDefaultValue(defaultRule.isForceDisableInjector()).setSaveConsumer(responserUnitRef.get()::setForceDisableInjector).build());
                        }}, false);
                    } else { // 现有
                        String colorPrefix = ("*".equals(responserRuleUnit.getAddress()) || (MinecraftClient.getInstance().getCurrentServerEntry() != null && Pattern.compile(responserRuleUnit.getAddress()).matcher(MinecraftClient.getInstance().getCurrentServerEntry().address).matches())) ? "§a" : "§6";
                        Text displayText = Text.translatable("text.config.chattools.option.responserDisplay", colorPrefix + responserRuleUnit.getAddress(), responserRuleUnit.isForceDisableInjector() ? "§a✔" : "§c✘", responserRuleUnit.getPattern(), responserRuleUnit.getMessage());
                        return new MultiElementListEntry<>(displayText, responserRuleUnit, new ArrayList<>() {{
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.responserAddress"), responserRuleUnit.getAddress()).setTooltip(Text.translatable("text.config.chattools.option.responserAddress.@Tooltip")).setDefaultValue(new ChatResponser.ResponserRuleUnit().getAddress()).setSaveConsumer(responserRuleUnit::setAddress).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER_ALLOW_STAR).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.responserMatchPattern"), responserRuleUnit.getPattern()).setTooltip(Text.translatable("text.config.chattools.option.responserMatchPattern.@Tooltip")).setDefaultValue(new ChatResponser.ResponserRuleUnit().getPattern()).setSaveConsumer(responserRuleUnit::setPattern).setErrorSupplier(REGEX_COMPILE_ERROR_SUPPLIER).build());
                            add(eb.startStrField(Text.translatable("text.config.chattools.option.responserString"), responserRuleUnit.getMessage()).setTooltip(Text.translatable("text.config.chattools.option.responserString.@Tooltip")).setDefaultValue(new ChatResponser.ResponserRuleUnit().getMessage()).setSaveConsumer(responserRuleUnit::setMessage).build());
                            add(eb.startBooleanToggle(Text.translatable("text.config.chattools.option.responserForceDisableInjector"), responserRuleUnit.isForceDisableInjector()).setTooltip(Text.translatable("text.config.chattools.option.responserString.@Tooltip")).setDefaultValue(new ChatResponser.ResponserRuleUnit().isForceDisableInjector()).setSaveConsumer(responserRuleUnit::setForceDisableInjector).build());
                        }}, false);
                    }
                }));
        return builder;
    }
}
