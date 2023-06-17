package net.apple70cents.chattools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.apple70cents.chattools.config.ModClothConfig;
import net.apple70cents.chattools.config.ModConfigFallback;
import net.apple70cents.chattools.features.quickchat.MacroChat;
import net.apple70cents.chattools.features.quickchat.QuickRepeat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        ClientTickEvents.START_WORLD_TICK.register(client -> QuickRepeat.checkQuickRepeat());

        // 注册 Macro Chat
        ClientTickEvents.START_WORLD_TICK.register(client -> {
            if (config.macroChatEnabled) {
                MacroChat.tick();
            }
        });

        // 注册指令
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register((LiteralArgumentBuilder<FabricClientCommandSource>) getBuilder()));
    }

    /**
     * 键盘指定按键 或 鼠标侧键 与修饰键被按下
     *
     * @param translationKey 按键的键值（例如key.mouse.4）
     * @param modifier       修饰符键值
     * @return 是否符合要求
     */
    public static boolean isKeyPressedOrMouseKeyClicked(String translationKey, ModClothConfig.CustomModifier modifier) {
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


    /**
     * 获取指令参数构造器
     *
     * @return 指令参数构造器
     */
    static LiteralArgumentBuilder<?> getBuilder() {
        return CommandManager.literal("chattools")
                .then(literal("opengui") // chattools opengui
                        .executes(t -> opengui()))
                .then(literal("on") // chattools on
                        .executes(t -> {
                            config.modEnabled = true;
                            ChatTools.LOGGER.info("[ChatTools] Command Executed: Enabled ChatTools");
                            MinecraftClient.getInstance().player.sendMessage(Text.translatable("key.chattools.enable"), true);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("off") // chattools off
                        .executes(t -> {
                            config.modEnabled = false;
                            ChatTools.LOGGER.info("[ChatTools] Command Executed: Disabled ChatTools");
                            MinecraftClient.getInstance().player.sendMessage(Text.translatable("key.chattools.disable"), true);
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    /**
     * 打开 GUI
     *
     * @return 命令成功状态码
     */
    static int opengui() {
        MinecraftClient.getInstance().player.sendMessage(Text.translatable("text.config.chattools.title"), true);
        MinecraftClient.getInstance().setOverlay(new ScreenOverlay(MinecraftClient.getInstance(), ModClothConfig.getConfigBuilder().setParentScreen(null).build()));
        ChatTools.LOGGER.info("[ChatTools] Command Executed: GUI opened");
        return Command.SINGLE_SUCCESS;
    }
}
