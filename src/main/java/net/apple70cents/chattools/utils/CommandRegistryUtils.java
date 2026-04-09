package net.apple70cents.chattools.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.apple70cents.chattools.config.common.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.util.Tuple;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
//? if >=1.21.11 {
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.Util;
*///?}
//? if >=26.1 {
//?}

//? if >=1.21.5 {
import com.mojang.serialization.JavaOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.core.HolderLookup;
//?} elif >=1.20.4 {
/*import net.minecraft.commands.ParserUtils;
import net.minecraft.core.HolderLookup;
*///?}

//? if >=1.19 {
import net.minecraft.commands.CommandBuildContext;
//?} else {
/*import java.util.UUID;
*///?}

//? if FABRIC {

/*//? if >=26.1 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
//?} elif >=1.19 {
/^import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
^///?} else {
/^// Fabric v2 begins to work since 1.19
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;
^///?}

*///?} elif NEOFORGE {
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
//?}

public class CommandRegistryUtils {

    public static void register() {
//? if FABRIC {
        /*//? if >=1.19 {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
        	dispatcher.register(CommandRegistryUtils.getBuilder(registryAccess));
        });
        //?} else {
        /^ClientCommandManager.DISPATCHER.register(CommandRegistryUtils.getBuilder());
        ^///?}
*///?} elif NEOFORGE {
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                CommandRegistryUtils.getBuilder(event.getBuildContext())
        ));
//?}


    }

//? if NEOFORGE {
    public static LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
//?}

    public static LiteralArgumentBuilder<
//? if FABRIC {
            /*FabricClientCommandSource
*///?} elif NEOFORGE {
            CommandSourceStack
