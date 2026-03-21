package net.apple70cents.chattools.utils;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.apple70cents.chattools.config.SpecialUnits;
import net.minecraft.client.Minecraft;
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
        if (InputConstants.UNKNOWN.getName().equals(translationKey)) {
            return false;
        }
//#if MC>=12109
        Window window = Minecraft.getInstance().getWindow();
//#else
//$$    long window = Minecraft.getInstance().getWindow().getWindow();
//#endif
        InputConstants.Key key = InputConstants.getKey(translationKey);
        int keyCode = key.getValue();

        // @formatter:off
        // This check is GREEDY, which means if `key` = D, `modifier` = Alt, it just cares whether these two keys are both activated.
        if ((modifier.equals(SpecialUnits.KeyModifiers.ALT) &&
                !(InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) ||
                        InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT)))
        || (modifier.equals(SpecialUnits.KeyModifiers.SHIFT) &&
                !(InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                        InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)))
        || (modifier.equals(SpecialUnits.KeyModifiers.CTRL) &&
                !(InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                        || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL)))
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
                        !(InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT));
                    break;
                case SHIFT :
                    lazyModePass =
                        !(InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL));
                    break;
                case ALT :
                    lazyModePass =
                        !(InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT));
                    break;
                case CTRL :
                    lazyModePass =
                        !(InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT));
                    break;
                default :
                    lazyModePass = true;
            }
            lazyModePass = lazyModePass & !InputConstants.isKeyDown(window, GLFW.GLFW_KEY_F3);
            if (!lazyModePass){
                return false;
            }
        }
        // @formatter:on
        if (key.getType().equals(InputConstants.Type.KEYSYM)) {
            return InputConstants.isKeyDown(window, keyCode);
        } else if (key.getType().equals(InputConstants.Type.MOUSE)) {
            return GLFW.glfwGetMouseButton(
//#if MC>=12109
                    window.handle()
//#else
//$$                window
//#endif
                    , keyCode) == GLFW.GLFW_PRESS;
        }
        return false;
    }
}
