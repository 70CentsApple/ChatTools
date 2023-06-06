package net.apple70cents.chattools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.apple70cents.chattools.config.ModClothConfig;
import net.apple70cents.chattools.config.ModConfigFallback;
import net.apple70cents.chattools.features.chatbubbles.BubbleRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;


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

        if (CONFIGS_LOADED) {
            ModClothConfig.load();
        }
        config = CONFIGS_LOADED ? ModClothConfig.get() : new ModConfigFallback();

        // 注册 Quick Repeat
        ClientTickEvents.START_WORLD_TICK.register(client -> checkQuickRepeat());

        // 注册指令
        ClientCommandManager.DISPATCHER.register((LiteralArgumentBuilder<FabricClientCommandSource>) getBuilder());
    }

    private static boolean isKeyPressedOrMouseKeyClicked(String translationKey, ModClothConfig.CustomModifier modifier) {
        MinecraftClient mc = MinecraftClient.getInstance();
        long handle = mc.getWindow().getHandle();
        InputUtil.Key key = InputUtil.fromTranslationKey(translationKey);
        int keyCode = key.getCode();

        if ((modifier.equals(ModClothConfig.CustomModifier.ALT) && !(InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_ALT)))
                || (modifier.equals(ModClothConfig.CustomModifier.SHIFT) && !(InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_SHIFT)))
                || (modifier.equals(ModClothConfig.CustomModifier.CTRL) && !(InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_CONTROL)))) {
            return false;
        }

        if (key.getCategory().equals(InputUtil.Type.KEYSYM)) {
            return InputUtil.isKeyPressed(handle, keyCode);
        } else if (key.getCategory().equals(InputUtil.Type.MOUSE)) {
            return GLFW.glfwGetMouseButton(handle, keyCode) == GLFW.GLFW_PRESS;
        }
        return false;
    }


    private static boolean keyWasPressed;

    public static void checkQuickRepeat() {
        if (config.quickRepeatKey.equals(InputUtil.UNKNOWN_KEY.getTranslationKey())) {
            return;
        }

        if (isKeyPressedOrMouseKeyClicked(config.quickRepeatKey, config.quickRepeatKeyModifier)) {
            if (!keyWasPressed) {
                keyWasPressed = true;
                ChatTools.LOGGER.info("[ChatTools] Triggered the latest command.");
                MinecraftClient mc = MinecraftClient.getInstance();
                List<String> history = mc.inGameHud.getChatHud().getMessageHistory();
                if (history.isEmpty()) {
                    mc.player.sendMessage(new TranslatableText("text.config.chattools.option.quickRepeatFailure"), true);
                } else {
                    mc.player.sendChatMessage(history.get(history.size() - 1));
                }
            }
        } else {
            keyWasPressed = false;
        }
    }

    static LiteralArgumentBuilder<?> getBuilder() {
        return literal("chattools")
                .then(literal("opengui") // chattools opengui
                        .executes(t -> opengui()))
                .then(literal("on") // chattools on
                        .executes(t -> {
                            config.modEnabled = true;
                            ChatTools.LOGGER.info("[ChatTools] Command Executed: Enabled ChatTools");
                            MinecraftClient.getInstance().player.sendMessage(new TranslatableText("key.chattools.enable"), true);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("off") // chattools off
                        .executes(t -> {
                            config.modEnabled = false;
                            ChatTools.LOGGER.info("[ChatTools] Command Executed: Disabled ChatTools");
                            MinecraftClient.getInstance().player.sendMessage(new TranslatableText("key.chattools.disable"), true);
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    static int opengui() {
        MinecraftClient.getInstance().player.sendMessage(new TranslatableText("text.config.chattools.title"), true);
        MinecraftClient.getInstance().setOverlay(new ScreenOverlay(MinecraftClient.getInstance(), ModClothConfig.getConfigBuilder().setParentScreen(null).build()));
        ChatTools.LOGGER.info("[ChatTools] Command Executed: GUI opened");
        return Command.SINGLE_SUCCESS;
    }
}
