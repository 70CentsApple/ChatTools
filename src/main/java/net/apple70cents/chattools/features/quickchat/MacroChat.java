package net.apple70cents.chattools.features.quickchat;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ModClothConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import static net.apple70cents.chattools.ChatTools.config;
import static net.apple70cents.chattools.ChatTools.isKeyPressedOrMouseKeyClicked;

public class MacroChat {
    public enum MacroMode {
        LAZY, GREEDY
    }

    public static class MacroUnit {
        private String key;
        private ModClothConfig.CustomModifier modifier;
        private MacroMode mode;
        private String command;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public ModClothConfig.CustomModifier getModifier() {
            return modifier;
        }

        public void setModifier(ModClothConfig.CustomModifier modifier) {
            this.modifier = modifier;
        }

        public MacroMode getMode() {
            return mode;
        }

        public void setMode(MacroMode mode) {
            this.mode = mode;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public MacroUnit() {
            this.key = InputUtil.UNKNOWN_KEY.getTranslationKey();
            this.modifier = ModClothConfig.CustomModifier.NONE;
            this.mode = MacroMode.LAZY;
            this.command = "";
        }

        public boolean keyWasPressed; // 这里不能是static，调试了好久qwq

        public void tick() {
            if (key.equals(InputUtil.UNKNOWN_KEY.getTranslationKey())) {
                return;
            } else if (MinecraftClient.getInstance().currentScreen != null) { // 只有当前overlay为空才能继续
                return;
            }
            boolean lazyModePass = true;
            if (mode == MacroMode.LAZY) {
                long handle = MinecraftClient.getInstance().getWindow().getHandle();
                // 太不优雅了 得改掉
                lazyModePass = switch (modifier) {
                    case NONE ->
                            !(InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_ALT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_CONTROL) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_SHIFT));
                    case SHIFT ->
                            !(InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_ALT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_CONTROL));
                    case ALT ->
                            !(InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_CONTROL) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_SHIFT));
                    case CTRL ->
                            !(InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_ALT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_SHIFT));
                };
                lazyModePass = lazyModePass & !InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_F3);
            }
            if (isKeyPressedOrMouseKeyClicked(key, modifier) && lazyModePass) {
                if (!keyWasPressed) {
                    keyWasPressed = true;
                    ChatTools.LOGGER.info("[ChatTools] Triggered Macro: " + command);
                    ChatTools.sendPlayerChat(command);
                }
            } else {
                keyWasPressed = false;
            }
        }
    }

    /**
     * 聊天宏的主要逻辑
     */
    public static void tick() {
        if (!config.macroChatEnabled) {
            return;
        }
        for (MacroUnit macro : ModClothConfig.get().macroChatList) {
            macro.tick();
        }
    }
}
