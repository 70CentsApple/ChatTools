package net.apple70cents.chattools.utils;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.apple70cents.chattools.config.common.SpecialUnits;
import net.minecraft.client.Minecraft;
//? if >=26.3 {
import org.lwjgl.sdl.SDLMouse;
//?} else {
/*import org.lwjgl.glfw.GLFW;
*///?}

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author 70CentsApple
 */
public class KeyboardUtils {

    // Resolve names through Minecraft so each version supplies its own keycodes/scancodes.
    private static final int[] ALT_KEYS = {
            InputConstants.getKey("key.keyboard.left.alt").getValue(),
            InputConstants.getKey("key.keyboard.right.alt").getValue()
    };
    private static final int[] CTRL_KEYS = {
            InputConstants.getKey("key.keyboard.left.control").getValue(),
            InputConstants.getKey("key.keyboard.right.control").getValue()
    };
    private static final int[] SHIFT_KEYS = {
            InputConstants.getKey("key.keyboard.left.shift").getValue(),
            InputConstants.getKey("key.keyboard.right.shift").getValue()
    };
    private static final int F3_KEY = InputConstants.getKey("key.keyboard.f3").getValue();

    private static final int[] ALL_MODIFIER_KEYS = concat(concat(ALT_KEYS, CTRL_KEYS), SHIFT_KEYS);

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
            if (InputConstants.isKeyDown(
//? if >=26.3 {
//?} else {
                    /*window,
*///?}
                    key)) return true;
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
            if (isAnyKeyDown(window, forbiddenKeys)) {
                return false;
            }
//? if >= 26.3 {
            boolean f3Down = InputConstants.isKeyDown(F3_KEY);
//?} else {
            /*boolean f3Down = InputConstants.isKeyDown(window, F3_KEY);
*///?}
            if (f3Down) {
                return false;
            }
        }

//? if >= 26.3 {
        if (key.getType().equals(InputConstants.Type.KEYBOARD)) {
            return InputConstants.isKeyDown(keyCode);
//?} else {
        /*if (key.getType().equals(InputConstants.Type.KEYSYM)) {
            return InputConstants.isKeyDown(window, keyCode);
*///?}
        } else if (key.getType().equals(InputConstants.Type.MOUSE)) {
//? if >=26.3 {
            return keyCode >= 1 && keyCode <= Integer.SIZE
                    && (SDLMouse.SDL_GetMouseState(null, null) & (1 << (keyCode - 1))) != 0;
//?} elif >=1.21.9 {
            /*return GLFW.glfwGetMouseButton(window.handle(), keyCode) == GLFW.GLFW_PRESS;
*///?} else {
            /*return GLFW.glfwGetMouseButton(window, keyCode) == GLFW.GLFW_PRESS;
*///?}
        }
        return false;
    }
}
