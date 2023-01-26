package net.apple70cents.chatnotifier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.shedaniel.autoconfig.AutoConfig;
import net.apple70cents.chatnotifier.config.ModConfig;
import net.apple70cents.chatnotifier.config.ModConfigFallback;
import net.apple70cents.chatnotifier.config.ModConfigProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

public class ChatNotifier implements ModInitializer {
    public static String MODID;
    public static Logger LOGGER;
    public static ModConfig config;
    public static boolean CONFIGS_LOADED;

    @Override
    public void onInitialize() {
        CONFIGS_LOADED = FabricLoader.getInstance().isModLoaded("cloth_config") || FabricLoader.getInstance().isModLoaded("cloth-config") || FabricLoader.getInstance().isModLoaded("cloth-config2");
        MODID = "chatnotifier";
        LOGGER = LoggerFactory.getLogger(MODID);

        if (CONFIGS_LOADED) {
            LOGGER.info("[ChatNotifier] Cloth Config detected, trying to initialize.");
            ModConfig.init();
        }
        config = CONFIGS_LOADED ? new ModConfigProvider().getConfig() : new ModConfigFallback();

        // 注册指令
        ClientCommandManager.DISPATCHER.register((LiteralArgumentBuilder<FabricClientCommandSource>) getBuilder());
    }

    static LiteralArgumentBuilder<?> getBuilder() {
        return literal("chatnotifier")
                .then(literal("opengui") // chatnotifier opengui
                        .executes(t -> opengui()))
                .then(literal("on") // chatnotifier on
                        .executes(t -> {
                            config.modEnabled = true;
                            ChatNotifier.LOGGER.info("[ChatNotifier] Command Executed: Enabled ChatNotifier");
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("off") // chatnotifier off
                        .executes(t -> {
                            config.modEnabled = false;
                            ChatNotifier.LOGGER.info("[ChatNotifier] Command Executed: Disabled ChatNotifier");
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    static int opengui(){
        MinecraftClient.getInstance().setOverlay(new ScreenOverlay(MinecraftClient.getInstance(), AutoConfig.getConfigScreen(ModConfig.class, null).get()));
        ChatNotifier.LOGGER.info("[ChatNotifier] Command Executed: GUI opened");
        return Command.SINGLE_SUCCESS;
    }
}
