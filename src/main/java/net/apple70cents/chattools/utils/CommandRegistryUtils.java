package net.apple70cents.chattools.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ConfigScreenGenerator;
import net.apple70cents.chattools.config.ConfigStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

//#if MC>=11900
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
//#else
// fabric v2 begin to work since 1.19
//$$ import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
//$$ import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
//#endif


public class CommandRegistryUtils {
    public static void register() {
        //#if MC>=11900
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register((LiteralArgumentBuilder<FabricClientCommandSource>) CommandRegistryUtils.getBuilder());
        });
        //#else
        //$$ ClientCommandManager.DISPATCHER.register((LiteralArgumentBuilder<FabricClientCommandSource>) CommandRegistryUtils.getBuilder());
        //#endif
    }

    public static LiteralArgumentBuilder<?> getBuilder() {
        // @formatter:off
        return literal("chattools")
            // chattools download
            .then(literal("download").executes(t -> {
                LoggerUtils.info("[ChatTools] Command Executed: Trying to download Addon Toast dependencies");
                DownloadUtils.startDownloadWithCallback((file, progress, nowKB, totalKB) -> {
                    MessageUtils.sendToActionbar(TextUtils.trans("texts.download.process", file, progress, nowKB, totalKB));
                });
                if (DownloadUtils.checkIfFullyReady()){
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.download.success"));
                }
                return Command.SINGLE_SUCCESS;
            }))
            // chattools opengui
            .then(literal("opengui").executes(t -> {
                MessageUtils.sendToActionbar(TextUtils.trans("gui.title"));
                MinecraftClient.getInstance()
                               .setOverlay(new ScreenOverlayHelper(MinecraftClient.getInstance(), ConfigScreenGenerator
                                       .getConfigBuilder().setParentScreen(null).build()) {
                               });
                LoggerUtils.info("[ChatTools] Command Executed: GUI opened");
                return Command.SINGLE_SUCCESS;
            }))
            // chattools on
            .then(literal("on").executes(t -> {
                ChatTools.CONFIG.set("general.ChatTools.Enabled", true);
                LoggerUtils.info("[ChatTools] Command Executed: Enabled ChatTools");
                MessageUtils.sendToActionbar(TextUtils.trans("texts.on"));
                return Command.SINGLE_SUCCESS;
            }))
            // chattools off
            .then(literal("off").executes(t -> {
                ChatTools.CONFIG.set("general.ChatTools.Enabled", false);
                LoggerUtils.info("[ChatTools] Command Executed: Disabled ChatTools");
                MessageUtils.sendToActionbar(TextUtils.trans("texts.off"));
                return Command.SINGLE_SUCCESS;
            }))
            // chattools config
            .then(literal("config")
                // chattools config openfile
                .then(literal("openfile").executes(t -> {
                    Util.getOperatingSystem().open(ConfigStorage.FILE);
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.requireRestart"));
                    return Command.SINGLE_SUCCESS;
                }))).then(literal("regex_checker")
                // one arg
                .then(argument("regex", StringArgumentType.string()).executes(t -> {
                        Pair<Boolean, String> result = checkRegex(StringArgumentType.getString(t, "regex"));
                        MessageUtils.sendToNonPublicChat(((MutableText) TextUtils.literal(result.getRight())).formatted(result.getLeft() ? Formatting.GREEN : Formatting.RED));
                        return Command.SINGLE_SUCCESS;
                    })
                    // two args
                    .then(argument("test_context", StringArgumentType.string()).executes(t -> {

                        Pair<Boolean, String> result = checkRegex(StringArgumentType.getString(t, "regex"));
                        if (!result.getLeft()) {
                            MessageUtils.sendToNonPublicChat(((MutableText) TextUtils.literal(result.getRight())).formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        if (Pattern
                                .compile(StringArgumentType.getString(t, "regex"))
                                .matcher(StringArgumentType.getString(t, "test_context"))
                                .find()) {
                            MessageUtils.sendToNonPublicChat(((MutableText) TextUtils.literal(String.format("Context [%s] could pass the RegEx test!", StringArgumentType.getString(t, "test_context")))).formatted(Formatting.GREEN));
                        } else {
                            MessageUtils.sendToNonPublicChat(((MutableText) TextUtils.literal(String.format("Context [%s] could NOT pass the RegEx test!", StringArgumentType.getString(t, "test_context")))).formatted(Formatting.RED));
                        }
                        return Command.SINGLE_SUCCESS;
                    }))));
            // @formatter:on
    }

    public static Pair<Boolean, String> checkRegex(String pattern) {
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return new Pair<>(false, e.getMessage().replace("\r", ""));
        }
        return new Pair<>(true, "There's nothing wrong with the RegEx pattern.");
    }
}