//?}
            > getBuilder(
//? if >=1.19 {
            CommandBuildContext buildContext
//?}
    ) {
        // @formatter:off
        return literal("chattools")
            // chattools send_to_client
            .then(literal("send_to_client")
                // chattools send_to_client text
                .then(literal("text").then(argument("message", ClientComponentArgument.textComponent(
//? if >=1.20.6 {
                        buildContext
//?}
                    )).executes(t -> {
//? if >=26.1 {
                    Component text = ComponentUtils.resolve(ResolutionContext.builder()
                                    .withSource(new FakeCommandSource(Minecraft.getInstance().player))
                                    .withEntityOverride(Minecraft.getInstance().player).build(),
                            ClientComponentArgument.getComponent(t, "message"));
//?} else {
                    /*Component text = ComponentUtils.updateForEntity(new FakeCommandSource(Minecraft.getInstance().player), ClientComponentArgument.getComponent(t, "message"), Minecraft.getInstance().player, 0);
*///?}
                    MessageUtils.sendToNonPublicChat(text);
                    return Command.SINGLE_SUCCESS;
                })))
                // chattools send_to_client actionbar
                .then(literal("actionbar").then(argument("message", ClientComponentArgument.textComponent(
//? if >=1.20.6 {
                        buildContext
//?}
                    )).executes(t -> {
//? if >=26.1 {
                    Component text = ComponentUtils.resolve(ResolutionContext.builder()
                                .withSource(new FakeCommandSource(Minecraft.getInstance().player))
                                .withEntityOverride(Minecraft.getInstance().player).build(),
                            ClientComponentArgument.getComponent(t, "message"));
//?} else {
                    /*Component text = ComponentUtils.updateForEntity(new FakeCommandSource(Minecraft.getInstance().player), ClientComponentArgument.getComponent(t, "message"), Minecraft.getInstance().player, 0);
*///?}
                    MessageUtils.sendToActionbar(text);
                    return Command.SINGLE_SUCCESS;
                }).then(argument("duration_in_milliseconds", IntegerArgumentType.integer()).executes(t -> {
//? if >=26.1 {
                        Component text = ComponentUtils.resolve(ResolutionContext.builder()
                                    .withSource(new FakeCommandSource(Minecraft.getInstance().player))
                                    .withEntityOverride(Minecraft.getInstance().player).build(),
                                ClientComponentArgument.getComponent(t, "message"));
//?} else {
                        /*Component text = ComponentUtils.updateForEntity(new FakeCommandSource(Minecraft.getInstance().player), ClientComponentArgument.getComponent(t, "message"), Minecraft.getInstance().player, 0);
*///?}
                        int duration = IntegerArgumentType.getInteger(t, "duration_in_milliseconds");
                        MessageUtils.sendToActionbar(text, duration);
                        return Command.SINGLE_SUCCESS;
                })))))
//? if !LITE_VERSION {
            // chattools download
            .then(literal("download").executes(t -> {
                LoggerUtils.info("[ChatTools] Command Executed: Trying to download Addon Toast dependencies");
                DownloadUtils.startDownloadWithCallback((file, progress, nowKB, totalKB) -> {
                    MessageUtils.sendToOriginalActionbar(TextUtils.trans("texts.download.process", file, progress, nowKB, totalKB));
                });
                if (DownloadUtils.checkIfFullyReady()){
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.download.success"));
                }
                return Command.SINGLE_SUCCESS;
            }))
//?}
            // chattools opengui
            .then(literal("opengui").executes(t -> {
                MessageUtils.sendToActionbar(TextUtils.trans("gui.title"));
                Minecraft.getInstance()
                               .setOverlay(new ScreenOverlayHelper(Minecraft.getInstance(),
                                       ConfigScreenFactory.createScreen(null)) {
                               });
                LoggerUtils.info("[ChatTools] Command Executed: GUI opened");
                return Command.SINGLE_SUCCESS;
            }))
            // chattools on
            .then(literal("on").executes(t -> {
                ConfigUtils.set("general.ChatTools.Enabled", true);
                LoggerUtils.info("[ChatTools] Command Executed: Enabled ChatTools");
                MessageUtils.sendToActionbar(TextUtils.trans("texts.on"));
                return Command.SINGLE_SUCCESS;
            }))
            // chattools off
            .then(literal("off").executes(t -> {
                ConfigUtils.set("general.ChatTools.Enabled", false);
                LoggerUtils.info("[ChatTools] Command Executed: Disabled ChatTools");
                MessageUtils.sendToActionbar(TextUtils.trans("texts.off"));
                return Command.SINGLE_SUCCESS;
            }))
            // chattools get_message
            .then(literal("get_message").then(argument("hash",StringArgumentType.string()).executes(t -> {
                String hash = StringArgumentType.getString(t,"hash");
                LoggerUtils.info("[ChatTools] Getting message by hash: " + hash);
                TextUtils.MessageUnit messageUnit = TextUtils.getMessageUnitByHash(hash);
                if (messageUnit != null) {
                    LoggerUtils.info(String.format("[ChatTools] Time:%d Text:%s", messageUnit.unixTimestamp, messageUnit.message));
                    Minecraft.getInstance().setScreen(new CopyFeatureScreen(messageUnit));
                } else {
                    Component errorText = TextUtils.literal("[ChatTools] Failed to get message by hash: " + hash).copy().withStyle(ChatFormatting.RED);
                    LoggerUtils.error(errorText.getString());
                    MessageUtils.sendToNonPublicChat(errorText);
                }
                return Command.SINGLE_SUCCESS;
            })))
            // chattools config
            .then(literal("config")
                // chattools config openfile
                .then(literal("openfile").executes(t -> {
                    Util.getPlatform().openFile(ConfigStorage.FILE);
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.requireReload"));
                    return Command.SINGLE_SUCCESS;
                }))
                // chattools config reload
                .then(literal("reload").executes(t -> {
                    ConfigUtils.init();
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.reload"));
                    return Command.SINGLE_SUCCESS;
                }))
                // chattools config get
                .then(literal("get")
                    .then(argument("key", StringArgumentType.string())
                            .suggests(new ConfigKeysSuggestionProvider(3))
                            .executes(t -> {
                        String key = StringArgumentType.getString(t, "key");
                        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.get", key, ConfigUtils.get(key)));
                        return Command.SINGLE_SUCCESS;
                    }))
                )
                // chattools config set
                .then(literal("set")
                    .then(argument("key", StringArgumentType.string()).suggests(new ConfigKeysSuggestionProvider(2))
                        .then(argument("value", StringArgumentType.string()).executes(t -> {
                            String key = StringArgumentType.getString(t, "key");
                            String value = StringArgumentType.getString(t, "value");
                            updateConfig(key, value);
                            return Command.SINGLE_SUCCESS;
                        }).then(argument("save", BoolArgumentType.bool()).executes(t -> {
                                String key = StringArgumentType.getString(t, "key");
                                String value = StringArgumentType.getString(t, "value");
                                updateConfig(key, value);
                                if (BoolArgumentType.getBool(t, "save")) {
                                    ConfigUtils.save();
                                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.saved"));
                                }
                                return Command.SINGLE_SUCCESS;
                        })))
                    )
                )
                // chattools config toggle
                .then(literal("toggle")
                        .then(argument("key", StringArgumentType.string()).suggests(new ConfigKeysSuggestionProvider(1))
                                .executes(t -> {
                                    String key = StringArgumentType.getString(t, "key");
                                    toggleBooleanConfig(key);
                                    return Command.SINGLE_SUCCESS;
                                }).then(argument("save", BoolArgumentType.bool()).executes(t -> {
                                    String key = StringArgumentType.getString(t, "key");
                                    toggleBooleanConfig(key);
                                    if (BoolArgumentType.getBool(t, "save")) {
                                        ConfigUtils.save();
                                        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.saved"));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        )
                )
            )
            // chattools regex_checker
            .then(literal("regex_checker")
                // one arg
                .then(argument("regex", StringArgumentType.string()).executes(t -> {
                        Tuple<Boolean, String> result = checkRegex(StringArgumentType.getString(t, "regex"));
                        MessageUtils.sendToNonPublicChat(TextUtils.literal(result.getB()).copy().withStyle(result.getA() ? ChatFormatting.GREEN : ChatFormatting.RED));
                        return Command.SINGLE_SUCCESS;
                    })
                    // two args
                    .then(argument("test_context", StringArgumentType.string()).executes(t -> {
                        String regex = StringArgumentType.getString(t, "regex");
                        String testContext = StringArgumentType.getString(t, "test_context");
                        Tuple<Boolean, String> result = checkRegex(regex);
                        if (!result.getA()) {
                            MessageUtils.sendToNonPublicChat(TextUtils.literal(result.getB()).copy().withStyle(ChatFormatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        if (Pattern.compile(regex).matcher(testContext).find()) {
                            MessageUtils.sendToNonPublicChat(TextUtils.literal(String.format("[ChatTools] Context [%s] could pass the RegEx test!", testContext)).copy().withStyle(ChatFormatting.GREEN));
                        } else {
                            MessageUtils.sendToNonPublicChat(TextUtils.literal(String.format("[ChatTools] Context [%s] could NOT pass the RegEx test!", testContext)).copy().withStyle(ChatFormatting.RED));
                        }
                        return Command.SINGLE_SUCCESS;
                    }))));
        // @formatter:on
    }

    public static Tuple<Boolean, String> checkRegex(String pattern) {
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return new Tuple<>(false, e.getMessage().replace("\r", ""));
        }
        return new Tuple<>(true, "There's nothing wrong with the RegEx pattern.");
    }

    public static void toggleBooleanConfig(String key) {
        if (ConfigUtils.get(key) == null) {
            MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.toggle.error", key));
            return;
        }
        boolean now = ConfigUtils.getBoolean(key);
        updateConfig(key, String.valueOf(!now));
    }

    public static void updateConfig(String key, String value) {
        try {
            // KFC Easter Egg
            if ("general.kfc".equals(key)) {
                ConfigUtils.set(key, Boolean.parseBoolean(value));
                MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set", key, ConfigUtils.get(key)));
                if (Boolean.parseBoolean(value)) {
                    MessageUtils.sendToNonPublicChat(TextUtils.literal("§l§c肯德基§f疯狂§c星期四§f，V我§c50"));
                }
                return;
            }
            // normal
            if (!ConfigGuiLoader.getKey2TypeMappings().containsKey(key)) {
                // if we don't have that key, we consider it as a string
                ConfigUtils.set(key, value);
                MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.warning", key));
            } else {
                switch (String.valueOf(ConfigGuiLoader.getKey2TypeMappings().get(key))) {
                    case "boolean":
                        ConfigUtils.set(key, Boolean.parseBoolean(value));
                        break;
                    case "String":
                        ConfigUtils.set(key, String.valueOf(value));
                        break;
                    case "intSlider":
                    case "intField":
                        ConfigUtils.set(key, Integer.parseInt(value));
                        break;
                    case "doubleField":
                        ConfigUtils.set(key, Double.parseDouble(value));
                        break;
                    case "EnumToastModes":
                        ConfigUtils.set(key, SpecialUnits.ToastModes.valueOf(value));
                        break;
                    case "EnumTranslators":
                        ConfigUtils.set(key, SpecialUnits.TranslatorModes.valueOf(value));
                        break;
                    case "EnumKeyModifiers":
                        ConfigUtils.set(key, SpecialUnits.KeyModifiers.valueOf(value));
                        break;
                    case "FAQ":
                    case "sub":
                    case "keycode":
                    case "StringList":
                    case "FormatterList":
                    case "MacroList":
                    case "BubbleList":
                    case "ResponderList":
                    case "CustomJoinMessageList":
                        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.unsupported", key));
                        // ignore it, so return in advance
                        return;
                    case "":
                    default:
                        ConfigUtils.set(key, String.valueOf(value));
                        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.warning", key));
                }
            }
            MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set", key, ConfigUtils.get(key)));
        } catch (Exception e) {
            LoggerUtils.error("[ChatTools] Error setting config value", e);
            MessageUtils.sendToNonPublicChat(TextUtils.literal(e.toString()).copy()
                    .setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED)));
        }
    }

    public static class ClientComponentArgument implements ArgumentType<Component> {
        public static final DynamicCommandExceptionType INVALID_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(text -> TextUtils.transWithPrefix("argument.component.invalid", "", text));
//? if >=1.20.6 {
        private final HolderLookup.Provider holderLookupProvider;
//?}

//? if >=1.20.6 {
        private ClientComponentArgument(HolderLookup.Provider holderLookupProvider) {
            this.holderLookupProvider = holderLookupProvider;
        }

        public static ClientComponentArgument textComponent(CommandBuildContext buildContext) {
            return new ClientComponentArgument(buildContext);
        }
//?} else {
        /*private ClientComponentArgument() {}
        public static ClientComponentArgument textComponent() {return new ClientComponentArgument();}
*///?}

        public static Component getComponent(final CommandContext<
//? if FABRIC {
                /*FabricClientCommandSource
*///?} elif NEOFORGE {
                CommandSourceStack
//?}
                > context, final String name) {
            return context.getArgument(name, Component.class);
        }

        @Override
        public Component parse(final StringReader stringReader) throws CommandSyntaxException {
            try {
//? if >=1.21.5 {
                return SnbtGrammar.createParser(JavaOps.INSTANCE).withCodec(
                        this.holderLookupProvider.createSerializationContext(JavaOps.INSTANCE), SnbtGrammar.createParser(JavaOps.INSTANCE), ComponentSerialization.CODEC, INVALID_COMPONENT_EXCEPTION
                ).parseForCommands(stringReader);
//?} elif >=1.20.6 {
                /*return ParserUtils.parseJson(this.holderLookupProvider, stringReader, ComponentSerialization.CODEC);
*///?} elif >=1.20.4 {
                /*return ParserUtils.parseJson(stringReader, ComponentSerialization.CODEC);
*///?} else {
                /*Component component = Component.Serializer.fromJson(stringReader);
                if (component == null) { throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, "empty"); }
                else { return component; }
*///?}
            } catch (Exception var4) {
                String string = var4.getCause() != null ? var4.getCause().getMessage() : var4.getMessage();
                throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, string);
            }
        }
    }

    public static class FakeCommandSource extends CommandSourceStack {
        public FakeCommandSource(LocalPlayer player) {
            super(new CommandSource() {
//? if >=1.19 {
                      @Override
                      public void sendSystemMessage(Component component) {
                          MessageUtils.sendToNonPublicChat(component);
                      }
//?} elif >=1.17 {
                      /*@Override public void sendMessage(Component component, UUID uuid) {MessageUtils.sendToNonPublicChat(component);}
                      @Override public boolean alwaysAccepts() {return CommandSource.super.alwaysAccepts();}
*///?} else {
                      /*@Override public void sendMessage(Component component, UUID uuid) {MessageUtils.sendToNonPublicChat(component);}
*///?}

                      @Override
                      public boolean acceptsSuccess() {
                          return true;
                      }

                      @Override
                      public boolean acceptsFailure() {
                          return true;
                      }

                      @Override
                      public boolean shouldInformAdmins() {
                          return true;
                      }
                  }, player.position(), player.getRotationVector(), null,
//? if >=1.21.11 {
                    PermissionSet.ALL_PERMISSIONS
//?} else {
                    /*4
*///?}
                    , player.getScoreboardName(), player.getName(), null, player);
        }
    }
}
