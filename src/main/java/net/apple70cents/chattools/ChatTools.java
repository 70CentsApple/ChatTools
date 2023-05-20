package net.apple70cents.chattools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import net.apple70cents.chattools.config.ModClothConfig;
import net.apple70cents.chattools.config.ModConfigFallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class ChatTools implements ModInitializer {
    public static String MODID;
    public static Logger LOGGER;
    public static ModClothConfig config;
    public static boolean CONFIGS_LOADED;

    @Override
    public void onInitialize() {
        CONFIGS_LOADED = FabricLoader.getInstance().isModLoaded("cloth_config") || FabricLoader.getInstance().isModLoaded("cloth-config") || FabricLoader.getInstance().isModLoaded("cloth-config2");
        MODID = "chattools";
        LOGGER = LoggerFactory.getLogger(MODID);

        config = CONFIGS_LOADED ? ModClothConfig.get() : new ModConfigFallback();
        if(CONFIGS_LOADED) {
            ModClothConfig.load();
        }

        // 注册指令
        ClientCommandManager.DISPATCHER.register((LiteralArgumentBuilder<FabricClientCommandSource>) getBuilder());
    }

    static LiteralArgumentBuilder<?> getBuilder() {
        return literal("chattools")
                .then(literal("opengui") // chattools opengui
                        .executes(t -> opengui()))
                .then(literal("on") // chattools on
                        .executes(t -> {
                            config.modEnabled = true;
                            ChatTools.LOGGER.info("[ChatTools] Command Executed: Enabled ChatTools");
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("off") // chattools off
                        .executes(t -> {
                            config.modEnabled = false;
                            ChatTools.LOGGER.info("[ChatTools] Command Executed: Disabled ChatTools");
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    static int opengui(){
        MinecraftClient.getInstance().setOverlay(new ScreenOverlay(MinecraftClient.getInstance(), ModClothConfig.getConfigBuilder().setParentScreen(null).build()));
        ChatTools.LOGGER.info("[ChatTools] Command Executed: GUI opened");
        return Command.SINGLE_SUCCESS;
    }
}
