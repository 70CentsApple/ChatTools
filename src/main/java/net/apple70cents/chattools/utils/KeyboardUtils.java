package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.config.SpecialUnits;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * @author 70CentsApple
 */
public class KeyboardUtils {
    /**
     * check if is a key is being pressed while the modifier key is pressed as well
     *
     * @param translationKey the key such as 'key.mouse.4'
     * @param modifier       the modifier
     * @param mode           the macro mode
     * @return success or not
     */
    public static boolean isKeyPressingWithModifier(String translationKey, SpecialUnits.KeyModifiers modifier, SpecialUnits.MacroModes mode) {
        if (InputUtil.UNKNOWN_KEY.getTranslationKey().equals(translationKey)) {
            return false;
        }
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        InputUtil.Key key = InputUtil.fromTranslationKey(translationKey);
        int keyCode = key.getCode();

        // @formatter:off
        // This check is GREEDY, which means if `key` = D, `modifier` = Alt, it just cares whether these two keys are both activated.
        if ((modifier.equals(SpecialUnits.KeyModifiers.ALT) &&
                !(InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) ||
                        InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT)))
        || (modifier.equals(SpecialUnits.KeyModifiers.SHIFT) &&
                !(InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                        InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT)))
        || (modifier.equals(SpecialUnits.KeyModifiers.CTRL) &&
                !(InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)
                        || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL)))
        ) {
            return false;
        }
        if(SpecialUnits.MacroModes.LAZY.equals(mode)){
            // Here we deal with LAZY mode if needed.
            // It is so stupid, but it works
            boolean lazyModePass;
            switch (modifier) {
                case NONE :
                    lazyModePass =
                        !(InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT));
                    break;
                case SHIFT :
                    lazyModePass =
                        !(InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL));
                    break;
                case ALT :
                    lazyModePass =
                        !(InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT));
                    break;
                case CTRL :
                    lazyModePass =
                        !(InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                            InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT));
                    break;
                default :
                    lazyModePass = true;
            }
            lazyModePass = lazyModePass & !InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_F3);
            if (!lazyModePass){
                return false;
            }
        }
        // @formatter:on
        if (key.getCategory().equals(InputUtil.Type.KEYSYM)) {
            return InputUtil.isKeyPressed(handle, keyCode);
        } else if (key.getCategory().equals(InputUtil.Type.MOUSE)) {
            return GLFW.glfwGetMouseButton(handle, keyCode) == GLFW.GLFW_PRESS;
        }
        return false;
    }
}
