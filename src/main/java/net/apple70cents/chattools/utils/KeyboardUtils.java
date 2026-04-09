package net.apple70cents.chattools.utils;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.apple70cents.chattools.config.common.SpecialUnits;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author 70CentsApple
 */
public class KeyboardUtils {

    private static final int[] ALT_KEYS = {GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT};
    private static final int[] CTRL_KEYS = {GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL};
    private static final int[] SHIFT_KEYS = {GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT};

    private static final int[] ALL_MODIFIER_KEYS = {
            GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT,
            GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL,
            GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT
    };

    // Maps each modifier to the keys that MUST be pressed for it
    private static final Map<SpecialUnits.KeyModifiers, int[]> REQUIRED_KEYS = new EnumMap<>(SpecialUnits.KeyModifiers.class);
    // Maps each modifier to the keys that must NOT be pressed in LAZY mode (i.e. other modifiers)
    private static final Map<SpecialUnits.KeyModifiers, int[]> FORBIDDEN_KEYS_IN_LAZY = new EnumMap<>(SpecialUnits.KeyModifiers.class);

    static {
        REQUIRED_KEYS.put(SpecialUnits.KeyModifiers.ALT, ALT_KEYS);
        REQUIRED_KEYS.put(SpecialUnits.KeyModifiers.SHIFT, SHIFT_KEYS);
        REQUIRED_KEYS.put(SpecialUnits.KeyModifiers.CTRL, CTRL_KEYS);
        REQUIRED_KEYS.put(SpecialUnits.KeyModifiers.NONE, new int[0]);

        FORBIDDEN_KEYS_IN_LAZY.put(SpecialUnits.KeyModifiers.NONE, ALL_MODIFIER_KEYS);
        FORBIDDEN_KEYS_IN_LAZY.put(SpecialUnits.KeyModifiers.SHIFT, concat(ALT_KEYS, CTRL_KEYS));
        FORBIDDEN_KEYS_IN_LAZY.put(SpecialUnits.KeyModifiers.ALT, concat(CTRL_KEYS, SHIFT_KEYS));
        FORBIDDEN_KEYS_IN_LAZY.put(SpecialUnits.KeyModifiers.CTRL, concat(ALT_KEYS, SHIFT_KEYS));
    }

    private static int[] concat(int[] a, int[] b) {
        int[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

//? if >=1.21.9 {
    private static boolean isAnyKeyDown(Window window, int[] keys) {
        for (int key : keys) {
            if (InputConstants.isKeyDown(window, key)) return true;
        }
        return false;
    }
//?} else {
    /*private static boolean isAnyKeyDown(long window, int[] keys) {
        for (int key : keys) {
            if (InputConstants.isKeyDown(window, key)) return true;
        }
        return false;
    }
*///?}

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
//? if >=1.21.9 {
        Window window = Minecraft.getInstance().getWindow();
//?} else {
        /*long window = Minecraft.getInstance().getWindow().getWindow();
*///?}
        InputConstants.Key key = InputConstants.getKey(translationKey);
        int keyCode = key.getValue();

        // GREEDY check: verify the required modifier keys are pressed
        int[] requiredKeys = REQUIRED_KEYS.get(modifier);
        if (requiredKeys.length > 0 && !isAnyKeyDown(window, requiredKeys)) {
            return false;
        }

        // LAZY mode: verify no other modifier keys are pressed
        if (SpecialUnits.MacroModes.LAZY.equals(mode)) {
            int[] forbiddenKeys = FORBIDDEN_KEYS_IN_LAZY.get(modifier);
            if (forbiddenKeys != null && isAnyKeyDown(window, forbiddenKeys)) {
                return false;
            }
            if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_F3)) {
                return false;
            }
        }

        if (key.getType().equals(InputConstants.Type.KEYSYM)) {
            return InputConstants.isKeyDown(window, keyCode);
        } else if (key.getType().equals(InputConstants.Type.MOUSE)) {
            return GLFW.glfwGetMouseButton(
//? if >=1.21.9 {
                    window.handle()
//?} else {
                    /*window
*///?}
                    , keyCode) == GLFW.GLFW_PRESS;
        }
        return false;
    }
}
